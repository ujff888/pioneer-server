package cn.litgame.wargame.core.mapper;

import cn.litgame.wargame.core.model.Account;

public interface AccountMapper {

	public Account getAccount(String account,int platformType);
	public int addAccount(Account account);
	public int delAccount(int id);
}
