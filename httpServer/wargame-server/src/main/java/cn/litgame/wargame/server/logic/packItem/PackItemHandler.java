package cn.litgame.wargame.server.logic.packItem;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class PackItemHandler extends KHttpMessageHandler {
	
	@Resource(name = "packItemProcess")
	private PackItemProcess packItemProcess;

	@Override
	public int handler(MessageBody messageBody) 
	{

		switch(messageBody.getMessageType())
		{
		case MSG_ID_GET_PLAYER_PACKITEM:
		   packItemProcess.getItemList(messageBody.getCsGetItemPack());
		   return KHttpMessageHandler.CATCH_HANDLER;
	    default:
		   return KHttpMessageHandler.NOT_CATCH_HANDLER;
		}
	}
}
