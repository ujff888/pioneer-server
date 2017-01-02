package cn.litgame.wargame.server.logic.market;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class MarketHandler extends KHttpMessageHandler {
	
	@Resource(name = "marketProcess")
	private MarketProcess marketProcess;
	
	@Override
	public boolean handle(MessageBody messageBody) {
		switch(messageBody.getMessageType()){
		case MSG_ID_QUERY_MARKET_ORDER:
			marketProcess.queryMarketOrder(messageBody.getCsQueryMarketOrder());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SHOW_MY_ORDER:
			marketProcess.showMyOrder(messageBody.getCsShowMyOrderInfo());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_CREATE_CITY_ORDER:
			marketProcess.createCityOrder(messageBody.getCsCreateCityOrderInfo());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_DEAL:
			marketProcess.deal(messageBody.getCsDeal());
			return KHttpMessageHandler.CATCH_HANDLER;
		default:
			return KHttpMessageHandler.NOT_CATCH_HANDLER;
		}
	}
}
