package cn.litgame.wargame.server.logic.battle;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class BattleHandler  extends KHttpMessageHandler{

	@Resource(name = "battleProcess")
	private BattleProcess battleProcess;
	
	public int handler(MessageBody messageBody) {
		switch(messageBody.getMessageType()){
		case MSG_ID_PRODUCTION_TROOP:
			battleProcess.productionTroop(messageBody.getCsProductionTroop());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_CANCEL_PRODUCTION_TROOP:
			battleProcess.cancelProductionTroop(messageBody.getCsCancelProductionTroop());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_DISBAND_TROOP:
			battleProcess.disbandTroop(messageBody.getCsDisbandTroop());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_BATTLE_LIST:
			battleProcess.battleList(messageBody.getCsBattleList());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_ROUND_LIST:
			battleProcess.roundList(messageBody.getCsRoundList());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_ROUND_DETAIL:
			battleProcess.roundDetail(messageBody.getCsRoundDetail());
			return KHttpMessageHandler.CATCH_HANDLER;
		default:
			return KHttpMessageHandler.NOT_CATCH_HANDLER;
		}
	}

}
