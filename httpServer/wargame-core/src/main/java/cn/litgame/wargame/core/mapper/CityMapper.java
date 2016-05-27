package cn.litgame.wargame.core.mapper;

import java.util.List;

import cn.litgame.wargame.core.model.City;

public interface CityMapper {

	/**
	 * 获取一个玩家的城市列表
	 * @param playerId
	 * @return
	 */
	public List<City> getCitysByPlayerId(long playerId);
	/**
	 * 获取某个城市
	 * @param playerId
	 * @param cityId
	 * @return
	 */
	public City getCity(int cityId);
	/**
	 * 更新城市信息
	 * @param city
	 * @return
	 */
	public int updateCity(City city);
	/**
	 * 删除城市
	 * @param playerId
	 * @param cityId
	 * @return
	 */
	public int delCity(int cityId);

	/**
	 * 创建城市
	 * @param city
	 * @return
	 */
	public int createCity(City city);
	
	/**
	 * 根据岛屿id获取岛屿上的所有城市
	 * @param landId
	 * @return
	 */
	public List<City> getCityByLandId(int landId);
	/**
	 * 获取指定坐标点的城市信息
	 * @param landId
	 * @param position
	 * @return
	 */
	public City getCityByPos(int landId,int position);
	
	/**
	 * 获取一个玩家的城市数量
	 * @param playerId
	 * @return
	 */
	public int getCityCount(long playerId);
	
	public List<City> selectByRange(int start, int length);
	
	public int count();
}
