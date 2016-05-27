package cn.litgame.wargame.core.logic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.kriver.core.common.TimeUtils;
import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.CityStatus;
import cn.litgame.wargame.core.auto.GameProtos.LandData;
import cn.litgame.wargame.core.auto.GameProtos.MainBuild;
import cn.litgame.wargame.core.auto.GameProtos.Palace;
import cn.litgame.wargame.core.auto.GameProtos.Pier;
import cn.litgame.wargame.core.auto.GameProtos.ProductionTroop;
import cn.litgame.wargame.core.auto.GameProtos.PubBuild;
import cn.litgame.wargame.core.auto.GameProtos.SCSetCapital;
import cn.litgame.wargame.core.auto.GameProtos.SCShowBuild;
import cn.litgame.wargame.core.auto.GameProtos.SCShowTechBuild;
import cn.litgame.wargame.core.auto.GameProtos.SimpleCityInfo;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.auto.GameProtos.TroopInfo;
import cn.litgame.wargame.core.auto.GameResProtos.ResBuild;
import cn.litgame.wargame.core.auto.GameResProtos.ResLand;
import cn.litgame.wargame.core.logic.queue.GameActionLogic;
import cn.litgame.wargame.core.mapper.CityMapper;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.GameAction;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.core.model.PlayerState;
import cn.litgame.wargame.core.model.Troop;

import com.google.protobuf.InvalidProtocolBufferException;

@Service
public class CityLogic {

	private final static Logger log = Logger.getLogger(CityLogic.class);

	@Resource(name = "cityMapper")
	private CityMapper cityMapper;

	@Resource(name = "configLogic")
	private ConfigLogic configLogic;

	@Resource(name = "battleLogic")
	private BattleLogic battleLogic;

	@Resource(name = "mapLogic")
	private MapLogic mapLogic;

	@Resource(name = "playerLogic")
	private PlayerLogic playerLogic;

	@Resource(name = "buildingLogic")
	private BuildingLogic buildingLogic;

	@Resource(name = "gameActionLogic")
	private GameActionLogic gameActionLogic;

	@Resource(name = "rankLogic")
	private RankLogic rankLogic;

	public int getFreePerson(City myCity) {
		return (int) (myCity.getTotalPerson() - myCity.getWoodWorker()
				- myCity.getResourceWorker() - myCity.getScientist());
	}

	/**
	 * 传入一个每小时的生产率，获取获得第一个整数1所需要的时间间隔 返回的是毫秒，方便与时间戳加减
	 * 
	 * @param rate
	 * @return
	 */
	public long getProductionTimeInterval(double rate) {
		return (long) ((3600D / Math.abs(rate)) * 1000);
	}

	/**
	 * 获取空闲市民的金币产出量，每小时
	 * 
	 * @param city
	 * @return
	 */
	public double getFreePersionGoldRate(City city) {
		return this.getFreePerson(city) * 3;
	}

	/**
	 * 获取城市的金币产出量，每小时
	 * 
	 * @param city
	 * @return
	 */
	public double getCityGold(City city) {
		return city.getTotalPerson() * 3 - city.getScientist() * 6;
	}

	/**
	 * 获取科技点的产出量
	 * 
	 * @param totalTeacher
	 * @return
	 */
	public double getTechPointRate(int totalTeacher) {
		return totalTeacher * 1;
	}

	public void showTechBuild(City city, SCShowTechBuild.Builder builder) {
		builder.setCityTeacher(city.getScientist());
		// List<City> citys = this.getCity(city.getPlayerId());
		// PlayerTech pt = playerLogic.getPlayerTech(city.getPlayerId());
		// int totalTeacher = 0;
		// for(City c : citys){
		// totalTeacher += c.getScientist();
		// }
		// builder.setTotalTeacher(totalTeacher);
		// builder.setTotalTechPoint(pt.getTechPoint());
		builder.setTechPointRate(this.getTechPointRate(city.getScientist()));
		builder.setPerson(this.getFreePerson(city));
		builder.setGoldRate(this.getGoldRateByHour(city));
	}

