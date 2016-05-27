package cn.litgame.wargame.core.model;

import java.sql.Timestamp;
import java.util.Arrays;

public class GameAction {
	private long actionId;
	private int actionType;
	private long sourcePlayerId;
	private long targetPlayerId;
	private int sourceCityId;
	private int targetCityId;
	private byte[] actionData;
	private Timestamp createTime;
	private Timestamp overTime;//定时器的触发时间，也就本任务状态的结束时间
	private int shipCount;
	private long loadingTime;//装载需要的时间,单位毫秒
	private int actionState;//动作的状态
	
	
	public int getActionState() {
		return actionState;
	}
	public void setActionState(int actionState) {
		this.actionState = actionState;
	}

	public long getLoadingTime() {
		return loadingTime;
	}
	public void setLoadingTime(long loadingTime) {
		this.loadingTime = loadingTime;
	}
	public int getShipCount() {
		return shipCount;
	}
	public void setShipCount(int shipCount) {
		this.shipCount = shipCount;
	}
	public long getActionId() {
		return actionId;
	}
	public void setActionId(long actionId) {
		this.actionId = actionId;
	}
	public int getActionType() {
		return actionType;
	}
	public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	public long getSourcePlayerId() {
		return sourcePlayerId;
	}
	public void setSourcePlayerId(long sourcePlayerId) {
		this.sourcePlayerId = sourcePlayerId;
	}
	public long getTargetPlayerId() {
		return targetPlayerId;
	}
	public void setTargetPlayerId(long targetPlayerId) {
		this.targetPlayerId = targetPlayerId;
	}
	public int getSourceCityId() {
		return sourceCityId;
	}
	public void setSourceCityId(int sourceCityId) {
		this.sourceCityId = sourceCityId;
	}
	public int getTargetCityId() {
		return targetCityId;
	}
	public void setTargetCityId(int targetCityId) {
		this.targetCityId = targetCityId;
	}
	public byte[] getActionData() {
		return actionData;
	}
	public void setActionData(byte[] actionData) {
		this.actionData = actionData;
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
	
	@Override
	public String toString() {
		return "GameAction [actionId=" + actionId + ", actionType="
				+ actionType + ", sourcePlayerId=" + sourcePlayerId
				+ ", targetPlayerId=" + targetPlayerId + ", sourceCityId="
				+ sourceCityId + ", targetCityId=" + targetCityId
				+ ", actionData=" + Arrays.toString(actionData)
				+ ", createTime=" + createTime + ", overTime=" + overTime
				+ ", shipCount=" + shipCount + ", loadingTime=" + loadingTime
				+ ", actionState=" + actionState + "]";
	}
}
