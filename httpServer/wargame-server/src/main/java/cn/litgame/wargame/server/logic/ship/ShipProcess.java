package cn.litgame.wargame.server.logic.ship;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.core.auto.GameProtos.CSBuyShip;
import cn.litgame.wargame.core.auto.GameProtos.CSCancelTransportTask;
import cn.litgame.wargame.core.auto.GameProtos.CSOverTransportTask;
import cn.litgame.wargame.core.auto.GameProtos.CSShowBuyShip;
import cn.litgame.wargame.core.auto.GameProtos.CSStartTransportTask;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.SCBuyShip;
import cn.litgame.wargame.core.auto.GameProtos.SCCancelTransportTask;
import cn.litgame.wargame.core.auto.GameProtos.SCOverTransportTask;
import cn.litgame.wargame.core.auto.GameProtos.SCShowBuyShip;
import cn.litgame.wargame.core.auto.GameProtos.SCStartTransportTask;
import cn.litgame.wargame.core.auto.GameProtos.SCUpdatePlayerInfo;
import cn.litgame.wargame.core.auto.GameProtos.ShipType;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.logic.RankLogic;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.GameAction;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.core.model.Ship;
import cn.litgame.wargame.server.message.KHttpMessageProcess;

@Service
public class ShipProcess extends KHttpMessageProcess {
	
	public void startTransport(CSStartTransportTask msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		TransportTask task = msg.getTask();
		Player player = httpMessageManager.getPlayer();
		City sourceCity = cityLogic.getCity(task.getSourceCityId());
		City targetCity = cityLogic.getCity(task.getTargetCityId());
		if(sourceCity == null || targetCity == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
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
		
		CityResource resource = task.getResource();
		
		//检查运输船
		int needShipCount = shipLogic.getResourceShipCount(resource,0);
		int currentShipCount = shipLogic.getFreeShip(player.getPlayerId());
		if(currentShipCount < needShipCount){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_TRANSPORTATION);
			httpMessageManager.send(builder);
			return;
		}
		
		//移除资源
		cityLogic.removeCityResource(sourceCity, resource.getWood(),resource.getStone(), resource.getCrystal(), resource.getMetal(), resource.getFood());
		httpMessageManager.changeCityResource(sourceCity);
		
		long loadingTime = shipLogic.getResourceShipTime(resource,0, wharfSpeed);
		
		//todo:校验行动点
		//发起一个定时任务
		TransportTask.Builder result = TransportTask.newBuilder();
		result.setShipCount(needShipCount);
		result.setType(GameActionType.TRANSPORT);
		result.setResource(task.getResource());
		MessageCode mc = gameActionLogic.createTransportShipTask(result, sourceCity, targetCity, loadingTime);
				
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		SCStartTransportTask.Builder sc = SCStartTransportTask.newBuilder();
		sc.setTask(result);
		builder.setScStartTransportTask(sc);
		httpMessageManager.send(builder);
	}
	
	public void cancelTransport(CSCancelTransportTask msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		builder.setMessageType(MessageType.MSG_ID_CANCEL_TRANSPORT_TASK);
		SCCancelTransportTask.Builder scCancelTransportTask = SCCancelTransportTask.newBuilder();
		Player player = httpMessageManager.getPlayer();
		GameAction ga = gameActionLogic.getGameAction(msg.getTaskId());
		
		
		MessageCode mc = shipLogic.cancelTransportTask(player.getPlayerId(), msg.getTaskId());
		
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		if(ga.getActionType() == GameActionType.CREATE_CITY_VALUE){
			playerLogic.updatePlayerGold(player.getPlayerId(), player.getGold()+configLogic.getGlobalConfig().getCreateCityGold());
			player.setNeedSave(true);
		}
		City sourceCity = cityLogic.getCity(ga.getSourceCityId());
		
		httpMessageManager.changeCityResource(sourceCity);
		builder.setNeedUpdateResource(true);
		
		scCancelTransportTask.setTaskId(msg.getTaskId());
		builder.setMessageCode(mc);
		builder.setScCancelTransportTask(scCancelTransportTask);
		httpMessageManager.send(builder);
	}
	
	public void overTransport(CSOverTransportTask msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		builder.setMessageType(MessageType.MSG_ID_OVER_TRANSPORT_TASK);
		SCOverTransportTask.Builder scOverTransportTask = SCOverTransportTask.newBuilder();
		Player player = httpMessageManager.getPlayer();
		GameAction gameAction = gameActionLogic.getGameAction(msg.getTaskId());
		TransportTask task;
		try {
			 task = TransportTask.parseFrom(gameAction.getActionData());
		} catch (InvalidProtocolBufferException e) {
			builder.setMessageCode(MessageCode.ERR);
			httpMessageManager.send(builder);
			return;
		}
		
		int diamondCostPerMin = configLogic.getGlobalConfig().getShippingTimeDiamond();
		
		long nowTime = System.currentTimeMillis();
		int minuteNeed;
		if(gameAction.getActionState() == TransportStatus.LOADING_VALUE){
			long loadingStartTime = Long.valueOf(String.valueOf(task.getLoadingStartTime())) * 1000;
			long millisNeed = gameAction.getLoadingTime() + loadingStartTime - nowTime;
			minuteNeed = (int)(millisNeed/1000/60);
		}else if(gameAction.getActionState() == TransportStatus.TRANSIT_VALUE
				|| gameAction.getActionState() == TransportStatus.BACKING_VALUE){
			long millisNeed = gameAction.getOverTime().getTime() - nowTime;
			minuteNeed = (int)(millisNeed/1000/60);
		}else{
			builder.setMessageCode(MessageCode.ERR);
			httpMessageManager.send(builder);
			return;
		}
		int diamondCost = diamondCostPerMin * (minuteNeed > 0 ? minuteNeed : 0);
		
		if(diamondCost > player.getDiamond()){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_DIAMOND);
			httpMessageManager.send(builder);
			return;
		}
		
