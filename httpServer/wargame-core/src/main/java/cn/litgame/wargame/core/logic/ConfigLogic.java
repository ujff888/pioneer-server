package cn.litgame.wargame.core.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameResProtos.BattleGround;
import cn.litgame.wargame.core.auto.GameResProtos.GameResource;
import cn.litgame.wargame.core.auto.GameResProtos.GlobalConfig;
import cn.litgame.wargame.core.auto.GameResProtos.PayInfo;
import cn.litgame.wargame.core.auto.GameResProtos.ResBuild;
import cn.litgame.wargame.core.auto.GameResProtos.ResBuildType;
import cn.litgame.wargame.core.auto.GameResProtos.ResLand;
import cn.litgame.wargame.core.auto.GameResProtos.ResLandResource;
import cn.litgame.wargame.core.auto.GameResProtos.ResShopItem;
import cn.litgame.wargame.core.auto.GameResProtos.ResSystem;
import cn.litgame.wargame.core.auto.GameResProtos.ResTech;
import cn.litgame.wargame.core.auto.GameResProtos.ResTroop;
import cn.litgame.wargame.core.auto.GameResProtos.ShopShelfType;

@Service
public class ConfigLogic {
	private final static Logger log = Logger.getLogger(ConfigLogic.class);
	
	private String configVersion;
	private GameResource gameResource;
	private boolean loaded = false;
	private Map<Integer,ResTroop> troops = new HashMap<Integer,ResTroop>();
	private Map<Integer,BattleGround> landBattleGrounds = new HashMap<Integer, BattleGround>();
	private Map<Integer,BattleGround> airBattleGrounds = new HashMap<Integer, BattleGround>();
	private Map<String,PayInfo> payInfos = new HashMap<String,PayInfo>();
	private Map<Integer,ResLand> resLands = new HashMap<Integer, ResLand>();
	private Map<String,ResLand> resLandMap = new HashMap<String,ResLand>();
	
	private Map<Integer,ResBuildType> resBuildTypes = new HashMap<Integer, ResBuildType>();
	private Map<Integer,Map<Integer,ResBuild>> resBuilds = new HashMap<Integer, Map<Integer,ResBuild>>();
	private Map<Integer,ResTech> resTeches = new HashMap<Integer, ResTech>();
	private Map<Integer,ResLandResource> woodResource = new HashMap<Integer, ResLandResource>();
	private Map<Integer,ResLandResource> specialResource = new HashMap<Integer,ResLandResource>();
	private Map<Integer,ResSystem> resSystems = new HashMap<Integer, ResSystem>();
	private Map<Integer,ResShopItem> resShopItems=new HashMap<Integer, ResShopItem>();
	private Map<ShopShelfType,ArrayList<ResShopItem>> resShopItemsDivided=new HashMap<ShopShelfType,ArrayList<ResShopItem>>();
	private GlobalConfig globalConfig;
	
	/**
	 * 根据x,z坐标获取岛屿的配置
	 * @param x
	 * @param z
	 * @return
	 */
	public ResLand getResLand(int x, int z){
		return resLandMap.get(x +","+ z);
	}
	
	/**
	 * 获取政治体制的配置
	 * @param systemId
	 * @return
	 */
	public ResSystem getResSystem(int systemId){
		return this.resSystems.get(systemId);
	}
	/**
	 * 获取岛屿上的木头资源矿的配置
	 * @param level
	 * @return
	 */
	public ResLandResource getWoodResource(int level){
		return this.woodResource.get(level);
	}
	/**
	 * 获取岛屿上的特殊资源矿配置
	 * @param level
	 * @return
	 */
	public ResLandResource getSpeceialResource(int level){
		return this.specialResource.get(level);
	}
	
	public GlobalConfig getGlobalConfig(){
		return this.globalConfig;
	}
	public PayInfo getPayInfo(String producId){
		return this.payInfos.get(producId);
	}
	
	public ResLand getResLand(int landId){
		return resLands.get(landId);
	}
	
	public ResTroop getResTroop(int troopId){
		return troops.get(troopId);
	}
	
	public ArrayList<ResShopItem> getResShopByType(ShopShelfType shopType)
	{
		return resShopItemsDivided.get(shopType);
	}
	
	public Map<Integer,ResShopItem> getResShop()
	{
		return this.resShopItems;
	}
	
	
	public BattleGround getBattleGround(int type, int level){
		BattleGround.Builder builder = BattleGround.newBuilder();
		if(type == 1 && level == 1){
			//等级1
			builder.setLevel(1);
			builder.setType(1);
			builder.setFly(10);
			builder.setFlyCount(1);
			builder.setFlyFire(10);
			builder.setFlyFireCount(1);
			builder.setFire(30);
			builder.setFireCount(1);
			builder.setRemote(30);
			builder.setRemoteCount(3);
			builder.setWeight(30);
			builder.setWeightCount(3);
			builder.setLight(0);
			builder.setLightCount(0);
		}

		if(type == 1 && level == 5){
			//等级2
			builder.setLevel(5);
			builder.setType(1);
			builder.setFly(30);
			builder.setFlyCount(1);
			builder.setFlyFire(30);
			builder.setFlyFireCount(1);
			builder.setFire(30);
			builder.setFireCount(2);
			builder.setRemote(30);
			builder.setRemoteCount(5);
			builder.setWeight(30);
			builder.setWeightCount(5);
			builder.setLight(30);
			builder.setLightCount(2);
		}
		return builder.build();
		
//		if(type == 1){
//			return landBattleGrounds.get(level);
//		}else{
//			return airBattleGrounds.get(level);
//		}
	}
	
