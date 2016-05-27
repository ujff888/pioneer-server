package cn.litgame.wargame.core.logic;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.mapper.PackItemMapper;
import cn.litgame.wargame.core.model.PackItem;

@Service
public class PackItemLogic {
	
	private final static Logger log=Logger.getLogger(PackItemLogic.class);
	
	@Resource(name = "packItemMapper")
	private PackItemMapper packItemMapper;
	
	public PackItem getPackItem(long itemId)
	{
		return packItemMapper.getPackItem(itemId);
	}
	
	public List<PackItem> getPackItemByPlayerId(long playerId)
	{
		return packItemMapper.getPackItemByPlayerId(playerId);
	}
	
	public Boolean ConsumeOrUsePackItem(int itemType)
	{
		//to do the item usage
		//to do: consume the packeItem logic
		// errorID errrorId = logic(itemid,itemAmount);
		//sql delete the itme and its amount
		//packItemMapper.consumePackItem(itemType);
		return true;
	}
	
	public Boolean ObtainItemIntoPack(PackItem item)
	{
		packItemMapper.addPackItem(item);
		return true;
	}
	
	public int GetItemAmount(int itemType, long playerId)
	{
		return 0;
		//return packItemMapper.getItemAmountByType(itemType,playerId);
	}
}
