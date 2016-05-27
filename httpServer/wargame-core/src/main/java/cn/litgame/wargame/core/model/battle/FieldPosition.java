package cn.litgame.wargame.core.model.battle;

import java.io.Serializable;
import java.util.ArrayList;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;

/**
 * 阵地位置的抽象，包括格子的数目和容量
 * 
 * @author 熊纪元
 *
 */
public class FieldPosition implements Serializable{
	private static final long serialVersionUID = 6589758109408896764L;
	//位置的类型
	private BattleFieldType type;
	private int capacity;
	private int count;
	//一个位置包含若干个格子
	private ArrayList<Slot> slots;

	public FieldPosition(BattleFieldType battleFieldType, int capacity, int count) {
		this.type = battleFieldType;
		this.capacity = capacity;
		this.count = count;
		slots = new ArrayList<>(count);
		for(int i=0;i<count;i++){
			slots.add(new Slot());
		}
	}

	public BattleFieldType getType() {
		return type;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getCount() {
		return count;
	}

	public ArrayList<Slot> getSlots() {
		return slots;
	}

	@Override
	public String toString() {
		StringBuilder sb =  new StringBuilder("FieldPosition [type=" + type + ", capacity=" + capacity + ", count=" + count + ", slots=[");
		for(Slot s : slots){
			sb.append("size: " + s.getSize() + " ");
		}
		sb.append("]]");
		return sb.toString();
	}

}
