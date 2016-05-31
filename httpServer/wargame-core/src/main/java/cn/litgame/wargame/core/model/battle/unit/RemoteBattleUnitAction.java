package cn.litgame.wargame.core.model.battle.unit;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.auto.GameResProtos.TroopType;
import cn.litgame.wargame.core.model.battle.BattleField;
import cn.litgame.wargame.core.model.battle.Damage;
import cn.litgame.wargame.core.model.battle.FieldPosition;
import cn.litgame.wargame.core.model.battle.Slot;
@Service
public class RemoteBattleUnitAction extends BattleUnitAction{
	private static final BattleFieldType[] order_remote = {
			BattleFieldType.FIELD_CLOSE,
			BattleFieldType.FIELD_SIDE,
			BattleFieldType.FIELD_REMOTE
	};
	private static final BattleFieldType[] order_close = {
			BattleFieldType.FIELD_CLOSE,
			BattleFieldType.FIELD_REMOTE,
			BattleFieldType.FIELD_SIDE
	};
	
	@Override
	public BattleFieldType[] getOrder(Slot slot) {
		if(slot.getBattleFieldType() == BattleFieldType.FIELD_REMOTE)
			return order_remote;
		else
			return order_close;
	}

	@Override
	public TroopType getTroopType() {
		return TroopType.REMOTE;
	}

	@Override
	public void doAction(Map<BattleFieldType, FieldPosition> enemy, Map<BattleFieldType, FieldPosition> self,
			BattleFieldType type, BattleField battleField, Map<BattleFieldType, Damage> targetDamages, Slot slot) {
		int totalDamage = 0;
		int targetCount = 0;
		double percent = 0;
		
		if(slot.getAmount() > 0){
			percent = slot.getPercent2();
			totalDamage = slot.getAttack2();
			slot.setAmount(slot.getAmount() - 1);
		}else{
			percent = slot.getPercent();
			totalDamage = slot.getAttack();
		}
		targetCount = getTargetCount(totalDamage, percent);
		
		this.attack(enemy, targetCount, totalDamage, targetDamages, slot);
	}

}