	/**
	 * 设置科学家的人数
	 */
	public MessageCode setTecher(City city, int techer) {
		List<Building> bs = buildingLogic.getBuildings(city.getCityId(), 1002);
		if (bs == null || bs.size() != 1) {
			return MessageCode.NOT_FOUND_TECH;
		}
		Building b = bs.get(0);

		ResBuild resBuild = configLogic.getResBuild(1002, b.getLevel());
		if (techer > resBuild.getArg1()) {
			return MessageCode.HAD_LIMIT_TECHER;
		}
		// TODO:处理科技点数的增加问题
		city.setScientist(techer);
		if (this.getFreePerson(city) < techer) {
			return MessageCode.NOT_ENOUGH_PERSON;
		}
		this.updateCity(city);
		return MessageCode.OK;
	}

	public int getGoldRateByHour(City city) {
		double free = city.getTotalPerson() - city.getWoodWorker()
				- city.getResourceWorker() - city.getScientist();
		int r = (int) free * 3 - city.getScientist() * 6;
		return r;
	}

	/**
	 * 获取这个城市的木头产量/每小时
	 * 
	 * @param city
	 * @return
	 */
	public double getWoodRateByHour(City city) {
		return city.getWoodWorker() * 1;// TODO:各种加成;
	}

	/**
	 * 获取这个城市的特殊资源产量/每小时
	 * 
	 * @param city
	 * @return
	 */
	public double getResourceRateByHour(City city) {
		return city.getResourceWorker() * 1;// TODO:各种加成
	}

	/**
	 * 根据岛屿id获取城市列表
	 * 
	 * @param landId
	 * @return
	 */
	public List<City> getCityByLandId(int landId) {
		return this.cityMapper.getCityByLandId(landId);
	}

	/**
	 * 计算民意、人口之间的增长关系 TODO:这个函数需要测试一下，应该是有问题的哈
	 * 
	 * @param publicOpinion
	 * @return
	 */
	public void updateCityPublicOpinion(City city, double publicOpinion,
			long nowTime) {
		// 上次的计算时间
		long lastTime = city.getLastSetTime().getTime();
		// 计算间隔小于10分钟直接return
		if (nowTime - lastTime < 1000 * 60 * 10) {
			return;
		}
		ResBuild resBuild = configLogic.getResBuild(1001, city.getLevel());
		if (city.getTotalPerson() == resBuild.getArg1()
				|| city.getTotalPerson() == 0) {
			return;
		}

		double rate = this.getPersonRate(city);

		// 计算获取整数递增的最小的时间间隔
		long timeInterval = this.getProductionTimeInterval(rate);
		int addCount = rate >= 0 ? 1 : -1;

		if ((nowTime - lastTime) > timeInterval) {
			city.setTotalPerson(city.getTotalPerson() + addCount);
			if (city.getTotalPerson() >= resBuild.getArg1()) {
				city.setTotalPerson(resBuild.getArg1());
				city.getLastSetTime().setTime(nowTime);
			} else if (city.getTotalPerson() < 0) {
				city.setTotalPerson(0);
			} else {
				city.getLastSetTime().setTime(
						city.getLastSetTime().getTime() + timeInterval);
			}

			this.updateCityPublicOpinion(city, publicOpinion - addCount,
					nowTime);

			// 把人口递增的时间段内金钱的差量计算进去
			city.setWaitGold(city.getWaitGold()
					+ (addCount * 3d * timeInterval) / TimeUtils.HOUR);
		}
	}

	/**
	 * 获取博物馆的所有民意加成
	 * 
	 * @param city
	 * @return
	 */
	public int getTotalMuseumPO(City city) {

		return 0;
	}

