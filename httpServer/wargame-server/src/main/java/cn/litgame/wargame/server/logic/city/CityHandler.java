package cn.litgame.wargame.server.logic.city;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class CityHandler extends KHttpMessageHandler{

	@Resource(name = "cityProcess")
	private CityProcess cityProcess;
	
	public boolean handle(MessageBody messageBody) {
		switch(messageBody.getMessageType()){
		case MSG_ID_BUILD_LEVEL_UP:
			cityProcess.buildLevelUp(messageBody.getCsBuildLevelUp());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_CREATE_BUILDING:
			cityProcess.createBuilding(messageBody.getCsCreateBuilding());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SPEEDY_BUILDING:
			cityProcess.speedyBuilding(messageBody.getCsSpeedyBuilding());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_CHECK_BUILD:
			cityProcess.checkBuild(messageBody.getCsCheckBuild());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SET_PUB_LEVEL:
			cityProcess.setPubLevel(messageBody.getCsSetPubLevel());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SET_TECHER:
			cityProcess.setTecher(messageBody.getCsSetTecher());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SHOW_TECH_BUILD:
			cityProcess.showTechBuild(messageBody.getCsShowTechBuild());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SHOW_TECH_PROGRESS:
			cityProcess.showTechProgress(messageBody.getCsShowTechProgress());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_BUILD_SHOW:
			cityProcess.buildShow(messageBody.getCsShowBuild());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_CHANGE_CITY_NAME:
			cityProcess.changeCityName(messageBody.getCsChangeCityName());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_CREATE_CITY:
			cityProcess.createCity(messageBody.getCsCreateCity());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_MOVE_CITY:
			cityProcess.moveCity(messageBody.getCsMoveCity());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_BUY_PERSON:
			cityProcess.buyPerson(messageBody.getCsBuyPerson());
			return KHttpMessageHandler.CATCH_HANDLER;
		default:
			return KHttpMessageHandler.NOT_CATCH_HANDLER;
		}
	}

}
