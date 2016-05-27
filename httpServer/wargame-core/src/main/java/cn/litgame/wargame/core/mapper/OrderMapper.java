package cn.litgame.wargame.core.mapper;

import java.util.List;

import cn.litgame.wargame.core.model.Order;

/**
 * @title 历史订单
 * 
 * @author wangshaojun 
 *
 * @version 0.00 2014-07-16 add
 */
public interface OrderMapper {
	
	public List<Order> getOrderHistoryList(long playerId);
	public int insert(Order orderHistory);
	public int update(Order orderHistory);
	public Order select(long playerId,String orderId);
}
