package cn.litgame.wargame.core.model.battle.unit;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.battle.Damage;

import java.util.List;
import java.util.Map;

/**
 * 轻型部队
 * 
 * @author 熊纪元
 *
 */
public class LightBattleUnit extends BattleUnit {
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
	
	public LightBattleUnit(BattleTroop bt, int count, long playerId, int cityId) {
		super(bt, count, playerId, cityId);
	}

	public LightBattleUnit(GameProtos.BattleUnit unit){
		super(unit);
	}

	@Override
	public BattleFieldType[] getOrder() {
		switch(this.getBattleFieldType()){
		case FIELD_CLOSE:
			return LightBattleUnit.order_close;
		case FIELD_SIDE:
			return LightBattleUnit.order_side;
		default:
			throw new RuntimeException("unkown BattleFieldType: " + this.getBattleFieldType());
		}
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
		
		this.attack(enemy, targetCount, totalDamage, targetDamages);
	}
}