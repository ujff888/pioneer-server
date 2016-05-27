package cn.litgame.wargame.core.logic.queue.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MarketType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameProtos.CityOrderInfo;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.MarketOrderInfo;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.auto.GameResProtos.ResourceType;
import cn.litgame.wargame.core.logic.MarketLogic;
import cn.litgame.wargame.core.logic.PlayerLogic;
import cn.litgame.wargame.core.logic.queue.GameActionEvent;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.GameAction;
import cn.litgame.wargame.core.model.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

@Service
public class MarketingGameAction extends GameActionEvent{
	@Resource(name = "marketLogic")
	private MarketLogic marketLogic;
	
	@Resource(name = "jedisStoragePool")
	private JedisPool jedisStoragePool;
	
	@Resource(name = "playerLogic")
	private PlayerLogic playerLogic;
	
	private final boolean REVERSE_SOURCE_PLAYER = true;
	
	@Override
	public GameActionType getGameActionType() {
		return GameActionType.MARKET;
	}

	@Override
	public void doLogic(GameAction gameAction, long nowTime) throws InvalidProtocolBufferException {
		if(gameAction.getActionState() == TransportStatus.TRANSIT_VALUE){
			shipLogic.useOverShip(transportTask.getSourcePlayerId(), transportTask.getShipCount());
			MarketOrderInfo order = transportTask.getOrder();
			int resourceCount = order.getCount();
			int resourcePrice = order.getPrice();
			
			//这里获取对方的订单信息，购买数量的最大值应该是对方挂单的记录，resourceCount重新赋值
			CityOrderInfo latestCityOrder = marketLogic.getOrderInfo(targetCity.getLandId(), targetCity.getCityId());
			CityOrderInfo.Builder newCityOrder = CityOrderInfo.newBuilder();
			newCityOrder.setCityId(latestCityOrder.getCityId());
			newCityOrder.setCityName(latestCityOrder.getCityName());
			newCityOrder.setDistance(latestCityOrder.getDistance());
			for(MarketOrderInfo moi : latestCityOrder.getMarketOrderInfoList()){
				if(moi.getResourceType() == order.getResourceType() && this.isMutualExclusive(moi.getMarketType(), order.getMarketType())){
					resourceCount = Math.min(moi.getCount(), resourceCount);
					MarketOrderInfo.Builder builder = MarketOrderInfo.newBuilder(moi);
					builder.setCount(moi.getCount() - resourceCount);
					newCityOrder.addMarketOrderInfo(builder);
				}else{
					newCityOrder.addMarketOrderInfo(moi);
				}
			}
			
			
			TransportTask.Builder newTask = TransportTask.newBuilder();
			
			newTask.setType(GameActionType.TRANSPORT);
			newTask.setShipCount(transportTask.getShipCount());
			
			int wood = order.getResourceType() == ResourceType.WOOD ? resourceCount : 0;
			int stone = order.getResourceType() == ResourceType.STONE ? resourceCount : 0;
			int crystal = order.getResourceType() == ResourceType.CRYSTAL ? resourceCount : 0;
			int metal = order.getResourceType() == ResourceType.METAL ? resourceCount : 0;
			int food = order.getResourceType() == ResourceType.FOOD ? resourceCount : 0;
			
			Player targetPlayer = playerLogic.getPlayer(targetCity.getPlayerId());
			
			if(order.getMarketType() == MarketType.MARKET_BUY){
				CityResource.Builder resource = CityResource.newBuilder();
				
				MessageCode mc = cityLogic.checkResource(targetCity, wood,stone,crystal,metal,food);
						
				if(mc != MessageCode.OK){
					gameActionLogic.createTransportShipTaskSkipLoading(newTask, targetCity, sourceCity, REVERSE_SOURCE_PLAYER);
					Player sourcePlayer = playerLogic.getPlayer(sourceCity.getPlayerId());
					playerLogic.updatePlayerGold(sourcePlayer.getPlayerId(), sourcePlayer.getGold() + resourcePrice*resourceCount);
					return;
				}
				

				//检查码头
				List<Building> wharfs = buildingLogic.getBuildings(targetCity.getCityId(), 1008);
				if(wharfs == null || wharfs.size() == 0){
					gameActionLogic.createTransportShipTaskSkipLoading(newTask, targetCity, sourceCity, REVERSE_SOURCE_PLAYER);
					Player sourcePlayer = playerLogic.getPlayer(sourceCity.getPlayerId());
					playerLogic.updatePlayerGold(sourcePlayer.getPlayerId(), sourcePlayer.getGold() + resourcePrice*resourceCount);
					return;
				}
				
				cityLogic.removeCityResource(targetCity, wood, stone, crystal, metal, food);
				
				marketLogic.updateCityOrderInfo(targetCity, newCityOrder.build());
				
				int wharfSpeed = 0;
				for(Building b : wharfs){
					wharfSpeed += configLogic.getResBuild(b.getBuildId(), b.getLevel()).getArg1();
				}
				
				resource.setWood(wood).setFood(food).setStone(stone).setCrystal(crystal).setMetal(metal);
				newTask.setResource(resource);
				
				long loadingTime = shipLogic.getResourceShipTime(resource.build(), 0, wharfSpeed);
				
				playerLogic.updatePlayerGold(targetPlayer.getPlayerId(), targetPlayer.getGold() + resourcePrice*resourceCount);
				
				gameActionLogic.createTransportShipTask(newTask, targetCity, sourceCity, loadingTime, REVERSE_SOURCE_PLAYER);
				gameActionLogic.delGameAction(gameAction.getActionId());
			}else if(order.getMarketType() == MarketType.MARKET_SELL){
				switch(order.getResourceType()){
				case FOOD:
					targetCity.setFood(targetCity.getFood() + resourceCount);
					break;
				case METAL:
					targetCity.setMetal(targetCity.getMetal() + resourceCount);
					break;
				case STONE:
					targetCity.setStone(targetCity.getStone() + resourceCount);
					break;
				case CRYSTAL:
					targetCity.setCrystal(targetCity.getCrystal() + resourceCount);
					break;
				case WOOD:
					targetCity.setWood(targetCity.getWood() + resourceCount);
					break;
				}
				
				marketLogic.updateCityOrderInfo(targetCity, newCityOrder.build());
				//TODO:更新对方的订单信息
				cityLogic.updateCity(targetCity);
				Player sourcePlayer = playerLogic.getPlayer(sourceCity.getPlayerId());
				playerLogic.updatePlayerGold(sourcePlayer.getPlayerId(), sourcePlayer.getGold() + resourcePrice*resourceCount);
				
				gameActionLogic.createTransportShipTaskSkipLoading(newTask, targetCity, sourceCity, REVERSE_SOURCE_PLAYER);
				gameActionLogic.delGameAction(gameAction.getActionId());
			}	
		}
	}
	
	private boolean isMutualExclusive(MarketType type1, MarketType type2){
		if(type1 == type2)
			return false;
		if(type1 == MarketType.MARKET_BUY && type2 == MarketType.MARKET_SELL)
			return true;
		if(type1 == MarketType.MARKET_SELL && type2 == MarketType.MARKET_BUY)
			return true;
		return false;
	}

}
