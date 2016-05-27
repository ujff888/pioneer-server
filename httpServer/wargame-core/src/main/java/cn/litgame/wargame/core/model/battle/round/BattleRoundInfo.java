package cn.litgame.wargame.core.model.battle.round;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BattleRoundInfo implements Serializable {
	private static final long serialVersionUID = -7712299298285192926L;
	private long playeId;
	private int cityId;
	private Map<Integer, RoundTroopInfo> roundTroopInfoMap = new HashMap<>();
	
	public BattleRoundInfo(){}
	
	public BattleRoundInfo(long playerId, int cityId){
		this.playeId = playerId;
		this.cityId = cityId;
	}
	
	public long getPlayeId() {
		return playeId;
	}
	public void setPlayeId(long playeId) {
		this.playeId = playeId;
	}
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	
	public Map<Integer, RoundTroopInfo> getRoundTroopInfoMap() {
		return this.roundTroopInfoMap;
	}

	@Override
	public String toString() {
		return "BattleRound1 [playeId=" + playeId + ", cityId=" + cityId + ", roundTroopInfoMap=" + roundTroopInfoMap
				+ "]";
	}

	public RoundTroopInfo getRoundTroopInfo(int troopId) {
		return roundTroopInfoMap.get(troopId);
	}

	public void putRoundTroopInfo(int troopId, RoundTroopInfo rti) {
		roundTroopInfoMap.put(troopId, rti);
	}
}
