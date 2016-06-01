package cn.litgame.wargame.core.model.battle;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.BattleTroop;

/**
 * 阵型中具体的格子，用来容纳作战单位
 * 
 * @author 熊纪元
 *
 */
public class Slot{
	public static final int EMPTY_TROOP_ID = -1;
	
	private int num = -1;
	private int size = 0;
	private int capacity;
	private int resTroopId = EMPTY_TROOP_ID;
	private int originalHp;
	private int originalCount;
	private BattleFieldType battleFieldType;
	private long playerId;
	private int cityId;
	private int attack;
	private double percent;
	private int attack2;
	private double percent2;
	private int amount;
	private int hp;
	private int defense;
	private int space;
	
	public Slot(int num,int capacity) {
		this.num = num;
		this.capacity = capacity;
	}

	public Slot(GameProtos.Slot slot){
		this.num = slot.getSeqNo();
		this.size = slot.getSize();
		this.capacity = slot.getCapacity();
		this.resTroopId = slot.getResTroopId();
		this.originalHp = slot.getOriginalHp();
		this.originalCount = slot.getOrginalCount();
		this.battleFieldType = slot.getBattleFieldType();
		this.playerId = slot.getPlayerId();
		this.cityId = slot.getCityId();
		this.attack = slot.getAttack();
		this.percent = slot.getPercent();
		this.attack2 = slot.getAttack2();
		this.percent2 = slot.getPercent2();
		this.amount = slot.getAmount();
		this.hp = slot.getHp();
		this.defense = slot.getDefense();
		this.space = slot.getSpace();
	}

	public Slot(BattleTroop bt, int count, long playerId, int cityId) {
		this.resTroopId = bt.getResTroop().getId();
		this.percent = bt.getResTroop().getPercent();
		this.percent2 = bt.getResTroop().getPercent2();
		
		this.amount = bt.getResTroop().getAmount();
		this.attack = count*bt.getResTroop().getAttack();
		this.attack2 = count*bt.getResTroop().getAttack2();
		this.hp = count*bt.getResTroop().getHp();
		this.defense = count*bt.getResTroop().getDefense();
		this.space = count*bt.getResTroop().getSpace();

		this.originalHp = bt.getResTroop().getHp();
		this.originalCount = count;
		
		this.playerId = playerId;
		this.cityId = cityId;
	}
	
	public void clear() {
		this.size = 0;
		this.resTroopId = EMPTY_TROOP_ID;
	}
	
	public int getOriginalHp() {
		return originalHp;
	}

	public void setOriginalHp(int originalHp) {
		this.originalHp = originalHp;
	}

	public int getOriginalCount() {
		return originalCount;
	}

	public void setOriginalCount(int originalCount) {
		this.originalCount = originalCount;
	}

	public BattleFieldType getBattleFieldType() {
		return battleFieldType;
	}

	public void setBattleFieldType(BattleFieldType battleFieldType) {
		this.battleFieldType = battleFieldType;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public int getAttack2() {
		return attack2;
	}

	public void setAttack2(int attack2) {
		this.attack2 = attack2;
	}

	public double getPercent2() {
		return percent2;
	}

	public void setPercent2(double percent2) {
		this.percent2 = percent2;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public int getDefense() {
		return defense;
	}

	public void setDefense(int defense) {
		this.defense = defense;
	}

	public int getSpace() {
		return space;
	}

	public void setSpace(int space) {
		this.space = space;
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
//	public void add(BattleUnit bu){
//		if(this.isEmpty()){
//			this.setResTroopId(bu.getTroopId());
//		}
//		this.size += bu.getSpace();
//	}
	
	public Slot add(Slot slot){
		if(!this.isEmpty()){
			if(this.playerId != slot.getPlayerId() 
					|| this.cityId != slot.getCityId()
					|| this.getResTroopId() != slot.getResTroopId()){
				throw new IllegalArgumentException("不兼容的Slot不能相加！");
			}
		}
		
		this.originalCount += slot.originalCount;
		this.amount = (this.amount+slot.getAmount())/(this.getCount() + slot.getCount());
		
		return this;
	}
	
	public Slot subtract(Slot slot){
		return this;
	}

	public Slot subtract(int num) {
		int oldCount = this.originalCount;
		this.attack = this.attack/oldCount*(oldCount-num);
		this.attack2 = this.attack2/oldCount*(oldCount-num);
		this.defense = this.defense/oldCount*(oldCount-num);
		this.originalCount -= num;
		return this;
	}
	
//	public void remove(BattleUnit bu){
//		this.size -= bu.getSpace();
//		if(this.isEmpty()){
//			this.setResTroopId(EMPTY_TROOP_ID);
//		}
//	}
	
	public boolean isFireUnit(){
		return false;
	}
	
	public boolean isFortificationUnit(){
		return false;
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
	
	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	@Override
	public String toString() {
		return "Slot [num=" + num + ", size=" + size + ", capacity=" + capacity + ", resTroopId=" + resTroopId
				+ ", originalHp=" + originalHp + ", originalCount=" + originalCount + ", battleFieldType="
				+ battleFieldType + ", playerId=" + playerId + ", cityId=" + cityId + ", attack=" + attack
				+ ", percent=" + percent + ", attack2=" + attack2 + ", percent2=" + percent2 + ", amount=" + amount
				+ ", hp=" + hp + ", defense=" + defense + ", space=" + space + "]";
	}

	public GameProtos.Slot convertToProto() {
		GameProtos.Slot.Builder builder = GameProtos.Slot.newBuilder();
		builder.setCapacity(this.capacity);
		builder.setResTroopId(this.resTroopId);
		builder.setSize(this.size);
		builder.setSeqNo(this.num);
		if(!this.isEmpty()){
			builder.setOriginalHp(this.originalHp);
			builder.setOrginalCount(this.originalCount);
			builder.setBattleFieldType(this.battleFieldType);
			builder.setPlayerId(this.playerId);
			builder.setCityId(this.cityId);
			builder.setAttack(this.attack);
			builder.setPercent(this.percent);
			builder.setAttack2(this.attack2);
			builder.setPercent2(this.percent2);
			builder.setAmount(this.amount);
			builder.setDefense(this.defense);
			builder.setSpace(this.space);
		}
		
		return builder.build();
	}

	public int getCount() {
		if(this.isEmpty())
			return 0;
		return (hp+originalHp-1)/originalHp;
	}

	public boolean isFrom(Army army) {
		return this.playerId == army.getPlayerId() && this.cityId == army.getCityId();
	}

	public void takeDamage(int ad) {
		this.hp -= ad < defense ? 0 : ad-defense;
	}
}
