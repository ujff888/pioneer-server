package cn.litgame.wargame.core.model.battle;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.BattleDetail;
import cn.litgame.wargame.core.auto.GameProtos.SimpleBattleInfo;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.auto.GameResProtos.BattleGround;
import cn.litgame.wargame.core.auto.GameResProtos.TroopType;
import cn.litgame.wargame.core.logic.*;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.core.model.battle.unit.*;
import org.apache.log4j.Logger;
import org.kriver.core.common.KeyUtil;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * 战场的抽象，包括两方军队和两方的阵地分布
 * 
 * @author 熊纪元
 *
 */
public class BattleField{
	static BuildingLogic buildingLogic = SpringContext.getContext().getBean(BuildingLogic.class);

	static CityLogic cityLogic = SpringContext.getBean(CityLogic.class);

	static PlayerLogic playerLogic = SpringContext.getBean(PlayerLogic.class);

	static ConfigLogic configLogic = SpringContext.getBean(ConfigLogic.class);
	
	private static final Slot largeSlot = new Slot(0,Integer.MAX_VALUE);

	public final static boolean LAND = true;
	public final static boolean FLY = false;
	private final static Logger log = Logger.getLogger(BattleField.class);
	
	private final String UUID;
	
	private final boolean isLand;
	private final int fieldCityId;
	
	private HashMap<BattleFieldType, FieldPosition> fieldPositionsForOffence = new HashMap<>();
	private HashMap<BattleFieldType, FieldPosition> fieldPositionsForDefence = new HashMap<>();
	
	//待结算的伤害
	transient private Map<BattleFieldType, Damage> damageOffence;
	transient private Map<BattleFieldType, Damage> damageDefence;

	private List<Army> armysOffence = new ArrayList<>();
	private List<Army> armysDefence = new ArrayList<>();
	
	private final long startTime;
	private static final long nextActionTime = 1000;//下一次的触发时间,单位是毫秒
	private int currentRoundNum = 1;//当前的回合数
	
	private GameProtos.BattleResult result = GameProtos.BattleResult.FIGHTING;
	
	//历史的回合数也要存盘
	private cn.litgame.wargame.core.model.battle.protoround.BattleRound currentRoundPb;
	private List<cn.litgame.wargame.core.model.battle.protoround.BattleRound> roundHistoryPb = new ArrayList<>();
	
	private double moraleOff = 1;
	private double moraleDef = 1;
	
	private double moraleDownPercent = 0.1;
	private double moraleBuffIndex = 0;
	private double weakMoraleExtra = 0;
	
	public static final boolean OFFENCE = true;
	public static final boolean DEFENCE = false;

	/**
	 * 每个阵型的上场顺序
	 */
	public final static BattleFieldType[] positionOrder = {
			BattleFieldType.FIELD_FLY ,
			BattleFieldType.FIELD_FLY_FIRE ,
			BattleFieldType.FIELD_FIRE,
			BattleFieldType.FIELD_REMOTE,
			BattleFieldType.FIELD_CLOSE,
			BattleFieldType.FIELD_SIDE
	};

	/**
	 * 每个阵型的攻击顺序
	 */
	public final static BattleFieldType[] attackOrder = {
			BattleFieldType.FIELD_FLY ,
			BattleFieldType.FIELD_FLY_FIRE , 
			BattleFieldType.FIELD_FIRE,
			BattleFieldType.FIELD_SIDE,
			BattleFieldType.FIELD_REMOTE, 
			BattleFieldType.FIELD_CLOSE
			};

	public BattleField(GameProtos.BattleField battleField){
		this.UUID = battleField.getBattleId();
		this.fieldCityId = battleField.getFieldCityId();
		this.isLand = battleField.getIsLand();
		this.startTime = battleField.getStartTime();

		for(GameProtos.ArmyDetail ad : battleField.getArmysOffenceList()){
			this.armysOffence.add(new Army(ad));
		}
		for(GameProtos.ArmyDetail ad : battleField.getArmysDefenceList()){
			this.armysDefence.add(new Army(ad));
		}

		this.currentRoundNum = battleField.getCurrentRoundNum();
		this.currentRoundPb = new cn.litgame.wargame.core.model.battle.protoround.BattleRound(this.currentRoundNum);
		this.moraleBuffIndex = battleField.getMoraleBuffIndex();
		this.moraleDef = battleField.getMoraleDef();
		this.moraleDownPercent = battleField.getMoraleDownPercent();
		this.moraleOff = battleField.getMoraleOff();
		this.result = battleField.getResult();
		for(GameProtos.BattleRound round : battleField.getRoundHistoryList()){
			this.roundHistoryPb.add(new cn.litgame.wargame.core.model.battle.protoround.BattleRound(round));
		}
		
		this.weakMoraleExtra = battleField.getWeakMoraleExtra();

		this.setFieldPosition(OFFENCE, battleField);
		this.setFieldPosition(DEFENCE, battleField);
	}

