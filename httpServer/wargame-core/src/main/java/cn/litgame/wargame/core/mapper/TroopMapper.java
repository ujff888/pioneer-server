package cn.litgame.wargame.core.mapper;

import java.util.List;

import cn.litgame.wargame.core.model.Troop;

public interface TroopMapper {
	public int createTroop(Troop troop);
	public int updateTroop(Troop troop);
	public int delTroop(long troopId);
	public Troop getTroop(long troopId);
	public List<Troop> getTroopsByPlayerId(long playerId);
	public List<Troop> getTroopsByCityIdAndType(int cityId,int troopType);
	public List<Troop> getTroopsByCityId(int cityId);
	
}
