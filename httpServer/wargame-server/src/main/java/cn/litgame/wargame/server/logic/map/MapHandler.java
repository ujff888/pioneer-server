package cn.litgame.wargame.server.logic.map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class MapHandler extends KHttpMessageHandler {

	@Resource(name = "mapProcess")
	private MapProcess mapProcess;
	
	@Override
	public int handler(MessageBody messageBody) {
		switch(messageBody.getMessageType()){
		case MSG_ID_SHOW_WORLD:
			mapProcess.showWorld(messageBody.getCsShowWorld());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SHOW_LAND:
			mapProcess.showLand(messageBody.getCsShowLand());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SHOW_CITY:
			mapProcess.showCity(messageBody.getCsShowCity());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_LAND_DONATION:
			mapProcess.landDonation(messageBody.getCsLandDonation());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SHOW_LAND_RESOURCE:
			mapProcess.showLandResource(messageBody.getCsShowLandResource());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SET_WORKER:
			mapProcess.setWorker(messageBody.getCsSetWorker());
			return KHttpMessageHandler.CATCH_HANDLER;
		default:
			return KHttpMessageHandler.NOT_CATCH_HANDLER;
		}
	}

}
