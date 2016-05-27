package cn.litgame.wargame.core.mapper;

import cn.litgame.wargame.core.model.PlayerState;

public interface PlayerStateMapper {

	public int createPlayerState(PlayerState playerState);
	public PlayerState getPlayerState(long playerId);
	public int updatePlayerState(PlayerState playerState);
	public int delPlayerState(long playerId);
	
}
