package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

/**
 * 浮岛
 * @author Administrator
 *
 */
public class Land {
	private int landId;
	private int woodLevel;
	private int woodExp;
	private int resourceLevel;
	private int resourceExp;
	private Timestamp woodTime;
	private Timestamp resourceTime;
	
	
	
	public Timestamp getWoodTime() {
		return woodTime;
	}
	public void setWoodTime(Timestamp woodTime) {
		this.woodTime = woodTime;
	}
	public Timestamp getResourceTime() {
		return resourceTime;
	}
	public void setResourceTime(Timestamp resourceTime) {
		this.resourceTime = resourceTime;
	}
	public int getLandId() {
		return landId;
	}
	public void setLandId(int landId) {
		this.landId = landId;
	}
	public int getWoodLevel() {
		return woodLevel;
	}
	public void setWoodLevel(int woodLevel) {
		this.woodLevel = woodLevel;
	}
	public int getWoodExp() {
		return woodExp;
	}
	public void setWoodExp(int woodExp) {
		this.woodExp = woodExp;
	}
	public int getResourceLevel() {
		return resourceLevel;
	}
	public void setResourceLevel(int resourceLevel) {
		this.resourceLevel = resourceLevel;
	}
	public int getResourceExp() {
		return resourceExp;
	}
	public void setResourceExp(int resourceExp) {
		this.resourceExp = resourceExp;
	}
	
	@Override
	public String toString() {
		return "Land [landId=" + landId + ", woodLevel=" + woodLevel
				+ ", woodExp=" + woodExp + ", resourceLevel=" + resourceLevel
				+ ", resourceExp=" + resourceExp + ", woodTime=" + woodTime
				+ ", resourceTime=" + resourceTime + "]";
	}
	
}
