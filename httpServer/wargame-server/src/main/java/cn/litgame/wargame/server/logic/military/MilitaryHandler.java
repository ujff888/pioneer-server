package cn.litgame.wargame.server.logic.military;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;
@Service
public class MilitaryHandler extends KHttpMessageHandler {
	@Resource(name = "militaryProcess")
	private MilitaryProcess militaryProcess;
	
	@Override
	public int handler(MessageBody messageBody) {
		switch(messageBody.getMessageType()){
		case MSG_ID_MILITARY_INFO:
			militaryProcess.militaryInfo(messageBody.getCsMilitaryInfo());
			return CATCH_HANDLER;
		case MSG_ID_MILITARY_ACTION:
			militaryProcess.militaryAction(messageBody.getCsMilitaryAction());
			return CATCH_HANDLER;
		default:
			return NOT_CATCH_HANDLER;
		}
	}

}
