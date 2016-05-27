package cn.litgame.wargame.core.model.battle.round;

import java.io.Serializable;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;

public class RoundTroopDetail implements Serializable {
	private static final long serialVersionUID = -5414600635951718659L;
	private int troopId;
	private BattleFieldType fieldType;
	private int count;
	private int lost;
	private double amountRemain;
	
	public RoundTroopDetail(int troopId, BattleFieldType type, int count, int lost, double amountRemain) {
		this.troopId = troopId;
		this.fieldType = type;
		this.count = count;
		this.lost = lost;
		this.amountRemain = amountRemain;
	}
	public int getTroopId() {
		return troopId;
	}
	public void setTroopId(int troopId) {
		this.troopId = troopId;
	}
	public BattleFieldType getFieldType() {
		return fieldType;
	}
	public void setFieldType(BattleFieldType fieldType) {
		this.fieldType = fieldType;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getLost() {
		return lost;
	}
	public void setLost(int lost) {
		this.lost = lost;
	}
	public double getAmountRemain() {
		return amountRemain;
	}
	public void setAmountRemain(double amountRemain) {
		this.amountRemain = amountRemain;
	}
	@Override
	public String toString() {
		return "RoundTroopDetail [troopId=" + troopId + ", fieldType=" + fieldType + ", count="
				+ count + ", lost=" + lost + ", amountRemain=" + amountRemain + "]";
	}
}