	/**
	 * 获取一个城市的民意
	 * 
	 * @param playerId
	 * @param cityId
	 * @return
	 */
	public double getCityPO(City city) {
		PlayerState ps = playerLogic.getPlayerState(city.getPlayerId());
		double publicOpinion = configLogic.getGlobalConfig()
				.getBasePublicOpinion() + ps.getAddPublicOpinion();
		if (city.isCapital()) {
			publicOpinion += ps.getAddPublicOpinionInCapital();
		}
		// TODO:政府体制的民意加成；
		publicOpinion += this.getSysPO(ps.getSystem());

		// TODO:处理博物馆的民意加成；
		publicOpinion += this.getTotalMuseumPO(city);
		// TODO：处理腐败的问题
		publicOpinion -= city.getTotalPerson() * this.getCorruption() / 100;

		publicOpinion = publicOpinion - city.getTotalPerson()
				+ buildingLogic.getTotalPubPO(city);

		return publicOpinion;
	}

	// 民意转换成人口增长率的系数
	public static double PersonRate = 0.02;

	public double getPersonRate(City city) {
		return this.getCityPO(city) * PersonRate;
	}

	/**
	 * TODO：这个函数需要重写，性能有很大的优化空间，更换实现方式
	 * 
	 * @param landIds
	 * @return
	 */
	public List<LandData> getLandData(List<Integer> landIds) {
		List<LandData> ds = new ArrayList<GameProtos.LandData>();
		if (landIds != null) {
			for (Integer landId : landIds) {
				LandData.Builder builder = LandData.newBuilder();
				builder.setLandId(landId);
				builder.setCount(mapLogic.getCityCountByLandId(landId));
				builder.setStatus(0);
				ds.add(builder.build());
			}
		}
		return ds;
	}

	public SimpleCityInfo convert(City city) {
		SimpleCityInfo.Builder builder = SimpleCityInfo.newBuilder();
		builder.setPlayerId(city.getPlayerId());
		builder.setCityName(city.getCityName());
		builder.setCityId(city.getCityId());
		builder.setPostion(city.getPosition());
		builder.setLevel(city.getLevel());
		builder.setStatus(CityStatus.valueOf(city.getCityStatus()));
		builder.setLandId(city.getLandId());
		builder.setPlayerName(playerLogic.getPlayer(city.getPlayerId()).getPlayerName());
		builder.setPlayerPoint((int)(rankLogic.getPlayerScore(city.getPlayerId(), RankLogic.RankType.TOTAL_RANK)));
		return builder.build();
	}

	/**
	 * 检查资源是否足够
	 * 
	 * @param player
	 * @param wood
	 * @param stone
	 * @param crystal
	 * @param metal
	 * @param food
	 * @return
	 */
	public MessageCode checkResource(City city, int wood, int stone,
			int crystal, int metal, int food) {
		if (wood > 0 && wood > city.getWood()) {
			return MessageCode.NOT_ENOUGH_WOOD;
		}
		if (stone > 0 && stone > city.getStone()) {
			return MessageCode.NOT_ENOUGH_STONE;
		}
		if (crystal > 0 && crystal > city.getCrystal()) {
			return MessageCode.NOT_ENOUGH_CRYSTAL;
		}
		if (metal > 0 && metal > city.getMetal()) {
			return MessageCode.NOT_ENOUGH_METAL;
		}
		if (food > 0 && food > city.getFood()) {
			return MessageCode.NOT_ENOUGH_FOOD;
		}
		return MessageCode.OK;
	}