	public void setFieldPosition(boolean isOffence, GameProtos.BattleField battleField) {
		Collection<GameProtos.FieldPosition> collection = isOffence ?
				battleField.getFieldPositionsForOffList() : battleField.getFieldPositionsForDefList();
		for(GameProtos.FieldPosition position : collection){
			ArrayList<Slot> slots = new ArrayList<>();
			for(GameProtos.Slot slot : position.getSlotList()){
				slots.add(new Slot(slot));
			}
			FieldPosition fieldPosition = new FieldPosition();
			fieldPosition.setCapacity(position.getCapacity());
			fieldPosition.setCount(position.getCount());
			fieldPosition.setType(position.getType());
			fieldPosition.setSlots(slots);

			if(isOffence)
				this.fieldPositionsForOffence.put(position.getType(), fieldPosition);
			else
				this.fieldPositionsForDefence.put(position.getType(), fieldPosition);
		}

	}

	public BattleField(BattleGround battleGround, boolean isLand, int fieldCityId) {
		this.UUID = KeyUtil.UUIDKey();
		this.fieldCityId = fieldCityId;
		this.isLand = isLand;
		this.startTime = System.currentTimeMillis();
		
		fieldPositionsForOffence.put(BattleFieldType.FIELD_FLY, new FieldPosition(BattleFieldType.FIELD_FLY, battleGround.getFly(), battleGround.getFlyCount()));
		fieldPositionsForOffence.put(BattleFieldType.FIELD_FLY_FIRE, new FieldPosition(BattleFieldType.FIELD_FLY_FIRE, battleGround.getFlyFire(), battleGround.getFlyFireCount()));
		fieldPositionsForOffence.put(BattleFieldType.FIELD_FIRE, new FieldPosition(BattleFieldType.FIELD_FIRE, battleGround.getFire(), battleGround.getFireCount()));
		fieldPositionsForOffence.put(BattleFieldType.FIELD_REMOTE, new FieldPosition(BattleFieldType.FIELD_REMOTE, battleGround.getRemote(), battleGround.getRemoteCount()));
		fieldPositionsForOffence.put(BattleFieldType.FIELD_CLOSE, new FieldPosition(BattleFieldType.FIELD_CLOSE, battleGround.getWeight(), battleGround.getWeightCount()));
		fieldPositionsForOffence.put(BattleFieldType.FIELD_SIDE, new FieldPosition(BattleFieldType.FIELD_SIDE, battleGround.getLight(), battleGround.getLightCount()));
		fieldPositionsForOffence.put(BattleFieldType.FIELD_SUPPORT, new FieldPosition(BattleFieldType.FIELD_SUPPORT, Integer.MAX_VALUE, 1));
		
		fieldPositionsForDefence.put(BattleFieldType.FIELD_FLY, new FieldPosition(BattleFieldType.FIELD_FLY, battleGround.getFly(), battleGround.getFlyCount()));
		fieldPositionsForDefence.put(BattleFieldType.FIELD_FLY_FIRE, new FieldPosition(BattleFieldType.FIELD_FLY_FIRE, battleGround.getFlyFire(), battleGround.getFlyFireCount()));
		fieldPositionsForDefence.put(BattleFieldType.FIELD_FIRE, new FieldPosition(BattleFieldType.FIELD_FIRE, battleGround.getFire(), battleGround.getFireCount()));
		fieldPositionsForDefence.put(BattleFieldType.FIELD_REMOTE, new FieldPosition(BattleFieldType.FIELD_REMOTE, battleGround.getRemote(), battleGround.getRemoteCount()));
		fieldPositionsForDefence.put(BattleFieldType.FIELD_CLOSE, new FieldPosition(BattleFieldType.FIELD_CLOSE, battleGround.getWeight(), battleGround.getWeightCount()));
		fieldPositionsForDefence.put(BattleFieldType.FIELD_SIDE, new FieldPosition(BattleFieldType.FIELD_SIDE, battleGround.getLight(), battleGround.getLightCount()));
		fieldPositionsForDefence.put(BattleFieldType.FIELD_SUPPORT, new FieldPosition(BattleFieldType.FIELD_SUPPORT, Integer.MAX_VALUE, 1));
		
		
		//TODO：这里使用惰性初始化的方式，参照
		//		Map<String ,List> data = new hashMap();
		//		list a = data.get(string)
		//		if(a == null){
		//			a = new list
		//			data.put(string,a)
		//		}
		//		a.add("*****")
		//

		if(this.isLand){
			//守方自带城墙
			List<Building> forts = buildingLogic.getBuildings(fieldCityId, 1011);
			if(!forts.isEmpty()){
				//troopsInFieldDefence.get(BattleFieldType.FIELD_CLOSE).add(new FortificationBattleUnit(forts.get(0)));
			}
		}
	}
	
