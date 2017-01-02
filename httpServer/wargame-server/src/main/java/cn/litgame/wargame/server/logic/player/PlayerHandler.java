package cn.litgame.wargame.server.logic.player;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class PlayerHandler extends KHttpMessageHandler{

	@Resource(name = "playerProcess")
	private PlayerProcess playerProcess;
	
	@Override
	public boolean handle(MessageBody mb) {
		switch(mb.getMessageType()){
		case MSG_ID_CREATE_PLAYER:
			playerProcess.createNewPlayer(mb.getCsCreatePlayer());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_FUNCTION_SWITCH:
			//playerProcess.functionSwith(messageContent.getCsSetCommonFlag());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_STUDY_TECH:
			playerProcess.studyTech(mb.getCsStudyTech());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_PAYMENT:
			playerProcess.payment(mb.getCsPayment());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SET_CAPITAL:
			playerProcess.setCapital(mb.getCsSetCapital());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_CHANGE_SYSTEM:
			playerProcess.changeSystem(mb.getCsChangeSystem());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SHOW_KING_INFO:
			playerProcess.showKingInfo(mb.getCsShowKingInfo());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_SHOW_RANK:
			playerProcess.showRank(mb.getCsShowRank());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_GM_COMMAND:
			playerProcess.gmCommand(mb.getCsGmCommand());
			return KHttpMessageHandler.CATCH_HANDLER;
		default:
			return KHttpMessageHandler.NOT_CATCH_HANDLER;
		}
	}
}
