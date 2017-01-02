package cn.litgame.wargame.server.logic.friend;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class FriendHandler extends KHttpMessageHandler{

	@Resource(name = "friendProcess")
	private FriendProcess friendProcess;
	
	@Override
	public boolean handle(MessageBody messageBody) {
		// TODO Auto-generated method stub
		return false;
	}

}
