package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

public class Building {
	private long id;
	private int cityId;
	private int buildId;
	private int level;
	private boolean isBuilding;
	private Timestamp buildTime;//建造完成的时间
	private int position;//位置
	private int count;//一些建筑物需要的特殊配置
	private long playerId;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public boolean isBuilding() {
		return isBuilding;
	}
	public void setBuilding(boolean isBuilding) {
		this.isBuilding = isBuilding;
	}

	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	public int getBuildId() {
		return buildId;
	}
	public void setBuildId(int buildId) {
		this.buildId = buildId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public Timestamp getBuildTime() {
		return buildTime;
	}
	public void setBuildTime(Timestamp buildTime) {
		this.buildTime = buildTime;
	}
	
	@Override
	public String toString() {
		return "Building [id=" + id + ", cityId=" + cityId + ", buildId="
				+ buildId + ", level=" + level + ", isBuilding=" + isBuilding
				+ ", buildTime=" + buildTime + ", position=" + position
				+ ", count=" + count + ", playerId=" + playerId + "]";
	}
}