	/**
	 * 处理城市的资源自然增长的问题
	 * 
	 * @param city
	 * @param nowTime
	 */
	public void updateCityResource(City city, long nowTime) {
		//如果间隔小于10分钟的话，不处理
		long resourceInterval = nowTime - city.getLastResourceSetTime().getTime();
		if (resourceInterval < 1000 * 60 * 10) {
			return;
		}
		
		double woodCount = this.getWoodRateByHour(city);
		double resourceCount = this.getResourceRateByHour(city);
		double woodRate = woodCount / TimeUtils.HOUR;// 每豪秒的产量
		double resourceRate = resourceCount / TimeUtils.HOUR;// 每豪秒的产量
		
		city.setWood(city.getWood() + (int) (resourceInterval * woodRate));
		int resourceValue = (int) (resourceInterval * resourceRate);
		ResLand resLand = configLogic.getResLand(city.getLandId());
		switch (resLand.getResourceType()) {
		case METAL:
			city.setMetal(city.getMetal() + resourceValue);
			break;
		case STONE:
			city.setStone(city.getStone() + resourceValue);
			break;
		case CRYSTAL:
			city.setCrystal(city.getCrystal() + resourceValue);
			break;
		case FOOD:
			city.setFood(city.getFood() + resourceValue);
			break;
		default:
			throw new RuntimeException("error resource Type,resLand===="
					+ resLand);
		}

		city.getLastResourceSetTime().setTime(nowTime);
	}

	/**
	 * 移除城市的资源
	 * 
	 * @param city
	 * @param wood
	 * @param stone
	 * @param crystal
	 * @param metal
	 * @param food
	 */
	public void removeCityResource(City city, int wood, int stone, int crystal,
			int metal, int food) {
		city.setWood(city.getWood() - wood);
		city.setStone(city.getStone() - stone);
		city.setCrystal(city.getCrystal() - crystal);
		city.setMetal(city.getMetal() - metal);
		city.setFood(city.getFood() - food);
		this.updateCity(city);
	}

	/**
	 * 移除城市的资源
	 * 
	 * @param city
	 * @param build
	 */
	public void removeCityResource(City city, ResBuild build) {
		city.setFood(city.getFood() - build.getFood());
		city.setWood(city.getWood() - build.getWood());
		city.setStone(city.getStone() - build.getStone());
		city.setCrystal(city.getCrystal() - build.getCrystal());
		city.setMetal(city.getMetal() - build.getMetal());
		this.updateCity(city);
	}

	/**
	 * 获取城市信息，并且同步一下资源数量
	 * 
	 * @param playerId
	 * @param cityId
	 * @return
	 */
	public City getCity(int cityId) {
		City city = cityMapper.getCity(cityId);
		if (city != null) {
			this.warpCity(city);
		}
		return city;
	}

	/**
	 * 对city对象做一些数据处理
	 * 
	 * @param city
	 */
	private void warpCity(City city) {
		double publicOpinion = this.getCityPO(city);
		long nowTime = System.currentTimeMillis();
		// 处理民意以及人口
		// record old totalPerson
		double oldTotalPerson = city.getTotalPerson();

		this.updateCityPublicOpinion(city, publicOpinion, nowTime);

		// new totalPerson
		double newTotalPerson = city.getTotalPerson();
		rankLogic.updateRankWithIncrement(RankLogic.RankType.TOTAL_RANK,
				city.getPlayerId(), newTotalPerson - oldTotalPerson);
		// 处理资源增长问题
		updateCityResource(city, nowTime);
		this.updateCity(city);
	}

	public List<City> getCity(long playerId) {
		return cityMapper.getCitysByPlayerId(playerId);
	}

	public boolean updateCity(City city) {
		return this.cityMapper.updateCity(city) > 0;
	}

	/**
	 * 设置一个城市为首都
	 * 
	 * @param city
	 * @param builder
	 * @return
	 */
	public MessageCode setCapital(City city, SCSetCapital.Builder builder) {

		return MessageCode.OK;
	}

	/**
	 * 获取指定坐标点的城市
	 * 
	 * @param landId
	 * @param position
	 * @return
	 */
	public City getCity(int landId, int position) {
		return this.cityMapper.getCityByPos(landId, position);
	}

