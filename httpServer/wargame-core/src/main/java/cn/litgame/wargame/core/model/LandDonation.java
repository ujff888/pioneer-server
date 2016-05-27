package cn.litgame.wargame.core.model;

public class LandDonation {
	private int id;
	private int landId;
	private int cityId;
	private long playerId;
	private int woodDonationCount;
	private int resourceDonationCount;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public int getWoodDonationCount() {
		return woodDonationCount;
	}
	public void setWoodDonationCount(int woodDonationCount) {
		this.woodDonationCount = woodDonationCount;
	}
	public int getResourceDonationCount() {
		return resourceDonationCount;
	}
	public void setResourceDonationCount(int resourceDonationCount) {
		this.resourceDonationCount = resourceDonationCount;
	}
	
	@Override
	public String toString() {
		return "LandDonation [id=" + id + ", landId=" + landId + ", cityId="
				+ cityId + ", playerId=" + playerId + ", woodDonationCount="
				+ woodDonationCount + ", resourceDonationCount="
				+ resourceDonationCount + "]";
	}
	
}
