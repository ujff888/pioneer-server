package cn.litgame.wargame.core.model.battle.unit;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.battle.Army;
import cn.litgame.wargame.core.model.battle.Damage;
import cn.litgame.wargame.core.model.battle.FieldPosition;
import cn.litgame.wargame.core.model.battle.Slot;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 细化的作战单位，是所有具体作战单位的基类
 * TODO：1.这个是具体的作战单位，为什么还要存cityId和playerId，这样不是重复的好多么，1万个单位就1万个cityId
 * 		这两个值抽出来，放到他的父级对象上，军队的对象上有这2个值就够了
 * 2.这里做个优化的工作，因为如果一场战斗里如果参战部队是10000，那么双方的部队加起来就是20000，也就是说，一场战斗就要实例化2万个对象
 * 	如果服务器里正在进行100场战斗，那就是200万个对象，这一个对象里至少15个int，15 * 4 = 60 ，60byte * 200万 = 120，000，000byte,也就是说120m。内存消耗太大了，要是1000场战斗，那就是1G的内存占用
 * 解决方案为：战斗中，每个格子只能上场一种单位，每个格子把士兵的血量、弹药 * 数量，当作一个整体来看，然后根据血量掉的百分比，决定死多少人，填充人数的时候，也按照百分比的方式加入到当前的格子内。
 * 同理，按照这个算法的话，那么城墙也就只能是按照格子去计算了，不能按照一个整体去计算了
 * @author 熊纪元
 *
 */
public abstract class BattleUnit implements Serializable{
	private static final long serialVersionUID = 5530170536339482598L;
	
	private static final Logger log = Logger.getLogger(BattleUnit.class);
	protected int originalHp;

	protected int originalCount;

	protected BattleFieldType battleFieldType;
	protected int slotNum;
	
	protected long playerId;
	protected int cityId;
	
	protected int troopId;
	protected int attack;
	protected double percent;
	protected int attack2;
	protected double percent2;
	protected int amount;
	protected int hp;
	protected int defense;
	protected int space;
	
	public BattleUnit(Building fort){
		this.playerId = fort.getPlayerId();
		this.cityId = fort.getCityId();
		this.originalHp = 0;
	}

	public BattleUnit(GameProtos.BattleUnit unit){
		this.originalHp = unit.getOriginalHp();
		this.originalCount = unit.getOrginalCount();
		this.battleFieldType = unit.getBattleFieldType();
		this.slotNum = unit.getSlotNum();
		this.playerId = unit.getPlayerId();
		this.cityId = unit.getCityId();
		this.troopId = unit.getTroopId();
		this.attack = unit.getAttack();
		this.percent = unit.getPercent();
		this.attack2 = unit.getAttack2();
		this.percent2 = unit.getPercent2();
		this.amount = unit.getAmount();
		this.hp = unit.getHp();
		this.defense = unit.getDefense();
		this.space = unit.getSpace();
	}
	
