package cn.litgame.wargame.core.model.battle;

/**
 * 伤害的抽象，包含伤害的总量和目标个数
 * 
 * @author 熊纪元
 *
 */
public class Damage{
	private int unitCount;
	private int damageValue;
	
	public Damage(int unitCount, int damageValue) {
		this.unitCount = unitCount;
		this.damageValue = damageValue;
	}
	
	public int getUnitCount() {
		return unitCount;
	}
	
	public void setUnitCount(int unitCount) {
		this.unitCount = unitCount;
	}
	
	public int getDamageValue() {
		return damageValue;
	}
	
	public void setDamageValue(int damageValue) {
		this.damageValue = damageValue;
	}
	
	public void add(int totalDamage, int targetCount) {
		this.unitCount += targetCount;
		this.damageValue += totalDamage;
	}
	
	public boolean isEmpty(){
		return (unitCount == 0 || damageValue == 0);
	}
	
	public int getAvgDamage(){
		return this.damageValue/this.unitCount;
	}

	@Override
	public String toString() {
		return "Damage [unitCount=" + unitCount + ", damageValue=" + damageValue + "]";
	}
	
}
