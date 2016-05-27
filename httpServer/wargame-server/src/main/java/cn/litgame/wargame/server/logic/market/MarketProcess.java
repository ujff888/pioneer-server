package cn.litgame.wargame.server.logic.market;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MarketType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameProtos.CSCreateCityOrderInfo;
import cn.litgame.wargame.core.auto.GameProtos.CSDeal;
import cn.litgame.wargame.core.auto.GameProtos.CSQueryMarketOrder;
import cn.litgame.wargame.core.auto.GameProtos.CSShowMyOrderInfo;
import cn.litgame.wargame.core.auto.GameProtos.CityOrderInfo;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.MarketOrderInfo;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.SCCreateCityOrderInfo;
import cn.litgame.wargame.core.auto.GameProtos.SCDeal;
import cn.litgame.wargame.core.auto.GameProtos.SCQueryMarketOrder;
import cn.litgame.wargame.core.auto.GameProtos.SCShowMyOrderInfo;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.auto.GameResProtos.ResBuild;
import cn.litgame.wargame.core.auto.GameResProtos.ResourceType;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.server.message.KHttpMessageProcess;

@Service
public class MarketProcess extends KHttpMessageProcess  {

	public void deal(CSDeal msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCDeal.Builder scDeal = SCDeal.newBuilder();
		
		Player player = httpMessageManager.getPlayer();
		if(player == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_PLAYER);
			httpMessageManager.send(builder);
			return;
		}
		
		City targetCity = cityLogic.getCity(msg.getCityId());
		City currentCity = cityLogic.getCity(msg.getCurrentCityId());
		
		if(targetCity == null || currentCity == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		MarketOrderInfo orderInfo = msg.getMarketOrderInfo();

		CityResource.Builder resource = CityResource.newBuilder();
		//检查运输船
		int needShipCount = shipLogic.getResourceShipCount(resource.build(), orderInfo.getCount());
		int currentShipCount = shipLogic.getFreeShip(player.getPlayerId());
		if(currentShipCount < needShipCount){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_TRANSPORTATION);
			httpMessageManager.send(builder);
			return;
		}
		
		//检查码头
		List<Building> wharfs = buildingLogic.getBuildings(currentCity.getCityId(), 1008);
		if(wharfs == null || wharfs.size() == 0){
			builder.setMessageCode(MessageCode.NOT_FOUND_WHARF);
			httpMessageManager.send(builder);
			return;
		}
		
		int resourceCount = orderInfo.getCount();
		int resourcePrice = orderInfo.getPrice();
		