	public boolean isLoaded(){
		return loaded;
	}
	private void check(){
		if(!loaded){
			throw new RuntimeException("ConfigLogic not load data");
		}
	}
	
	public boolean loadConfig(String path){
		try{
			if(!loaded){
				Resource resource = new FileSystemResource(path);
				gameResource = GameResource.parseFrom(resource.getInputStream());
				init();
				loaded = true;
			}
		}catch(IOException e){
			loaded = false;
			throw new RuntimeException("load config error");
		}
		return loaded;
	}

	public ResBuildType getResBuildType(int buildId){
		return resBuildTypes.get(buildId);
	}
	public ResBuild getResBuild(int buildId,int level){
		ResBuild resBuild = resBuilds.get(buildId).get(level);
		return resBuild;
	}
	public ResTech getResTech(int techId){
		return resTeches.get(techId);
	}
	
	private void init(){
		globalConfig = gameResource.getGlobalConfig();
		configVersion = gameResource.getVersion();
		List<ResTroop> resTroop = gameResource.getResTroopList();
		for(ResLand rl : gameResource.getResLandsList()){
			resLands.put(rl.getLandId(), rl);
			resLandMap.put(rl.getX() +","+ rl.getZ(), rl);
		}
		
		for(ResBuildType rbt : gameResource.getResBuildTypeList()){
			resBuildTypes.put(rbt.getTypeId(), rbt);
		}
		
		for(ResBuild rb : gameResource.getResBuildsList()){
			Map<Integer,ResBuild> ls = resBuilds.get(rb.getBuildId());
			if(ls == null){
				ls = new HashMap<Integer, ResBuild>();
				resBuilds.put(rb.getBuildId(), ls);
			}
			ls.put(rb.getLevel(), rb);
		}
		for(ResSystem resSystem : gameResource.getResSystemsList()){
			resSystems.put(resSystem.getSystemId(), resSystem);
		}
		
		for(ResShopItem resShopItem:gameResource.getResShopShelfList()){
			resShopItems.put(resShopItem.getId(),resShopItem);
		}
		System.out.println("shop item is:"+resShopItemsDivided.get(ShopShelfType.HOT));
		if(resShopItemsDivided.get(ShopShelfType.HOT)==null)
			resShopItemsDivided.put(ShopShelfType.HOT, new ArrayList<ResShopItem>());
		if(resShopItemsDivided.get(ShopShelfType.INCRE)==null)
			resShopItemsDivided.put(ShopShelfType.INCRE, new ArrayList<ResShopItem>());
		if(resShopItemsDivided.get(ShopShelfType.RESOURCES)==null)
			resShopItemsDivided.put(ShopShelfType.RESOURCES, new ArrayList<ResShopItem>());
		if(resShopItemsDivided.get(ShopShelfType.OTHERS)==null)
			resShopItemsDivided.put(ShopShelfType.OTHERS, new ArrayList<ResShopItem>());
		if(resShopItemsDivided.get(ShopShelfType.WAR_SHELF)==null)
			resShopItemsDivided.put(ShopShelfType.WAR_SHELF, new ArrayList<ResShopItem>());
		
		for (Map.Entry<Integer, ResShopItem> entry : resShopItems.entrySet())
		{
			if(entry.getValue().getShelfType()==ShopShelfType.HOT)
				resShopItemsDivided.get(ShopShelfType.HOT).add(entry.getValue());
			else if(entry.getValue().getShelfType()==ShopShelfType.INCRE)
				resShopItemsDivided.get(ShopShelfType.INCRE).add(entry.getValue());
			else if(entry.getValue().getShelfType()==ShopShelfType.OTHERS)
				resShopItemsDivided.get(ShopShelfType.OTHERS).add(entry.getValue());
			else if(entry.getValue().getShelfType()==ShopShelfType.WAR_SHELF)
				resShopItemsDivided.get(ShopShelfType.WAR_SHELF).add(entry.getValue());
			else if(entry.getValue().getShelfType()==ShopShelfType.RESOURCES)
				resShopItemsDivided.get(ShopShelfType.RESOURCES).add(entry.getValue());
		}
		
		for(ResTech rt : gameResource.getResTechesList()){
			resTeches.put(rt.getTechId(), rt);
		}
		
		for(ResTroop rt : resTroop){
			troops.put(rt.getId(), rt);
		}
		for(PayInfo info : gameResource.getPayInfoList()){
			payInfos.put(info.getProducId(), info);
		}
		
		for(ResLandResource rlr : gameResource.getResLandResourceList()){
			if(rlr.getType() == 1){
				this.woodResource.put(rlr.getLevel(), rlr);
			}
			if(rlr.getType() == 2){
				this.specialResource.put(rlr.getLevel(), rlr);
			}
		}
		
		List<BattleGround> bgs = gameResource.getBattleGroundsList();
		for(BattleGround bg :bgs){
			if(bg.getType() == 1){
				landBattleGrounds.put(bg.getLevel(),bg);
			}else if(bg.getType() == 2){
				airBattleGrounds.put(bg.getLevel(),bg);
			}
		}
	}
	public static void main(String[] args) {
		System.out.println(137573171226L % 12);
	}
}
