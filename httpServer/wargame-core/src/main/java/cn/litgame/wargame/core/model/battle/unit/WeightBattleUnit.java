package cn.litgame.wargame.core.model.battle.unit;

import java.util.List;
import java.util.Map;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.battle.Damage;

/**
 * 重型部队
 * 
 * @author 熊纪元
 *
 */
public class WeightBattleUnit extends BattleUnit {
	private static final BattleFieldType[] order_close = {
			BattleFieldType.FIELD_CLOSE,
			BattleFieldType.FIELD_REMOTE,
			BattleFieldType.FIELD_SIDE
	};
	
	public WeightBattleUnit(BattleTroop bt) {
		super(bt);
	}

	@Override
	public BattleFieldType[] getOrder() {
		return WeightBattleUnit.order_close;
	}

	@Override
	public void doAction(Map<BattleFieldType, List<BattleUnit>> enemy, Map<BattleFieldType, List<BattleUnit>> self,
			Map<BattleFieldType, Damage> targetDamages) {
		int totalDamage = 0;
		int targetCount = 0;
		double percent = 0;
		
		percent = this.percent;
		totalDamage = this.attack;
		
		targetCount = getTargetCount(totalDamage, percent);
		
		this.attack(enemy, targetCount, totalDamage,  targetDamages);
	}

}
