package cn.litgame.wargame.core.model.battle.unit;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.battle.Damage;

import java.util.List;
import java.util.Map;

/**
 * 远程部队
 * 
 * @author 熊纪元
 *
 */
public class RemoteBattleUnit extends BattleUnit {
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
	
	public RemoteBattleUnit(BattleTroop bt, int count, long playerId, int cityId) {
		super(bt, count, playerId, cityId);
	}

	public RemoteBattleUnit(GameProtos.BattleUnit unit) {
		super(unit);
	}
	@Override
	public BattleFieldType[] getOrder() {
		switch(this.getBattleFieldType()){
		case FIELD_CLOSE:
			return RemoteBattleUnit.order_close;
		case FIELD_REMOTE:
			return RemoteBattleUnit.order_remote;
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
		
		if(this.amount > 0){
			percent = this.percent2;
			totalDamage = this.attack2;
			this.amount--;
		}else{
			percent = this.percent;
			totalDamage = this.attack;
		}
		targetCount = getTargetCount(totalDamage, percent);
		
		this.attack(enemy, targetCount, totalDamage, targetDamages);
	}
}
