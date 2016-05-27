package cn.litgame.wargame.core.mapper;

import cn.litgame.wargame.core.model.Bill;

public interface BillMapper {

	public Bill getBill(String orderId);
	public int insert(Bill b);
}
