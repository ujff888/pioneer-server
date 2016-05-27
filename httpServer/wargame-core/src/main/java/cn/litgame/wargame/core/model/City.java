package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

import cn.litgame.wargame.core.auto.GameResProtos.ResourceType;

public class City {
	private int cityId;
	private String cityName;
	private int landId;
	private int position;
	private long playerId;
	private int food;
	private int stone;
	private int crystal;
	private int metal;
	private int wood;
	private int level;
	private boolean isCapital;//是首都
	private Timestamp createCityTime;//创建城市的时间
	private double totalPerson;//总人口
	private int woodWorker;//伐木工人
	private int resourceWorker;//资源工人
	private int scientist;//科学家
	private Timestamp lastSetTime;//上一次的民意以及人口结算时间
	private Timestamp lastResourceSetTime;//上一次的资源结算时间
	private int cityStatus;//城市的状态
	
	private double waitGold;//计算人口增长的时候，金钱的差量，等待计算金钱的时候要算进去
	private int[] resource = {0,0,0,0,0};

	/**
	 * 获取某种类型的资源
	 * @param type
	 * @return
	 */
	public int getResource(ResourceType type){
		return resource[type.getNumber() -1];
	}
	
	/**
	 * 增加资源
	 * @param type
	 * @param count
	 */
	public void addResource(ResourceType type, int count){
		switch(type){
		case STONE:
			this.setStone(this.getStone() + count);
			break;
		case FOOD:
			this.setFood(this.getFood() + count);
			break;
		case WOOD:
			this.setWood(this.getWood() + count);
			break;
		case CRYSTAL:
			this.setCrystal(this.getCrystal() + count);
			break;
		case METAL:
			this.setMetal(this.getMetal() + count);
			break;
		}
	}
	
	/**
	 * 复写资源
	 * @param type
	 * @param count
	 */
	public void setResource(ResourceType type, int count){
		switch(type){
		case STONE:
			this.setStone(count);
			break;
		case FOOD:
			this.setFood(count);
			break;
		case WOOD:
			this.setWood(count);
			break;
		case CRYSTAL:
			this.setCrystal(count);
			break;
		case METAL:
			this.setMetal(count);
			break;
		}
	}
	
	public int getCityStatus() {
		return cityStatus;
	}
	public void setCityStatus(int cityStatus) {
		this.cityStatus = cityStatus;
	}
	public Timestamp getCreateCityTime() {
		return createCityTime;
	}
	public void setCreateCityTime(Timestamp createCityTime) {
		this.createCityTime = createCityTime;
	}
	public Timestamp getLastResourceSetTime() {
		return lastResourceSetTime;
	}
	public void setLastResourceSetTime(Timestamp lastResourceSetTime) {
		this.lastResourceSetTime = lastResourceSetTime;
	}
	public double getWaitGold() {
		return waitGold;
	}
	public void setWaitGold(double waitGold) {
		this.waitGold = waitGold;
	}
	public double getTotalPerson() {
		return totalPerson;
	}
	public void setTotalPerson(double totalPerson) {
		this.totalPerson = totalPerson;
	}
	public int getWoodWorker() {
		return woodWorker;
	}
	public void setWoodWorker(int woodWorker) {
		this.woodWorker = woodWorker;
	}
	public int getResourceWorker() {
		return resourceWorker;
	}
	public void setResourceWorker(int resourceWorker) {
		this.resourceWorker = resourceWorker;
	}
	public int getScientist() {
		return scientist;
	}
	public void setScientist(int scientist) {
		this.scientist = scientist;
	}
	public Timestamp getLastSetTime() {
		return lastSetTime;
	}
	public void setLastSetTime(Timestamp lastSetTime) {
		this.lastSetTime = lastSetTime;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	public int getLandId() {
		return landId;
	}
	public void setLandId(int landId) {
		this.landId = landId;
	}
	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	public int getFood() {
		return food;
	}
	public void setFood(int food) {
		this.resource[ResourceType.FOOD_VALUE -1] = food;
		this.food = food;
	}
	public int getStone() {
		return stone;
	}
	public void setStone(int stone) {
		this.resource[ResourceType.STONE_VALUE -1] = stone;
		this.stone = stone;
	}
	public int getCrystal() {
		return crystal;
	}
	public void setCrystal(int crystal) {
		this.resource[ResourceType.CRYSTAL_VALUE -1] = crystal;
		this.crystal = crystal;
	}
	public int getMetal() {
		return metal;
	}
	public void setMetal(int metal) {
		this.resource[ResourceType.METAL_VALUE -1] = metal;
		this.metal = metal;
	}
	public int getWood() {
		return wood;
	}
	public void setWood(int wood) {
		this.resource[ResourceType.WOOD_VALUE -1] = wood;
		this.wood = wood;
	}

	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public boolean isCapital() {
		return isCapital;
	}
	public void setCapital(boolean isCapital) {
		this.isCapital = isCapital;
	}
	
	@Override
	public String toString() {
		return "City [cityId=" + cityId + ", cityName=" + cityName
				+ ", landId=" + landId + ", position=" + position
				+ ", playerId=" + playerId + ", food=" + food + ", stone="
				+ stone + ", crystal=" + crystal + ", metal=" + metal
				+ ", wood=" + wood + ", level=" + level + ", isCapital="
				+ isCapital + ", createCityTime=" + createCityTime
				+ ", totalPerson=" + totalPerson + ", woodWorker=" + woodWorker
				+ ", resourceWorker=" + resourceWorker + ", scientist="
				+ scientist + ", lastSetTime=" + lastSetTime
				+ ", lastResourceSetTime=" + lastResourceSetTime
				+ ", cityStatus=" + cityStatus + ", waitGold=" + waitGold + "]";
	}
}
