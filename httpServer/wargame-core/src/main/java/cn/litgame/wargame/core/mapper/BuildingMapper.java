package cn.litgame.wargame.core.mapper;

import java.util.List;

import cn.litgame.wargame.core.model.Building;

public interface BuildingMapper {
	/**
	 * 获取某个城市所有的建筑
	 * @param playerId
	 * @param cityId
	 * @return
	 */
	public List<Building> getBuildings(int cityId);
	/**
	 * 获取某个城市的某种建筑
	 * @param playerId
	 * @param cityId
	 * @return
	 */
	public List<Building> getBuildingsByBuildId(int cityId,int buildId);
	/**
	 * 更新建筑信息
	 * @param building
	 * @return
	 */
	public int updateBuilding(Building building);
	/**
	 * 获取建筑信息
	 * @param playerId
	 * @param cityid
	 * @param position
	 * @return
	 */
	public Building getBuilding(int cityId,int position);
	
	/**
	 * 拆除掉某个位置的建筑
	 * @param playerId
	 * @param cityId
	 * @param position
	 * @return
	 */
	public int delBuilding(int id);
	/**
	 * 创建建筑
	 * @param building
	 * @return
	 */
	public int createBuilding(Building building);
	
	/**
	 * 获取玩家的某种建筑的集合
	 * @param playerId
	 * @param buildId
	 * @return
	 */
	public List<Building> getBuildsByPlayerId(long playerId,int buildId);
}
