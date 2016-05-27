package cn.litgame.wargame.server.logic.packItem;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.CSGetItemPack;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.SCGetItemPack;
import cn.litgame.wargame.core.logic.PackItemLogic;
import cn.litgame.wargame.core.model.PackItem;
import cn.litgame.wargame.server.logic.HttpMessageManager;
import cn.litgame.wargame.server.message.KHttpMessageProcess;

@Service
public class PackItemProcess extends KHttpMessageProcess  {

	public void getItemList(CSGetItemPack msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		
		List<PackItem> itemList=packItemLogic.getPackItemByPlayerId(httpMessageManager.getPlayerId());
		SCGetItemPack.Builder itemPackList=SCGetItemPack.newBuilder();
		
		if(itemList != null){
			for(PackItem packItem : itemList){
				GameProtos.PackItem.Builder packItemResponse = GameProtos.PackItem.newBuilder();
				packItemResponse.setItemAmount(packItem.getItemAmount());
				packItemResponse.setItemType(packItem.getItemType());
				itemPackList.addItemList(packItemResponse);
			}
		}
		
		builder.setScGetItemPack(itemPackList);
		httpMessageManager.send(builder);
	}
	

}
