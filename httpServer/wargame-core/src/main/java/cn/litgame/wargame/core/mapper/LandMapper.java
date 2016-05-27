package cn.litgame.wargame.core.mapper;

import cn.litgame.wargame.core.model.Land;

public interface LandMapper {

	/**
	 * 创建岛屿信息
	 * @param land
	 * @return
	 */
	public int createLand(Land land);
	/**
	 * 获取岛屿信息
	 */
	public Land getLand(int landId);
	/**
	 * 更新岛屿信息
	 * @param land
	 * @return
	 */
	public int updateLand(Land land);
	/**
	 * 删除岛屿的信息
	 * @param landId
	 * @param cityId
	 * @return
	 */
	public int deleteLand(int landId);
}
