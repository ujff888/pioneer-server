package cn.litgame.wargame.core.model;

/**
 * 市场订单
 * @author Administrator
 *
 */
public class MarketOrder {

	private long orderId;
	private long playerId;
	private int cityId;
	private int price;
	private int count;
	private int resourceType;
	private int gold;
	private int orderType;//0是买1是卖
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
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
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getResourceType() {
		return resourceType;
	}
	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}
	public int getGold() {
		return gold;
	}
	public void setGold(int gold) {
		this.gold = gold;
	}
	public int getOrderType() {
		return orderType;
	}
	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}
	
	@Override
	public String toString() {
		return "MarketOrder [orderId=" + orderId + ", playerId=" + playerId
				+ ", cityId=" + cityId + ", price=" + price + ", count="
				+ count + ", resourceType=" + resourceType + ", gold=" + gold
				+ ", orderType=" + orderType + "]";
	}
}
