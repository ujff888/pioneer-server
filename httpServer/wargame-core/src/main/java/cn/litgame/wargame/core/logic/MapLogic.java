package cn.litgame.wargame.core.logic;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.SCLandDonation;
import cn.litgame.wargame.core.auto.GameProtos.SCShowLandResource;
import cn.litgame.wargame.core.auto.GameProtos.Worker;
import cn.litgame.wargame.core.auto.GameResProtos.ResLand;
import cn.litgame.wargame.core.auto.GameResProtos.ResLandResource;
import cn.litgame.wargame.core.mapper.LandDonationMapper;
import cn.litgame.wargame.core.mapper.LandMapper;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Land;
import cn.litgame.wargame.core.model.LandDonation;
import cn.litgame.wargame.core.model.Player;

@Service
public class MapLogic {
	
	@Resource(name = "landMapper")
	private LandMapper landMapper;
	
	@Resource(name = "landDonationMapper")
	private LandDonationMapper landDonationMapper;
	
	@Resource(name = "cityLogic")
	private CityLogic cityLogic;
	
	@Resource(name = "configLogic")
	private ConfigLogic configLogic;
	
	@Resource(name = "playerLogic")
	private PlayerLogic playerLogic;
	
	@Resource(name = "buildingLogic")
	private BuildingLogic buildingLogic;
	
	/**
	 * 获取2个岛屿之间的距离
	 * @param sourceLandId
	 * @param targetLandId
	 * @return
	 */
	public int getLandDistance(int sourceLandId,int targetLandId){
		double distance = 0;
		if(sourceLandId != targetLandId){
			ResLand a = configLogic.getResLand(sourceLandId);
			ResLand b = configLogic.getResLand(targetLandId);
			int x = Math.abs(a.getX() - b.getX());
			int z = Math.abs(a.getZ() - b.getZ());
			distance = (int)Math.sqrt(x * x + z * z);
		}
		return (int)distance;
	}
	/**
	 * 获取2个岛屿之间的时间距离
	 * @param sourceLandId
	 * @param targetLandId
	 * @return 返回的是需要的时间，单位是秒
	 */
	public int getLandTimeDistance(int sourceLandId,int targetLandId,int speed){
		return (int)((this.getLandDistance(sourceLandId, targetLandId) + 1) * (configLogic.getGlobalConfig().getSpeedBaseTime()/speed));
	}

	private final static Logger log = Logger.getLogger(MapLogic.class);
	
	public LandDonation getLandDonation(int landId,long playerId,int cityId){
		return this.landDonationMapper.getLandDonation(landId, playerId, cityId);
	}
	public void updateLandDonation(LandDonation landDonation){
		this.landDonationMapper.updateLandDonation(landDonation);
	}
	public List<LandDonation> getLandDonation(int landId){
		return this.landDonationMapper.getLandDonations(landId);
	}
	
	public GameProtos.LandDonation convert(LandDonation landDonation,int type){
		GameProtos.LandDonation.Builder builder = GameProtos.LandDonation.newBuilder();
		City city = cityLogic.getCity(landDonation.getCityId());
		Player player = playerLogic.getPlayer(landDonation.getPlayerId());
		
		builder.setCityId(landDonation.getCityId());
		builder.setCityLevel(city.getLevel());
		builder.setCityName(city.getCityName());
		int count = type == 1 ? landDonation.getWoodDonationCount() : landDonation.getResourceDonationCount();
		builder.setCount(count);
		int worker = type == 1 ? city.getWoodWorker() : city.getResourceWorker();
		builder.setWorker(worker);
		builder.setPlayerId(player.getPlayerId());
		builder.setPlayerName(player.getPlayerName());
		return builder.build();
	}
	
	public int getCityCountByLandId(int landId){
		return this.landDonationMapper.getCityCountByLandId(landId);
	}
	public Land getLand(int landId){
		Land land = this.landMapper.getLand(landId);
		if(land == null && configLogic.getResLand(landId) != null){
			land = new Land();
			land.setLandId(landId);
			land.setWoodLevel(1);
			land.setWoodExp(0);
			long nowTime = System.currentTimeMillis();
			land.setWoodTime(new Timestamp(nowTime));
			land.setResourceLevel(1);
			land.setResourceExp(0);
			land.setResourceTime(new Timestamp(nowTime));
			this.landMapper.createLand(land);
		}
		return land;
	}
	
