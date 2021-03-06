package cn.litgame.wargame.core.model.battle.unit;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.battle.Damage;

import java.util.List;
import java.util.Map;

/**
 * 支援类型
 * 
 * @author 熊纪元
 *
 */
public class LogisticsBattleUnit extends BattleUnit {

	public LogisticsBattleUnit(BattleTroop bt, int count, long playerId, int cityId) {
		super(bt, count, playerId, cityId);
	}

	public LogisticsBattleUnit(GameProtos.BattleUnit unit) {
		super(unit);
	}
	
	@Override
	public BattleFieldType[] getOrder() {
		// TODO Auto-generated method stub
		return null;
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
		
		
	}

}
