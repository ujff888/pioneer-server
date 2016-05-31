package cn.litgame.wargame.core.model.battle;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;

import java.util.ArrayList;
import java.util.List;

/**
 * 阵地位置的抽象，包括格子的数目和容量
 * 
 * @author 熊纪元
 *
 */
public class FieldPosition{
	//位置的类型
	private BattleFieldType type;
	private int capacity;
	private int count;
	//一个位置包含若干个格子
	private ArrayList<Slot> slots;

	public FieldPosition() {}

	public FieldPosition(BattleFieldType battleFieldType, int capacity, int count) {
		this.type = battleFieldType;
		this.capacity = capacity;
		this.count = count;
		slots = new ArrayList<>(count);
		for(int i=0;i<count;i++){
			slots.add(new Slot(i, capacity));
		}
	}

	public List<Slot> getSlotsWithTroop() {
		ArrayList<Slot> slots = new ArrayList<>();
		
		for(Slot slot : this.getSlots()){
			if(!slot.isEmpty())
				slots.add(slot);
		}
		return slots;
	}
	
	public BattleFieldType getType() {
		return type;
	}

	public void setType(BattleFieldType type) {
		this.type = type;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public ArrayList<Slot> getSlots() {
		return slots;
	}

	public void setSlots(ArrayList<Slot> slots) {
		this.slots = slots;
	}
	
	public Slot getSlot(int i){
		return this.slots.get(i);
	}

	@Override
	public String toString() {
		return "FieldPosition{" +
				"type=" + type +
				", capacity=" + capacity +
				", count=" + count +
				", slots=" + slots +
				'}';
	}
	
	public GameProtos.FieldPosition convertToProto() {
		GameProtos.FieldPosition.Builder builder = GameProtos.FieldPosition.newBuilder();
		builder.setCapacity(this.capacity);
		builder.setCount(this.count);
		builder.setType(this.type);
		for(Slot slot : this.slots){
			builder.addSlot(slot.convertToProto());
		}

		return builder.build();
	}
}
