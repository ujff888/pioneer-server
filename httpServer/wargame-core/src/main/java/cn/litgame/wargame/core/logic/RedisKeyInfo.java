package cn.litgame.wargame.core.logic;

public interface RedisKeyInfo {
	//cn.litgame.wargame.core.logic.queue.GameActionCenter.queue_key=wargame_time_queue
	/**
	 * 游戏动作的定时器队列
	 */
	public static final String queue_key = "wargame_time_queue";

	//cn.litgame.wargame.core.logic.ChatLogic.TOKEN=gotye_access_token
	/**
	 * 亲家聊天的通信token
	 */
	public static final String TOKEN = "gotye_access_token";

	//cn.litgame.wargame.core.logic.ChatLogic.worldChannelId_key=world_chat_key
	/**
	 * 亲家聊天的世界频道
	 */
	public static final String worldChannelId_key = "world_chat_key";

	//cn.litgame.wargame.core.logic.ChatLogic.qj_api_url_key=qj_api_url
	/**
	 * 亲家api调用地址
	 */
	public static final String qj_api_url_key = "qj_api_url";

	//cn.litgame.wargame.core.logic.RankLogic.TOTAL_RANK_KEY=total_rank
	/**
	 * 玩家排行榜总榜
	 */
	public static final String TOTAL_RANK_KEY = "total_rank";

	//cn.litgame.wargame.core.logic.RankLogic.WARFARE_RANK_KEY=warfare_rank
	/**
	 * 玩家排行榜"战争元帅"项排行
	 */
	public static final String WARFARE_RANK_KEY = "warfare_rank";

	//cn.litgame.wargame.core.logic.RankLogic.BEAT_RECORD_RANK_KEY=beat_record_rank
	/**
	 * 玩家排行榜"历史消灭敌人总数"项排行
	 */
	public static final String BEAT_RECORD_RANK_KEY = "beat_record_rank";

	//cn.litgame.wargame.core.logic.RankLogic.GOLD_RANK_KEY=gold_rank
	/**
	 * 玩家排行榜"金币存量"项排行
	 */
	public static final String GOLD_RANK_KEY = "gold_rank";

	//cn.litgame.wargame.core.logic.RankLogic.SCIENCE_RANK_KEY=science_rank
	/**
	 * 玩家排行榜"科技巨人"项排行
	 */
	public static final String SCIENCE_RANK_KEY = "science_rank";

	//cn.litgame.wargame.core.logic.RankLogic.BUILDING_RANK_KEY=building_rank
	/**
	 * 玩家排行榜"建筑大师"项排行
	 */
	public static final String BUILDING_RANK_KEY = "building_rank";

	//cn.litgame.wargame.core.logic.PlayerLogic.Server_Land_Id=currentLandId
	/**
	 * 玩家排行榜""项排行
	 */
	public static final String Server_Land_Id = "currentLandId";

	//cn.litgame.wargame.core.logic.ShipLogic.transport_ship_key=ransport_ship
	/**
	 * 获取玩家应该出生在哪个岛屿时调用
	 */
	public static final String transport_ship_key = "ransport_ship";

	//cn.litgame.wargame.server.logic.GameConfigManager.CURRENT_VERSION = currentVersion
	/**
	 * 获取客户端当前版本信息时调用
	 */
	public static final String CURRENT_VERSION  = " currentVersion";

	/**
	 * 战斗简略信息
	 */
	public static final String SIMPLE_BATTLE_INFO_KEY = "simpleBattleInfo";
	
	/**
	 * 简略战报
	 */
	public static final String SIMPLE_BATTLE_ROUND_KEY = "simpleBattleRound";
	
	/**
	 * 详细战报
	 */
	
	public static final String BATTLE_ROUND_DETAIL_KEY = "battleRoundDetail";
}
