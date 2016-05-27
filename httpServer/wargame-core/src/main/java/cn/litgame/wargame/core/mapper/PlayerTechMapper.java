package cn.litgame.wargame.core.mapper;

import cn.litgame.wargame.core.model.PlayerTech;

public interface PlayerTechMapper {
	public void createPlayerTech(PlayerTech playerTech);
	public void updatePlayerTech(PlayerTech playerTech);
	public void deletePlayerTech(long playerId);
	public PlayerTech getPlayerTech(long playerId);
	
}
