package cn.litgame.wargame.core.mapper;

import java.util.List;

import cn.litgame.wargame.core.model.LandDonation;

public interface LandDonationMapper {
	/**
	 * 获取某个岛屿的捐献信息
	 * @param landId
	 * @return
	 */
	public List<LandDonation> getLandDonations(int landId);
	/**
	 * 创建岛屿的捐献信息
	 * @param landDonation
	 * @return
	 */
	public int createLandDonation(LandDonation landDonation);
	/**
	 * 更新岛屿的捐献信息
	 * @param landDonation
	 * @return
	 */
	public int updateLandDonation(LandDonation landDonation);
	/**
	 * 删除岛屿的捐献信息
	 * @param landId
	 * @param cityId
	 * @return
	 */
	public int deleteLandDonation(int id);
	
	/**
	 * 获取岛屿的捐献信息
	 * @param landId
	 * @param cityId
	 * @return
	 */
	public LandDonation getLandDonation(int landId,long playerId,int cityId);
	
	/**
	 * 根据岛屿id查询城市数量
	 * @param landId
	 * @return
	 */
	public int getCityCountByLandId(int landId);
}
