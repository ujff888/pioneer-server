package cn.litgame.wargame.core.logic;

import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.SCProductionTroop;
import cn.litgame.wargame.core.auto.GameProtos.TroopInfo;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.auto.GameResProtos.BattleGround;
import cn.litgame.wargame.core.auto.GameResProtos.ResTroop;
import cn.litgame.wargame.core.auto.GameResProtos.TroopType;
import cn.litgame.wargame.core.logic.queue.GameActionLogic;
import cn.litgame.wargame.core.mapper.BattleMapper;
import cn.litgame.wargame.core.mapper.TroopMapper;
import cn.litgame.wargame.core.model.*;
import cn.litgame.wargame.core.model.battle.*;
import cn.litgame.wargame.core.model.battle.unit.BattleUnitAction;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
public class BattleLogic {
	@Resource(name = "battleMapper")
	private BattleMapper battleMapper;
	
	@Resource(name = "troopMapper")
	private TroopMapper troopMapper;
	
	@Resource(name = "configLogic")
	private ConfigLogic configLogic;
	
	@Resource(name = "gameActionLogic")
	private GameActionLogic gameActionLogic;
	
	@Resource(name = "cityLogic")
	private CityLogic cityLogic;
	
	@Resource(name = "buildingLogic")
	private BuildingLogic buildingLogic;
	
	@Resource(name = "playerLogic")
	private PlayerLogic playerLogic;
	
	@Resource(name = "rankLogic")
	private RankLogic rankLogic;
	
	@Resource(name = "jedisStoragePool")
	private JedisPool jedisStoragePool;
	
	private final static Logger log = Logger.getLogger(BattleLogic.class);
	
	private Map<TroopType, BattleUnitAction> battleUnitActions = new HashMap<>();
	
	public void registBattleUnitAction(TroopType key, BattleUnitAction value){
		this.battleUnitActions.put(key, value);
	}
	
	private BattleUnitAction getBattleUnitActionByTroopType(TroopType type) {
		return battleUnitActions.get(type);
	}
	
	public BattleField initBattleField(List<Army> offenceArmys, List<Army> defenceArmys, BattleGround battleGround, boolean isLand, int cityId) {
		return new BattleField(offenceArmys, defenceArmys, battleGround, isLand, cityId);
	}
	
	public GameProtos.BattleResult fight(BattleField field){
		//BattleField field = loadBattleField();
		while(field.getResult() == GameProtos.BattleResult.FIGHTING){
			//field = loadBattleField();
			this.nextRound(field);
			//saveBattleField(field);
		}
		return field.getResult();
	}
	
	public BattleField loadBattleField() {
		String redis_key = "battleField_cache";
		BattleField field = null;
		Jedis jedis = jedisStoragePool.getResource();
		try{
			GameProtos.BattleField p = GameProtos.BattleField.parseFrom(jedis.get(redis_key.getBytes()));
			field = new BattleField(p);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		} finally {
			jedis.close();
		}

		if(field == null)
			throw new RuntimeException("read BattleField error!");
		
		return field;
	}
	
