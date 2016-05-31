package cn.litgame.wargame.core.model.battle.unit;

import java.util.Map;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.auto.GameResProtos.TroopType;
import cn.litgame.wargame.core.model.battle.BattleField;
import cn.litgame.wargame.core.model.battle.Damage;
import cn.litgame.wargame.core.model.battle.FieldPosition;
import cn.litgame.wargame.core.model.battle.Slot;
@Service
public class WeightBattleUnitAction extends BattleUnitAction {
	private static final BattleFieldType[] order = {
			BattleFieldType.FIELD_CLOSE,
			BattleFieldType.FIELD_REMOTE,
			BattleFieldType.FIELD_SIDE
	};
	
	@Override
	public BattleFieldType[] getOrder(Slot slot) {
		return order;
	}

	@Override
	public TroopType getTroopType() {
		return TroopType.WEIGHT;
	}

	@Override
	public void doAction(Map<BattleFieldType, FieldPosition> enemy, Map<BattleFieldType, FieldPosition> self,
			BattleFieldType type, BattleField battleField, Map<BattleFieldType, Damage> targetDamages, Slot slot) {
		int totalDamage = 0;
		int targetCount = 0;
		double percent = 0;
		
		percent = slot.getPercent();
		totalDamage = slot.getAttack();
		
		targetCount = getTargetCount(totalDamage, percent);
		
		this.attack(enemy, targetCount, totalDamage,  targetDamages, slot);
	}

}
