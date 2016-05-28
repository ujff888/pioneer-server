package cn.litgame.wargame.core.model.battle.troop;

import java.io.Serializable;

import cn.litgame.wargame.core.auto.GameResProtos.ResTroop;

public class BattleTroop implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9153120937000864770L;
	private int count;
	private ResTroop resTroop;
	
	public BattleTroop(){}
	
	public BattleTroop(ResTroop rt, int i) {
		this.resTroop = rt;
		this.count = i;
	}

	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public ResTroop getResTroop() {
		return resTroop;
	}
	public void setResTroop(ResTroop resTroop) {
		this.resTroop = resTroop;
	}
	
	@Override
	public String toString() {
		return "BattleTroop [count=" + count + ", resTroop="
				+ resTroop + "]";
	}
}
