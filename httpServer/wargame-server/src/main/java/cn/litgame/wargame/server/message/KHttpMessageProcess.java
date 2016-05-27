package cn.litgame.wargame.server.message;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import redis.clients.jedis.JedisPool;
import cn.litgame.wargame.core.logic.BattleLogic;
import cn.litgame.wargame.core.logic.BuildingLogic;
import cn.litgame.wargame.core.logic.ChatLogic;
import cn.litgame.wargame.core.logic.CityLogic;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.logic.GameNoticeLogic;
import cn.litgame.wargame.core.logic.MapLogic;
import cn.litgame.wargame.core.logic.MarketLogic;
import cn.litgame.wargame.core.logic.PackItemLogic;
import cn.litgame.wargame.core.logic.PaymentLogic;
import cn.litgame.wargame.core.logic.PlayerLogic;
import cn.litgame.wargame.core.logic.RankLogic;
import cn.litgame.wargame.core.logic.ShipLogic;
import cn.litgame.wargame.core.logic.queue.GameActionLogic;
import cn.litgame.wargame.log.LogManager;
import cn.litgame.wargame.server.logic.GameConfigManager;
import cn.litgame.wargame.server.logic.HttpMessageManager;

public abstract class KHttpMessageProcess {

	@Resource(name = "marketLogic")
	protected MarketLogic marketLogic;
	
	@Resource(name = "playerLogic")
	protected PlayerLogic playerLogic;

	@Resource(name = "cityLogic")
	protected CityLogic cityLogic;
	
	@Resource(name = "mapLogic")
	protected MapLogic mapLogic;
	
	@Resource(name = "battleLogic")
	protected BattleLogic battleLogic;
	
	@Resource(name = "paymentLogic")
	protected PaymentLogic paymentLogic;
	
	@Resource(name = "buildingLogic")
	protected BuildingLogic buildingLogic;
	
	@Resource(name = "configLogic")
	protected ConfigLogic configLogic;
	
	@Resource(name = "gameActionLogic")
	protected GameActionLogic gameActionLogic;
	
	@Resource(name = "shipLogic")
	protected ShipLogic shipLogic;
	
	@Resource(name = "gameConfigManager")
	protected GameConfigManager gameConfigManager;

	@Resource(name = "httpMessageManager")
	protected HttpMessageManager httpMessageManager;

	@Resource(name = "logManager")
	protected LogManager logManager;

	@Resource(name = "packItemLogic")
	protected PackItemLogic packItemLogic;
	
	@Resource(name = "jedisStoragePool")
	protected JedisPool jedisStoragePool;

	@Resource(name = "gameNoticeLogic")
	GameNoticeLogic gameNoticeLogic;
	
	@Resource(name = "chatLogic")
	protected ChatLogic chatLogic;
	
	@Resource(name = "rankLogic")
	protected RankLogic rankLogic;
	
	protected final static Logger log = Logger.getLogger(KHttpMessageProcess.class);
}