		MessageCode mc = shipLogic.overTransportTask(player.getPlayerId(), msg.getTaskId());

		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		
		player.setDiamond(player.getDiamond() - diamondCost);
		player.setNeedSave(true);
		builder.setNeedUpdateResource(true);
		
		scOverTransportTask.setTaskId(msg.getTaskId());
		builder.setMessageCode(mc);
		builder.setScOverTransportTask(scOverTransportTask);
		httpMessageManager.send(builder);
	}
	
	public void showBuyShip(CSShowBuyShip msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCShowBuyShip.Builder scShowBuyShip = SCShowBuyShip.newBuilder();
		List<Ship> ships = shipLogic.getShips(httpMessageManager.getPlayerId());
		int goldShip = 0,diamondShip = 0,friendShip = 0,systemShip = 0,loginShip = 0;
		
		for(Ship s : ships){
			switch(s.getShipType()){
			case ShipType.GOLD_SHIP_VALUE:
				goldShip = s.getCount();
				break;
			case ShipType.DIAMOND_SHIP_VALUE:
				diamondShip = s.getCount();
				break;
			case ShipType.FRIEND_SHIP_VALUE:
				friendShip = s.getCount();
				break;
			case ShipType.LOGIN_SHIP_VALUE:
				loginShip = s.getCount();
				break;
			case ShipType.SYSTEM_SHIP_VALUE:
				systemShip = s.getCount();
				break;
			}
		}
		scShowBuyShip.setGoldShipCount(goldShip);
		scShowBuyShip.setDiamondShipCount(diamondShip);
		scShowBuyShip.setFriendShipCount(friendShip);
		scShowBuyShip.setSystemShipCount(systemShip);
		scShowBuyShip.setLoginShipCount(loginShip);
		scShowBuyShip.setGoldPrice(shipLogic.getGoldShipPrice(goldShip));
		scShowBuyShip.setDiamondPrice(shipLogic.getDiamondShipPrice(diamondShip));
		builder.setScShowBuyShip(scShowBuyShip);
		httpMessageManager.send(builder);
	}
	
	public void buyShip(CSBuyShip msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Ship ship = shipLogic.getShipByType(httpMessageManager.getPlayerId(), msg.getShipType());
		int count = ship == null ? 0 : ship.getCount();
		int price = 0;
		int afterPrice = 0;
		Player player = httpMessageManager.getPlayer();
		if(msg.getShipType() == ShipType.GOLD_SHIP){
			price = shipLogic.getGoldShipPrice(count);
			if(price > player.getGold()){
				builder.setMessageCode(MessageCode.NOT_ENOUGH_GOLD);
				httpMessageManager.send(builder);
				return;
			}
			if(count >= configLogic.getGlobalConfig().getGoldMaxTransportNum()){
				builder.setMessageCode(MessageCode.HAD_LIMIT_COUNT);
				httpMessageManager.send(builder);
				return;
			}
			afterPrice = shipLogic.getGoldShipPrice(count + 1);
			//player.setGold(player.getGold() - price);
			playerLogic.updatePlayerGold(player.getPlayerId(), player.getGold() - price);
			rankLogic.updateRankWithIncrement(RankLogic.RankType.TOTAL_RANK, player.getPlayerId(), Double.valueOf(price/400));
			
		}else if(msg.getShipType() == ShipType.DIAMOND_SHIP){
			price = shipLogic.getDiamondShipPrice(count);
			if(price > player.getDiamond()){
				builder.setMessageCode(MessageCode.NOT_ENOUGH_DIAMOND);
				httpMessageManager.send(builder);
				return;
			}
			if(count >= configLogic.getGlobalConfig().getDiamondMaxTransportNum()){
				builder.setMessageCode(MessageCode.HAD_LIMIT_COUNT);
				httpMessageManager.send(builder);
				return;
			}
			afterPrice = shipLogic.getDiamondShipPrice(count + 1);
			player.setDiamond(player.getDiamond() - price);
		}
		playerLogic.updatePlayer(player);
		Ship s = shipLogic.addShip(player.getPlayerId(), msg.getShipType(), 1);
		SCBuyShip.Builder scBuyShip = SCBuyShip.newBuilder();
		scBuyShip.setShipCount(s.getCount());
		scBuyShip.setShipType(msg.getShipType());
		scBuyShip.setPrice(afterPrice);
		builder.setScBuyShip(scBuyShip);
		
		SCUpdatePlayerInfo.Builder scUpdatePlayerInfo = SCUpdatePlayerInfo.newBuilder();
		scUpdatePlayerInfo.setGold(player.getGold());
		scUpdatePlayerInfo.setDiamond(player.getDiamond());
		builder.setScUpdatePlayerInfo(scUpdatePlayerInfo);
		httpMessageManager.send(builder);
	}
}
