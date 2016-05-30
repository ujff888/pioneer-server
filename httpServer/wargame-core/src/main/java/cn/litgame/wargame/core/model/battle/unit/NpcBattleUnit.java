package cn.litgame.wargame.core.model.battle.unit;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.battle.Damage;

import java.util.List;
import java.util.Map;

/**
 * npc类型
 * 
 * @author 熊纪元
 *
 */
public class NpcBattleUnit extends BattleUnit {

	public NpcBattleUnit(BattleTroop bt, int count, long playerId, int cityId) {
		super(bt, count, playerId, cityId);
	}

	public NpcBattleUnit(GameProtos.BattleUnit unit) {
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
		// TODO Auto-generated method stub
		
	}
}
