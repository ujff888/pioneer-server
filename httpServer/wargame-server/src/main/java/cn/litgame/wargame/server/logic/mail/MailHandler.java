package cn.litgame.wargame.server.logic.mail;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class MailHandler extends KHttpMessageHandler{

	@Resource(name = "mailProcess")
	private MailProcess mailProcess;
	
	@Override
	public boolean handle(MessageBody messageBody) {
		// TODO Auto-generated method stub
		return false;
	}

}
