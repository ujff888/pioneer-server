package cn.litgame.wargame.core.mapper;

import cn.litgame.wargame.core.model.PlayerHistory;

public interface PlayerHistoryMapper {
	public int insert(PlayerHistory playerHistory);
	public int update(PlayerHistory playerHistory);
	public int delete(long playerId);
	public PlayerHistory select(long playerId);
}