	/**
	 * 创建一个城市
	 * 
	 * @param playerId
	 * @param landId
	 * @param position
	 * @param isFirst
	 * @param food
	 * @param stone
	 * @param crystal
	 * @param metal
	 * @param wood
	 * @param person
	 * @return
	 */
	public City createCity(long playerId, int landId, int position,
			boolean isFirst, int food, int stone, int crystal, int metal,
			int wood, int person, int status) {
		City city = new City();
		city.setPlayerId(playerId);
		city.setCityName(configLogic.getGlobalConfig().getDefaultCityName());
		city.setLandId(landId);
		city.setPosition(position);
		city.setFood(food);
		city.setStone(stone);
		city.setCrystal(crystal);
		city.setMetal(metal);
		city.setWood(wood);
		city.setTotalPerson(person);
		city.setLevel(1);
		city.setCapital(isFirst);
		Timestamp time = new Timestamp(System.currentTimeMillis());
		city.setCreateCityTime(time);
		city.setLastResourceSetTime(time);
		city.setLastSetTime(time);
		city.setCityStatus(status);
		if (this.cityMapper.createCity(city) > 0) {
			return city;
		}
		log.error("create city error");
		return null;
	}

	public int deleteCity(int cityId) {
		return cityMapper.delCity(cityId);
	}
	
	/**
	 * 创建分村专用
	 * 
	 * @param playerId
	 * @param landId
	 * @param position
	 * @return
	 */
	public City createOtherCity(long playerId, int landId, int position) {
		City city = new City();
		city.setCityName(configLogic.getGlobalConfig().getDefaultCityName());
		city.setPlayerId(playerId);
		city.setLandId(landId);
		city.setPosition(position);
		city.setLevel(1);
		city.setCapital(false);
		Timestamp time = new Timestamp(System.currentTimeMillis());
		city.setCreateCityTime(time);
		city.setLastResourceSetTime(time);
		city.setLastSetTime(time);
		city.setCityStatus(CityStatus.CITY_Building_VALUE);
		if (this.cityMapper.createCity(city) > 0) {
			return city;
		}
		log.error("create city error");
		return null;
	}

	/**
	 * 加速建造，没有扣除元宝也没有消耗道具，直接完成。FIXME
	 * 
	 * @param player
	 * @param oldBuilding
	 * @return
	 */
	public Building buildFinished(Player player, City city, Building oldBuilding) {
		int level = oldBuilding.getLevel() + 1;
		ResBuild resBuild = configLogic.getResBuild(oldBuilding.getBuildId(),
				level);
		if (resBuild == null) {
			log.error("max level,oldBuilding=" + oldBuilding);
			return oldBuilding;
		}
		oldBuilding.setBuilding(false);
		oldBuilding.setLevel(level);
		if (resBuild.getBuildId() == 1001) {
			city.setLevel(level);
			this.updateCity(city);
		}
		oldBuilding.setBuildTime(new Timestamp(System.currentTimeMillis()));
		buildingLogic.updateBuilding(oldBuilding);
		return oldBuilding;
	}

	public MessageCode buildingLevelUp(Player player, Building oldBuilding,
			City city, boolean isOver) {
		ResBuild resBuild = configLogic.getResBuild(oldBuilding.getBuildId(),
				oldBuilding.getLevel() + 1);
		if (resBuild == null) {
			return MessageCode.HAD_MAX_LEVEL;
		}
		MessageCode code = this.checkResource(city, resBuild.getWood(),
				resBuild.getStone(), resBuild.getCrystal(),
				resBuild.getMetal(), resBuild.getFood());
		if (code != MessageCode.OK) {
			return code;
		}
		if(isOver){
			int needDiamond = buildingLogic.getNeedDiamondSpeed(resBuild.getTime() * 1000);
			if(player.getDiamond() < needDiamond){
				return MessageCode.NOT_ENOUGH_DIAMOND;
			}
			player.setDiamond(player.getDiamond() - needDiamond);
			player.setNeedSave(true);
		}
		
		this.removeCityResource(city, resBuild);
		if(isOver){
			oldBuilding.setBuildTime(new Timestamp(System.currentTimeMillis()));
			oldBuilding.setBuilding(false);
			oldBuilding.setLevel(oldBuilding.getLevel() + 1);
			if (resBuild.getBuildId() == 1001) {
				city.setLevel(oldBuilding.getLevel());
				this.updateCity(city);
			}
		}else{
			oldBuilding.setBuildTime(new Timestamp(System.currentTimeMillis()
					+ resBuild.getTime() * 1000));
			oldBuilding.setBuilding(true);

		}

		buildingLogic.updateBuilding(oldBuilding);
		return MessageCode.OK;
	}

