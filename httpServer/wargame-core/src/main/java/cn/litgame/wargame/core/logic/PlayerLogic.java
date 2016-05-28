package cn.litgame.wargame.core.logic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kriver.core.common.MathUtils;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.PlatformType;
import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.CityStatus;
import cn.litgame.wargame.core.auto.GameProtos.SCChangeSystem;
import cn.litgame.wargame.core.auto.GameProtos.SCStudyTech;
import cn.litgame.wargame.core.auto.GameResProtos.ResSystem;
import cn.litgame.wargame.core.auto.GameResProtos.ResTech;
import cn.litgame.wargame.core.mapper.AccountMapper;
import cn.litgame.wargame.core.mapper.LandDonationMapper;
import cn.litgame.wargame.core.mapper.PlayerHistoryMapper;
import cn.litgame.wargame.core.mapper.PlayerMapper;
import cn.litgame.wargame.core.mapper.PlayerStateMapper;
import cn.litgame.wargame.core.mapper.PlayerTechMapper;
import cn.litgame.wargame.core.model.Account;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.core.model.PlayerState;
import cn.litgame.wargame.core.model.PlayerTech;

@Service
public class PlayerLogic {
	private final static Logger logger = Logger.getLogger(PlayerLogic.class);
	
	@Resource(name = "playerStateMapper")
	private PlayerStateMapper playerStateMapper;
	
	@Resource(name = "jedisStoragePool")
	private JedisPool jedisStoragePool;

	@Resource(name = "jedisCachePool")
	private JedisPool jedisCachePool;
	
	@Resource(name = "playerMapper")
	private PlayerMapper playerMapper;
	
	@Resource(name = "accountMapper")
	private AccountMapper accountMapper;
	
	@Resource(name = "battleLogic")
	private BattleLogic battleLogic;
	
	@Resource(name = "paymentLogic")
	private PaymentLogic paymentLogic;
	
	@Resource(name = "cityLogic")
	private CityLogic cityLogic;
	
	@Resource(name = "chatLogic")
	private ChatLogic chatLogic;
	
	@Resource(name = "playerHistoryMapper")
	private PlayerHistoryMapper playerHistoryMapper;
	
	@Resource(name = "configLogic")
	private ConfigLogic configLogic;
	
	@Resource(name = "mapLogic")
	private MapLogic mapLogic;
	
	@Resource(name = "landDonationMapper")
	private LandDonationMapper landDonationMapper;
	
	@Resource(name = "playerTechMapper")
	private PlayerTechMapper playerTechMapper;
	
	@Resource(name = "buildingLogic")
	private BuildingLogic buildingLogic;
	
	@Resource(name = "rankLogic")
	private RankLogic rankLogic;
	
	private final static String PLAYER_CACHE_KEY = "p_";
	private final static int EXPIRE_SECOND = 60 * 60 * 24;//缓存一天
	private final static String PLAYER_INDEX = "player_index";
	public final static String UID_PLAYER = "uid_player";
	private String buildPlayerCacheKey(long playerId){
		return PLAYER_CACHE_KEY + playerId;
	}
	
	/**
	 * 改变政体
	 * @param player
	 * @param targetSystemId
	 * @param builder
	 * @return
	 */
	public MessageCode changeSystem(Player player,int targetSystemId,SCChangeSystem.Builder builder){
		
		ResSystem resSystem = configLogic.getResSystem(targetSystemId);
		PlayerTech pt = this.getPlayerTech(player.getPlayerId());
		PlayerState ps = this.getPlayerState(player.getPlayerId());
		if(resSystem.getTechId() != 0
				&& !this.techAllow(pt, resSystem.getTechId())){
			return MessageCode.CAN_NOT_CHANGE_SYSTEM;
		}
		if(ps.getSystem() == targetSystemId){
			return MessageCode.CAN_NOT_CHANGE_SYSTEM;
		}
		if(ps.getSystem() == configLogic.getGlobalConfig().getNoSystemId()){
			return MessageCode.HAD_CHANGE_SYSTEM;
		}
		int needGold = cityLogic.getCityCount(player.getPlayerId()) * configLogic.getGlobalConfig().getChangeSystemGold();
		if(player.getGold() < needGold){
			return MessageCode.NOT_ENOUGH_GOLD;
		}
		//player.setGold(player.getGold() - needGold);
		updatePlayerGold(player.getPlayerId(), player.getGold() - needGold);
		ps.setSystem(configLogic.getGlobalConfig().getNoSystemId());
		ps.setTargetSystemId(targetSystemId);
		long nowTime = System.currentTimeMillis();
		ps.setOverSystemTime(new Timestamp(
				nowTime + cityLogic.getCityCount(player.getPlayerId()) * configLogic.getGlobalConfig().getChangeSystemTime()* 1000));
		this.updatePlayerState(ps);
		builder.setSystemId(targetSystemId);
		builder.setOverTime((int)((ps.getOverSystemTime().getTime() - nowTime)/1000));
		return MessageCode.OK;
	}
	
