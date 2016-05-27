package cn.litgame.wargame.core.mapper;

import java.util.List;

import cn.litgame.wargame.core.model.PackItem;

public interface PackItemMapper 
{
	public int addPackItem(PackItem item);
	public int consumePackItem(int itemType);
	public PackItem getPackItem(long itemId);
	public List<PackItem> getPackItemByPlayerId(long playerId);
	//public int getItemAmountByType(int itemType, long playerId);
	public int removePackItem(long itemId);
}
