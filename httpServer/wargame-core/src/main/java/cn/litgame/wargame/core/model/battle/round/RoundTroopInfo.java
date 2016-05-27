package cn.litgame.wargame.core.model.battle.round;

import java.io.Serializable;

public class RoundTroopInfo implements Serializable {
	private static final long serialVersionUID = -4597340370722513169L;
	private int troopId;
	private int count;
	private int lost;
	
	public RoundTroopInfo(){}
	
	public RoundTroopInfo(int troopId, int count, int lost){
		this.troopId = troopId;
		this.count = count;
		this.lost = lost;
	}
	
	public int getTroopId() {
		return troopId;
	}
	public void setTroopId(int troopId) {
		this.troopId = troopId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getLost() {
		return lost;
	}
	public void setLost(int lost) {
		this.lost = lost;
	}
	
	public RoundTroopInfo add(RoundTroopInfo r) {
		if(r.getTroopId() != this.troopId)
			throw new IllegalArgumentException("different troopId!");
		return new RoundTroopInfo(this.troopId, this.count+r.getCount(), this.lost+r.getLost());
	}
	
	@Override
	public String toString() {
		return "RoundTroopInfo [troopId=" + troopId + ", count=" + count + ", lost=" + lost + "]";
	}
}