	/**
	 * 获取用户状态
	 * @param playerId
	 * @return
	 */
	public PlayerState getPlayerState(long playerId){
		PlayerState ps = this.playerStateMapper.getPlayerState(playerId);
		if(ps.getSystem() == configLogic.getGlobalConfig().getNoSystemId()
				&& ps.getOverSystemTime().getTime() <= System.currentTimeMillis()){
			ps.setSystem(ps.getTargetSystemId());
			ps.setTargetSystemId(0);
			ps.setOverSystemTime(null);
			this.updatePlayerState(ps);
		}
		return ps;
	}	
	
	/**
	 * 更新用户状态
	 * @param ps
	 */
	public void updatePlayerState(PlayerState ps){
		this.playerStateMapper.updatePlayerState(ps);
	}
	
	/**
	 * 更新用户金币
	 */
	public boolean updatePlayerGold(Long playerId, int goldSum) {
		Player player = this.getPlayer(playerId);
		int oldGoldSum = player.getGold();
		player.setGold(goldSum);
		if(this.updatePlayer(player)) {
			rankLogic.updateRankWithIncrement(RankLogic.RankType.GOLD_RANK, playerId, Double.valueOf(goldSum - oldGoldSum));
			return true;
		}
		
		return false;
	}
	
	private boolean allowStudyTech(ResTech res,int[] ps){
		List<Integer> techIds = res.getTechIdsList();
		for(Integer id : techIds){
			if(id != 0){
				ResTech r = configLogic.getResTech(id);
				int progress = ps[r.getTechTypeId() -1];
				if((progress | 1 << r.getIndex()) != progress){
					return false;
				}
			}
		}
		return true;
	}
	
	public MessageCode studyTech(long playerId,int techId,SCStudyTech.Builder builder){
		ResTech rt = configLogic.getResTech(techId);
		if(rt == null){
			return MessageCode.NOT_FOUND_TECH;
		}
		PlayerTech pt = this.getPlayerTech(playerId);
		if(rt.getTechPoint() > pt.getTechPoint()){
			return MessageCode.NOT_ENOUGH_TECH_POINT;
		}
		
		int[] ps = {pt.getFlyProgress(),pt.getEconomicProgress(),pt.getScienceProgress(),pt.getMilitaryProgress()};
		if(!this.allowStudyTech(rt, ps)){
			return MessageCode.CAN_NOT_STUDY_TEACH;
		}
		pt.setTechPoint(pt.getTechPoint() - rt.getTechPoint());
		
		rankLogic.updateRankWithIncrement(RankLogic.RankType.SCIENCE_RANK, playerId, Double.valueOf(rt.getTechPoint()/100));
		
		ps[rt.getTechTypeId() - 1] = ps[rt.getTechTypeId() - 1] | 1 << rt.getIndex();
		pt.setFlyProgress(ps[0]);
		pt.setEconomicProgress(ps[1]);
		pt.setScienceProgress(ps[2]);
		pt.setMilitaryProgress(ps[3]);
		
		if(techId == 19){
			pt.setFlyLevel(pt.getFlyLevel() + 1);
		}else if(techId == 37){
			pt.setEconomicLevel(pt.getEconomicLevel() + 1);
		}else if(techId == 56){
			pt.setScienceLevel(pt.getScienceLevel() + 1);
		}else if(techId == 70){
			pt.setMilitaryLevel(pt.getMilitaryLevel() + 1);
		}
		
		this.updatePlayerTech(pt);
		builder.setTechId(techId);
		builder.setTechPoint(pt.getTechPoint());
		return MessageCode.OK;
	}
	/**
	 * 刷新科技点
	 * @param pt
	 */
	public void flushTechPoint(PlayerTech pt,long nowTime){
		List<City> citys = cityLogic.getCity(pt.getPlayerId());
		int total = 0;
		for(City c : citys){
			total += c.getScientist();
		}
		pt.setTotalTecher(total);
		double techPointRate = cityLogic.getTechPointRate(total);
		pt.setTechPointRate(techPointRate);
		
		long interval = nowTime - pt.getLastFlushTime().getTime();
		if(interval > 5000){
			int techPoint = (int)((techPointRate/3600000) * interval);
			if(techPoint > 0){
				pt.setTechPoint(pt.getTechPoint() + techPoint);
				pt.setLastFlushTime(new Timestamp(nowTime));
				this.updatePlayerTech(pt);
			}
		}
	}
	
