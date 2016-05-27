package cn.litgame.wargame.core.mapper;

import java.util.List;

import cn.litgame.wargame.core.model.Ship;

public interface ShipMapper {

	public List<Ship> getShipsByPlayerId(long playerId);
	public Ship getShipByType(long playerId,int shipType);
	public int addShip(Ship ship);
	public int updateShip(Ship ship);
	public int delShip(long shipId);
	
}
