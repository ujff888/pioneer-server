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
public class BattleField implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1011195645618729863L;

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
	
	//战场上的部队
	private Map<BattleFieldType, List<BattleUnit>> troopsInFieldOffence = new HashMap<>();
	private Map<BattleFieldType, List<BattleUnit>> troopsInFieldDefence = new HashMap<>();
	
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
	//transient private BattleRound currentRound;
	//private List<BattleRound> roundHistory = new ArrayList<>();

	transient private cn.litgame.wargame.core.model.battle.protoround.BattleRound currentRoundPb;
	transient private List<cn.litgame.wargame.core.model.battle.protoround.BattleRound> roundHistoryPb = new ArrayList<>();

	//transient private Map<String, BattleRoundInfo> roundInfoOff;
	//transient private Map<String, BattleRoundInfo> roundInfoDef;
	
	//transient private BattleRoundDetail roundDetailOff;
	//transient private BattleRoundDetail roundDetailDef;
	
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
	private final static BattleFieldType[] positionOrder = {
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
	private final static BattleFieldType[] attackOrder = {
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
		this.moraleBuffIndex = battleField.getMoraleBuffIndex();
		this.moraleDef = battleField.getMoraleDef();
		this.moraleDownPercent = battleField.getMoraleDownPercent();
		this.moraleOff = battleField.getMoraleOff();
		this.result = battleField.getResult();
//		for(GameProtos.BattleRound round : battleField.getRoundHistoryList()){
//			this.roundHistoryPb.add(new cn.litgame.wargame.core.model.battle.protoround.BattleRound(round));
//		}
		this.setTroopsInField(OFFENCE, battleField);
		this.setTroopsInField(DEFENCE, battleField);
		this.weakMoraleExtra = battleField.getWeakMoraleExtra();

		this.setFieldPosition(OFFENCE, battleField);
		this.setFieldPosition(DEFENCE, battleField);
	}

	private void setTroopsInField(boolean isOffence, GameProtos.BattleField field){
		Collection<GameProtos.BattleUnitList> collection = isOffence ?
				field.getTroopsInFieldOffList() : field.getTroopsInFieldDefList();
		for(GameProtos.BattleUnitList unitList : collection){
			List<BattleUnit> battleUnits = new ArrayList<>();
			for(GameProtos.BattleUnit pbUnit : unitList.getUnitList()){
				TroopType troopType = configLogic.getResTroop(pbUnit.getTroopId()).getTroopType();
				BattleUnit bu = initBattleUnit(troopType, pbUnit);
				if(bu != null)
					battleUnits.add(bu);
			}
			if(isOffence)
				this.troopsInFieldOffence.put(unitList.getType(), battleUnits);
			else
				this.troopsInFieldDefence.put(unitList.getType(), battleUnits);
		}
	}

	private BattleUnit initBattleUnit(TroopType type, GameProtos.BattleUnit unit) {
		BattleUnit bu = null;
		switch (type){
			case REMOTE:
				bu = new RemoteBattleUnit(unit);
				return bu;
			case LIGHT:
				bu = new LightBattleUnit(unit);
				return bu;
			case WEIGHT:
				bu = new WeightBattleUnit(unit);
				return bu;
			case FLY_AIR:
				bu = new FlyAirBattleUnit(unit);
				return bu;
			case FLY_FIRE:
				bu = new FlyFireBattleUnit(unit);
				return bu;
			case FIRE:
				bu = new FireBattleUnit(unit);
				return bu;
			case LOGISTICS:
				bu = new LogisticsBattleUnit(unit);
				return bu;
			case NPC:
				bu = new NpcBattleUnit(unit);
				return bu;
			default:
				return bu;
		}
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
	
	public Map<BattleFieldType, List<BattleUnit>> getTroopsInFieldOffence() {
		return troopsInFieldOffence;
	}

	public void setTroopsInFieldOffence(Map<BattleFieldType, List<BattleUnit>> troopsInFieldOffence) {
		this.troopsInFieldOffence = troopsInFieldOffence;
	}

	public Map<BattleFieldType, List<BattleUnit>> getTroopsInFieldDefence() {
		return troopsInFieldDefence;
	}

	public void setTroopsInFieldDefence(Map<BattleFieldType, List<BattleUnit>> troopsInFieldDefence) {
		this.troopsInFieldDefence = troopsInFieldDefence;
	}

	public List<BattleUnit> getTroopsInFieldOffenceByPosition(BattleFieldType type) {
		List<BattleUnit> result = troopsInFieldOffence.get(type);
		if(result == null){
			result = new ArrayList<>();
			troopsInFieldOffence.put(type, result);
		}
		return result;
	}
	
	public List<BattleUnit> getTroopsInFieldDefenceByPosition(BattleFieldType type) {
		List<BattleUnit> result = troopsInFieldDefence.get(type);
		if(result == null){
			result = new ArrayList<>();
			troopsInFieldDefence.put(type, result);
		}
		return result;
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

	public Army getArmy(BattleUnit battleUnit, boolean isOffence){
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
	
	/**
	 * 双方军队就位
	 *
	 * @return 双方战场上军队的损失情况
	 */
	private void armyInPosition() {
		log.info("进攻方军队上场");
		putOffenceInPosition();
		log.info("防守方军队上场");
		putDefenceInPosition();
	}
	
	public int fight() {
 		while(result == GameProtos.BattleResult.FIGHTING){
 			nextRound();
 			saveRound();
 		}
		log.info("战斗结束");
		if(isOffenceDefeated() && isDefenceDefeated()){
			log.info("双方不分胜负");
			return 0;
		}else if(isDefenceDefeated()){
			log.info("防守方被击败");
			return 1;
		}else{
			log.info("进攻方被击败");
			return -1;
		}
 	}
	
	public BattleRound saveRound(){
//		currentRound.save(this);
//		roundHistory.add(currentRound);
//		return currentRound;
		return null;
	}
	
	private boolean isDefenceDefeated() {
		if(this.moraleDef < 0)
			return true;
		int remain = getTroopsInFieldDefenceByPosition(BattleFieldType.FIELD_CLOSE).size();
		remain += getTroopsInFieldDefenceByPosition(BattleFieldType.FIELD_REMOTE).size();
		
		if(remain != 0)
			return false;
		
		return (!hasNextUnit(armysDefence, BattleFieldType.FIELD_CLOSE, largeSlot) &&
					!hasNextUnit(armysDefence, BattleFieldType.FIELD_REMOTE, largeSlot));
	}

	private boolean isOffenceDefeated() {
		if(this.moraleOff < 0)
			return true;
		int remain = getTroopsInFieldOffenceByPosition(BattleFieldType.FIELD_CLOSE).size();
		remain += getTroopsInFieldOffenceByPosition(BattleFieldType.FIELD_REMOTE).size();
		
		if(remain != 0)
			return false;
		
		return (!hasNextUnit(armysOffence, BattleFieldType.FIELD_CLOSE, largeSlot) &&
					!hasNextUnit(armysOffence, BattleFieldType.FIELD_REMOTE, largeSlot));
	}

	public void nextRound(){
		this.initDamage(); 
		
//		roundInfoOff = new HashMap<>();
//		for(Army a : this.getArmysOffence()){
//			BattleRoundInfo br = new BattleRoundInfo();
//			br.setPlayeId(a.getPlayerId());
//			br.setCityId(a.getCityId());
//			roundInfoOff.put(a.getPlayerId()+","+a.getCityId(), br);
//		}
//
//		roundInfoDef = new HashMap<>();
//		for(Army a : this.getArmysDefence()){
//			BattleRoundInfo br = new BattleRoundInfo();
//			br.setPlayeId(a.getPlayerId());
//			br.setCityId(a.getCityId());
//			roundInfoDef.put(a.getPlayerId()+","+a.getCityId(), br);
//		}
//
//		roundDetailOff = new BattleRoundDetail();
//		roundDetailDef = new BattleRoundDetail();
//
//		roundDetailOff.setMorale(this.moraleOff);
//		roundDetailDef.setMorale(this.moraleDef);
		
		//currentRound = new BattleRound(currentRoundNum);
		currentRoundPb = new cn.litgame.wargame.core.model.battle.protoround.BattleRound(currentRoundNum);
		
		log.info("双方上场");
		armyInPosition();		
		
		log.info("双方行动");
		for(BattleFieldType type : attackOrder){
			offenceAction(troopsInFieldDefence, troopsInFieldOffence, type);
			defenceAction(troopsInFieldOffence, troopsInFieldDefence, type);
		}
		
		log.info("进攻方结算伤害");
		this.clearField(OFFENCE);
		log.info("防守方结算伤害");
		this.clearField(DEFENCE);
		
		this.heal(OFFENCE);
		this.heal(DEFENCE);
		
		//this.collectRoundInfo(currentRound);
		this.moraleDown();

		this.collectRoundInfo();

		currentRoundNum++;
		
		if(!isOffenceDefeated() && !isDefenceDefeated()){
			result = GameProtos.BattleResult.FIGHTING;
		}else if(isDefenceDefeated() && !isOffenceDefeated()){
			log.info("防守方被击败");
			result = GameProtos.BattleResult.OFFENCE_WIN;
		}else if(isOffenceDefeated() && !isDefenceDefeated()){
			log.info("进攻方被击败");
			result = GameProtos.BattleResult.DEFENCE_WIN;
		}else{
			log.info("双方不分胜负");
			result =GameProtos.BattleResult.EVEN;
		}
	}
	
	private void collectRoundInfo(){
//		this.generateRoundInfo(OFFENCE);
//		this.generateRoundInfo(DEFENCE);
//
//		currentRound.setRoundInfoOff(roundInfoOff);
//		currentRound.setRoundInfoDef(roundInfoDef);
//		currentRound.setRoundDetailOff(roundDetailOff);
//		currentRound.setRoundDetailDef(roundDetailDef);
		currentRoundPb.generateRoundInfo(this, OFFENCE);
		currentRoundPb.generateRoundInfo(this, DEFENCE);

		currentRoundPb.generateRoundDetail(this, OFFENCE);
		currentRoundPb.generateRoundDetail(this, DEFENCE);
		//roundHistoryPb.add(currentRoundPb);
	}

//	private void generateRoundInfo(boolean isOffence){
//		Collection<Entry<BattleFieldType, List<BattleUnit>>> entries = isOffence ? troopsInFieldOffence.entrySet() : troopsInFieldDefence.entrySet();
//		BattleRoundDetail roundDetail = isOffence ? roundDetailOff : roundDetailDef;
//		for(Entry<BattleFieldType, List<BattleUnit>> entry : entries){
//			for(BattleUnit bu : entry.getValue()){
//				int count = bu.getCount();
//				BattleRoundInfo br = this.getBattleRound(bu.getPlayerId(), bu.getCityId(), isOffence);
//				RoundTroopInfo rti = br.getRoundTroopInfo(bu.getTroopId());
//				if(rti == null)
//					rti = new RoundTroopInfo(bu.getTroopId(), 0, 0);
//
//				rti.setCount(rti.getCount() + count);
//				br.putRoundTroopInfo(rti.getTroopId(), rti);
//
//				RoundTroopDetail rtd = roundDetail.getRoundTroopDetail(bu.getTroopId(), bu.getPosition().getType());
//				if(rtd == null)
//					rtd = new RoundTroopDetail(bu.getTroopId(), bu.getPosition().getType(), 0, 0, bu.getAmountRemain());
//				rtd.setCount(rtd.getCount() + count);
//				roundDetail.putRoundTroopDetail(rtd.getTroopId(), rtd.getFieldType(), rtd);
//			}
//		}
//		Collection<Army> armys = isOffence ? this.armysOffence : this.armysDefence;
//		for(Army a : armys){
//			BattleRoundInfo br = this.getBattleRound(a.getPlayerId(), a.getCityId(), isOffence);
//			for(List<BattleTroop> troops : a.getBackupBattleTroops().values()){
//				for(BattleTroop bt : troops){
//					int resTroopId = bt.getResTroop().getId();
//					RoundTroopInfo rti = br.getRoundTroopInfo(resTroopId);
//					if(rti == null)
//						rti = new RoundTroopInfo(resTroopId, 0, 0);
//
//					rti.setCount(rti.getCount()+bt.getCount());
//					br.putRoundTroopInfo(rti.getTroopId(), rti);
//				}
//			}
//		}
//	}
	
	private void heal(boolean isOffence) {
		
	}

	/**
	 * 士气降低
	 */
	private void moraleDown(){
		this.moraleOff = this.moraleOff - this.getMoraleDownPercent();
		this.moraleDef = this.moraleDef - this.getMoraleDownPercent();
		
		//int lostOff = currentRound.getTotalLost(OFFENCE);
		//int lostDef = currentRound.getTotalLost(DEFENCE);

		int lostOff = currentRoundPb.getTotalLost(OFFENCE);
		int lostDef = currentRoundPb.getTotalLost(DEFENCE);

		if(lostOff != lostDef){
			if(lostOff > lostDef)
				this.moraleOff = this.moraleOff - this.getMoraleDownPercent()*this.getWeakMoraleExtra();
			else
				this.moraleDef = this.moraleDef - this.getMoraleDownPercent()*this.getWeakMoraleExtra();
		}
		
		//currentRound.getRoundDetailOff().setMorale(moraleOff);
		//currentRound.getRoundDetailDef().setMorale(moraleDef);
	}
	
	private void initDamage(){
		damageOffence = new HashMap<>();
		damageDefence = new HashMap<>();
		
		damageOffence.put(BattleFieldType.FIELD_CLOSE, new Damage(0,0));
		damageOffence.put(BattleFieldType.FIELD_REMOTE, new Damage(0,0));
		damageOffence.put(BattleFieldType.FIELD_FIRE, new Damage(0,0));
		damageOffence.put(BattleFieldType.FIELD_SIDE, new Damage(0,0));
		damageOffence.put(BattleFieldType.FIELD_FLY, new Damage(0,0));
		damageOffence.put(BattleFieldType.FIELD_FLY_FIRE, new Damage(0,0));
		damageOffence.put(BattleFieldType.FIELD_SUPPORT, new Damage(0,0));
		
		damageDefence.put(BattleFieldType.FIELD_CLOSE, new Damage(0,0));
		damageDefence.put(BattleFieldType.FIELD_REMOTE, new Damage(0,0));
		damageDefence.put(BattleFieldType.FIELD_FIRE, new Damage(0,0));
		damageDefence.put(BattleFieldType.FIELD_SIDE, new Damage(0,0));
		damageDefence.put(BattleFieldType.FIELD_FLY, new Damage(0,0));
		damageDefence.put(BattleFieldType.FIELD_FLY_FIRE, new Damage(0,0));
		damageDefence.put(BattleFieldType.FIELD_SUPPORT, new Damage(0,0));
	}
	
	private void offenceAction(Map<BattleFieldType, List<BattleUnit>> enemy, Map<BattleFieldType, List<BattleUnit>> self, BattleFieldType type) {
		log.info("进攻方"+type+"位置行动");
		int i=0;
		for(BattleUnit bu : getTroopsInFieldOffenceByPosition(type)){
			log.info(i++);
			bu.doAction(enemy, self, damageDefence);
		}
	}
	
	private void defenceAction(Map<BattleFieldType, List<BattleUnit>> enemy, Map<BattleFieldType, List<BattleUnit>> self, BattleFieldType type) {
		log.info("防守方"+type+"位置行动");
		int i = 0;
		for(BattleUnit bu : getTroopsInFieldDefenceByPosition(type)){
			log.info(i++);
			bu.doAction(enemy, self, damageOffence);
		}
	}
	
	/**
	 * 作战单位上场
	 */
	private void putOffenceInPosition() {
		for(BattleFieldType type : positionOrder){
			FieldPosition position = this.fieldPositionsForOffence.get(type);
			for(Slot slot : position.getSlots()){
				BattleUnit bunit = null;
				if(hasNextUnit(armysOffence, type, slot)){
					bunit = getNextUnit(armysOffence, type, slot);
					if(bunit != null)
						pushAUnitToFieldOffence(bunit, position, slot);
				}
			}
		}
		

	}
	
	private boolean hasNextUnit(List<Army> armys, BattleFieldType type, Slot slot) {
		for(Army a : armys) {
			if(a.hasNextUnit(type, slot))
				return true;
		}
		return false;
	}
	
	private void pushAUnitToFieldOffence(BattleUnit bu, FieldPosition position, Slot slot) {
		bu.comeToField(position, slot);
		List<BattleUnit> units = this.getTroopsInFieldOffenceByPosition(position.getType());
		if(units == null){
			units = new ArrayList<>();
			units.add(bu);
			this.getTroopsInFieldOffence().put(position.getType(), units);
			return;
		}
		for(BattleUnit unit : units){
			if(unit.getTroopId() == bu.getTroopId()
					&& unit.getBattleFieldType() == position.getType()
					&& unit.getSlotNum() == bu.getSlotNum()){
				unit = unit.add(bu);
				return;
			}
		}
		
		this.getTroopsInFieldOffenceByPosition(position.getType()).add(bu);
	}
	
	private void putDefenceInPosition() {
		for(FieldPosition position : fieldPositionsForDefence.values()){
			if(position.getType() == BattleFieldType.FIELD_CLOSE
					&& getTroopsInFieldDefenceByPosition(BattleFieldType.FIELD_CLOSE).size() != 0
					&& getTroopsInFieldDefenceByPosition(BattleFieldType.FIELD_CLOSE).get(0).isFortificationUnit()){
				
				continue;
			}
			for(Slot slot : position.getSlots()){
				BattleFieldType type = position.getType();
				BattleUnit bunit = null;
				if(hasNextUnit(armysDefence, type, slot)){
					bunit = getNextUnit(armysDefence, type, slot);
					if(bunit != null)
						pushAUnitToFieldDefence(bunit, position, slot);
				}
			}
		}
	}
	
	private void pushAUnitToFieldDefence(BattleUnit bu, FieldPosition position, Slot slot) {
		bu.comeToField(position, slot);
		List<BattleUnit> units = this.getTroopsInFieldDefenceByPosition(position.getType());
		if(units == null){
			units = new ArrayList<>();
			units.add(bu);
			this.getTroopsInFieldDefence().put(position.getType(), units);
			return;
		}
		for(BattleUnit unit : units){
			if(unit.getTroopId() == bu.getTroopId()
					&& unit.getBattleFieldType() == position.getType()
					&& unit.getSlotNum() == bu.getSlotNum()){
				unit = unit.add(bu);
				return;
			}
		}
		
		this.getTroopsInFieldDefenceByPosition(position.getType()).add(bu);
	}

	/**
	 * 整理战场，包括结算伤害和将没有弹药的远程单位重新分配至近战后备
	 * @Param isOffence
	 * @return
	 */
	private void clearField(boolean isOffence) {
		//结算伤害
		for(Entry<BattleFieldType, Damage> entry : getDamage(isOffence).entrySet()){
			if(!entry.getValue().isEmpty()){
				takeDamage(isOffence, entry.getKey(), entry.getValue());
			}
		}
		
		this.reArange(isOffence);
	}
	
	private Map<BattleFieldType, Damage> getDamage(boolean isOffence) {
		return (isOffence ? this.damageOffence : this.damageDefence);
	}

	/**
	 * 结算伤害
	 * @param isOffence
	 * @param type
     * @param damage
     */
 	private void takeDamage(boolean isOffence, BattleFieldType type, Damage damage) {
		List<BattleUnit> targets = null;
		if(isOffence)
			targets = this.getTroopsInFieldOffenceByPosition(type);
		else
			targets = this.getTroopsInFieldDefenceByPosition(type);
		int size = targets.size();
		int count = damage.getUnitCount(); 
		if(count>size)
			damage.setUnitCount(size);
		targets = targets.subList(0, damage.getUnitCount());
		while(!damage.isEmpty() && damage.getUnitCount() > 0 && !targets.isEmpty()){
			for(BattleUnit bu : targets){
				int ad = damage.getAvgDamage();
				bu.takeDamage(ad);
				log.info("一个单位"+bu.getTroopId()+"受到"+(ad > bu.getDefense() ? ad-bu.getDefense() : 0)+"点伤害，生命值:"+bu.getHp()+" 类型"+bu.getBattleFieldType());
				damage.setDamageValue(damage.getDamageValue() - ad);
				damage.setUnitCount(damage.getUnitCount() - 1);
			}
		}
		removeZeroHpUnitFromField(type, isOffence);
		
	}
	
	private void removeZeroHpUnitFromField(BattleFieldType type, boolean isOffence) {
		Iterator<BattleUnit> itr = null;
		if(isOffence)
			itr = getTroopsInFieldOffenceByPosition(type).iterator();
		else
			itr = getTroopsInFieldDefenceByPosition(type).iterator();
		while(itr.hasNext()){
			BattleUnit bu = itr.next();
			int lostNo = bu.getOriginalCount() - bu.getCount();
			if(lostNo > 0){
				bu.setOriginalCount(bu.getOriginalCount() - lostNo);
				log.info("一个单位："+bu.getTroopId()+"数目由"+(bu.getOriginalCount()+lostNo)+"变为"+bu.getOriginalCount());
			}
			if(lostNo > 0){
				currentRoundPb.addLost(bu.getTroopId(), lostNo, type, isOffence);
//				for(int i=0; i<lostNo; i++){
//					BattleRoundInfo br = this.getBattleRound(bu.getPlayerId(), bu.getCityId(), isOffence);
//					if(br == null)
//						br = new BattleRoundInfo(bu.getPlayerId(), bu.getCityId());
//					RoundTroopInfo rti = br.getRoundTroopInfo(bu.getTroopId());
//					if(rti == null)
//						rti = new RoundTroopInfo(bu.getTroopId(), 0, 0);
//					rti.setLost(rti.getLost()+1);
//					br.putRoundTroopInfo(bu.getTroopId(), rti);
//
//					this.putBattleRound1(bu.getPlayerId(), bu.getCityId(), br, isOffence);
//
//					RoundTroopDetail rtd = this.getRoundTroopDetail(bu.getTroopId(), type, isOffence);
//					if(rtd == null)
//						rtd = new RoundTroopDetail(bu.getTroopId(), type, 0, 0, bu.getAmountRemain());
//					rtd.setLost(rtd.getLost()+1);
//					rtd.setAmountRemain(bu.getAmountRemain());
//
//					this.putRoundTroopDetail(bu.getTroopId(), type, rtd, isOffence);
//				}
			}
			if(bu.getHp() <= 0){
				itr.remove();

				FieldPosition fieldPosition = isOffence ?
						this.fieldPositionsForOffence.get(type)
						: this.fieldPositionsForDefence.get(type);
				Slot slot = fieldPosition.getSlots().get(bu.getSlotNum());
				slot.remove(bu);
				log.info("一个单位退场" + bu.getTroopId() + "," + bu.getBattleFieldType());
			}
		}
	}

//	private BattleRoundInfo getBattleRound(long playerId, int cityId, boolean isOffence) {
//		return isOffence ?
//				this.roundInfoOff.get(playerId+","+cityId) :
//					this.roundInfoDef.get(playerId+","+cityId);
//	}
//
//	private void putBattleRound1(long playerId, int cityId, BattleRoundInfo br, boolean isOffence) {
//		if(isOffence){
//			roundInfoOff.put(playerId+","+cityId, br);
//		}else{
//			roundInfoDef.put(playerId+","+cityId, br);
//		}
//
//	}
//
//	private RoundTroopDetail getRoundTroopDetail(int troopId, BattleFieldType type, boolean isOffence) {
//		return isOffence ?
//				roundDetailOff.getRoundTroopDetail(troopId,  type) :
//					roundDetailDef.getRoundTroopDetail(troopId, type);
//	}
//
//	private void putRoundTroopDetail(int troopId, BattleFieldType type, RoundTroopDetail roundTroopDetail, boolean isOffence) {
//		if(isOffence)
//			roundDetailOff.putRoundTroopDetail(troopId, type, roundTroopDetail);
//		else
//			roundDetailDef.putRoundTroopDetail(troopId, type, roundTroopDetail);
//
//	}
	
	private void reArange(boolean isOffence){
		//将没有弹药的远程单位重新分配至后备部队
		this.removeUnitOutofAmmo(BattleFieldType.FIELD_REMOTE, TroopType.REMOTE_NO_AMMO, isOffence);
			
		//将没有弹药的轰炸单位直接移出战场
		this.removeUnitOutofAmmo(BattleFieldType.FIELD_FLY_FIRE, TroopType.FLY_FIRE, isOffence);
			
		//将没有弹药的空战单位直接移出战场
		this.removeUnitOutofAmmo(BattleFieldType.FIELD_FLY, TroopType.FLY_AIR, isOffence);
	}
	
	private void removeUnitOutofAmmo(BattleFieldType fieldType, TroopType troopType, boolean isOffence){
		Iterator<BattleUnit> it = getTroopsInFieldOffenceByPosition(fieldType).iterator();
		while(it.hasNext()){
			BattleUnit bu = it.next();
			if(bu.getAmount() <= 0){
				it.remove();
				getArmy(bu, isOffence).addAUnit(troopType, bu);

				FieldPosition fieldPosition = isOffence ?
						this.fieldPositionsForOffence.get(fieldType)
						: this.fieldPositionsForDefence.get(fieldType);
				Slot slot = fieldPosition.getSlots().get(bu.getSlotNum());
				slot.remove(bu);
			}
		}
	}
	
	private BattleUnit getNextUnit(List<Army> armys, BattleFieldType position, Slot slot){
		for(Army army : armys){
			if(army.hasNextUnit(position, slot))
				return army.getNextUnit(position, slot);
		}
		return null;
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
				", \ntroopsInFieldOffence=" + troopsInFieldOffence +
				", \ntroopsInFieldDefence=" + troopsInFieldDefence +
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

//		for(cn.litgame.wargame.core.model.battle.protoround.BattleRound round : this.roundHistoryPb){
//			battleField.addRoundHistory(round.convertToProto());
//		}

		log.info(currentRoundPb);

		battleField.setStartTime(this.startTime);
		battleField.addAllTroopsInFieldOff(this.getTroopsInFieldProto(OFFENCE));
		battleField.addAllTroopsInFieldDef(this.getTroopsInFieldProto(DEFENCE));
		battleField.setBattleId(this.UUID);
		battleField.setWeakMoraleExtra(this.weakMoraleExtra);
		battleField.addAllFieldPositionsForOff(this.getFieldPositionProto(OFFENCE));
		battleField.addAllFieldPositionsForDef(this.getFieldPositionProto(DEFENCE));

		return battleField.build();
	}

	private List<GameProtos.BattleUnitList> getTroopsInFieldProto(boolean isOffence) {
		List<GameProtos.BattleUnitList> result = new ArrayList<>();
		Map<BattleFieldType, List<BattleUnit>> map = isOffence ?
				this.getTroopsInFieldOffence() : this.getTroopsInFieldDefence();
		for(Entry<BattleFieldType, List<BattleUnit>> entry : map.entrySet()){
			GameProtos.BattleUnitList.Builder listBuilder = GameProtos.BattleUnitList.newBuilder();
			listBuilder.setType(entry.getKey());
			for(BattleUnit battleUnit : entry.getValue()){
				listBuilder.addUnit(battleUnit.convertToProto());
			}
			result.add(listBuilder.build());
		}
		return result;
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
