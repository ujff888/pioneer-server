package cn.litgame.wargame.core.mapper;


import java.util.List;

import cn.litgame.wargame.core.model.MarketOrder;

public interface MarketMapper {
	
	/**
	 * 创建订单
	 * @param marketOrder
	 * @return
	 */
	public int createMarketOrder(MarketOrder marketOrder);
	
	public void delMarketOrder(long orderId);
	
	public int updateMarketOrder(MarketOrder marketOrder);
	
	public List<MarketOrder> getMarketOrders(int cityId);
	
	
}