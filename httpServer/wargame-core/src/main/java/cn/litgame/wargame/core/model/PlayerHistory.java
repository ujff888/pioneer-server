package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

/**
 * 记录玩家的一些统计信息
 * @author Administrator
 *
 */
public class PlayerHistory {
	private long playerId;
	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	
}