	public BattleField(List<Army> offenceArmys, List<Army> defenceArmys, BattleGround battleGround, boolean isLand, int fieldCityId) {
		this(battleGround, isLand, fieldCityId);

		this.armysOffence.addAll(offenceArmys);
		this.armysDefence.addAll(defenceArmys);
	}
	
	public List<cn.litgame.wargame.core.model.battle.protoround.BattleRound> getRoundHistoryPb() {
		return roundHistoryPb;
	}

	public void setRoundHistoryPb(List<cn.litgame.wargame.core.model.battle.protoround.BattleRound> roundHistoryPb) {
		this.roundHistoryPb = roundHistoryPb;
	}

	public HashMap<BattleFieldType, FieldPosition> getFieldPositionsForOffence() {
		return fieldPositionsForOffence;
	}

	public void setFieldPositionsForOffence(HashMap<BattleFieldType, FieldPosition> fieldPositionsForOffence) {
		this.fieldPositionsForOffence = fieldPositionsForOffence;
	}

	public HashMap<BattleFieldType, FieldPosition> getFieldPositionsForDefence() {
		return fieldPositionsForDefence;
	}

	public void setFieldPositionsForDefence(HashMap<BattleFieldType, FieldPosition> fieldPositionsForDefence) {
		this.fieldPositionsForDefence = fieldPositionsForDefence;
	}

	public Map<BattleFieldType, Damage> getDamageOffence() {
		return damageOffence;
	}

	public void setDamageOffence(Map<BattleFieldType, Damage> damageOffence) {
		this.damageOffence = damageOffence;
	}

	public Map<BattleFieldType, Damage> getDamageDefence() {
		return damageDefence;
	}

	public void setDamageDefence(Map<BattleFieldType, Damage> damageDefence) {
		this.damageDefence = damageDefence;
	}

	public cn.litgame.wargame.core.model.battle.protoround.BattleRound getCurrentRoundPb() {
		return currentRoundPb;
	}

	public void setCurrentRoundPb(cn.litgame.wargame.core.model.battle.protoround.BattleRound currentRoundPb) {
		this.currentRoundPb = currentRoundPb;
	}

	public void setResult(GameProtos.BattleResult result) {
		this.result = result;
	}

	public String getUUID() {
		return UUID;
	}

	public int getFieldCityId() {
		return fieldCityId;
	}

	public boolean isLand() {
		return isLand;
	}

	public long getStartTime() {
		return startTime;
	}

	public int getCurrentRoundNum() {
		return currentRoundNum;
	}

	public void setCurrentRoundNum(int currentRoundNum) {
		this.currentRoundNum = currentRoundNum;
	}

	public long getNextActionTime() {
		return nextActionTime;
	}

	public double getWeakMoraleExtra() {
		return weakMoraleExtra;
	}

	public void setWeakMoraleExtra(double weakMoraleExtra) {
		this.weakMoraleExtra = weakMoraleExtra;
	}

	public double getMoraleBuffIndex() {
		return moraleBuffIndex;
	}

	public void setMoraleBuffIndex(double moralBuffIndex) {
		this.moraleBuffIndex = moralBuffIndex;
	}