	public List<GameProtos.City> getCitysProto(long playerId) {
		List<GameProtos.City> result = new ArrayList<GameProtos.City>();
		for (City city : this.getCity(playerId)) {
			List<Building> bs = buildingLogic.getBuildings(city.getCityId());
			List<Troop> troops = battleLogic.getTroopsByCityId(city);
			result.add(this.convert(city, bs, troops));
		}
		return result;
	}

	public GameProtos.City convert(City city, List<Building> bs,
			List<Troop> troops) {
		GameProtos.City.Builder builder = GameProtos.City.newBuilder();
		builder.setLandId(city.getLandId());
		builder.setCityId(city.getCityId());
		builder.setCityName(city.getCityName());
		builder.setPosition(city.getPosition());
		builder.setPlayerId(city.getPlayerId());
		builder.setFood(city.getFood());
		builder.setStone(city.getStone());
		builder.setCrystal(city.getCrystal());
		builder.setMetal(city.getMetal());
		builder.setWood(city.getWood());
		// 这里需要优化一个cityAction的动作
		builder.setActionPoint(3);
		for (Building building : bs) {
			if (building.getPosition() == 0) {
				builder.setLevel(building.getLevel());
			}
			builder.addBuildings(buildingLogic.convert(city, building));
		}
		if (troops != null) {
			TroopInfo.Builder troopInfo = TroopInfo.newBuilder();
			for (Troop troop : troops) {
				if (troop.getTroopType() == 1) {

					troopInfo.addLandTroop(battleLogic.convert(troop));
				} else if (troop.getTroopType() == 2) {
					troopInfo.addFlyTroop(battleLogic.convert(troop));
				}
			}
			builder.setTroopInfo(troopInfo);
		}

		builder.setIsCapital(city.isCapital());
		builder.setFreePersion(this.getFreePerson(city));
		return builder.build();
	}

	// TODO:计算兵营上限
	/**
	 * 获取陆军上限
	 * 
	 * @param city
	 * @return
	 */
	public int getLandArmyLimit(City city) {
		return 100;
	}

	// 获取空军上限
	public int getFlyArmyLimit(City city) {
		return 100;
	}

	// 获取当前的行动点
	private int getActionPoint() {
		return 0;
	}

	// 获取腐败率
	public int getCorruption() {
		return 0;
	}

	/**
	 * 当前的政府体制的加成
	 * 
	 * @param system
	 * @return
	 */
	public int getSysPO(int system) {
		return 10;
	}

	/**
	 * 当前科技的加成
	 * 
	 * @param player
	 * @return
	 */
	public int getTechPO(Player player) {
		return 10;
	}

