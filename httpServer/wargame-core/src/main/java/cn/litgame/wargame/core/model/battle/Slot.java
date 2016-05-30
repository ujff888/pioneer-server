package cn.litgame.wargame.core.model.battle;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.model.battle.unit.BattleUnit;

import java.io.Serializable;

/**
 * 阵型中具体的格子，用来容纳作战单位
 * 
 * @author 熊纪元
 *
 */
public class Slot implements Serializable{
	public static final int EMPTY_TROOP_ID = -1;
	
	private static final long serialVersionUID = -3388682106941570858L;
	private int num = -1;
	private int size = 0;
	private int capacity;
	private int resTroopId = EMPTY_TROOP_ID;
	
	public Slot(int num,int capacity) {
		this.num = num;
		this.capacity = capacity;
	}

	public Slot(GameProtos.Slot slot){
		this.num = slot.getSeqNo();
		this.size = slot.getSize();
		this.capacity = slot.getCapacity();
		this.resTroopId = slot.getResTroopId();
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public int getCapacity() {
		return capacity;
	}
	public int getResTroopId() {
		return resTroopId;
	}
	public void setResTroopId(int resTroopId) {
		this.resTroopId = resTroopId;
	}
	public void add(BattleUnit bu){
		if(this.isEmpty()){
			this.setResTroopId(bu.getTroopId());
		}
		this.size += bu.getSpace();
	}
	
	public void remove(BattleUnit bu){
		this.size -= bu.getSpace();
		if(this.isEmpty()){
			this.setResTroopId(EMPTY_TROOP_ID);
		}
	}
	
	public boolean isEmpty() {
		return size <= 0;
	}
	
	public boolean isFull() {
		return size >= capacity;
	}
	
	public int getFreeSpace() {
		return capacity - size;
	}

	@Override
	public String toString() {
		return "Slot [size=" + size + ", capacity=" + capacity + ", resTroopId=" + resTroopId + "]";
	}

	public GameProtos.Slot convertToProto() {
		GameProtos.Slot.Builder builder = GameProtos.Slot.newBuilder();
		builder.setCapacity(this.capacity);
		builder.setResTroopId(this.resTroopId);
		builder.setSize(this.size);
		builder.setSeqNo(this.num);

		return builder.build();
	}
}
