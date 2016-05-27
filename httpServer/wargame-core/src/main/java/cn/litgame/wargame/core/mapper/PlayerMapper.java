package cn.litgame.wargame.core.mapper;

import java.util.List;

import cn.litgame.wargame.core.model.Player;

public interface PlayerMapper {
	public int insert(Player player);
	public int update(Player player);
	public int delete(long playerId);
	public Player select(long playerId);
	public List<Player> selectAll();
	public List<Player> selectByRange(int start, int length);
	public int count();
}
