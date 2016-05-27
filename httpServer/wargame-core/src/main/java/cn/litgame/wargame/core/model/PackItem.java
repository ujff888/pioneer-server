package cn.litgame.wargame.core.model;

import java.util.Date;

public class PackItem {
	
	private long itemId;
	private int itemType;
	private long playerId;
	private int itemAmount;
	
//	public PackItem(int itemType, long playerId, int itemAmount)
//	{
//		this.itemType=itemType;
//		this.itemAmount=itemAmount;
//		this.playerId=playerId;
//	}
	
	
	public long getItemId() {
		return itemId;
	}


	public void setItemId(long itemId) {
		this.itemId = itemId;
	}


	public int getItemType() {
		return itemType;
	}


	public void setItemType(int itemType) {
		this.itemType = itemType;
	}


	public long getPlayerId() {
		return playerId;
	}


	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}


	public int getItemAmount() {
		return itemAmount;
	}


	public void setItemAmount(int itemAmount) {
		this.itemAmount = itemAmount;
	}


	@Override
	public String toString() {
		return "Pack [id=" + itemId + ", itemType=" + itemType + ", itemAmount="
				+ itemAmount + ", playerId=" + playerId + "]";
	}

}