	public double getMoraleDownPercent() {
		return moraleDownPercent;
	}

	public void setMoraleDownPercent(double moraleDownPercent) {
		this.moraleDownPercent = moraleDownPercent;
	}

	public static BattleField parseFromByteArray(byte[] bytes) {
		try(ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				ObjectInputStream ois = new ObjectInputStream(bis);){
			BattleField field = (BattleField) ois.readObject();
			if(field != null)
				return field;
		} catch (IOException | ClassNotFoundException e) {
			log.error(e);
		} 
		throw new RuntimeException("read BattleField error!");
	}
	
	public byte[] toByteArray() {
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);){
			oos.writeObject(this);
			return bos.toByteArray();
		} catch (IOException e) {
			log.error(e);
			return new byte[0];
		}
	}
	
	public List<Army> getArmysOffence() {
		return armysOffence;
	}

	public void setArmysOffence(List<Army> armysOffence) {
		this.armysOffence = armysOffence;
	}

	public List<Army> getArmysDefence() {
		return armysDefence;
	}

	public void setArmysDefence(List<Army> armysDefence) {
		this.armysDefence = armysDefence;
	}

	public void addOffence(Army army){
		this.armysOffence.add(army);
	}
	
	public void addDefences(Army army){
		this.armysDefence.add(army);
	}
	
	public void addOffences(List<Army> armys){
		this.armysOffence.addAll(armys);
	}
	
	public void addDefences(List<Army> armys){
		this.armysDefence.addAll(armys);
	}

	public GameProtos.BattleResult getResult(){
		return this.result;
	}
	
	public double getMoraleOff() {
		return moraleOff;
	}

	public void setMoraleOff(double moraleOff) {
		this.moraleOff = moraleOff;
	}

	public double getMoraleDef() {
		return moraleDef;
	}

	public void setMoraleDef(double moraleDef) {
		this.moraleDef = moraleDef;
	}

	public Army getArmy(Slot battleUnit, boolean isOffence){
		if(isOffence){
			for(Army army : armysOffence){
				if(battleUnit.isFrom(army))
					return army;
			}
		}else{
			for(Army army : armysDefence){
				if(battleUnit.isFrom(army))
					return army;
			}
		}
		throw new RuntimeException("can not find such Army, army: "+battleUnit.getPlayerId()+","+battleUnit.getCityId());
	}

	public boolean isDefenceDefeated() {
		if(this.moraleDef < 0)
			return true;
		int remain = 0;
		for(Slot slot : this.fieldPositionsForDefence.get(BattleFieldType.FIELD_CLOSE).getSlots()){
			remain += slot.getCount();
		}
		
		for(Slot slot : this.fieldPositionsForDefence.get(BattleFieldType.FIELD_REMOTE).getSlots()){
			remain += slot.getCount();
		}
		
		if(remain != 0)
			return false;
		
		return (!hasNextUnit(armysDefence, BattleFieldType.FIELD_CLOSE, largeSlot) &&
					!hasNextUnit(armysDefence, BattleFieldType.FIELD_REMOTE, largeSlot));
	}

	private boolean hasNextUnit(List<Army> armys, BattleFieldType type, Slot slot) {
		for(Army a : armys) {
			if(a.hasNextUnit(type, slot))
				return true;
		}
		return false;
	}

	public boolean isOffenceDefeated() {
		if(this.moraleOff < 0)
			return true;
		int remain = 0;
		
		for(Slot slot : this.fieldPositionsForOffence.get(BattleFieldType.FIELD_CLOSE).getSlots()){
			remain += slot.getCount();
		}
		
		for(Slot slot : this.fieldPositionsForOffence.get(BattleFieldType.FIELD_REMOTE).getSlots()){
			remain += slot.getCount();
		}
		
		if(remain != 0)
			return false;
		
		return (!hasNextUnit(armysOffence, BattleFieldType.FIELD_CLOSE, largeSlot) &&
					!hasNextUnit(armysOffence, BattleFieldType.FIELD_REMOTE, largeSlot));
	}

	public GameProtos.SimpleBattleInfo.Builder convertToSimpleBattleInfoBuilder() {
		SimpleBattleInfo.Builder builder = SimpleBattleInfo.newBuilder();
		builder.setBattleId(this.UUID);
		City city = cityLogic.getCity(this.fieldCityId);
		Player player = playerLogic.getPlayer(city.getPlayerId());
		builder.setIsOver(this.result != GameProtos.BattleResult.FIGHTING);
		builder.setCityId(city.getCityId());
		builder.setCityLevel(city.getLevel());
		builder.setCityName(city.getCityName());
		builder.setPlayerName(player.getPlayerName());
		if(builder.getIsOver()){
			if(this.result ==GameProtos.BattleResult.OFFENCE_WIN){
				for(Army a : this.armysOffence){
					builder.addWinnerId(a.getPlayerId());
				}
			}else if(this.result == GameProtos.BattleResult.DEFENCE_WIN){
				for(Army a : this.armysDefence){
					builder.addWinnerId(a.getPlayerId());
				}
			}
			long overTime = this.getStartTime() + (this.currentRoundNum - 2)*BattleField.nextActionTime;
			builder.setLastRoundTime((int) (overTime/1000));
		}
		
		return builder;
	}
	
	public GameProtos.BattleDetail.Builder convertToBattleDetailBuilder() {
		BattleDetail.Builder builder = BattleDetail.newBuilder();

		
		return builder;
		
	}

	@Override
	public String toString() {
		return "BattleField{" +
				"UUID='" + UUID + '\'' +
				", \nisLand=" + isLand +
				", \nfieldCityId=" + fieldCityId +
				", \nfieldPositionsForOffence=" + fieldPositionsForOffence +
				", \nfieldPositionsForDefence=" + fieldPositionsForDefence +
				", \ndamageOffence=" + damageOffence +
				", \ndamageDefence=" + damageDefence +
				", \narmysOffence=" + armysOffence +
				", \narmysDefence=" + armysDefence +
				", \nstartTime=" + startTime +
				", \ncurrentRoundNum=" + currentRoundNum +
				", \nresult=" + result +
				", \ncurrentRoundPb=" + currentRoundPb +
				", \nroundHistoryPb=" + roundHistoryPb +
				", \nmoraleOff=" + moraleOff +
				", \nmoraleDef=" + moraleDef +
				", \nmoraleDownPercent=" + moraleDownPercent +
				", \nmoraleBuffIndex=" + moraleBuffIndex +
				", \nweakMoraleExtra=" + weakMoraleExtra +
				'}';
	}

	public List<BattleRound> getRoundHistory() {
		return new ArrayList<>(0);
	}

	public GameProtos.BattleField convertToProto() {
		GameProtos.BattleField.Builder battleField = GameProtos.BattleField.newBuilder();
		for(Army army : this.armysOffence){
			battleField.addArmysOffence(army.convertToProto());
		}
		for(Army army : this.armysDefence){
			battleField.addArmysDefence(army.convertToProto());
		}
		battleField.setCurrentRoundNum(this.currentRoundNum);
		battleField.setFieldCityId(this.fieldCityId);
		battleField.setIsLand(this.isLand);
		battleField.setMoraleBuffIndex(this.moraleBuffIndex);
		battleField.setMoraleDef(this.moraleDef);
		battleField.setMoraleDownPercent(this.moraleDownPercent);
		battleField.setMoraleOff(this.moraleOff);
		battleField.setNextActionTime(this.getNextActionTime());
		battleField.setResult(this.result);

		for(cn.litgame.wargame.core.model.battle.protoround.BattleRound round : this.roundHistoryPb){
			battleField.addRoundHistory(round.convertToProto());
		}

		log.info(currentRoundPb);

		battleField.setStartTime(this.startTime);
		
		battleField.setBattleId(this.UUID);
		battleField.setWeakMoraleExtra(this.weakMoraleExtra);
		battleField.addAllFieldPositionsForOff(this.getFieldPositionProto(OFFENCE));
		battleField.addAllFieldPositionsForDef(this.getFieldPositionProto(DEFENCE));

		return battleField.build();
	}

	private List<GameProtos.FieldPosition> getFieldPositionProto(boolean isOffence) {
		List<GameProtos.FieldPosition> result = new ArrayList<>();
		Map<BattleFieldType, FieldPosition> map = isOffence ?
				this.fieldPositionsForOffence : this.fieldPositionsForDefence;
		for(Entry<BattleFieldType, FieldPosition> entry : map.entrySet()){
			result.add(entry.getValue().convertToProto());
		}

		return result;
	}
}
