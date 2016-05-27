package cn.litgame.wargame.core.model.battle.unit;

import java.util.List;
import java.util.Map;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.battle.Damage;

/**
 * npc类型
 * 
 * @author 熊纪元
 *
 */
public class NpcBattleUnit extends BattleUnit {

	public NpcBattleUnit(BattleTroop bt) {
		super(bt);
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
