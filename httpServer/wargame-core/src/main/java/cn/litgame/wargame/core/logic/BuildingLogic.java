package cn.litgame.wargame.core.logic;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.ResBuild;
import cn.litgame.wargame.core.logic.queue.GameActionLogic;
import cn.litgame.wargame.core.mapper.BuildingMapper;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.GameAction;

@Service
public class BuildingLogic {
	
	@Resource(name = "configLogic")
	private ConfigLogic configLogic;
	
	@Resource(name = "buildingMapper")
	private BuildingMapper buildingMapper;
	
	@Resource(name = "gameActionLogic")
	private GameActionLogic gameActionLogic;
	
	@Resource(name = "cityLogic")
	private CityLogic cityLogic;
	
	/**
	 * 获取博物馆的建筑基础民意加成
	 * @param b
	 * @return
	 */
	public int getMuseumBuildPO(Building b){
		ResBuild res = configLogic.getResBuild(b.getBuildId(), b.getLevel());
		return res.getArg1();
	}
	/**
	 * 获取博物馆的文化条约民意加成
	 * @param b
	 * @return
	 */
	public int getMuseumPO(Building b){
		return 0;
	}
	/**
	 * 获取博物馆，TODO：//还没有同步文化条约的数量
	 * @param city
	 * @return
	 */
	public Building getMuseum(City city){
		List<Building> ms = this.getBuildings(city.getCityId(), 1007);//获取博物馆
		if(ms == null || ms.size() != 1){
			return null;
		}
		return ms.get(0);
	}
	
	/**
	 * 获取总督府或者王宫，首都就是王宫
	 * @param city
	 * @return
	 */
	public Building getPalace(City city){
		int buildId = city.isCapital() ? 1005 : 1006;
		List<Building> ps = this.getBuildings(city.getCityId(),buildId);
		if(ps == null || ps.size() != 1){
			return null;
		}
		return ps.get(0);
	}
	
	/**
	 * 获取酒馆的接口，处理了资源不足的时候，自动取消掉食物消耗量，其他函数不可以直接从数据库拿酒馆
	 * @param city
	 * @return
	 */
	public Building getPubInfo(City city){
		List<Building> ps = this.getBuildings(city.getCityId(),1004);//获取酒馆
		if(ps == null || ps.size() != 1){
			return null;
		}
		Building pub = ps.get(0);
		ResBuild res = configLogic.getResBuild(1004, pub.getCount());
		if(res != null && res.getArg1() > city.getFood()){
			pub.setCount(0);
			this.updateBuilding(pub);
		}
		return pub;
	}
	/**
	 * 获取酒馆建筑的基础民意加成
	 * @param b
	 * @return
	 */
	public int getPubBasePO(Building b){
		if(b == null || b.getLevel() == 0){
			return 0;
		}
		int level = b.getLevel();
		ResBuild rb = configLogic.getResBuild(1004, level);
		return rb.getArg2();
	}
	/**
	 * 获取酒馆的食物消耗量所增加的民意
	 * @param b
	 * @return
	 */
	public int getPubFoodPO(Building b){
		if(b == null){
			return 0;
		}
		int level =  b.getCount();
		if(level == 0){
			return 0;
		}
		return configLogic.getResBuild(1004, level).getArg3();
	}
	/**
	 * 获取某个城市的酒馆加成民意,包含建筑与食品消耗
	 * @param city
	 * @return
	 */
	public int getTotalPubPO(City city){
		Building b  = this.getPubInfo(city);
		return this.getPubBasePO(b) + this.getPubFoodPO(b);
	}
	
	/**
	 * 获取城市内的某种建筑集合
	 * @param cityId
	 * @param buildId
	 * @return
	 */
	public List<Building> getBuildings(int cityId, int buildId){
		return this.buildingMapper.getBuildingsByBuildId(cityId, buildId);
	}
	
