package cn.litgame.wargame.core.model.battle.unit;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.battle.Army;
import cn.litgame.wargame.core.model.battle.Damage;
import cn.litgame.wargame.core.model.battle.FieldPosition;
import cn.litgame.wargame.core.model.battle.Slot;

/**
 * 细化的作战单位，是所有具体作战单位的基类
 * 
 * @author 熊纪元
 *
 */
public abstract class BattleUnit implements Serializable{
	private static final long serialVersionUID = 5530170536339482598L;
	
	private static final Logger log = Logger.getLogger(BattleUnit.class);
	protected final int originalAmount;
	
	protected FieldPosition position;
	protected Slot slot;
	
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
		this.originalAmount = 0;
	}
	
	public BattleUnit(BattleTroop bt) {
		this.troopId = bt.getResTroop().getId();
		this.attack = bt.getResTroop().getAttack();
		this.percent = bt.getResTroop().getPercent();
		this.attack2 = bt.getResTroop().getAttack2();
		this.percent2 = bt.getResTroop().getPercent2();
		this.amount = bt.getResTroop().getAmount();
		this.hp = bt.getResTroop().getHp();
		this.defense = bt.getResTroop().getDefense();
		this.space = bt.getResTroop().getSpace();
		this.originalAmount = bt.getResTroop().getAmount();
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
	public FieldPosition getPosition() {
		return position;
	}

	public void setPosition(FieldPosition position) {
		this.position = position;
	}
	
	public Slot getSlot() {
		return this.slot;
	}
	
	public void setSlot(Slot slot) {
		this.slot = slot;
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
		this.setPosition(position);
		this.setSlot(slot);
		this.slot.add(this);
		log.info("一个单位"+this.troopId+"上场，位置:"+position.getType());
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
		return "BattleUnit [position=" + position + ", slot=" + slot + ", playerId=" + playerId + ", cityId=" + cityId
				+ ", troopId=" + troopId + ", attack=" + attack + ", percent=" + percent + ", attack2=" + attack2
				+ ", percent2=" + percent2 + ", amount=" + amount + ", hp=" + hp + ", defense=" + defense + ", space="
				+ space + "]";
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
	
	public double getAmountRemain() {
		if(this.originalAmount == 0)
			return -1;
		else{
			return (double)amount/originalAmount;
		}
	}
}