		//购买的话直接发船，进入定时器
		if(orderInfo.getMarketType() == MarketType.MARKET_BUY){
			TransportTask.Builder task = TransportTask.newBuilder();
			task.setType(GameActionType.MARKET);
			task.setShipCount(needShipCount);
			task.setOrder(orderInfo);
			
			gameActionLogic.createTransportShipTaskSkipLoading(task, currentCity, targetCity, false);
			playerLogic.updatePlayerGold(player.getPlayerId(), player.getGold() - resourceCount*resourcePrice);
			player.setNeedSave(true);
		}
		//出售的话先装载，再发货
		else if(orderInfo.getMarketType() == MarketType.MARKET_SELL){
			builder.setNeedUpdateResource(true);
			
			TransportTask.Builder task = TransportTask.newBuilder();
			task.setCreateTime((int) (System.currentTimeMillis()/1000));
			task.setType(GameActionType.MARKET);
			task.setShipCount(needShipCount);
			task.setOrder(orderInfo);
			
			int wood = orderInfo.getResourceType() == ResourceType.WOOD ? resourceCount : 0;
			int stone = orderInfo.getResourceType() == ResourceType.STONE ? resourceCount : 0;
			int crystal = orderInfo.getResourceType() == ResourceType.CRYSTAL ? resourceCount : 0;
			int metal = orderInfo.getResourceType() == ResourceType.METAL ? resourceCount : 0;
			int food = orderInfo.getResourceType() == ResourceType.FOOD ? resourceCount : 0;
			
			MessageCode mc = cityLogic.checkResource(targetCity, wood,stone,crystal,metal,food);
			
			if(mc != MessageCode.OK){
				builder.setMessageCode(mc);
				httpMessageManager.send(builder);
				return;
			}
			
			boolean notEnoughResourceToSell = false;
			
			switch(orderInfo.getResourceType()){
			case FOOD:
				notEnoughResourceToSell = targetCity.getFood() < resourceCount;
				if(!notEnoughResourceToSell){
					resource.setFood(resourceCount);
				}
				break;
			case METAL:
				notEnoughResourceToSell = targetCity.getMetal() < resourceCount;
				if(!notEnoughResourceToSell){
					resource.setMetal(resourceCount);
				}
				break;
			case STONE:
				notEnoughResourceToSell = targetCity.getStone() < resourceCount;
				if(!notEnoughResourceToSell){
					resource.setStone(resourceCount);
				}
				break;
			case CRYSTAL:
				notEnoughResourceToSell = targetCity.getCrystal() < resourceCount;
				if(!notEnoughResourceToSell){
					resource.setCrystal(resourceCount);
				}
				break;
			case WOOD:
				notEnoughResourceToSell = targetCity.getWood() < resourceCount;
				if(!notEnoughResourceToSell){
					resource.setWood(resourceCount);
				}
				break;
			}
			
			task.setResource(resource);
			int wharfSpeed = 0;
			for(Building b : wharfs){
				wharfSpeed += configLogic.getResBuild(b.getBuildId(), b.getLevel()).getArg1();
			}
			long loadingTime = shipLogic.getResourceShipTime(resource.build(), 0, wharfSpeed);
			gameActionLogic.createTransportShipTask(task, currentCity, targetCity, loadingTime);
			httpMessageManager.changeCityResource(currentCity);
			
		}else if(orderInfo.getMarketType() == MarketType.MARKET_FRIEND){
			
		}
		builder.setScDeal(scDeal);
		httpMessageManager.send(builder);
	}
	
	public void queryMarketOrder(CSQueryMarketOrder msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		List<Building> markets = buildingLogic.getBuildings(city.getCityId(), 1013);
		if(markets == null || markets.size() == 0){
			builder.setMessageCode(MessageCode.NOT_FOUND_MARKET);
			httpMessageManager.send(builder);
			return;
		}
		Building market = markets.get(0);
		int distance = (market.getLevel() - 1)/2 + 1;
		List<CityOrderInfo> result = marketLogic.showCityOrderInfo(distance, city.getLandId(),msg.getCityId());
		SCQueryMarketOrder.Builder scQueryMarketOrder = SCQueryMarketOrder.newBuilder();
		scQueryMarketOrder.addAllCityOrderInfo(result);
		builder.setScQueryMarketOrder(scQueryMarketOrder);
		httpMessageManager.send(builder);
	}

	/**
	 * 更新我的订单信息
	 * @param msg
	 */
	public void createCityOrder(CSCreateCityOrderInfo msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Player player = httpMessageManager.getPlayer();
		CityOrderInfo orderInfo = msg.getCityOrderInfo();
		
		City city = cityLogic.getCity(orderInfo.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		CityOrderInfo oldOrderInfo = marketLogic.getOrderInfo(city.getLandId(), city.getCityId());
		
		List<Building> markets = buildingLogic.getBuildings(orderInfo.getCityId(), 1013);
		if(markets == null || markets.size() == 0){
			builder.setMessageCode(MessageCode.NOT_FOUND_MARKET);
			httpMessageManager.send(builder);
			return;
		}
		Building market = markets.get(0);
		ResBuild resBuild = configLogic.getResBuild(market.getBuildId(), market.getLevel());
		int totalCount = 0;
		int totalGold = 0;
		for(MarketOrderInfo m : orderInfo.getMarketOrderInfoList()){
			if(m.getMarketType() == MarketType.MARKET_SELL){
				totalCount += m.getCount();
			}else if(m.getMarketType() == MarketType.MARKET_BUY){
				totalGold += m.getCount() * m.getPrice();
			}
			//价格的校验
			if(m.getPrice()< configLogic.getGlobalConfig().getResourcePriceMin()
					|| m.getPrice() > configLogic.getGlobalConfig().getResourcePriceMax()){
				builder.setMessageCode(MessageCode.HAD_LIMIT_PRICE);
				httpMessageManager.send(builder);
				return;
			}
		}
		
		int oldTotalGold = 0;
		if(oldOrderInfo != null){
			for(MarketOrderInfo m : oldOrderInfo.getMarketOrderInfoList()){
				if(m.getMarketType() == MarketType.MARKET_BUY){
					oldTotalGold += m.getCount() * m.getPrice();
				}
			}
		}
		
		
		if(totalCount > resBuild.getArg1()){
			builder.setMessageCode(MessageCode.HAD_LIMIT_COUNT);
			httpMessageManager.send(builder);
			return;
		}
		if(totalGold  - oldTotalGold > player.getGold()){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_GOLD);
			httpMessageManager.send(builder);
			return;
		}

		httpMessageManager.changeCityResource(city);
		marketLogic.saveOrderInfo(player,city, orderInfo,oldOrderInfo,false);
		builder.setScCreateCityOrderInfo(SCCreateCityOrderInfo.newBuilder());
		httpMessageManager.send(builder);
	}
	
	public void showMyOrder(CSShowMyOrderInfo msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		CityOrderInfo info = marketLogic.getOrderInfo(city.getLandId(), city.getCityId());
		SCShowMyOrderInfo.Builder scShowMyOrderInfo = SCShowMyOrderInfo.newBuilder();
		if(info != null){
			scShowMyOrderInfo.setCityOrderInfo(info);
		}
		builder.setScShowMyOrderInfo(scShowMyOrderInfo);
		httpMessageManager.send(builder);
	}
}
