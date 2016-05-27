package cn.litgame.wargame.core.logic;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.auto.GameGlobalProtos.MarketType;
import cn.litgame.wargame.core.auto.GameProtos.CityOrderInfo;
import cn.litgame.wargame.core.auto.GameProtos.MarketOrderInfo;
import cn.litgame.wargame.core.auto.GameResProtos.ResourceType;
import cn.litgame.wargame.core.model.City;

import org.junit.Before;
import org.junit.Test;

public class MarketActionTest {
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	
	ConfigLogic configLogic = context.getBean(ConfigLogic.class);
	MarketLogic marketLogic = context.getBean(MarketLogic.class);
	CityLogic cityLogic = context.getBean(CityLogic.class);
	
	City targetCity;
	City sourceCity;
	CityOrderInfo cityOrder;
	MarketOrderInfo order;
	int resourceCount;
	
	//@Before
	public void before(){
		configLogic.loadConfig(this.getClass().getResource("/pb.bytes").getPath());
		
		targetCity = cityLogic.getCity(149);
		sourceCity = cityLogic.getCity(150);
		cityOrder = marketLogic.getOrderInfo(targetCity.getLandId(), targetCity.getCityId());
		
		MarketOrderInfo.Builder builder = MarketOrderInfo.newBuilder();
		builder.setCount(50);
		builder.setPrice(16);
		builder.setResourceType(ResourceType.STONE);
		builder.setMarketType(MarketType.MARKET_BUY);

		order = builder.build();
		
		System.out.println("order");
		System.out.println(order);
		
		resourceCount = order.getCount();
	}
	
	//@Test
	public void test(){
		
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
				System.out.println("in if");
				System.out.println(builder);
			}else{
				newCityOrder.addMarketOrderInfo(moi);
				System.out.println("in else");
				System.out.println(moi);
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
