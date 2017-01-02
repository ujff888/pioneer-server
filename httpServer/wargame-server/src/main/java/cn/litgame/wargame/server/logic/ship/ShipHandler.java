package cn.litgame.wargame.server.logic.ship;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class ShipHandler extends KHttpMessageHandler{

	@Resource(name = "shipProcess")
	private ShipProcess shipProcess;

	@Override
	public boolean handle(MessageBody messageBody) {
		switch(messageBody.getMessageType()){
		case MSG_ID_SHOW_BUY_SHIP:
			shipProcess.showBuyShip(messageBody.getCsShowBuyShip());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_BUY_SHIP:
			shipProcess.buyShip(messageBody.getCsBuyShip());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_START_TRANSPORT_TASK:
			shipProcess.startTransport(messageBody.getCsStartTransportTask());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_CANCEL_TRANSPORT_TASK:
			shipProcess.cancelTransport(messageBody.getCsCancelTransportTask());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_OVER_TRANSPORT_TASK:
			shipProcess.overTransport(messageBody.getCsOverTransportTask());
			return KHttpMessageHandler.CATCH_HANDLER;
		default:
			return KHttpMessageHandler.NOT_CATCH_HANDLER;
		}
	}
}
