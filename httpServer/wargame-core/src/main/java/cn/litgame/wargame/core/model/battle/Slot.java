package cn.litgame.wargame.core.model.battle;

import java.io.Serializable;

import cn.litgame.wargame.core.model.battle.unit.BattleUnit;

/**
 * 阵型中具体的格子，用来容纳作战单位
 * 
 * @author 熊纪元
 *
 */
public class Slot implements Serializable{
	private static final long serialVersionUID = -3388682106941570858L;
	private int size = 0;
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}

	public void add(BattleUnit bu){
		this.size += bu.getSpace();
	}
	
	public void remove(BattleUnit bu){
		this.size -= bu.getSpace();
	}
	
	@Override
	public String toString() {
		return "Slot [size=" + size + "]";
	}
}