	public void updatePlayerTech(PlayerTech pt){
		this.playerTechMapper.updatePlayerTech(pt);
	}
	public void deletePlayerTech(long playerId){
		this.playerTechMapper.deletePlayerTech(playerId);
	}
	
	
	/**
	 * 检查科技是否解锁
	 * @param playerId
	 * @param techId
	 * @return
	 */
	public boolean techAllow(PlayerTech pt,int techId){
		ResTech res = configLogic.getResTech(techId);
		if(res == null){
			return true;
		}
		int[] ps = {pt.getFlyProgress(),pt.getEconomicProgress(),pt.getScienceProgress(),pt.getMilitaryProgress()};
		int value = ps[res.getTechTypeId() - 1];
		return value == (value | res.getIndex() << 1);
	}
	
	public PlayerTech getPlayerTech(long playerId){
		PlayerTech pt = this.playerTechMapper.getPlayerTech(playerId);
		long nowTime = System.currentTimeMillis();
		if(pt == null){
			pt = new PlayerTech();
			pt.setPlayerId(playerId);
			pt.setTechPoint(configLogic.getGlobalConfig().getDefaultTechPoint());//初始科技点
			pt.setLastFlushTime(new Timestamp(nowTime));
			pt.setEconomicProgress(0);
			pt.setFlyProgress(0);
			pt.setMilitaryProgress(0);
			pt.setScienceProgress(0);
			this.playerTechMapper.createPlayerTech(pt);
			return pt;
		}
		this.flushTechPoint(pt,nowTime);
		return pt;
	}
	
	/**
	 * 刷新用户的金币
	 * @param player
	 */
	public void flushPlayerGold(Player player){
		//处理每个城市的金币增量
		//获取用户的金币数量
	}
	
	/**
	 * 创建用户角色
	 * @param playerId
	 * @param deviceType
	 * @param playerName
	 * @param bindAccount
	 * @param platformUid
	 * @param platformType
	 * @return
	 */
	public Player createPlayer(long playerId, String deviceType,String playerName,String bindAccount, String platformUid,PlatformType platformType) {
		//如果设备类型或角色名称为空时，返回null
		if (StringUtils.isBlank(deviceType) || StringUtils.isBlank(playerName)) {
			logger.error("create player error,deviceType=" + deviceType
					+ ",playerName=" + playerName);
			return null;
		}
		//设置玩家默认信息
		Player player = new Player();
		player.setDeviceType(deviceType);
		player.setPlayerId(playerId);
		player.setPlayerName(playerName);
		player.setBindAccount(bindAccount);
		player.setPlatformUid(platformUid);
		player.setPlatformType(platformType.getNumber());
		player.setCreateTime(new Timestamp(System.currentTimeMillis()));
		player.setLevel(1);
		player.setPassword(DigestUtils.md5Hex(String.valueOf(MathUtils.random(100000, 999999))));
		player.setChatOn(0);
		player.setGold(configLogic.getGlobalConfig().getInitGold());
		player.setDiamond(configLogic.getGlobalConfig().getInitDiamond());
		//插入玩家信息
		if (playerMapper.insert(player) != 1) {
			logger.error("insert error,player="+player);
			return null;
		}
		playerStateMapper.createPlayerState(new PlayerState(playerId,configLogic.getGlobalConfig().getDefaultSystemId()));
		
		rankLogic.updateRankWithIncrement(RankLogic.RankType.GOLD_RANK, playerId, Double.valueOf(player.getGold()));
		
		return player;
	}
	
	public void addAccount(String platformUid,PlatformType type, long playerId){
		this.accountMapper.addAccount(new Account(platformUid,type.getNumber(),playerId));
	}
	
	
	public static int maxLandId = 5000;
	private final static String Server_Land_Id = "currentLandId";
	
