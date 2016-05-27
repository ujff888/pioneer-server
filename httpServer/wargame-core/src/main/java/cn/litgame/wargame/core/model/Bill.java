package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

/**
 * 账单
 * @author Administrator
 *
 */
public class Bill {
	private long playerId;
	private String orderId;
	private Timestamp createTime;
	private int rmb;
	private String productId;
	private int diamond;
	
	public Bill(){}
	public Bill(long playerId,String orderId,int rmb,String productId,int diamond){
		this.playerId = playerId;
		this.orderId = orderId;
		this.rmb = rmb;
		this.productId = productId;
		this.diamond = diamond;
		this.createTime = new Timestamp(System.currentTimeMillis());
	}
	
	public int getDiamond() {
		return diamond;
	}
	public void setDiamond(int diamond) {
		this.diamond = diamond;
	}
	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public int getRmb() {
		return rmb;
	}
	public void setRmb(int rmb) {
		this.rmb = rmb;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	
	@Override
	public String toString() {
		return "Bill [playerId=" + playerId + ", orderId=" + orderId
				+ ", createTime=" + createTime + ", rmb=" + rmb
				+ ", productId=" + productId + ", diamond=" + diamond + "]";
	}
	
}