	public void updateLand(Land land){
		this.landMapper.updateLand(land);
	}
	
	/**
	 * 获取岛屿的信息
	 * @param landId
	 * @return
	 */
	public GameProtos.Land getLandInfo(int landId){
		GameProtos.Land.Builder builder = GameProtos.Land.newBuilder();
		builder.setLandId(landId);
		
		List<City> citys = cityLogic.getCityByLandId(landId);
		if(citys != null){
			for(City city : citys){
				builder.addCityInfos(cityLogic.convert(city));
			}
		}
		Land land = this.getLand(landId);
		if(land != null){
			builder.setWoodLevel(land.getWoodLevel());
			if(land.getWoodTime() != null){
				builder.setWoodTime((int)(land.getWoodTime().getTime()/1000));
			}
			builder.setResourceLevel(land.getResourceLevel());
			if(land.getResourceTime() != null){
				builder.setResourceTime((int)(land.getResourceTime().getTime()/1000));
			}
		}

		return builder.build();
	}
	
	public MessageCode setWorker(int cityId,List<Worker> workers){
		City myCity = cityLogic.getCity(cityId);
		if(myCity == null){
			return MessageCode.NOT_FOUND_CITY;
		}
		Land land = this.getLand(myCity.getLandId());
		for(Worker worker : workers){
			if(worker.getType() == 1||worker.getType() == 2){
				//1是伐木工，2是特殊资源工
				ResLandResource rlr = worker.getType() == 1 ? configLogic.getWoodResource(land.getWoodLevel())
						: configLogic.getSpeceialResource(land.getResourceLevel());
				//TODO:这里要处理临时工的问题，允许溢出
				if(worker.getCount() > rlr.getMaxWorker()){
					return MessageCode.HAD_LIMIT_PERSON;
				}
				int currentWorker = worker.getType() == 1 ? myCity.getWoodWorker() : myCity.getResourceWorker();
				
				if(worker.getCount() > (currentWorker + cityLogic.getFreePerson(myCity))){
					return MessageCode.NOT_ENOUGH_PERSON;
				}
				if(worker.getType() == 1){
					myCity.setWoodWorker(worker.getCount());
				}else{
					myCity.setResourceWorker(worker.getCount());
				}
			}else if(worker.getType() == 3){
				//设置科学家
				myCity.setScientist(worker.getCount());
			}else if(worker.getType() == 4){
				//设置祭祀
				//TODO:做神庙系统的时候处理这里
			}else{
				return MessageCode.ERR;
			}
		}

		cityLogic.updateCity(myCity);
		return MessageCode.OK;
	}
	