	/**
	 * 获取当前应该出生在哪个岛屿
	 * @return
	 */
	public int getServerLandId(){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			String value = jedis.get(Server_Land_Id);
			if(value == null){
				jedis.set(Server_Land_Id, "10001");
				return 10001;
			}
			int oldLandId = Integer.parseInt(value);
			int landId = this.checkLandId(oldLandId);
			if(landId != oldLandId){
				jedis.set(Server_Land_Id, String.valueOf(landId));
			}
			return landId;
		}finally{
			jedis.close();
		}

	}
	
	/**
	 * 当岛屿的注册用户大于4个的时候，选择下一个岛屿为出生地，按照50 * 100的岛屿计算，支持2万个注册用户
	 * @param landId
	 * @return
	 */
	public int checkLandId(int landId){
		int count = mapLogic.getCityCountByLandId(landId);
		if(count > 4){
			landId++;
			return checkLandId(landId);
		}
		return landId;
	}
	
	/**
	 * 检查服务器的状态，看是否允许注册用户
	 * @return
	 */
	public boolean allowRegist(){
		if(this.getServerLandId() > maxLandId){
			return false;
		}
		return true;
	}
	
	/**
	 * 在岛屿内获取一个随机的出生位置
	 * @param landId
	 * @return
	 */
	private int getRandomPositionInLand(int landId){
		Set<Integer> positions = new HashSet<Integer>();
		for(int i = 0;i<16;i++){
			positions.add(i);
		}
		List<City> citys = cityLogic.getCityByLandId(landId);
		if(citys != null){
			for(City city : citys){
				positions.remove(city.getPosition());
			}
		}
		return (int)positions.toArray()[0];
	}
	
	/**
	 * 获取一个新用户的出生城市
	 * @return
	 */
	public GameProtos.City getBornCity(long playerId){
		int landId = this.getServerLandId();
		int pos = this.getRandomPositionInLand(landId);
		int resource = configLogic.getGlobalConfig().getDefaultResource();
		int person = configLogic.getGlobalConfig().getDefaultCityPerson();
		City city = cityLogic.createCity(playerId, landId, pos, true,resource,resource,resource,resource,resource,person,CityStatus.CITY_NORMAL_VALUE);
		
		//创建主城
		Building b = buildingLogic.createBuilding(playerId, city.getCityId(), 1001, 0,false);
		List<Building> bs = new ArrayList<Building>();
		bs.add(b);
		
		//创建岛屿捐献记录
		mapLogic.createLandDonation(landId, playerId, city.getCityId());
		return cityLogic.convert(city, bs,null);
	}
	
	public Account getAccount(String platformUid,PlatformType type){
		return this.accountMapper.getAccount(platformUid, type.getNumber());
	}
	
	/**
	 * 通过玩家ID获取相应的玩家信息
	 * 
	 * @param playerId
	 * @return
	 */
	public Player getPlayer(Long playerId) {
		Player player = playerMapper.select(playerId);
		return player;
	}
	
	private final static String New_Player_Key = "np_";
	public String buildNewPlayerKey(long playerId){
		return New_Player_Key + playerId;
	}
	public void addNewPlayerStepIds(long playerId,List<Integer> ids){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			String[] sid = new String[ids.size()];
			for(int i = 0;i<sid.length;i++){
				sid[i] = String.valueOf(ids.get(i));
			}
			jedis.sadd(this.buildNewPlayerKey(playerId), sid);
		}finally{
			jedis.close();
		}
		
	}
	
	public List<Integer> getNewPlayerStepIds(long playerId){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			List<Integer> result = new ArrayList<Integer>();
			Set<String> ids = jedis.smembers(this.buildNewPlayerKey(playerId));
			if(ids != null){
				for(String s :ids){
					result.add(Integer.parseInt(s));
				}
			}
			return result;
		}finally{
			jedis.close();
		}
		
	}
	/**
	 * 更新玩家的信息
	 * 
	 * @param player
	 * @return
	 */
	public boolean updatePlayer(Player player) {
		boolean result = playerMapper.update(player) > 0;
		return result;
	}
	
	public GameProtos.Player getPlayerMessage(Player player){
		GameProtos.Player.Builder builder = GameProtos.Player.newBuilder();
		builder.setPlatformType(PlatformType.valueOf(player.getPlatformType()));
		builder.setAccount(player.getPlatformUid());
		builder.setPlayerId(player.getPlayerId());
		builder.setPwd(player.getPassword());
		builder.setPlayerName(player.getPlayerName());
		builder.setIcon(player.getIcon() == null ? "" : player.getIcon());
		builder.setGold(player.getGold());
		builder.setLevel(player.getLevel());
		builder.setDiamond(player.getDiamond());
		builder.setStatus(player.getStatus());
		builder.setVip(player.getVip());
		if(player.getVipTime() != null){
			builder.setVipTime((int)(player.getVipTime().getTime()/1000));
		}
		
		
		Set<String> productIds = paymentLogic.getProductIdsByPlayerId(player.getPlayerId());
		if(productIds != null){
			for(String s : productIds){
				builder.addProductIds(s);
			}
		}
		builder.setWorldChatKey(chatLogic.getWroldChatChannel(null));
		return builder.build();
	}
	
	public static void main(String[] args) {
		System.out.println(137573171273L % 12);
	}

	public List<Player> selectByRange(int start, int length) {
		return playerMapper.selectByRange(start, length);
	}

	public int count() {
		return playerMapper.count();
	}
}
