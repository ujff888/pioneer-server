package cn.litgame.wargame.core.model.battle.unit;

import java.util.List;
import java.util.Map;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.battle.Damage;
import cn.litgame.wargame.core.model.battle.troop.BattleTroop;

/**
 * 空战类型
 * 
 * @author 熊纪元
 *
 */
public class FlyAirBattleUnit extends BattleUnit {
	private static final BattleFieldType[] order = {
			BattleFieldType.FIELD_FLY_FIRE,
			BattleFieldType.FIELD_FLY
	};
	
	public FlyAirBattleUnit(BattleTroop bt, int count, long playerId, int cityId) {
		super(bt, count, playerId, cityId);
	}

	@Override
	public BattleFieldType[] getOrder(){
		return FlyAirBattleUnit.order;
	}

	@Override
	public void doAction(Map<BattleFieldType, List<BattleUnit>> enemy, Map<BattleFieldType, List<BattleUnit>> self,
			Map<BattleFieldType, Damage> targetDamages) {
		int totalDamage = 0;
		int targetCount = 0;
		double percent = 0;
		
		if(this.amount > 0){
			percent = this.percent2;
			totalDamage = this.attack2;
			this.amount--;
		}else{
			return;
		}
		targetCount = getTargetCount(totalDamage, percent);
		this.attack(enemy, targetCount, totalDamage, targetDamages);		
	}
}
