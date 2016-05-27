package cn.litgame.wargame.server.logic.shop;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.logic.packItem.PackItemProcess;
import cn.litgame.wargame.server.message.KHttpMessageHandler;

@Service
public class ShopItemHandler extends KHttpMessageHandler {
	@Resource(name = "shopItemProcess")
	private ShopItemProcess shopItemProcess;
	
	@Override
	public int handler(MessageBody messageBody) 
	{
		switch(messageBody.getMessageType())
		{
		case MSG_ID_GET_SHOP_SHELF:
			shopItemProcess.getShopShelf(messageBody.getCsShopPack());
			   return KHttpMessageHandler.CATCH_HANDLER;
		    default:
			   return KHttpMessageHandler.NOT_CATCH_HANDLER;
		}
	}

}
