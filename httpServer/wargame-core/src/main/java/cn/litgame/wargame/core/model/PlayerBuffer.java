package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

public class PlayerBuffer {
	private long id;
	private long playerId;
	private int bufferId;
	private int count;
	private int sourceId;
	private Timestamp createTime;
	private Timestamp overTime;
	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	public int getBufferId() {
		return bufferId;
	}
	public void setBufferId(int bufferId) {
		this.bufferId = bufferId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getSourceId() {
		return sourceId;
	}
	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Timestamp getOverTime() {
		return overTime;
	}
	public void setOverTime(Timestamp overTime) {
		this.overTime = overTime;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "PlayerBuffer [id=" + id + ", playerId=" + playerId
				+ ", bufferId=" + bufferId + ", count=" + count + ", sourceId="
				+ sourceId + ", createTime=" + createTime + ", overTime="
				+ overTime + "]";
	}
}
