package cn.litgame.wargame.core.model.battle.unit;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.logic.BuildingLogic;
import cn.litgame.wargame.core.logic.CityLogic;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.battle.Damage;

public class FortificationBattleUnit extends BattleUnit {

	private static final long serialVersionUID = -5708761058884830221L;
	
	@Resource(name = "configLogic")
	private ConfigLogic configLogic;
	
	@Resource(name = "cityLogic")
	private CityLogic cityLogic;
	
	@Resource(name = "buildingLogic")
	private BuildingLogic buildingLogic;
	
	private static final BattleFieldType[] order_close = {
			BattleFieldType.FIELD_CLOSE,
			BattleFieldType.FIELD_REMOTE,
			BattleFieldType.FIELD_SIDE
	};
	
	public FortificationBattleUnit(Building fort) {
		super(fort);
	}

	@Override
	public boolean isFortificationUnit() {
		return true;
	}
	
	@Override
	public BattleFieldType[] getOrder() {
		return FortificationBattleUnit.order_close;
	}

	@Override
	public void doAction(Map<BattleFieldType, List<BattleUnit>> enemy, Map<BattleFieldType, List<BattleUnit>> self,
			Map<BattleFieldType, Damage> targetDamages) {
		// TODO Auto-generated method stub

	}

}