	public MessageCode landDonation(City myCity, int type,int count,SCLandDonation.Builder scLandDonation){

		if(myCity.getWood() < count){
			return MessageCode.NOT_ENOUGH_RESOURCE;
		}
		
		Land land = this.getLand(myCity.getLandId());
		ResLandResource rlr = null;
		int level = 0;
		int exp = 0;
		Timestamp buildTime = null;
		
		if(type == 1){
			level = land.getWoodLevel();
			exp = land.getWoodExp();
			rlr = configLogic.getWoodResource(level);
			buildTime = land.getWoodTime();
		}else if(type == 2){
			level = land.getResourceLevel();
			exp = land.getResourceExp();
			rlr = configLogic.getSpeceialResource(level);
			buildTime = land.getResourceTime();
			
		}else{
			return MessageCode.ERR;
		}
		long nowTime = System.currentTimeMillis();
		if(buildTime != null && buildTime.getTime() > nowTime){
			return MessageCode.Resource_IS_Building;
		}
		if(rlr.getNeedWood() == 0){
			return MessageCode.HAD_MAX_LEVEL;
		}
		if((count + exp) > rlr.getNeedWood()){
			return MessageCode.Resource_Donation_Count_ERR;
		}
		
		LandDonation ld = this.getLandDonation(myCity.getLandId(), myCity.getPlayerId(), myCity.getCityId());
		
		if(type == 1){
			land.setWoodExp(land.getWoodExp() + count);
			if(land.getWoodExp() == rlr.getNeedWood()){
				land.setWoodLevel(land.getWoodLevel() + 1);
				land.setWoodExp(0);
				land.setWoodTime(new Timestamp(nowTime + rlr.getTime()* 1000));
			}
			ld.setWoodDonationCount(ld.getWoodDonationCount() + count);
		}else if(type == 2){
			land.setResourceExp(land.getResourceExp() + count);
			if(land.getResourceExp() == rlr.getNeedWood()){
				land.setResourceLevel(land.getResourceLevel() + 1);
				land.setResourceExp(0);
				land.setResourceTime(new Timestamp(nowTime + rlr.getTime()* 1000));
			}
			ld.setResourceDonationCount(ld.getWoodDonationCount() + count);
		}
		myCity.setWood(myCity.getWood() - count);
		cityLogic.updateCity(myCity);
		this.updateLand(land);
		this.updateLandDonation(ld);
		
		int donation = 0;
		if(type == 1){
			scLandDonation.setLandCount(land.getWoodExp());
			scLandDonation.setLevel(land.getWoodLevel());
			donation = ld.getWoodDonationCount();
		}else{
			scLandDonation.setLandCount(land.getResourceExp());
			scLandDonation.setLevel(land.getResourceLevel());
			donation = ld.getResourceDonationCount();
		}
		
		scLandDonation.setType(type);
		scLandDonation.setCityId(myCity.getCityId());
		scLandDonation.setCityCount(donation);
		
		return MessageCode.OK;
	}
	
	public MessageCode showLandResource(int landId, int type,int cityId,SCShowLandResource.Builder builder){
		Land land = this.getLand(landId);
		if(land == null){
			return MessageCode.NOT_FOUND_LAND;
		}
		
		City myCity = cityLogic.getCity(cityId);
		if(myCity == null){
			return MessageCode.NOT_FOUND_CITY;
		}
		
		builder.setPerson(cityLogic.getFreePerson(myCity));
		long nowTime = System.currentTimeMillis();
		if(type == 1){
			builder.setWorker((int)myCity.getWoodWorker());
			builder.setWorkRate((int)cityLogic.getWoodRateByHour(myCity));
			
			builder.setLevel(land.getWoodLevel());
			builder.setExp(land.getWoodExp());
			
			if(land.getWoodTime() != null && nowTime < land.getWoodTime().getTime()){
				builder.setBuildTime((int)(land.getWoodTime().getTime()/1000));
			}else{
				builder.setBuildTime(0);
			}
			
		}else if(type == 2){
			builder.setWorker((int)myCity.getResourceWorker());
			builder.setWorkRate((int)cityLogic.getResourceRateByHour(myCity));
			
			builder.setLevel(land.getResourceLevel());
			builder.setExp(land.getResourceExp());
			
			if(land.getResourceTime() != null && nowTime < land.getResourceTime().getTime()){
				builder.setBuildTime((int)(land.getResourceTime().getTime()/1000));
			}else{
				builder.setBuildTime(0);
			}
		}else{
			return MessageCode.ERR;
		}
		
		builder.setCityGoldRate(cityLogic.getGoldRateByHour(myCity));
		
		List<LandDonation> lds = this.getLandDonation(landId);
		if(lds != null){
			for(LandDonation l : lds){
				builder.addLandDonations(this.convert(l, type));
			}
		}

		return MessageCode.OK;
	}
	
	/**
	 * 创建用户的捐献记录
	 * @param landId
	 * @param playerId
	 * @param cityId
	 * @param woodCount
	 * @param resourceCount
	 */
	public void createLandDonation(int landId,long playerId,int cityId){
		LandDonation ld = this.getLandDonation(landId,playerId,cityId);
		if(ld == null){
			ld = new LandDonation();
			ld.setCityId(cityId);
			ld.setLandId(landId);
			ld.setPlayerId(playerId);
			ld.setResourceDonationCount(0);
			ld.setWoodDonationCount(0);
			this.landDonationMapper.createLandDonation(ld);
		}
	}

}
