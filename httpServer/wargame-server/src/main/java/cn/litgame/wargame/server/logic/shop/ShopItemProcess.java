package cn.litgame.wargame.server.logic.shop;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos.CSGetShopShelf;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.SCGetShopItem;
import cn.litgame.wargame.core.auto.GameResProtos.ResShopItem;
import cn.litgame.wargame.core.auto.GameResProtos.ShopShelfType;
import cn.litgame.wargame.server.message.KHttpMessageProcess;
@Service
public class ShopItemProcess extends KHttpMessageProcess {

	public void getShopShelf(CSGetShopShelf msg)
	{
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();

		int type=msg.getShopType();
		ArrayList<ResShopItem> shopItemList=configLogic.getResShopByType(ShopShelfType.valueOf(msg.getShopType()));
		if(shopItemList.size()==0)
		{
			System.out.println("No shopping item to sell!");
			return;
		}
		SCGetShopItem.Builder shopItemListSendOut=SCGetShopItem.newBuilder();
		for(ResShopItem shopItem:shopItemList)
		{
			cn.litgame.wargame.core.auto.GameProtos.ShopItem.Builder shopItemResponse=cn.litgame.wargame.core.auto.GameProtos.ShopItem.newBuilder();
			shopItemResponse.setItemPrice(shopItem.getItemPrice());
			shopItemResponse.setItemType(shopItem.getItemType());
			shopItemListSendOut.addItemList(shopItemResponse);
		}
		builder.setScShopPack(shopItemListSendOut);
		httpMessageManager.send(builder);

	}

}
