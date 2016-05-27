package cn.litgame.wargame.server.logic.city;

import java.util.List;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.CSBuildLevelUp;
import cn.litgame.wargame.core.auto.GameProtos.CSBuyPerson;
import cn.litgame.wargame.core.auto.GameProtos.CSChangeCityName;
import cn.litgame.wargame.core.auto.GameProtos.CSCheckBuild;
import cn.litgame.wargame.core.auto.GameProtos.CSCreateBuilding;
import cn.litgame.wargame.core.auto.GameProtos.CSCreateCity;
import cn.litgame.wargame.core.auto.GameProtos.CSMoveCity;
import cn.litgame.wargame.core.auto.GameProtos.CSSetPubLevel;
import cn.litgame.wargame.core.auto.GameProtos.CSSetTecher;
import cn.litgame.wargame.core.auto.GameProtos.CSShowBuild;
import cn.litgame.wargame.core.auto.GameProtos.CSShowTechBuild;
import cn.litgame.wargame.core.auto.GameProtos.CSShowTechProgress;
import cn.litgame.wargame.core.auto.GameProtos.CSSpeedyBuilding;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.SCBuildLevelUp;
import cn.litgame.wargame.core.auto.GameProtos.SCBuyPerson;
import cn.litgame.wargame.core.auto.GameProtos.SCChangeCityName;
import cn.litgame.wargame.core.auto.GameProtos.SCCheckBuild;
import cn.litgame.wargame.core.auto.GameProtos.SCCreateBuilding;
import cn.litgame.wargame.core.auto.GameProtos.SCCreateCity;
import cn.litgame.wargame.core.auto.GameProtos.SCMoveCity;
import cn.litgame.wargame.core.auto.GameProtos.SCSetPubLevel;
import cn.litgame.wargame.core.auto.GameProtos.SCSetTecher;
import cn.litgame.wargame.core.auto.GameProtos.SCShowBuild;
import cn.litgame.wargame.core.auto.GameProtos.SCShowTechBuild;
import cn.litgame.wargame.core.auto.GameProtos.SCShowTechProgress;
import cn.litgame.wargame.core.auto.GameProtos.SCSpeedyBuilding;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.auto.GameResProtos.GlobalConfig;
import cn.litgame.wargame.core.auto.GameResProtos.ResBuild;
import cn.litgame.wargame.core.auto.GameResProtos.ResBuildType;
import cn.litgame.wargame.core.logic.RankLogic;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.core.model.PlayerTech;
import cn.litgame.wargame.server.message.KHttpMessageProcess;

@Service
public class CityProcess extends KHttpMessageProcess{
	
	public void buyPerson(CSBuyPerson msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		builder.setMessageType(MessageType.MSG_ID_BUY_PERSON);
		SCBuyPerson.Builder scBuyPerson = SCBuyPerson.newBuilder();
		
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		
		Player player = httpMessageManager.getPlayer();
		if(player == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_PLAYER);
			httpMessageManager.send(builder);
			return;
		}
		