	/**
	 * 设置市政厅的信息
	 * 
	 * @param player
	 * @param city
	 * @param builder
	 */
	private void setMainBuildInfo(Player player, City city,
			SCShowBuild.Builder builder) {
		MainBuild.Builder mb = MainBuild.newBuilder();
		mb.setTotalPerson((int) city.getTotalPerson());
		mb.setLandArmy(this.getLandArmyLimit(city));
		mb.setFlyArmy(this.getFlyArmyLimit(city));
		mb.setActionPoint(this.getActionPoint());
		mb.setPersonRate(this.getPersonRate(city));
		mb.setCityGoldRate((int) this.getCityGold(city));
		mb.setCorruption(this.getCorruption());
		mb.setFreePerson(this.getFreePerson(city));
		mb.setWoodWorker(city.getWoodWorker());
		mb.setWoodRate((int) this.getWoodRateByHour(city));
		mb.setResourceWorker(city.getResourceWorker());
		mb.setResourceRate((int) this.getResourceRateByHour(city));
		mb.setTecher(city.getScientist());
		mb.setMagicer(0);
		mb.setFreePersonGoldRate((int) this.getFreePersionGoldRate(city));
		mb.setTechPointRate((int) this.getTechPointRate(city.getScientist()));
		mb.setMagicPoint(0);

		PlayerState playerState = playerLogic.getPlayerState(player
				.getPlayerId());

		mb.setBasePO(configLogic.getGlobalConfig().getBasePublicOpinion());
		mb.setSysPO(this.getSysPO(playerState.getSystem()));
		mb.setTechPO(this.getTechPO(player));
		if (city.isCapital()) {
			mb.setCapitalPO(playerState.getAddPopulationCountInCapital());
		}
		Building pub = buildingLogic.getPubInfo(city);
		if (pub == null) {
			mb.setFoodBuildPO(0);
			mb.setFoodPO(0);
		} else {
			mb.setFoodBuildPO(buildingLogic.getPubBasePO(pub));
			mb.setFoodPO(buildingLogic.getPubFoodPO(pub));
		}
		Building museum = buildingLogic.getMuseum(city);
		if (museum == null) {
			mb.setMuseumBuildPO(0);
			mb.setMuseumPO(0);
		} else {
			mb.setMuseumBuildPO(buildingLogic.getMuseumBuildPO(museum));
			mb.setMuseumPO(buildingLogic.getMuseumPO(museum));
		}
		mb.setResourceType(configLogic.getResLand(city.getLandId())
				.getResourceType());
		builder.setMainBuild(mb);
	}

	/**
	 * 设置酒馆的信息
	 * 
	 * @param player
	 * @param city
	 * @param builder
	 */
	private void setPubInfo(Player player, City city,
			SCShowBuild.Builder builder) {
		Building pub = buildingLogic.getPubInfo(city);
		if (pub == null) {
			log.error("not found pub!!!");
			return;
		}
		PubBuild.Builder pubBuild = PubBuild.newBuilder();
		pubBuild.setCurrentPO((int) (this.getCityPO(city)));
		pubBuild.setCurrentLevel(pub.getCount());
		builder.setPubBuild(pubBuild);
	}

	/**
	 * 设置部队的生产信息
	 * 
	 * @param city
	 * @param type
	 * @param builder
	 */
	private void setProductionInfo(City city, int type,
			SCShowBuild.Builder builder) {
		long nowTime = System.currentTimeMillis();
		GameAction ga = type == 1 ? gameActionLogic.getProductionLandTroop(
				city, nowTime) : gameActionLogic.getProductionFlyTroop(city,
				nowTime);
		ProductionTroop.Builder pt = ProductionTroop.newBuilder();
		if (ga != null) {
			try {
				pt.setTroops(GameProtos.Troops.parseFrom(ga.getActionData()));
			} catch (InvalidProtocolBufferException e) {
				log.error("syn gameaction error", e);
				return;
			}
			pt.setOverTime((int) (ga.getOverTime().getTime() - nowTime) / 1000);
		}
		List<Troop> troops = type == 1 ? battleLogic.getLandTroopsByCityId(city,false)
				:battleLogic.getFlyTroopsByCityId(city, false);
		TroopInfo.Builder troopInfo = TroopInfo.newBuilder();
		for (Troop troop : troops) {
			if (troop.getTroopType() == 1) {
				troopInfo.addLandTroop(battleLogic.convert(troop));
			} else if (troop.getTroopType() == 2) {
				troopInfo.addFlyTroop(battleLogic.convert(troop));
			}
		}
		pt.setFreePerson(this.getFreePerson(city));
		builder.setProductionTroop(pt);
		builder.setTroopInfo(troopInfo);
	}

