package cn.litgame.wargame.core.model;

import java.util.Arrays;

/**
 * todo：这里要增加部队的当前城市，与部队的出发城市
 * @author Administrator
 *
 */
public class Troop {
	private long troopId;
	private long playerId;
	private int cityId;
	private int troopType;
	private int troopResId;
	private int count;
	public long getTroopId() {
		return troopId;
	}
	public void setTroopId(long troopId) {
		this.troopId = troopId;
	}
	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	public int getTroopType() {
		return troopType;
	}
	public void setTroopType(int troopType) {
		this.troopType = troopType;
	}

	public int getTroopResId() {
		return troopResId;
	}
	public void setTroopResId(int troopResId) {
		this.troopResId = troopResId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
	public String toString() {
		return "Troop [troopId=" + troopId + ", playerId=" + playerId
				+ ", cityId=" + cityId + ", troopType=" + troopType
				+ ", troopResId=" + troopResId + ", count=" + count + "]";
	}
}
