package cn.litgame.wargame.server.logic.login;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class LoginHandler extends KHttpMessageHandler{
	
	@Resource(name = "loginProcess")
	private LoginProcess loginProcess;
	
	@Override
	public int handler(MessageBody messageBody) {
		switch(messageBody.getMessageType()){
		case MSG_ID_CHECK_VERSION:
			loginProcess.checkVersion(messageBody.getCsCheckVersion());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_LOGIN:
			loginProcess.login(messageBody.getCsLogin());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_PING:
			loginProcess.ping(messageBody.getCsPing());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_BIND_ACCOUNT:
			loginProcess.bindAccount(messageBody.getCsBindAccount());
			return KHttpMessageHandler.CATCH_HANDLER;
		case MSG_ID_CHECK_GAMECENTER:
			//loginProcess.checkGameCenter(messageContent.getCsCheckGameCenterId());
			return KHttpMessageHandler.CATCH_HANDLER;
		default:
			return KHttpMessageHandler.NOT_CATCH_HANDLER;
		}
	}
}