	public BattleUnit(BattleTroop bt, int count, long playerId, int cityId) {
		this.troopId = bt.getResTroop().getId();
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
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public int getAttack() {
		return this.attack;
	}

	public BattleFieldType getBattleFieldType() {
		return battleFieldType;
	}

	public void setBattleFieldType(BattleFieldType battleFieldType) {
		this.battleFieldType = battleFieldType;
	}

	public int getSlotNum() {
		return slotNum;
	}

	public void setSlotNum(int slotNo) {
		this.slotNum = slotNo;
	}

	public int getTroopId() {
		return troopId;
	}

	public void setTroopId(int troopId) {
		this.troopId = troopId;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
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

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public void setAttack2(int attack2) {
		this.attack2 = attack2;
	}

	public void setSpace(int space) {
		this.space = space;
	}

	public int getAttack2() {
		return this.attack2;
	}
	public int getSpace() {
		return this.space;
	}

	public int getOriginalCount() {
		return originalCount;
	}

	public void setOriginalCount(int originalCount) {
		this.originalCount = originalCount;
	}
	public void setOriginalHp(int originalHp) {
		this.originalHp = originalHp;
	}
	public int getOriginalHp() {
		return originalHp;
	}

	public boolean isFrom(Army army){
		return (this.playerId == army.getPlayerId() && this.cityId == army.getCityId());
	}
	
	public boolean isFireUnit(){
		return false;
	}
	
	public boolean isFortificationUnit(){
		return false;
	}

	/**
	 * 作战单位上场
	 * 
	 * @param position
	 * @param slot
	 */
	public void comeToField(FieldPosition position, Slot slot){
		this.setBattleFieldType(position.getType());
		this.setSlotNum(slot.getNum());

		//slot.add(this);
		log.info("一个单位"+this.troopId+"上场，位置:"+position.getType()+" 数目："+this.getCount());
	}
	
	/**
	 * 根据作战单位的准确度决定目标的数量，如果目标数量大于伤害的总量则把目标数量设为与伤害总量相同
	 * 
	 * @param totalDamage
	 * @param percent
	 * @return
	 */
	protected int getTargetCount(int totalDamage, double percent) {
		int targetCount = 11 - (int)(10*percent);
		if(targetCount > totalDamage)
			targetCount = totalDamage;
		return (targetCount <= 0) ? 1 : targetCount;
	}
	
	public void attack(Map<BattleFieldType, List<BattleUnit>> enemy, int targetCount, int totalDamage, Map<BattleFieldType, Damage> targetDamages){
		BattleFieldType[] attackOrder = getOrder();
		
		while(targetCount > 0 && totalDamage > 0){
			int initCount = targetCount;
			int initDamage = totalDamage;
			for(BattleFieldType type : attackOrder){
				if(enemy.get(type) == null){
					enemy.put(type, new ArrayList<>());
					continue;
				}
				int size = enemy.get(type).size();
				if(size >= targetCount){
					if((enemy.get(type).get(0).isFortificationUnit()) && !(this.isFireUnit())){
						log.info("面对城墙，攻击无效");
						return;
					}
					targetDamages.get(type).add(totalDamage, targetCount);
					log.info("一个单位"+this.troopId+"对"+type+"类型敌人"+targetCount+"个单位造成"+totalDamage+"点伤害");
					return;
				}else{
					if(size > 0){
						if((enemy.get(type).get(0).isFortificationUnit()) && !(this.isFireUnit())){
							log.info("面对城墙，攻击无效");
							totalDamage -= totalDamage/targetCount*size;
							targetCount -= size;
						}else{
							targetDamages.get(type).add(totalDamage/targetCount*size, size);
							log.info("一个单位"+this.troopId+"对"+type+"类型敌人"+size+"个单位造成"+(totalDamage/targetCount*size)+"点伤害");
							totalDamage -= totalDamage/targetCount*size;
							targetCount -= size;
						}
					}
				}
			}
			if (initCount == targetCount && initDamage == totalDamage)
				break;
		}
	}

	@Override
	public String toString() {
		return "BattleUnit{" +
				"originalHp=" + originalHp +
				", originalCount=" + originalCount +
				", battleFieldType=" + battleFieldType +
				", slotNum=" + slotNum +
				", playerId=" + playerId +
				", cityId=" + cityId +
				", troopId=" + troopId +
				", attack=" + attack +
				", percent=" + percent +
				", attack2=" + attack2 +
				", percent2=" + percent2 +
				", amount=" + amount +
				", hp=" + hp +
				", defense=" + defense +
				", space=" + space +
				'}';
	}

	/**
	 * 获得该单位的攻击目标的优先级
	 * @return
	 */
	public abstract BattleFieldType[] getOrder();
	
	/**
	 * 作战单位行动的函数，对于一般的作战单位是攻击，对于支援类型的单位则是治疗等
	 * 
	 * @param enemy
	 * @param self
	 */
	public abstract void doAction(Map<BattleFieldType, List<BattleUnit>> enemy, Map<BattleFieldType, List<BattleUnit>> self, Map<BattleFieldType, Damage> targetDamages);
	
	public int getAmountRemain() {
		return this.amount;
	}

	public void takeDamage(int ad) {
		if(ad > this.defense)
			hp -= ad < hp ? ad : hp;
	}
	
	public int getCount() {
		return (hp+originalHp-1)/originalHp;
	}

	public BattleUnit add(BattleUnit bu) {
		this.amount = (amount*getCount() + bu.getAmount()*bu.getCount())/(getCount() + bu.getCount());
		this.attack += bu.getAttack();
		this.attack2 += bu.getAttack2();
		this.hp += bu.getHp();
		this.defense += bu.getDefense();
		this.space += bu.getSpace();
		this.originalCount += bu.getCount();
		
		return this;
	}

	public GameProtos.BattleUnit convertToProto() {
		GameProtos.BattleUnit.Builder battleUnitBuilder = GameProtos.BattleUnit.newBuilder();
		battleUnitBuilder.setOriginalHp(this.originalHp);
		battleUnitBuilder.setOrginalCount(this.originalCount);
		battleUnitBuilder.setBattleFieldType(this.battleFieldType);
		battleUnitBuilder.setSlotNum(this.slotNum);
		battleUnitBuilder.setPlayerId(this.playerId);
		battleUnitBuilder.setCityId(this.cityId);
		battleUnitBuilder.setTroopId(this.troopId);
		battleUnitBuilder.setAttack(this.attack);
		battleUnitBuilder.setPercent(this.percent);
		battleUnitBuilder.setAttack2(this.attack2);
		battleUnitBuilder.setPercent2(this.percent2);
		battleUnitBuilder.setAmount(this.amount);
		battleUnitBuilder.setHp(this.hp);
		battleUnitBuilder.setDefense(this.defense);
		battleUnitBuilder.setSpace(this.space);

		return battleUnitBuilder.build();
	}
}
