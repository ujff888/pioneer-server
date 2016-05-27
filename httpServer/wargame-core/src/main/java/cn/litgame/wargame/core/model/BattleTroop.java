package cn.litgame.wargame.core.model;

import cn.litgame.wargame.core.auto.GameResProtos.ResTroop;

public class BattleTroop {
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