	public void saveBattleField(BattleField battleField) {
		String redis_key = "battleField_cache";

		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			jedis.set(redis_key.getBytes(), battleField.convertToProto().toByteArray());
		}finally {
			jedis.close();
		}
	}
	
	/**
	 * 获取某只部队
	 * @return
	 * @param troopId
	 */
	public Troop getTroop(long troopId){
		return this.troopMapper.getTroop(troopId);
	}
	/**
	 * 创建部队
	 * @param troop
	 */
	public void createTroop(Troop troop){
		this.troopMapper.createTroop(troop);
	}
	
	/**
	 * 更新部队
	 * @param troop
	 */
	public void updateTroop(Troop troop){
		this.troopMapper.updateTroop(troop);
	}
	/**
	 * 删除部队
	 * @param troopId
	 */
	public void delTroop(long troopId){
		this.troopMapper.delTroop(troopId);
	}
	/**
	 * 获取某个城市的所有部队
	 * @param city
	 * @return
	 */
	public List<Troop> getTroopsByCityId(City city){
		try {
			//为了刷新一下生产队列，如果有已经生产好的加入到部队里
			gameActionLogic.getProductionTroop(city, System.currentTimeMillis());
		} catch (InvalidProtocolBufferException e) {
			log.error("get gameaction error", e);
		}
		return this.troopMapper.getTroopsByCityId(city.getCityId());
	}
	
	/**
	 * 获取城市里的陆军
	 * @param city
	 * @param flush 是否刷新生产中的队列
	 * @return
	 */
	public List<Troop> getLandTroopsByCityId(City city,boolean flush){
		if(flush){
			gameActionLogic.getProductionLandTroop(city, System.currentTimeMillis());
		}
		
		return this.troopMapper.getTroopsByCityIdAndType(city.getCityId(), 1);
	}
	
	/**
	 * 获取城市里的所有空军
	 * @param city
	 * @return
	 * 
	 */
	public List<Troop> getFlyTroopsByCityId(City city, boolean flush){
		if(flush){
			gameActionLogic.getProductionFlyTroop(city, System.currentTimeMillis());
		}
		
		return this.troopMapper.getTroopsByCityIdAndType(city.getCityId(), 2);
	}
	
	/**
	 * 取消生产部队
	 * @param city
	 * @param building
	 * @return
	 */
	public MessageCode cancelProductionTroop(City city,Building building){
		GameAction gameAction = null;
		long nowTime = System.currentTimeMillis();
		
		if(building.getBuildId() == 1009){
			gameAction = gameActionLogic.getProductionFlyTroop(city, nowTime);
		}else if(building.getBuildId() == 1010){
			gameAction = gameActionLogic.getProductionLandTroop(city, nowTime);
		}else{
			return MessageCode.NOT_FOUND_BUILDING;
		}
		if(gameAction != null){
			gameActionLogic.delGameAction(gameAction.getActionId());
		}
		return MessageCode.OK;
	}
	
	/**
	 * 生产部队的函数，TODO://需要处理被占领后生成资源消耗加倍的逻辑
	 * @param city
	 * @param troops
	 * @param position
	 * @param builder
	 * @return
	 */
	public MessageCode productionTroop(City city,GameProtos.Troops troops,int position,SCProductionTroop.Builder builder){
		Building building = buildingLogic.getBuilding(city.getCityId(), position);
		if(building == null){
			return MessageCode.NOT_FOUND_BUILDING;
		}
		if(building.getBuildId() != 1010 && building.getBuildId() != 1009){
			return MessageCode.NOT_FOUND_BUILDING;
		}
		int wood = 0,crystal = 0,metal = 0,food = 0,person = 0,totalCount = 0;
		long nowTime = System.currentTimeMillis();
		long overTime = nowTime;
		int maxLevel = 0;
		boolean techAllow = true;
		PlayerTech pt = playerLogic.getPlayerTech(city.getPlayerId());
		for(GameProtos.Troop troop : troops.getTroopsList()){
			ResTroop res = configLogic.getResTroop(troop.getTroopResId());
			wood += res.getWood();
			crystal += res.getCrystal();
			metal += res.getMetal();
			food += res.getGrain();
			person += res.getPerson();
			totalCount += troop.getCount();
			overTime += res.getTime() * troop.getCount() * 1000;
			if(res.getUnlockLevel() > maxLevel){
				maxLevel = res.getUnlockLevel();
			}
			if(!playerLogic.techAllow(pt, res.getUnlockTechId())){
				techAllow = false;
				break;
			}
		}
		if(!techAllow){
			return MessageCode.NOT_ENOUGH_TECH;
		}
		if(maxLevel > building.getLevel()){
			return MessageCode.NOT_ENOUGH_BUILDING_LEVEL;
		}
		
		int limit = building.getBuildId() == 1010 ? cityLogic.getLandArmyLimit(city) :  cityLogic.getFlyArmyLimit(city);
		if(totalCount > limit){
			return MessageCode.HAD_LIMIT_TROOP;
		}
		if(person > cityLogic.getFreePerson(city)){
			return MessageCode.NOT_ENOUGH_PERSON;
		}
		MessageCode code = cityLogic.checkResource(city, wood, 0, crystal, metal, food);
		if(code != MessageCode.OK){
			return code;
		}
		
		GameAction gameAction = null;
		if(building.getBuildId() == 1009){
			gameAction = gameActionLogic.getProductionFlyTroop(city,nowTime);
		}else if(building.getBuildId() == 1010){
			gameAction = gameActionLogic.getProductionLandTroop(city,nowTime);
		}
		
		if(gameAction != null){
			return MessageCode.HAD_PRODUCTION_TROOP;
		}
		//减去建造部队消耗的人口
		city.setTotalPerson(city.getTotalPerson() - person);
		cityLogic.removeCityResource(city, wood,0,crystal,metal,food);
		rankLogic.updateRankWithIncrement(RankLogic.RankType.WARFARE_RANK, city.getPlayerId(), Double.valueOf((wood + crystal + metal + food)/100));
		
		GameAction ga = gameActionLogic.productionTroop(city, troops, building.getBuildId(), nowTime,overTime);
		builder.setTroops(troops);
		builder.setNeedTime((int)(ga.getOverTime().getTime() - nowTime)/1000);
		return MessageCode.OK;
	}
	
	public GameProtos.Troop convert(Troop troop){
		GameProtos.Troop.Builder builder = GameProtos.Troop.newBuilder();
		builder.setId(troop.getTroopId());
		builder.setCount(troop.getCount());
		builder.setTroopResId(troop.getTroopResId());
		ResTroop res = configLogic.getResTroop(troop.getTroopResId());
		//TODO:处理科技加成的攻击与防御
		builder.setAttack(res.getAttack());
		builder.setAttack2(res.getAttack2());
		builder.setDefense(res.getDefense());
		return builder.build();
	}

	public MessageCode checkTroop(int cityId, TroopInfo troopInfo) {
		List<Troop> troops = this.getTroopsByCityId(cityLogic.getCity(cityId));
		for(GameProtos.Troop gt : troopInfo.getLandTroopList()){
			for(Troop t : troops){
				if(gt.getTroopResId() == t.getTroopResId() && gt.getCount() > t.getCount())
					return MessageCode.NOT_ENOUGH_TROOP;
			}
		}
		for(GameProtos.Troop gt : troopInfo.getFlyTroopList()){
			for(Troop t : troops){
				if(gt.getTroopResId() == t.getTroopResId() && gt.getCount() > t.getCount())
					return MessageCode.NOT_ENOUGH_TROOP;
			}
		}
		return MessageCode.OK;
	}

	public int getTroopWeight(TroopInfo troopInfo) {
		int weight = 0;
		for(GameProtos.Troop gt : troopInfo.getLandTroopList()){
			ResTroop rt = configLogic.getResTroop(gt.getTroopResId());
			weight += rt.getWeight() * gt.getCount();
		}
		for(GameProtos.Troop gt : troopInfo.getFlyTroopList()){
			ResTroop rt = configLogic.getResTroop(gt.getTroopResId());
			weight += rt.getWeight() * gt.getCount();
		}
		return weight;
	}

	public void removeTroopFromCity(long playerId, int cityId, TroopInfo troopInfo) {
		for(GameProtos.Troop gt : troopInfo.getLandTroopList()){
			Troop t = this.getCertainTroop(playerId, cityId, gt.getTroopResId());
			t.setCount(t.getCount() - gt.getCount());
			if(t.getCount() <= 0)
				this.delTroop(t.getTroopId());
			else
				this.updateTroop(t);
			
		}
		for(GameProtos.Troop gt : troopInfo.getFlyTroopList()){
			Troop t = this.getTroop(gt.getTroopResId());
			t.setCount(t.getCount() - gt.getCount());
			this.updateTroop(t);
		}
		
	}

	private Troop getCertainTroop(long playerId, int cityId, int troopResId) {
		return troopMapper.getCertainTroop(playerId, cityId, troopResId);
	}

	public void increaseTroop(City targetCity, GameProtos.Troop t) {
		boolean troopExists = false;
		Troop troop = null;
		for(Troop tr : this.getTroopsByCityId(targetCity)){
			if(tr.getTroopResId() == t.getTroopResId()){
				troopExists = true;
				troop = tr;
				break;
			}
		}
		
		if(troopExists){
			troop.setCount(troop.getCount() + t.getCount());
			this.updateTroop(troop);
		}else{
			troop = new Troop();
			troop.setPlayerId(targetCity.getPlayerId());
			troop.setCityId(targetCity.getCityId());
			troop.setTroopResId(t.getTroopResId());
			troop.setTroopType(t.getTroopResId()<2017?1:2);
			this.createTroop(troop);
		}
	}

	public void nextRound(BattleField battleField){
		this.initDamage(battleField); 
		
		//currentRoundPb = new cn.litgame.wargame.core.model.battle.protoround.BattleRound(currentRoundNum);
		
		log.info("双方上场");
		this.armyInPosition(battleField);		
		
		log.info("双方行动");
		for(BattleFieldType type : BattleField.attackOrder){
			log.info("进攻方"+type+"位置行动");
			for(Slot slot : battleField.getFieldPositionByType(BattleField.OFFENCE, type).getSlots()){
				offenceAction(battleField.getFieldPositionsForDefence(), battleField.getFieldPositionsForOffence(), type, battleField, slot);
			}
			log.info("防守方"+type+"行动");
			for(Slot slot : battleField.getFieldPositionByType(BattleField.DEFENCE, type).getSlots()){
				defenceAction(battleField.getFieldPositionsForOffence(), battleField.getFieldPositionsForDefence(), type, battleField, slot);
			}
		}
		
		log.info("进攻方结算伤害");
		this.clearField(battleField, BattleField.OFFENCE);
		log.info("防守方结算伤害");
		this.clearField(battleField, BattleField.DEFENCE);
		
		//this.heal(OFFENCE);
		//this.heal(DEFENCE);
		
		//this.collectRoundInfo(currentRound);
		this.moraleDown(battleField);

		this.collectRoundInfo(battleField);

		battleField.setCurrentRoundNum(battleField.getCurrentRoundNum()+1);
		
		if(!battleField.isOffenceDefeated() && !battleField.isDefenceDefeated()){
			battleField.setResult(GameProtos.BattleResult.FIGHTING);
		}else if(battleField.isDefenceDefeated() && !battleField.isOffenceDefeated()){
			log.info("防守方被击败");
			battleField.setResult(GameProtos.BattleResult.OFFENCE_WIN);
		}else if(battleField.isOffenceDefeated() && !battleField.isDefenceDefeated()){
			log.info("进攻方被击败");
			battleField.setResult(GameProtos.BattleResult.DEFENCE_WIN);
		}else{
			log.info("双方不分胜负");
			battleField.setResult(GameProtos.BattleResult.EVEN);
		}
	}

	private void defenceAction(Map<BattleFieldType, FieldPosition> enemy,
			Map<BattleFieldType, FieldPosition> self, BattleFieldType type, BattleField battleField,
			Slot slot) {
		ResTroop resTroop = configLogic.getResTroop(slot.getResTroopId());
		if(resTroop != null){
			BattleUnitAction battleUnitAction = this.getBattleUnitActionByTroopType(resTroop.getTroopType());
			battleUnitAction.doAction(enemy, self, type, battleField, battleField.getDamageOffence(), slot);
		}
	}

	private void offenceAction(Map<BattleFieldType, FieldPosition> enemy,
			Map<BattleFieldType, FieldPosition> self, BattleFieldType type, BattleField battleField,
			Slot slot) {
		ResTroop resTroop = configLogic.getResTroop(slot.getResTroopId());
		if(resTroop != null){
			BattleUnitAction battleUnitAction = this.getBattleUnitActionByTroopType(resTroop.getTroopType());
			battleUnitAction.doAction(enemy, self, type, battleField, battleField.getDamageDefence(), slot);
		}
	}

	private void collectRoundInfo(BattleField battleField) {
		battleField.getCurrentRoundPb().generateRoundInfo(battleField, BattleField.OFFENCE);
		battleField.getCurrentRoundPb().generateRoundInfo(battleField, BattleField.DEFENCE);

		battleField.getCurrentRoundPb().generateRoundDetail(battleField, BattleField.OFFENCE);
		battleField.getCurrentRoundPb().generateRoundDetail(battleField, BattleField.DEFENCE);
		battleField.getRoundHistoryPb().add(battleField.getCurrentRoundPb());
	}

	private void moraleDown(BattleField battleField) {
		battleField.setMoraleOff(battleField.getMoraleOff() - battleField.getMoraleDownPercent());
		battleField.setMoraleDef(battleField.getMoraleDef() - battleField.getMoraleDownPercent());

		int lostOff = battleField.getCurrentRoundPb().getTotalLost(BattleField.OFFENCE);
		int lostDef = battleField.getCurrentRoundPb().getTotalLost(BattleField.DEFENCE);

		if(lostOff != lostDef){
			if(lostOff > lostDef)
				battleField.setMoraleOff(battleField.getMoraleOff() - battleField.getWeakMoraleExtra());
			else
				battleField.setMoraleDef(battleField.getMoraleDef() - battleField.getWeakMoraleExtra());
		}
	}

	private void clearField(BattleField battleField, boolean isOffence) {
		//结算伤害
		Map<BattleFieldType, Damage> damage = isOffence ? battleField.getDamageOffence() : battleField.getDamageDefence();
		for(Entry<BattleFieldType, Damage> entry : damage.entrySet()){
			if(!entry.getValue().isEmpty()){
				takeDamage(battleField, isOffence, entry.getKey(), entry.getValue());
			}
		}
		this.reArange(battleField, isOffence);
	}

	private void reArange(BattleField battleField, boolean isOffence) {
		//将没有弹药的远程单位重新分配至后备部队
		this.removeUnitOutofAmmo(battleField, BattleFieldType.FIELD_REMOTE, TroopType.REMOTE_NO_AMMO, isOffence);
			
		//将没有弹药的轰炸单位直接移出战场
		this.removeUnitOutofAmmo(battleField, BattleFieldType.FIELD_FLY_FIRE, TroopType.FLY_FIRE, isOffence);
			
		//将没有弹药的空战单位直接移出战场
		this.removeUnitOutofAmmo(battleField, BattleFieldType.FIELD_FLY, TroopType.FLY_AIR, isOffence);
		
	}

	private void removeUnitOutofAmmo(BattleField battleField, BattleFieldType fieldType, TroopType troopType, boolean isOffence) {
		FieldPosition position = battleField.getFieldPositionByType(isOffence, fieldType);
		for(Slot slot : position.getSlots()){
			if(slot.getAmount() <= 0){
				battleField.getArmy(slot, isOffence).addAUnit(troopType, slot);
				slot.clear();
			}
		}
	}

	private void takeDamage(BattleField battleField, boolean isOffence, BattleFieldType type, Damage damage) {
		FieldPosition position = battleField.getFieldPositionByType(isOffence, type);
		List<Slot> targets = position.getSlotsWithTroop();
		int size = targets.size();
		int count = damage.getUnitCount(); 
		if(count>size)
			damage.setUnitCount(size);
		
		while(!damage.isEmpty() && damage.getUnitCount() > 0 && !targets.isEmpty()){
			for(Slot bu : targets){
				int ad = damage.getAvgDamage();
				bu.takeDamage(ad);
				log.info("一个单位"+bu.getResTroopId()+"受到"+(ad > bu.getDefense() ? ad-bu.getDefense() : 0)+"点伤害，生命值:"+bu.getHp()+" 类型"+bu.getBattleFieldType());
				damage.setDamageValue(damage.getDamageValue() - ad);
				damage.setUnitCount(damage.getUnitCount() - 1);
			}
		}
		removeZeroHpUnitFromField(battleField, type, isOffence);
	}

	private void removeZeroHpUnitFromField(BattleField battleField, BattleFieldType type, boolean isOffence) {
		FieldPosition position = battleField.getFieldPositionByType(isOffence, type);
		for(Slot slot : position.getSlots()){
			if(!slot.isEmpty()){
				int lostNo = slot.getOriginalCount() - slot.getCount();
				if(lostNo > 0){
					log.info("一个单位："+slot.getResTroopId()+"数目由"+(slot.getOriginalCount()+lostNo)+"变为"+slot.getOriginalCount());
					slot.subtract(lostNo);
				}
				if(lostNo == slot.getOriginalCount())
					slot.clear();
			}
		}
	}

	private void armyInPosition(BattleField battleField) {

		for(BattleFieldType type : BattleField.positionOrder){
			log.info("进攻方"+type+"位置上场");
			FieldPosition position = battleField.getFieldPositionByType(BattleField.OFFENCE, type);
			for(Slot slot : position.getSlots()){
				Slot bunit = null;
				if(hasNextUnit(battleField.getArmysOffence(), type, slot)){
					bunit = getNextUnit(battleField.getArmysOffence(), type, slot);
					if(bunit != null)
						slot = slot.add(bunit);
				}
			}
			log.info("防守方"+type+"位置上场");
			position = battleField.getFieldPositionByType(BattleField.DEFENCE, type);
			if(position.getType() == BattleFieldType.FIELD_CLOSE
					&& battleField.getFieldPositionByType(BattleField.DEFENCE, BattleFieldType.FIELD_CLOSE).getSlot(0).isFortificationUnit()){
				continue;
			}

			for(Slot slot : position.getSlots()){
				Slot bunit = null;
				if(hasNextUnit(battleField.getArmysDefence(), type, slot)){
					bunit = getNextUnit(battleField.getArmysOffence(), type, slot);
					if(bunit != null)
						slot = slot.add(bunit);
				}
			}
		}

	}

	private Slot getNextUnit(List<Army> armys, BattleFieldType type, Slot slot) {
		for(Army army : armys){
			if(army.hasNextUnit(type, slot))
				return army.getNextUnit(type, slot);
		}
		return null;
	}

	private boolean hasNextUnit(List<Army> armys, BattleFieldType type, Slot slot) {
		for(Army a : armys) {
			if(a.hasNextUnit(type, slot))
				return true;
		}
		return false;
	}

	private void initDamage(BattleField battleField) {
		battleField.setDamageOffence(new HashMap<>());
		battleField.setDamageDefence(new HashMap<>());
		
		battleField.getDamageOffence().put(BattleFieldType.FIELD_CLOSE, new Damage(0,0));
		battleField.getDamageOffence().put(BattleFieldType.FIELD_REMOTE, new Damage(0,0));
		battleField.getDamageOffence().put(BattleFieldType.FIELD_FIRE, new Damage(0,0));
		battleField.getDamageOffence().put(BattleFieldType.FIELD_SIDE, new Damage(0,0));
		battleField.getDamageOffence().put(BattleFieldType.FIELD_FLY, new Damage(0,0));
		battleField.getDamageOffence().put(BattleFieldType.FIELD_FLY_FIRE, new Damage(0,0));
		battleField.getDamageOffence().put(BattleFieldType.FIELD_SUPPORT, new Damage(0,0));
		
		battleField.getDamageDefence().put(BattleFieldType.FIELD_CLOSE, new Damage(0,0));
		battleField.getDamageDefence().put(BattleFieldType.FIELD_REMOTE, new Damage(0,0));
		battleField.getDamageDefence().put(BattleFieldType.FIELD_FIRE, new Damage(0,0));
		battleField.getDamageDefence().put(BattleFieldType.FIELD_SIDE, new Damage(0,0));
		battleField.getDamageDefence().put(BattleFieldType.FIELD_FLY, new Damage(0,0));
		battleField.getDamageDefence().put(BattleFieldType.FIELD_FLY_FIRE, new Damage(0,0));
		battleField.getDamageDefence().put(BattleFieldType.FIELD_SUPPORT, new Damage(0,0));		
	}

	public BattleRound saveRound(BattleField field) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
