package cn.litgame.wargame.core.logic;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

@Service
public class RankLogic {
	@Resource(name="buildingLogic")
	private BuildingLogic buildingLogic;
	
	@Resource(name="cityLogic")
	private CityLogic cityLogic;
	
	@Resource(name="playerLogic")
	private PlayerLogic playerLogic;
	
	@Resource(name = "jedisStoragePool")
	private JedisPool jedisStoragePool;
	
	public final static String TOTAL_RANK_KEY = "total_rank";
	public final static String WARFARE_RANK_KEY = "warfare_rank";
	public final static String BEAT_RECORD_RANK_KEY = "beat_record_rank";
	public final static String GOLD_RANK_KEY = "gold_rank";
	public final static String SCIENCE_RANK_KEY = "science_rank";
	public final static String BUILDING_RANK_KEY = "building_rank";
	
	public enum RankType{
		TOTAL_RANK,WARFARE_RANK,BEAT_RECORD_RANK,GOLD_RANK,SCIENCE_RANK,BUILDING_RANK
	}

	public String getRankKey(RankType rankType) {
		switch(rankType){
		case TOTAL_RANK:
			return TOTAL_RANK_KEY;
		case WARFARE_RANK:
			return WARFARE_RANK_KEY;
		case BEAT_RECORD_RANK:
			return BEAT_RECORD_RANK_KEY;
		case GOLD_RANK:
			return GOLD_RANK_KEY;
		case SCIENCE_RANK:
			return SCIENCE_RANK_KEY;
		case BUILDING_RANK:
			return BUILDING_RANK_KEY;
		default:
			throw new RuntimeException("unkown rank type. ranktype = "+rankType);
		}
	}
	/*
	*//**
	 * 更新排行榜
	 * 
	 * @param rankType
	 * @param playerId
	 * @param playerScore
	 *//*
	public void updateRank(RankType rankType, Long playerId, Double newPlayerScore) {
		Jedis jedis = jedisStoragePool.getResource();
	
		try{
			if(rankType == RankType.BUILDING_RANK || rankType == RankType.WARFARE_RANK || rankType == RankType.SCIENCE_RANK) {
				Double oldBuildingScore = this.getPlayerScore(playerId, rankType);
				Double oldTotalScore = this.getPlayerScore(playerId, RankType.TOTAL_RANK);
				jedis.zadd(this.getRankKey(rankType), newPlayerScore, String.valueOf(playerId));
				if(rankType == RankType.SCIENCE_RANK) {
					jedis.zadd(this.getRankKey(RankType.TOTAL_RANK), oldTotalScore + (newPlayerScore - oldBuildingScore)*2, String.valueOf(playerId));	
				}
				jedis.zadd(this.getRankKey(RankType.TOTAL_RANK), oldTotalScore + newPlayerScore - oldBuildingScore, String.valueOf(playerId));
			}
			
			jedis.zadd(this.getRankKey(rankType), newPlayerScore, String.valueOf(playerId));
		}finally{
			jedis.close();
		}
	}*/
	
	/**
	 * 增量更新排行榜
	 * 
	 * @param rankType
	 * @param playerId
	 * @param increment
	 */
	public void updateRankWithIncrement(RankType rankType, Long playerId, Double increment) {
		Jedis jedis = jedisStoragePool.getResource();
	
		try{
			if(rankType == RankType.BUILDING_RANK || rankType == RankType.WARFARE_RANK || rankType == RankType.SCIENCE_RANK) {
				//jedis.zincrby(this.getRankKey(rankType), increment, String.valueOf(playerId));

				if(rankType == RankType.SCIENCE_RANK) {
					increment = increment*2;
				}
				jedis.zincrby(this.getRankKey(RankType.TOTAL_RANK), increment, String.valueOf(playerId));	
			}
			
			jedis.zincrby(this.getRankKey(rankType), increment, String.valueOf(playerId));
			if(jedis.zscore(this.getRankKey(rankType), String.valueOf(playerId)) < 0)
				jedis.zadd(this.getRankKey(rankType), 0, String.valueOf(playerId));
		}finally{
			jedis.close();
		}
	}
	
	/**
	 * 获取排行榜前length位
	 * 
	 * @param rankType
	 * @param length
	 * @return
	 */
	public Set<Tuple> getRankTop(RankType rankType, int length) {
		Jedis jedis = jedisStoragePool.getResource();
		Set<Tuple> resultSet = null;
		try{
			resultSet = jedis.zrevrangeWithScores(this.getRankKey(rankType), 0, length-1);
		}finally{
			jedis.close();
		}
		return resultSet;
	}
	
	/**
	 * 获取指定玩家排名,如果不存在就返回0
	 * 
	 * @param playerId
	 * @param rankType
	 * @return
	 */
	public long getPlayerRank(Long playerId, RankType rankType) {
		Jedis jedis = jedisStoragePool.getResource();
		Long playerRank = null;
		try{
			playerRank = jedis.zrevrank(this.getRankKey(rankType), String.valueOf(playerId));
		}finally{
			jedis.close();
		}
		if(playerRank == null)
			return 0;
		return playerRank + 1;
	}
	
	/**
	 * 获取指定玩家排名得分,如果不存在就返回0
	 * 
	 * @param playerId
	 * @param rankType
	 * @return
	 */
	public double getPlayerScore(Long playerId, RankType rankType) {
		Jedis jedis = jedisStoragePool.getResource();
		Double score = null;
		try{
			score = jedis.zscore(this.getRankKey(rankType), String.valueOf(playerId));
		}finally{
			jedis.close();
		}
		if(score == null)
			return 0;
		return score;
	}
	
}
