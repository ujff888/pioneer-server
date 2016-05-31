package cn.litgame.wargame.core.model.battle.unit;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.auto.GameResProtos.TroopType;
import cn.litgame.wargame.core.logic.BattleLogic;
import cn.litgame.wargame.core.model.battle.BattleField;
import cn.litgame.wargame.core.model.battle.Damage;
import cn.litgame.wargame.core.model.battle.FieldPosition;
import cn.litgame.wargame.core.model.battle.Slot;

public abstract class BattleUnitAction {
	
	private static final Logger log = Logger.getLogger(BattleUnitAction.class);
	
	@Resource(name = "battleLogic")
	private BattleLogic battleLogic;
	
	@PostConstruct
	public void regist() {
		battleLogic.registBattleUnitAction(this.getTroopType(), this);
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
	
	public void attack(Map<BattleFieldType, FieldPosition> enemy, int targetCount, int totalDamage, Map<BattleFieldType, Damage> targetDamages, Slot slot){
		BattleFieldType[] attackOrder = getOrder(slot);
		
		while(targetCount > 0 && totalDamage > 0){
			int initCount = targetCount;
			int initDamage = totalDamage;
			for(BattleFieldType type : attackOrder){
				if(enemy.get(type) == null){
					continue;
				}
				int size = enemy.get(type).getSlotsWithTroop().size();
				if(size >= targetCount){
					if((enemy.get(type).getSlot(0).isFortificationUnit()) && !(slot.isFireUnit())){
						log.info("面对城墙，攻击无效");
						return;
					}
					targetDamages.get(type).add(totalDamage, targetCount);
					log.info("一个单位"+slot.getResTroopId()+"对"+type+"类型敌人"+targetCount+"个单位造成"+totalDamage+"点伤害");
					return;
				}else{
					if(size > 0){
						if((enemy.get(type).getSlot(0).isFortificationUnit()) && !(slot.isFireUnit())){
							log.info("面对城墙，攻击无效");
							totalDamage -= totalDamage/targetCount*size;
							targetCount -= size;
						}else{
							targetDamages.get(type).add(totalDamage/targetCount*size, size);
							log.info("一个单位"+slot.getResTroopId()+"对"+type+"类型敌人"+size+"个单位造成"+(totalDamage/targetCount*size)+"点伤害");
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

	public abstract BattleFieldType[] getOrder(Slot slot);
	
	public abstract TroopType getTroopType(); 
	
	public abstract void doAction(Map<BattleFieldType, FieldPosition> enemy,
			Map<BattleFieldType, FieldPosition> self, BattleFieldType type,
			BattleField battleField,
			Map<BattleFieldType, Damage> targetDamages,
			Slot slot);
}