		int oldDiamond = player.getDiamond();
		int diamondCost = configLogic.getGlobalConfig().getBuyPersionDiamond();
		if(diamondCost > oldDiamond){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_DIAMOND);
			httpMessageManager.send(builder);
			return;
		}
		
		player.setDiamond(oldDiamond-diamondCost);
		player.setNeedSave(true);
		int increasePersonCount = configLogic.getGlobalConfig().getBuyPersionCount();
		int maxPerson = configLogic.getResBuild(1001, city.getLevel()).getArg1();
		
		if(city.getTotalPerson() + increasePersonCount > maxPerson){
			builder.setMessageCode(MessageCode.HAD_LIMIT_PERSON);
			httpMessageManager.send(builder);
			return;
		}
		
		city.setTotalPerson(city.getTotalPerson() + increasePersonCount);
		httpMessageManager.changeCityResource(city);
		builder.setNeedUpdateResource(true);
		
		scBuyPerson.setCityId(city.getCityId());
		scBuyPerson.setPerson((int)city.getTotalPerson());
		builder.setScBuyPerson(scBuyPerson);
		builder.setMessageCode(MessageCode.OK);
		
		httpMessageManager.send(builder);
	}
	
	public void changeCityName(CSChangeCityName msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		//TODO:敏感词过滤
		city.setCityName(msg.getCityName());
		cityLogic.updateCity(city);
		SCChangeCityName.Builder scChangeCityName = SCChangeCityName.newBuilder();
		scChangeCityName.setCityId(city.getCityId());
		scChangeCityName.setCityName(city.getCityName());
		builder.setScChangeCityName(scChangeCityName);
		httpMessageManager.send(builder);
	}
	
	public void buildShow(CSShowBuild msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Player player = httpMessageManager.getPlayer();
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		Building b = buildingLogic.getBuilding(msg.getCityId(), msg.getPosition());
		if(b == null || b.getLevel() == 0){
			builder.setMessageCode(MessageCode.NOT_FOUND_BUILDING);
			httpMessageManager.send(builder);
			return;
		}
		SCShowBuild.Builder scShowBuild = SCShowBuild.newBuilder();
		cityLogic.setShowBuildInfo(player,city, b, scShowBuild);
		builder.setScShowBuild(scShowBuild);
		httpMessageManager.send(builder);
	}
	
	/**
	 * 显示科技的进度
	 * @param msg
	 */
	public void showTechProgress(CSShowTechProgress msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Player player = httpMessageManager.getPlayer();
		PlayerTech pt  = playerLogic.getPlayerTech(player.getPlayerId());
		SCShowTechProgress.Builder scShowTechProgress = SCShowTechProgress.newBuilder();
		scShowTechProgress.setTotalTeacher(pt.getTotalTecher());
		scShowTechProgress.setTotalTechPoint(pt.getTechPoint());
		scShowTechProgress.setTechPointRate(pt.getTechPointRate());
		scShowTechProgress.addProgress(pt.getFlyProgress());
		scShowTechProgress.addProgress(pt.getEconomicProgress());
		scShowTechProgress.addProgress(pt.getScienceProgress());
		scShowTechProgress.addProgress(pt.getMilitaryProgress());
		scShowTechProgress.addMaxLevel(pt.getFlyLevel());
		scShowTechProgress.addMaxLevel(pt.getEconomicLevel());
		scShowTechProgress.addMaxLevel(pt.getScienceLevel());
		scShowTechProgress.addMaxLevel(pt.getMilitaryLevel());
		builder.setScShowTechProgress(scShowTechProgress);
		httpMessageManager.send(builder);
	}
	
	/**
	 * 打开科技面板
	 * @param msg
	 */
	public void showTechBuild(CSShowTechBuild msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		
		SCShowTechBuild.Builder scShowTechBuild = SCShowTechBuild.newBuilder();
		cityLogic.showTechBuild(city, scShowTechBuild);
		builder.setScShowTechBuild(scShowTechBuild);
		httpMessageManager.send(builder);
	}
	
	public void setTecher(CSSetTecher msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		City city = cityLogic.getCity(msg.getCityId());
		Player p = httpMessageManager.getPlayer();
		if(city == null || city.getPlayerId() != p.getPlayerId()){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		
		if(city.getScientist() == msg.getTecher()){
			builder.setMessageCode(MessageCode.ERR);
			httpMessageManager.send(builder);
			return;
		}
		
		MessageCode mc = cityLogic.setTecher(city, msg.getTecher());
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		SCSetTecher.Builder scSetTecher = SCSetTecher.newBuilder();
		scSetTecher.setCityId(msg.getCityId());
		scSetTecher.setTecher(msg.getTecher());
		builder.setScSetTecher(scSetTecher);
		httpMessageManager.send(builder);
	}
	
	public void setPubLevel(CSSetPubLevel msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		
		Building b = buildingLogic.getBuilding(msg.getCityId(), msg.getPosition());
		if(b == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_BUILDING);
			httpMessageManager.send(builder);
			return;
		}
		if(b.getBuildId() != 1004 || b.getLevel() < msg.getLevel()){
			builder.setMessageCode(MessageCode.ERR);
			httpMessageManager.send(builder);
			return;
		}
		b.setCount(msg.getLevel());
		buildingLogic.updateBuilding(b);
		SCSetPubLevel.Builder scSetPubLevel = SCSetPubLevel.newBuilder();
		scSetPubLevel.setCityId(b.getCityId());
		scSetPubLevel.setPosition(b.getPosition());
		scSetPubLevel.setLevel(b.getLevel());
		builder.setScSetPubLevel(scSetPubLevel);
		httpMessageManager.send(builder);
	}
	
	public void checkBuild(CSCheckBuild msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		Building b = buildingLogic.getBuilding(msg.getCityId(), msg.getPosition());
		if(b == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_BUILDING);
			httpMessageManager.send(builder);
			return;
		}
		SCCheckBuild.Builder scCheckBuild = SCCheckBuild.newBuilder();
		scCheckBuild.setBuilding(buildingLogic.convert(city,b));
		builder.setScCheckBuild(scCheckBuild);
		httpMessageManager.send(builder);
	}
	
	public static void main(String[] args) {
		int needDiamond = (int)Math.ceil(((1000d * 60 * 5 + 70000)/60000))/1;
		System.out.println(needDiamond);
	}
	
	public void speedyBuilding(CSSpeedyBuilding msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Player player = httpMessageManager.getPlayer();
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		Building b = buildingLogic.getBuilding(msg.getCityId(), msg.getPosition());
		if(b == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_BUILDING);
			httpMessageManager.send(builder);
			return;
		}
		if(!b.isBuilding()){
			builder.setMessageCode(MessageCode.NOT_NEED_SPEEDY);
			httpMessageManager.send(builder);
			return;
		}
		Building bb = null;
		long nowTime = System.currentTimeMillis();
		long overTime = b.getBuildTime().getTime();
		
		if(msg.getType() == 1){
			int needDiamond = buildingLogic.getNeedDiamondSpeed(overTime - nowTime);
			if(player.getDiamond() < needDiamond){
				builder.setMessageCode(MessageCode.NOT_ENOUGH_DIAMOND);
				httpMessageManager.send(builder);
				return;
			}
			player.setDiamond(player.getDiamond() - needDiamond);
			player.setNeedSave(true);
			bb = cityLogic.buildFinished(player,city, b);
		}else if(msg.getType() == 3){
			
			if(overTime - nowTime < 300000){
				bb = cityLogic.buildFinished(player,city, b);
			}else{
				builder.setMessageCode(MessageCode.ERR);
				httpMessageManager.send(builder);
				return;
			}
		}else if(msg.getType() == 2){
			//道具加速，待定
		}else{
			builder.setMessageCode(MessageCode.ERR);
			httpMessageManager.send(builder);
			return;
		}
		SCSpeedyBuilding.Builder scSpeedyBuilding = SCSpeedyBuilding.newBuilder();
		scSpeedyBuilding.setBuilding(buildingLogic.convert(city,bb));
		builder.setScSpeedyBuilding(scSpeedyBuilding);
		httpMessageManager.send(builder);
	}
	
	/**
	 * 迁移城市
	 * @param msg
	 */
	public void moveCity(CSMoveCity msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		City city = cityLogic.getCity(msg.getCityId());
		Player player = httpMessageManager.getPlayer();
		if(city == null || city.getPlayerId() != player.getPlayerId()){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		City targetCity = cityLogic.getCity(msg.getLandId(), msg.getPosition());
		if(targetCity != null){
			builder.setMessageCode(MessageCode.HAD_CITY_POSITION);
			httpMessageManager.send(builder);
			return;
		}
		if(player.getDiamond() < configLogic.getGlobalConfig().getMoveCityDiamond()){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_DIAMOND);
			httpMessageManager.send(builder);
			return;
		}
		player.setDiamond(player.getDiamond() - configLogic.getGlobalConfig().getMoveCityDiamond());
		playerLogic.updatePlayer(player);
		city.setLandId(msg.getLandId());
		city.setPosition(msg.getPosition());
		cityLogic.updateCity(city);
		SCMoveCity.Builder scMoveCity = SCMoveCity.newBuilder();
		scMoveCity.setCity(cityLogic.convert(city));
		builder.setScMoveCity(scMoveCity);
		httpMessageManager.send(builder);
	}
	
	/**
	 * 创建城市
	 * @param msg
	 */
	public void createCity(CSCreateCity msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Player player = httpMessageManager.getPlayer();
		City city = cityLogic.getCity(msg.getLandId(),msg.getPosition());
		CityResource cr = msg.getCityResource();
		GlobalConfig gc = configLogic.getGlobalConfig();
		
		if(city != null){
			builder.setMessageCode(MessageCode.HAD_BUILDING_POSITION);
			httpMessageManager.send(builder);
			return;
		}
		if(gc.getPosition().getCityPayDiamond() == msg.getPosition()
				&& gc.getCreateCityDiamond() > player.getDiamond()){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_DIAMOND);
			httpMessageManager.send(builder);
			return;
		}
		City sourceCity = cityLogic.getCity(msg.getCityId());
		if(sourceCity == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		if(gc.getCreateCityGold() > player.getGold()){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_GOLD);
			httpMessageManager.send(builder);
			return;
		}
		if(gc.getCreateCityPerson() > cityLogic.getFreePerson(sourceCity)){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_PERSON);
			httpMessageManager.send(builder);
			return;
		}
		if(gc.getCreateCityWood() + cr.getWood() > sourceCity.getWood()){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_RESOURCE);
			httpMessageManager.send(builder);
			return;
		}
		
		//检查码头
		List<Building> wharfs = buildingLogic.getBuildings(sourceCity.getCityId(), 1008);
		if(wharfs == null || wharfs.size() == 0){
			builder.setMessageCode(MessageCode.NOT_FOUND_WHARF);
			httpMessageManager.send(builder);
			return;
		}
		int wharfSpeed = 0;
		for(Building b : wharfs){
			wharfSpeed += configLogic.getResBuild(b.getBuildId(), b.getLevel()).getArg1();
		}
		
		//检查王宫的等级
		List<Building> buildings = buildingLogic.getBuildings(player.getPlayerId(), 1005);
		int level = buildings == null ? 0 : buildings.get(0).getLevel();
		if(level < cityLogic.getCityCount(player.getPlayerId())){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_KING_BUILD);
			httpMessageManager.send(builder);
			return;
		}
		
		//检查运输船
		int needShipCount = shipLogic.getResourceShipCount(msg.getCityResource(),gc.getCreateCityPerson()
				+ gc.getCreateCityWood());
		int currentShipCount = shipLogic.getFreeShip(player.getPlayerId());
		if(currentShipCount < needShipCount){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_TRANSPORTATION);
			httpMessageManager.send(builder);
			return;
		}
		
		//移除资源
		sourceCity.setTotalPerson(sourceCity.getTotalPerson() - gc.getCreateCityPerson());
		sourceCity.setWood(sourceCity.getWood() - gc.getCreateCityWood());
		
		cityLogic.removeCityResource(sourceCity, cr.getWood(),cr.getStone(), cr.getCrystal(), cr.getMetal(), cr.getFood());
		httpMessageManager.changeCityResource(sourceCity);
		
		//player.setGold(player.getGold() - gc.getCreateCityGold());
		playerLogic.updatePlayerGold(player.getPlayerId(), player.getGold() - gc.getCreateCityGold());
		if(gc.getPosition().getCityPayDiamond() == msg.getPosition()){
			player.setDiamond(player.getDiamond() - gc.getCreateCityDiamond());
		}
		player.setNeedSave(true);
		
		//建立城市
		City targetCity = cityLogic.createOtherCity(player.getPlayerId(), msg.getLandId(), msg.getPosition());
		
		
		long loadingTime = shipLogic.getResourceShipTime(msg.getCityResource(),gc.getCreateCityPerson() + gc.getCreateCityWood(), wharfSpeed);
		
		//发起一个定时任务
		TransportTask.Builder result = TransportTask.newBuilder();
		result.setShipCount(needShipCount);
		result.setType(GameActionType.CREATE_CITY);
		result.setResource(msg.getCityResource());
		gameActionLogic.createTransportShipTask(result, sourceCity, targetCity, loadingTime);
		
		SCCreateCity.Builder scCreateCity = SCCreateCity.newBuilder();
		scCreateCity.setCity(cityLogic.convert(targetCity));
		builder.setScCreateCity(scCreateCity);
		
		builder.setNeedUpdateResource(true);
		httpMessageManager.send(builder);
	}

	/**
	 * 建造建筑
	 * @param msg
	 */
	public void createBuilding(CSCreateBuilding msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Player player = httpMessageManager.getPlayer();

		Building b = buildingLogic.getBuilding(msg.getCityId(), msg.getPosition());
		if(b != null){
			builder.setMessageCode(MessageCode.HAD_BUILDING_POSITION);
			httpMessageManager.send(builder);
			return;
		}
		
		City city = cityLogic.getCity( msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		httpMessageManager.changeCityResource(city);
		
		//检查建筑的数量上限问题
		List<Building> buildings = buildingLogic.getBuildings(city.getCityId(), msg.getBuildId());
		if(buildings != null){
			if(msg.getBuildId() == 1003){//仓库只能造5个
				if(buildings.size() >= 5){
					builder.setMessageCode(MessageCode.HAD_LIMIT_BUILD);
					httpMessageManager.send(builder);
					return;
				}
			}else if(msg.getBuildId() == 1008){
				if(buildings.size() >= 2){//空港最多造2个
					builder.setMessageCode(MessageCode.HAD_LIMIT_BUILD);
					httpMessageManager.send(builder);
					return;
				}
			}else{
				if(buildings.size() >= 1){//一般建筑只能造1个
					builder.setMessageCode(MessageCode.HAD_LIMIT_BUILD);
					httpMessageManager.send(builder);
					return;
				}
			}
		}
		
		ResBuild resBuild = configLogic.getResBuild(msg.getBuildId(), 1);
		ResBuildType resBuildType = configLogic.getResBuildType(resBuild.getBuildId());
		PlayerTech pt = playerLogic.getPlayerTech(player.getPlayerId());
		if(resBuild == null || resBuildType == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_BUILDING);
			httpMessageManager.send(builder);
			return;
		}
		if(resBuildType.getTechId() != 0 && !playerLogic.techAllow(pt, resBuildType.getTechId())){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_TECH);
			httpMessageManager.send(builder);
			return;
		}
		MessageCode code = cityLogic.checkResource(city, resBuild.getWood(), resBuild.getStone(), resBuild.getCrystal(), resBuild.getMetal(), resBuild.getFood());
		if(code != MessageCode.OK){
			builder.setMessageCode(code);
			httpMessageManager.send(builder);
			return;
		}
		
		if(msg.getIsOver()){
			//立即完成的情况，检查一下钻石是否足够
			int needDiamond = buildingLogic.getNeedDiamondSpeed(resBuild.getTime() * 1000);
			if(player.getDiamond() < needDiamond){
				builder.setMessageCode(MessageCode.NOT_ENOUGH_DIAMOND);
				httpMessageManager.send(builder);
				return;
			}
			//扣除钻石
			player.setDiamond(player.getDiamond() - needDiamond);
			player.setNeedSave(true);
		}
		
		cityLogic.removeCityResource(city, resBuild);
		
		rankLogic.updateRankWithIncrement(RankLogic.RankType.BUILDING_RANK, city.getPlayerId(),
				Double.valueOf((resBuild.getWood() + resBuild.getStone() + resBuild.getCrystal() + resBuild.getMetal() + resBuild.getFood())/100));
		
		Building building = buildingLogic.createBuilding(player.getPlayerId(), msg.getCityId(), msg.getBuildId(), msg.getPosition(), !msg.getIsOver());
		SCCreateBuilding.Builder scCreateBuilding = SCCreateBuilding.newBuilder();
		scCreateBuilding.setBuilding(buildingLogic.convert(city,building));
		builder.setScCreateBuilding(scCreateBuilding);
		
		builder.setNeedUpdateResource(true);
		httpMessageManager.send(builder);
	}
	
	/**
	 * 建筑升级
	 * @param msg
	 */
	public void buildLevelUp(CSBuildLevelUp msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Player player = httpMessageManager.getPlayer();

		Building oldBuilding = buildingLogic.getBuilding( msg.getCityId(), msg.getPosition());
		if(oldBuilding == null){
			log.error(String.format("not found building,playerId=%s,cityId=%s,position=%s", player.getPlayerId(),msg.getCityId(),msg.getPosition()));
			builder.setMessageCode(MessageCode.NOT_FOUND_BUILDING);
			httpMessageManager.send(builder);
			return;
		}
		City city = cityLogic.getCity(oldBuilding.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		httpMessageManager.changeCityResource(city);

		MessageCode mc = cityLogic.buildingLevelUp(player,oldBuilding,city,msg.getIsOver());
		builder.setMessageCode(mc);
		
		if(mc == MessageCode.OK){
			SCBuildLevelUp.Builder scBuildLevelUp = SCBuildLevelUp.newBuilder();
			scBuildLevelUp.setTime((int)(oldBuilding.getBuildTime().getTime()/1000));
			builder.setScBuildLevelUp(scBuildLevelUp);
		}
		builder.setNeedUpdateResource(true);
		httpMessageManager.send(builder);
	}

}
