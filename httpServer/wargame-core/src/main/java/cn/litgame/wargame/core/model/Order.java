package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

/**
 * @title 订单历史记录
 * 
 * @author wangshaojun
 * 
 * @version 0.00 2014-07-16 add
 * 
 */
public class Order {

	private Long playerId;// 玩家ID
	private String orderId;// 订单ID
	private int status;// 状态 1:成功 0:接受订单
	private Timestamp createTime;// 创建时间

	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "Order [playerId=" + playerId + ", orderId=" + orderId
				+ ", status=" + status + ", createTime=" + createTime + "]";
	}
}
