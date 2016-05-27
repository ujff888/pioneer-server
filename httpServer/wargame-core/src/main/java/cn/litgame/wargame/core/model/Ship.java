package cn.litgame.wargame.core.model;

public class Ship {

	public Ship(){
		
	}
	public Ship(long playerId,int shipType,int count){
		this.playerId = playerId;
		this.shipType = shipType;
		this.count = count;
	}
	
	private long shipId;
	private long playerId;
	private int shipType;
	private int count;
	
	public long getShipId() {
		return shipId;
	}
	public void setShipId(long shipId) {
		this.shipId = shipId;
	}
	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	public int getShipType() {
		return shipType;
	}
	public void setShipType(int shipType) {
		this.shipType = shipType;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
	public String toString() {
		return "Ship [shipId=" + shipId + ", playerId=" + playerId
				+ ", shipType=" + shipType + ", count=" + count + "]";
	}
	
}
