package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

public class PlayerState {
	
	public PlayerState(){
		
	}
	
	public PlayerState(long playerId,int systemId){
		this.playerId = playerId;
		this.system = systemId;
	}
	
	private long playerId;
	private int shipCost;//舰船的军费
	private int landCost;//陆军的军费
	private int shipSpeed;//舰船的速度
	private int buildCost;//建造的费用
	private int addPublicOpinion;//民意
	private int addPopulationCount;//人口上限
	private int addPublicOpinionInCapital;//首都民意
	private int addPopulationCountInCapital;//首都人口上限
	private int addTechPoint;//科技点
	private int techCost;//科技费用
	private int function;//功能解锁
	private int system;//当前的政府体制
	private int targetSystemId;//如果处于变革中，存的是目标的政治体制
	private Timestamp overSystemTime;//结束政府体制变革的时间
	
	public int getTargetSystemId() {
		return targetSystemId;
	}

	public void setTargetSystemId(int targetSystemId) {
		this.targetSystemId = targetSystemId;
	}

	public Timestamp getOverSystemTime() {
		return overSystemTime;
	}

	public void setOverSystemTime(Timestamp overSystemTime) {
		this.overSystemTime = overSystemTime;
	}

	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	public int getShipCost() {
		return shipCost;
	}
	public void setShipCost(int shipCost) {
		this.shipCost = shipCost;
	}
	public int getLandCost() {
		return landCost;
	}
	public void setLandCost(int landCost) {
		this.landCost = landCost;
	}
	public int getShipSpeed() {
		return shipSpeed;
	}
	public void setShipSpeed(int shipSpeed) {
		this.shipSpeed = shipSpeed;
	}
	public int getBuildCost() {
		return buildCost;
	}
	public void setBuildCost(int buildCost) {
		this.buildCost = buildCost;
	}
	public int getAddPublicOpinion() {
		return addPublicOpinion;
	}
	public void setAddPublicOpinion(int addPublicOpinion) {
		this.addPublicOpinion = addPublicOpinion;
	}
	public int getAddPopulationCount() {
		return addPopulationCount;
	}
	public void setAddPopulationCount(int addPopulationCount) {
		this.addPopulationCount = addPopulationCount;
	}
	public int getAddPublicOpinionInCapital() {
		return addPublicOpinionInCapital;
	}
	public void setAddPublicOpinionInCapital(int addPublicOpinionInCapital) {
		this.addPublicOpinionInCapital = addPublicOpinionInCapital;
	}
	public int getAddPopulationCountInCapital() {
		return addPopulationCountInCapital;
	}
	public void setAddPopulationCountInCapital(int addPopulationCountInCapital) {
		this.addPopulationCountInCapital = addPopulationCountInCapital;
	}
	public int getAddTechPoint() {
		return addTechPoint;
	}
	public void setAddTechPoint(int addTechPoint) {
		this.addTechPoint = addTechPoint;
	}
	public int getTechCost() {
		return techCost;
	}
	public void setTechCost(int techCost) {
		this.techCost = techCost;
	}
	public int getFunction() {
		return function;
	}
	public void setFunction(int function) {
		this.function = function;
	}
	public int getSystem() {
		return system;
	}
	public void setSystem(int system) {
		this.system = system;
	}

	@Override
	public String toString() {
		return "PlayerState [playerId=" + playerId + ", shipCost=" + shipCost
				+ ", landCost=" + landCost + ", shipSpeed=" + shipSpeed
				+ ", buildCost=" + buildCost + ", addPublicOpinion="
				+ addPublicOpinion + ", addPopulationCount="
				+ addPopulationCount + ", addPublicOpinionInCapital="
				+ addPublicOpinionInCapital + ", addPopulationCountInCapital="
				+ addPopulationCountInCapital + ", addTechPoint="
				+ addTechPoint + ", techCost=" + techCost + ", function="
				+ function + ", system=" + system + ", targetSystemId="
				+ targetSystemId + ", overSystemTime=" + overSystemTime + "]";
	}
}
