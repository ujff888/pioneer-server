package cn.litgame.wargame.core.model.battle.round;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;

public class BattleRoundDetail implements Serializable{
	private static final long serialVersionUID = 7725726597385717157L;
	private double morale;
	private Map<String, RoundTroopDetail> roundTroopDetailMap = new HashMap<>();
	
	public double getMorale() {
		return morale;
	}
	public void setMorale(double morale) {
		this.morale = morale;
	}
	public Map<String, RoundTroopDetail> getRoundTroopDetailMap() {
		return this.roundTroopDetailMap;
	}
	
	@Override
	public String toString() {
		return "BattleRoundDetail [morale=" + morale + ", roundTroopDetailMap=" + roundTroopDetailMap + "]";
	}
	
	public RoundTroopDetail getRoundTroopDetail(int troopId, BattleFieldType type) {
		return roundTroopDetailMap.get(troopId+","+type);
	}
	
	public void putRoundTroopDetail(int troopId, BattleFieldType type, RoundTroopDetail roundTroopDetail) {
		roundTroopDetailMap.put(troopId+","+type, roundTroopDetail);
	}
}
