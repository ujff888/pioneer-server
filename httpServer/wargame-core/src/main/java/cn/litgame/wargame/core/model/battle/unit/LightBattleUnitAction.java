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
public class LightBattleUnitAction extends BattleUnitAction {
	private static final BattleFieldType[] order_close = {
			BattleFieldType.FIELD_CLOSE,
			BattleFieldType.FIELD_REMOTE,
			BattleFieldType.FIELD_SIDE
	};
	
	private static final BattleFieldType[] order_side = {
			BattleFieldType.FIELD_SIDE,
			BattleFieldType.FIELD_REMOTE,
			BattleFieldType.FIELD_FIRE,
			BattleFieldType.FIELD_CLOSE
	};
	
	@Override
	public BattleFieldType[] getOrder(Slot slot) {
		if(slot.getBattleFieldType() == BattleFieldType.FIELD_CLOSE)
			return order_close;
		else
			return order_side;
		
	}

	@Override
	public TroopType getTroopType() {
		return TroopType.LIGHT;
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
		
		this.attack(enemy, targetCount, totalDamage, targetDamages, slot);
	}

}