	/**
	 * 获取一个建筑
	 * @param playerId
	 * @param cityId
	 * @param position
	 * @return
	 */
	public Building getBuilding(int cityId,int position){
		Building b = buildingMapper.getBuilding( cityId, position);
		if(b != null){
			this.checkBuildingTime(b);
		}
		return b;
	}
	/**
	 * 检查建筑物的建造时间，如果建造已经完成的话，执行升级动作
	 * @param b
	 */
	public void checkBuildingTime(Building b){
		if(b.isBuilding()){
			if(System.currentTimeMillis() >= b.getBuildTime().getTime()){
				b.setBuilding(false);
				b.setLevel(b.getLevel() + 1);
				if(b.getBuildId() == 1001){
					City city = cityLogic.getCity(b.getCityId());
					city.setLevel(b.getLevel());
					cityLogic.updateCity(city);
				}
				this.updateBuilding(b);
			}
		}
	}
	

	/**
	 * 获取一个城市里的所有建筑
	 * @param playerId
	 * @param cityId
	 * @return
	 */
	public List<Building> getBuildings(int cityId){
		List<Building> bs = this.buildingMapper.getBuildings(cityId);
		if(bs != null){
			for(Building b : bs){
				this.checkBuildingTime(b);
			}
		}
		return bs;
	}

	/**
	 * 获取建筑加速所需要的钻石数量,毫秒
	 * @return
	 */
	public int getNeedDiamondSpeed(long buildTime){
		return (int)Math.ceil(((buildTime - 300000)/60000d))/configLogic.getGlobalConfig().getBuildingTimeDiamond();
	}
	
	/**
	 * 建造一个新的建筑
	 */
	public Building createBuilding(long playerId,int cityId,int buildId,int pos,boolean needBuilding){
		Building b = new Building();
		b.setBuildId(buildId);
		b.setBuilding(needBuilding);
		b.setCityId(cityId);
		b.setLevel(1);
		b.setPosition(pos);
		b.setPlayerId(playerId);
		if(needBuilding){
			b.setLevel(0);
			ResBuild resBuild = configLogic.getResBuild(buildId, 1);
			b.setBuildTime(new Timestamp(System.currentTimeMillis() + resBuild.getTime() * 1000));
		}
		this.buildingMapper.createBuilding(b);
		return b;
	}
	
	/**
	 * 更新某个建筑
	 * @param building
	 * @return
	 */
	public boolean updateBuilding(Building building){
		return buildingMapper.updateBuilding(building) == 1 ? true : false;
	}
	/**
	 * 删除某个建筑
	 * @param id
	 */
	public void delBuilding(int id){
		this.buildingMapper.delBuilding(id);
	}
	
	public GameProtos.Building convert(City city,Building building){
		GameProtos.Building.Builder builder = GameProtos.Building.newBuilder();
		builder.setPosition(building.getPosition());
		builder.setLevel(building.getLevel());
		builder.setBuildId(building.getBuildId());
		if(building.getBuildTime() != null){
			builder.setBuildTime((int)(building.getBuildTime().getTime()/1000));
		}
		long nowTime = System.currentTimeMillis();
		if(building.getBuildId() == 1009){
			GameAction ga = gameActionLogic.getProductionFlyTroop(city, nowTime);
			if(ga != null){
				builder.setProductionTime((int)(ga.getOverTime().getTime() - nowTime)/1000);
			}
		}else if(building.getBuildId() == 1010){
			GameAction ga = gameActionLogic.getProductionLandTroop(city, nowTime);
			if(ga != null){
				builder.setProductionTime((int)(ga.getOverTime().getTime() - nowTime)/1000);
			}
		}else{
			builder.setProductionTime((int)0);
		}
		
		
		return builder.build();
	}

	/**
	 * 某或玩家的某种建筑，在所有城市里寻找
	 * @param playerId
	 * @param buildId
	 * @return
	 */
	public List<Building> getBuildings(long playerId,int buildId){
		return this.buildingMapper.getBuildsByPlayerId(playerId, buildId);
	}
	
}
