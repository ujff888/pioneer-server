package cn.litgame.wargame.core.logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.SCProductionTroop;
import cn.litgame.wargame.core.auto.GameProtos.TroopInfo;
import cn.litgame.wargame.core.auto.GameResProtos.BattleGround;
import cn.litgame.wargame.core.auto.GameResProtos.ResTroop;
import cn.litgame.wargame.core.logic.queue.GameActionLogic;
import cn.litgame.wargame.core.mapper.BattleMapper;
import cn.litgame.wargame.core.mapper.TroopMapper;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.GameAction;
import cn.litgame.wargame.core.model.PlayerTech;
import cn.litgame.wargame.core.model.Troop;
import cn.litgame.wargame.core.model.battle.Army;
import cn.litgame.wargame.core.model.battle.BattleField;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.google.protobuf.InvalidProtocolBufferException;

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
	
	public void initAndSaveBattleField(List<Army> offenceArmys, List<Army> defenceArmys, BattleGround battleGround, boolean isLand, int cityId) {
		BattleField battleField = new BattleField(offenceArmys, defenceArmys, battleGround, isLand, cityId);
		String redis_key = "battleField_cache";	
		
		
		Jedis jedis = jedisStoragePool.getResource();
		try{
			jedis.set(redis_key.getBytes(), battleField.convertToProto().toByteArray());
		}finally{
			jedis.close();
		}
	}
	
	public BattleField initBattleField(List<Army> offenceArmys, List<Army> defenceArmys, BattleGround battleGround, boolean isLand, int cityId) {
		return new BattleField(offenceArmys, defenceArmys, battleGround, isLand, cityId);
	}
	
	public GameProtos.BattleResult fight(){
		BattleField field = loadBattleField();
		while(field.getResult() == GameProtos.BattleResult.FIGHTING){
			field = loadBattleField();
			field.nextRound();
			field.saveRound();
			saveBattleField(field);
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
		
		try(Jedis jedis = jedisStoragePool.getResource()){
			jedis.set(redis_key.getBytes(), battleField.convertToProto().toByteArray());
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
	 * @param cityId
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
	 * @param cityId
	 * @param troopType
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
	 * @param cityId
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
	 * @param troopType
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

	public void removeTroopFromCity(TroopInfo troopInfo) {
		for(GameProtos.Troop gt : troopInfo.getLandTroopList()){
			Troop t = this.getTroop(gt.getTroopResId());
			t.setCount(t.getCount() - gt.getCount());
			this.updateTroop(t);
		}
		for(GameProtos.Troop gt : troopInfo.getFlyTroopList()){
			Troop t = this.getTroop(gt.getTroopResId());
			t.setCount(t.getCount() - gt.getCount());
			this.updateTroop(t);
		}
		
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

}
