package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

public class PlayerTech {
	private long playerId;
	private int techPoint;
	private Timestamp lastFlushTime;
	private int flyProgress;//航空的进度
	private int economicProgress;//经济的进度
	private int scienceProgress;//科学的进度
	private int militaryProgress;//军事的进度
	private int flyLevel;
	private int economicLevel;
	private int militaryLevel;
	private int scienceLevel;
	
	//下面的字段是计算得来的
	private int totalTecher;
	private double techPointRate;
	
	
	
	public int getTotalTecher() {
		return totalTecher;
	}
	public void setTotalTecher(int totalTecher) {
		this.totalTecher = totalTecher;
	}
	public double getTechPointRate() {
		return techPointRate;
	}
	public void setTechPointRate(double techPointRate) {
		this.techPointRate = techPointRate;
	}
	public int getFlyLevel() {
		return flyLevel;
	}
	public void setFlyLevel(int flyLevel) {
		this.flyLevel = flyLevel;
	}
	public int getEconomicLevel() {
		return economicLevel;
	}
	public void setEconomicLevel(int economicLevel) {
		this.economicLevel = economicLevel;
	}
	public int getMilitaryLevel() {
		return militaryLevel;
	}
	public void setMilitaryLevel(int militaryLevel) {
		this.militaryLevel = militaryLevel;
	}
	public int getScienceLevel() {
		return scienceLevel;
	}
	public void setScienceLevel(int scienceLevel) {
		this.scienceLevel = scienceLevel;
	}
	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	public int getTechPoint() {
		return techPoint;
	}
	public void setTechPoint(int techPoint) {
		this.techPoint = techPoint;
	}
	public Timestamp getLastFlushTime() {
		return lastFlushTime;
	}
	public void setLastFlushTime(Timestamp lastFlushTime) {
		this.lastFlushTime = lastFlushTime;
	}
	public int getFlyProgress() {
		return flyProgress;
	}
	public void setFlyProgress(int flyProgress) {
		this.flyProgress = flyProgress;
	}
	public int getEconomicProgress() {
		return economicProgress;
	}
	public void setEconomicProgress(int economicProgress) {
		this.economicProgress = economicProgress;
	}
	public int getScienceProgress() {
		return scienceProgress;
	}
	public void setScienceProgress(int scienceProgress) {
		this.scienceProgress = scienceProgress;
	}
	public int getMilitaryProgress() {
		return militaryProgress;
	}
	public void setMilitaryProgress(int militaryProgress) {
		this.militaryProgress = militaryProgress;
	}
	@Override
	public String toString() {
		return "PlayerTech [playerId=" + playerId + ", techPoint=" + techPoint
				+ ", lastFlushTime=" + lastFlushTime + ", flyProgress="
				+ flyProgress + ", economicProgress=" + economicProgress
				+ ", scienceProgress=" + scienceProgress
				+ ", militaryProgress=" + militaryProgress + ", flyLevel="
				+ flyLevel + ", economicLevel=" + economicLevel
				+ ", militaryLevel=" + militaryLevel + ", scienceLevel="
				+ scienceLevel + ", totalTecher=" + totalTecher
				+ ", techPointRate=" + techPointRate + "]";
	}
	
}
