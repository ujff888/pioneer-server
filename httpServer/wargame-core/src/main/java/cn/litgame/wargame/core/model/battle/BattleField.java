package cn.litgame.wargame.core.model.battle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.http.impl.io.ContentLengthOutputStream;
import org.apache.log4j.Logger;
import org.kriver.core.common.KeyUtil;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.BattleDetail;
import cn.litgame.wargame.core.auto.GameProtos.SimpleBattleInfo;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.auto.GameResProtos.BattleGround;
import cn.litgame.wargame.core.auto.GameResProtos.TroopType;
import cn.litgame.wargame.core.logic.BuildingLogic;
import cn.litgame.wargame.core.logic.CityLogic;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.logic.PlayerLogic;
import cn.litgame.wargame.core.model.battle.round.BattleRoundInfo;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.core.model.battle.round.BattleRoundDetail;
import cn.litgame.wargame.core.model.battle.round.RoundTroopDetail;
import cn.litgame.wargame.core.model.battle.round.RoundTroopInfo;
import cn.litgame.wargame.core.model.battle.unit.BattleUnit;
import cn.litgame.wargame.core.model.battle.unit.FortificationBattleUnit;
import redis.clients.jedis.Jedis;

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

	@Resource(name = "buildingLogic")
	private static BuildingLogic buildingLogic;
	
	@Resource(name = "cityLogic")
	private static CityLogic cityLogic;
	
	@Resource(name = "playerLogic")
	private static PlayerLogic playerLogic;
	
	
	private static final Slot largeSlot = new Slot(Integer.MAX_VALUE);
	
	public final static int RESULT_FIGHTING = 0;
	public final static int RESULT_OFFENCE_VICTORY = 1;
	public final static int RESULT_DEFENCE_VICTORY = 2;
	public final static int RESULT_EVEN = 3;

	
	public final static boolean LAND = true;
	public final static boolean FLY = false;
	private final static Logger log = Logger.getLogger(BattleField.class);
	
	private final String UUID;
	
	private final boolean isLand;
	private final int fieldCityId;
	
	private HashMap<BattleFieldType, FieldPosition> fieldPositionsForOffence = new LinkedHashMap<>();
	private HashMap<BattleFieldType, FieldPosition> fieldPositionsForDefence = new LinkedHashMap<>();
	
	//战场上的部队
	private Map<BattleFieldType, List<BattleUnit>> troopsInFieldOffence = new HashMap<>();
	private Map<BattleFieldType, List<BattleUnit>> troopsInFieldDefence = new HashMap<>();
	
	//待结算的伤害
	transient private Map<BattleFieldType, Damage> damageOffence;
	transient private Map<BattleFieldType, Damage> damageDefence;

	private List<Army> armysOffence = new ArrayList<>();
	private List<Army> armysDefence = new ArrayList<>();
	
	private double offenceMorale;
	private double defenceMorale;
	
	private final long startTime;
	private static final long nextActionTime = 0;//下一次的触发时间,单位是毫秒
	private int currentRoundNum = 1;//当前的回合数
	
	private int result;//战斗结果 0：未结束 1：进攻方胜利 2：防守方胜利 3：不分胜负
	
	//历史的回合数也要存盘
	transient private BattleRound currentRound;
	
	transient private Map<String, BattleRoundInfo> roundInfoOff;
	transient private Map<String, BattleRoundInfo> roundInfoDef;
	
	transient private BattleRoundDetail roundDetailOff;
	transient private BattleRoundDetail roundDetailDef;
	
	private double moraleOff = 1;
	private double moraleDef = 1;
	
	private double moraleDownPercent = 0.1;
	private double moraldBuffIndex = 0;
	private double weakMoraleExtra = 0;
	
	public static final boolean OFFENCE = true;
	public static final boolean DEFENCE = false;
	
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

		
	}
	
	@PostConstruct
	public void init(){
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

	public double getMoralBuffIndex() {
		return moraldBuffIndex;
	}

	public void setMoralBuffIndex(double moralBuffIndex) {
		this.moraldBuffIndex = moralBuffIndex;
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

	public int getResult(){
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
	 * @param battleRound
	 * @return 双方战场上军队的损失情况
	 */
	private void armyInPosition() {
		log.info("进攻方军队上场");
		putOffenceInPosition();
		log.info("防守方军队上场");
		putDefenceInPosition();
	}
	
	public int fight() {
 		while(result == RESULT_FIGHTING){
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
		currentRound.save(this);
		return currentRound;
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
		
		roundInfoOff = new HashMap<>();
		for(Army a : this.getArmysOffence()){
			BattleRoundInfo br = new BattleRoundInfo();
			br.setPlayeId(a.getPlayerId());
			br.setCityId(a.getCityId());
			roundInfoOff.put(a.getPlayerId()+","+a.getCityId(), br);
		}
		
		roundInfoDef = new HashMap<>();
		for(Army a : this.getArmysDefence()){
			BattleRoundInfo br = new BattleRoundInfo();
			br.setPlayeId(a.getPlayerId());
			br.setCityId(a.getCityId());
			roundInfoDef.put(a.getPlayerId()+","+a.getCityId(), br);
		}
		
		roundDetailOff = new BattleRoundDetail();
		roundDetailDef = new BattleRoundDetail();
		
		roundDetailOff.setMorale(offenceMorale);
		roundDetailDef.setMorale(defenceMorale);
		
		currentRound = new BattleRound(currentRoundNum);
		
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
		
		this.collectRountInfo(currentRound);
		
		this.moraleDown();
		
		currentRoundNum++;
		
		if(!isOffenceDefeated() && !isDefenceDefeated()){
			result = RESULT_FIGHTING;
		}else if(isDefenceDefeated() && !isOffenceDefeated()){
			log.info("防守方被击败");
			result = RESULT_OFFENCE_VICTORY;
		}else if(isOffenceDefeated() && !isDefenceDefeated()){
			log.info("进攻方被击败");
			result = RESULT_DEFENCE_VICTORY;
		}else{
			log.info("双方不分胜负");
			result = RESULT_EVEN;
		}
	}
	
	private void collectRountInfo(BattleRound currentRound){
		for(Entry<BattleFieldType, List<BattleUnit>> entry : troopsInFieldOffence.entrySet()){
			for(BattleUnit bu : entry.getValue()){
				BattleRoundInfo br = this.getBattleRound(bu.getPlayerId(), bu.getCityId(), OFFENCE);
				RoundTroopInfo rti = br.getRoundTroopInfo(bu.getTroopId());
				if(rti == null)
					rti = new RoundTroopInfo(bu.getTroopId(), 0, 0);
				
				rti.setCount(rti.getCount()+1);
				br.putRoundTroopInfo(rti.getTroopId(), rti);
				
				RoundTroopDetail rtd = roundDetailOff.getRoundTroopDetail(bu.getTroopId(), bu.getPosition().getType());
				if(rtd == null)
					rtd = new RoundTroopDetail(bu.getTroopId(), bu.getPosition().getType(), 0, 0, bu.getAmountRemain());
				rtd.setCount(rtd.getCount()+1);
				roundDetailOff.putRoundTroopDetail(rtd.getTroopId(), rtd.getFieldType(), rtd);
			}
		}
		
		for(Entry<BattleFieldType, List<BattleUnit>> entry : troopsInFieldDefence.entrySet()){
			for(BattleUnit bu : entry.getValue()){
				BattleRoundInfo br = this.getBattleRound(bu.getPlayerId(), bu.getCityId(), DEFENCE);
				RoundTroopInfo rti = br.getRoundTroopInfo(bu.getTroopId());
				if(rti == null)
					rti = new RoundTroopInfo(bu.getTroopId(), 0, 0);
				
				rti.setCount(rti.getCount()+1);
				br.putRoundTroopInfo(rti.getTroopId(), rti);
				
				RoundTroopDetail rtd = roundDetailDef.getRoundTroopDetail(bu.getTroopId(), bu.getPosition().getType());
				if(rtd == null)
					rtd = new RoundTroopDetail(bu.getTroopId(), bu.getPosition().getType(), 0, 0, bu.getAmountRemain());
				rtd.setCount(rtd.getCount()+1);
				roundDetailDef.putRoundTroopDetail(rtd.getTroopId(), rtd.getFieldType(), rtd);
			}
		}
		
		currentRound.setRoundInfoOff(roundInfoOff);
		currentRound.setRoundInfoDef(roundInfoDef);
		currentRound.setRoundDetailOff(roundDetailOff);
		currentRound.setRoundDetailDef(roundDetailDef);
	}
	
	private void heal(boolean isOffence) {
		
	}

	/**
	 * 士气降低
	 */
	private void moraleDown(){
		this.moraleOff = this.moraleOff - this.getMoraleDownPercent();
		this.moraleDef = this.moraleDef - this.getMoraleDownPercent();
		
		int lostOff = currentRound.getTotalLost(OFFENCE);
		int lostDef = currentRound.getTotalLost(DEFENCE);
		
		if(lostOff != lostDef){
			if(lostOff > lostDef)
				this.moraleOff = this.moraleOff - this.getMoraleDownPercent()*this.getWeakMoraleExtra();
			else
				this.moraleDef = this.moraleDef - this.getMoraleDownPercent()*this.getWeakMoraleExtra();
		}
		
		currentRound.getRoundDetailOff().setMorale(moraleOff);
		currentRound.getRoundDetailDef().setMorale(moraleDef);
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
	 * 
	 * @param army
	 * @param fieldPositions
	 * @return
	 */
	private void putOffenceInPosition() {
		
		for(FieldPosition position : fieldPositionsForOffence.values()){
			for(Slot slot : position.getSlots()){
				BattleFieldType type = position.getType();
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
			if(unit.getTroopId() == bu.getTroopId() && unit.getSlot() == bu.getSlot()){
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
			if(unit.getTroopId() == bu.getTroopId() && unit.getSlot() == bu.getSlot()){
				unit = unit.add(bu);
				return;
			}
		}
		
		this.getTroopsInFieldDefenceByPosition(position.getType()).add(bu);
	}

	/**
	 * 整理战场，包括结算伤害和将没有弹药的远程单位重新分配至近战后备
	 * 
	 * @param army
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
	 * 
	 * @param army 承受伤害的军队
	 * @param type 承受伤害的位置
	 * @param damage
	 * @return
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
				log.info("一个单位"+bu.getTroopId()+"受到"+(ad > bu.getDefense() ? ad-bu.getDefense() : 0)+"点伤害，生命值:"+bu.getHp()+" 类型"+bu.getPosition().getType());
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
			int lostNo = bu.getHp()/bu.getOriginalHp();
			if(lostNo > 0){
				for(int i=0; i<lostNo; i++){
					BattleRoundInfo br = this.getBattleRound(bu.getPlayerId(), bu.getCityId(), isOffence);
					if(br == null)
						br = new BattleRoundInfo(bu.getPlayerId(), bu.getCityId());
					RoundTroopInfo rti = br.getRoundTroopInfo(bu.getTroopId());
					if(rti == null)
						rti = new RoundTroopInfo(bu.getTroopId(), 0, 0);
					rti.setLost(rti.getLost()+1);
					br.putRoundTroopInfo(bu.getTroopId(), rti);
					
					this.putBattleRound1(bu.getPlayerId(), bu.getCityId(), br, isOffence);
					
					RoundTroopDetail rtd = this.getRoundTroopDetail(bu.getTroopId(), type, isOffence);
					if(rtd == null)
						rtd = new RoundTroopDetail(bu.getTroopId(), type, 0, 0, bu.getAmountRemain());
					rtd.setLost(rtd.getLost()+1);
					rtd.setAmountRemain(bu.getAmountRemain());
					
					this.putRoundTroopDetail(bu.getTroopId(), type, rtd, isOffence);
				}
			}
			if(bu.getHp() <= 0){
				itr.remove();
				bu.getSlot().remove(bu);
				log.info("一个单位退场" + bu.getTroopId() + "," + bu.getPosition().getType());
			}
		}
	}

	private BattleRoundInfo getBattleRound(long playerId, int cityId, boolean isOffence) {
		return isOffence ?
				this.roundInfoOff.get(playerId+","+cityId) :
					this.roundInfoDef.get(playerId+","+cityId);
	}

	private void putBattleRound1(long playerId, int cityId, BattleRoundInfo br, boolean isOffence) {
		if(isOffence){
			roundInfoOff.put(playerId+","+cityId, br);
		}else{
			roundInfoDef.put(playerId+","+cityId, br);
		}
		
	}

	private RoundTroopDetail getRoundTroopDetail(int troopId, BattleFieldType type, boolean isOffence) {
		return isOffence ? 
				roundDetailOff.getRoundTroopDetail(troopId,  type) :
					roundDetailDef.getRoundTroopDetail(troopId, type);
	}

	private void putRoundTroopDetail(int troopId, BattleFieldType type, RoundTroopDetail roundTroopDetail, boolean isOffence) {
		if(isOffence)
			roundDetailOff.putRoundTroopDetail(troopId, type, roundTroopDetail);
		else
			roundDetailDef.putRoundTroopDetail(troopId, type, roundTroopDetail);
		
	}
	
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
				bu.getSlot().remove(bu);
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
		builder.setIsOver(this.result != RESULT_FIGHTING);		
		builder.setCityId(city.getCityId());
		builder.setCityLevel(city.getLevel());
		builder.setCityName(city.getCityName());
		builder.setPlayerName(player.getPlayerName());
		if(builder.getIsOver()){
			if(this.result == RESULT_OFFENCE_VICTORY){
				for(Army a : this.armysOffence){
					builder.addWinnerId(a.getPlayerId());
				}
			}else if(this.result == RESULT_DEFENCE_VICTORY){
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
		builder.setIsOver(this.result == RESULT_FIGHTING);
		builder.setBattleId(this.UUID);
		for(Army a : this.armysOffence){
			GameProtos.Army.Builder ambuilder = GameProtos.Army.newBuilder();
			City city = cityLogic.getCity(a.getCityId());
			Player player = playerLogic.getPlayer(a.getPlayerId());
			ambuilder.setPlayerName(player.getPlayerName());
			ambuilder.setCityName(city.getCityName());
			builder.addOffence(ambuilder);
			if(this.result == RESULT_OFFENCE_VICTORY){
				builder.addWinnerId(a.getPlayerId());
			}
		}
		for(Army a : this.armysDefence){
			GameProtos.Army.Builder ambuilder = GameProtos.Army.newBuilder();
			City city = cityLogic.getCity(a.getCityId());
			Player player = playerLogic.getPlayer(a.getPlayerId());
			ambuilder.setPlayerName(player.getPlayerName());
			ambuilder.setCityName(city.getCityName());
			builder.addDefence(ambuilder);
			if(this.result == RESULT_DEFENCE_VICTORY){
				builder.addWinnerId(a.getPlayerId());
			}
		}
		
		builder.setRoundNum(this.currentRoundNum-1);
		long overTime = this.getStartTime() + (this.currentRoundNum - 2)*BattleField.nextActionTime;
		builder.setLastRoundTime((int) (overTime/1000));
		builder.setCityName(cityLogic.getCity(fieldCityId).getCityName());
		
		return builder;
		
	}
	
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("offence: " + this.armysOffence + "\n");
		sb.append("defence: " + this.armysDefence + "\n");
		sb.append("fieldPositions: \n");
		sb.append("======Offence:======\n");
		for(Entry<?, ?> e : fieldPositionsForOffence.entrySet()){
			sb.append("key: " + e.getKey() +"\n");
			sb.append("value: " + e.getValue()+"\n");
		}
		sb.append("======Defence:======\n");
		for(Entry<?, ?> e : fieldPositionsForDefence.entrySet()){
			sb.append("key: " + e.getKey() +"\n");
			sb.append("value: " + e.getValue()+"\n");
		}
		return sb.toString();
	}
}