	/**
	 * 获取某个玩家的城市数量
	 * 
	 * @param playerId
	 * @return
	 */
	public int getCityCount(long playerId) {
		return this.cityMapper.getCityCount(playerId);
	}

	/**
	 * 设置王宫的信息
	 * 
	 * @param player
	 * @param city
	 * @param builder
	 */
	private void setPalaceInfo(Player player, City city,
			SCShowBuild.Builder builder) {
		Palace.Builder palace = Palace.newBuilder();
		PlayerState ps = playerLogic.getPlayerState(player.getPlayerId());
		palace.setSystemId(ps.getSystem());
		if (ps.getOverSystemTime() != null
				&& ps.getSystem() == configLogic.getGlobalConfig()
						.getNoSystemId()) {
			palace.setOverTime((int) ((ps.getOverSystemTime().getTime() - System
					.currentTimeMillis()) / 1000));
			palace.setTargetSystemId(ps.getTargetSystemId());
		}
		int cityCount = this.getCityCount(player.getPlayerId());
		palace.setNeedGold(configLogic.getGlobalConfig().getChangeSystemGold()
				* cityCount);
		palace.setNeedTime(configLogic.getGlobalConfig().getChangeSystemTime()
				* cityCount);
		builder.setPalace(palace);
	}

	/**
	 * 打开建筑查看信息的通用返回接口
	 * 
	 * @param city
	 * @param building
	 * @param builder
	 */
	public void setShowBuildInfo(Player player, City city, Building building,
			SCShowBuild.Builder builder) {

		switch (building.getBuildId()) {
		case 1001:// 市政厅
			this.setMainBuildInfo(player, city, builder);
			break;
		case 1004:// 酒馆
			this.setPubInfo(player, city, builder);
			break;
		case 1009:// 空军兵营
			this.setProductionInfo(city, 2, builder);
			break;
		case 1010:// 陆军兵营
			this.setProductionInfo(city, 1, builder);
			break;
		case 1005:// 王宫
			this.setPalaceInfo(player, city, builder);
			break;
		case 1008:// 空港
			this.setPierInfo(city, builder);
		default:
			log.error("not case buildId,buildId ====" + building.getBuildId());
		}
	}

	/**
	 * 设置空港的信息
	 * 
	 * @param city
	 */
	private void setPierInfo(City city, SCShowBuild.Builder builder) {
		List<GameAction> gas = gameActionLogic.getGameActionByCityId(city
				.getCityId());
		if (gas != null) {
			Pier.Builder pier = Pier.newBuilder();

			for (GameAction gameAction : gas) {
				TransportTask.Builder task = null;
				try {
					task = TransportTask.parseFrom(gameAction.getActionData())
							.toBuilder();
				} catch (InvalidProtocolBufferException e) {
					log.error("syn error", e);
					return;
				}

				task.setTargetCityId(gameAction.getTargetCityId());
				City targetCity = this.getCity(gameAction.getTargetCityId());
				task.setTargetCityName(targetCity.getCityName());
				task.setShipCount(gameAction.getShipCount());
				task.setCreateTime((int) (gameAction.getCreateTime().getTime() / 1000));
				task.setLoadingTime((int) (gameAction.getLoadingTime() / 1000));
				task.setOverTime((int) (gameAction.getOverTime().getTime() / 1000));
				task.setStatus(TransportStatus.valueOf(gameAction
						.getActionState()));
				task.setTaskId(gameAction.getActionId());
				pier.addTasks(task);
			}
			builder.setPier(pier);
		}
	}

}
