package cn.litgame.wargame.core.logic.queue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.logic.BattleLogic;
import cn.litgame.wargame.core.logic.CityLogic;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.logic.MapLogic;
import cn.litgame.wargame.core.logic.PlayerLogic;
import cn.litgame.wargame.core.logic.ShipLogic;
import cn.litgame.wargame.core.mapper.GameActionMapper;
import cn.litgame.wargame.core.mapper.TroopMapper;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.GameAction;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.core.model.Troop;
import cn.litgame.wargame.core.model.battle.BattleField;

import com.google.protobuf.InvalidProtocolBufferException;

@Service
public class GameActionLogic {

	private final static Logger log = Logger.getLogger(GameActionLogic.class);
	
	@Resource(name = "configLogic")
	private ConfigLogic configLogic;
	
	@Resource(name = "gameActionMapper")
	private GameActionMapper gameActionMapper;
	
	@Resource(name = "troopMapper")
	private TroopMapper troopMapper;
	
	@Resource(name = "battleLogic")
	private BattleLogic battleLogic;
	
	@Resource(name = "gameActionCenter")
	private GameActionCenter gameActionCenter;
	
	@Resource(name = "shipLogic")
	private ShipLogic shipLogic;
	
	@Resource(name = "mapLogic")
	private MapLogic mapLogic;
	
	@Resource(name = "cityLogic")
	private CityLogic cityLogic;
	
	@Resource(name = "playerLogic")
	private PlayerLogic playerLogic;
	
	/**
	 * 获取某个城市码头所有相关状态的运输船,装载、等待、返回
	 * @param cityId
	 * @return
	 */
	public List<GameAction> getGameActionByCityId(int cityId){
		return this.gameActionMapper.getGameActionsByCity(cityId,TransportStatus.BACKING_VALUE);
		
	}
	/**
	 * 这个是获取的指定状态的动作
	 * @param cityId
	 * @param state
	 * @return
	 */
	public List<GameAction> getGameActionByState(int cityId,int state){
		return this.gameActionMapper.getGameActionsByState(cityId, state,1000);
	}
	/**
	 * 获取一个最近的等待action
	 * @param cityId
	 * @return
	 */
	public GameAction getWaitingGameAction(int cityId){
		List<GameAction> gs = this.gameActionMapper.getGameActionsByState(cityId, TransportStatus.WAITING_VALUE, 1);

		return gs.size() < 1 ? null : gs.get(0);
	}
	/**
	 * 获取正在装载状态的action
	 * @param cityId
	 * @return
	 */
	public GameAction getLoadingGameAction(int cityId){
		List<GameAction>  gas = this.gameActionMapper.getGameActionsByCity(cityId, TransportStatus.LOADING_VALUE);

		return gas.size() < 1 ? null : gas.get(0);
	}
	
	/**
	 * 调用运输船发起一个定时任务，一般运输船出发都需要先装载，所以动作类型以actionData里记录的为主
	 * @param result，所携带的资源、部队、运输船数量、动作类型
	 * @param sourceCity
	 * @param targetCity
	 * @param loadingTime
	 * @return
	 */
	public MessageCode createTransportShipTask(TransportTask.Builder result,City sourceCity,City targetCity,long loadingTime){
		GameAction ga = this.getLoadingGameAction(sourceCity.getCityId());
		TransportStatus state = ga == null ? TransportStatus.LOADING : TransportStatus.WAITING;
		result.setStatus(state);
		GameAction gameAction = new GameAction();
		gameAction.setActionType(GameActionType.NEXT_STATE_ACTION_VALUE);
		gameAction.setSourcePlayerId(sourceCity.getPlayerId());
		gameAction.setTargetPlayerId(targetCity.getPlayerId());
		gameAction.setTargetCityId(targetCity.getCityId());
		gameAction.setSourceCityId(sourceCity.getCityId());
		gameAction.setShipCount(result.getShipCount());
		long nowTime = System.currentTimeMillis();
		long overTime = state == TransportStatus.LOADING ? nowTime + loadingTime : 0;
		gameAction.setCreateTime(new Timestamp(nowTime));
		gameAction.setOverTime(new Timestamp(overTime));
		gameAction.setLoadingTime(loadingTime);
		
		if(state == TransportStatus.LOADING)
			result.setLoadingStartTime((int) (gameAction.getCreateTime().getTime()/1000));
		
		gameAction.setActionData(result.build().toByteArray());
		gameAction.setActionState(state.getNumber());
		
		this.gameActionMapper.createGameAction(gameAction);
		if(state == TransportStatus.LOADING){
			gameActionCenter.addGameAction(gameAction.getActionId(), overTime);
		}
		
		shipLogic.useShip(sourceCity.getPlayerId(), result.getShipCount());
		return MessageCode.OK;
	}
	
	/**
	 * 调用运输船发起一个定时任务，一般运输船出发都需要先装载，所以动作类型以actionData里记录的为主
	 * @param result，所携带的资源、部队、运输船数量、动作类型
	 * @param sourceCity
	 * @param targetCity
	 * @param loadingTime
	 * @param reverseSourcePlayer
	 * 最后一个参数用于任务结束后返回运输船是确定运输船归属，因为在一些交易任务中尽管出发城市和目标城市相反，但运输船属于同一个玩家
	 * @return
	 */
	public MessageCode createTransportShipTask(TransportTask.Builder result,City sourceCity,City targetCity,long loadingTime, boolean reverseSourcePlayer){
		GameAction ga = this.getLoadingGameAction(sourceCity.getCityId());
		TransportStatus state = ga == null ? TransportStatus.LOADING : TransportStatus.WAITING;
		result.setStatus(state);
		GameAction gameAction = new GameAction();
		gameAction.setActionType(GameActionType.NEXT_STATE_ACTION_VALUE);
		
		if(reverseSourcePlayer){
			gameAction.setSourcePlayerId(targetCity.getPlayerId());
		}else{
			gameAction.setSourcePlayerId(sourceCity.getPlayerId());
		}
		gameAction.setTargetPlayerId(targetCity.getPlayerId());
		gameAction.setTargetCityId(targetCity.getCityId());
		gameAction.setSourceCityId(sourceCity.getCityId());
		gameAction.setShipCount(result.getShipCount());
		long nowTime = System.currentTimeMillis();
		long overTime = state == TransportStatus.LOADING ? nowTime + loadingTime : 0;
		gameAction.setCreateTime(new Timestamp(nowTime));
		gameAction.setOverTime(new Timestamp(overTime));
		gameAction.setLoadingTime(loadingTime);
		gameAction.setActionData(result.build().toByteArray());
		gameAction.setActionState(state.getNumber());
		this.gameActionMapper.createGameAction(gameAction);
		if(state == TransportStatus.LOADING){
			gameActionCenter.addGameAction(gameAction.getActionId(), overTime);
		}
		
		shipLogic.useShip(sourceCity.getPlayerId(), result.getShipCount());
		return MessageCode.OK;
	}
	
	/**
	 * 调用运输船发起一个不需要装载的定时任务
	 * 
	 * @param result
	 * @param sourceCity
	 * @param targetCity
	 * @param reverseSourcePlayer
	 * 最后一个参数用于任务结束后返回运输船是确定运输船归属，因为在一些交易任务中尽管出发城市和目标城市相反，但运输船属于同一个玩家
	 * @return
	 */
	public MessageCode createTransportShipTaskSkipLoading(TransportTask.Builder result,City sourceCity,City targetCity, boolean reverseSourcePlayer){
		result.setStatus(TransportStatus.TRANSIT);
		GameAction gameAction = new GameAction();
		gameAction.setActionType(GameActionType.NEXT_STATE_ACTION_VALUE);
		if(reverseSourcePlayer){
			gameAction.setSourcePlayerId(targetCity.getPlayerId());
		}else{
			gameAction.setSourcePlayerId(sourceCity.getPlayerId());
		}
		gameAction.setTargetPlayerId(targetCity.getPlayerId());
		gameAction.setTargetCityId(targetCity.getCityId());
		gameAction.setSourceCityId(sourceCity.getCityId());
		gameAction.setShipCount(result.getShipCount());
		long nowTime = System.currentTimeMillis();
		long overTime = nowTime;
		gameAction.setCreateTime(new Timestamp(nowTime));
		gameAction.setOverTime(new Timestamp(overTime));
		gameAction.setLoadingTime(0);
		gameAction.setActionData(result.build().toByteArray());
		gameAction.setActionState(TransportStatus.TRANSIT_VALUE);
		this.gameActionMapper.createGameAction(gameAction);
		gameActionCenter.addGameAction(gameAction.getActionId(), overTime);
		
		shipLogic.useShip(sourceCity.getPlayerId(), result.getShipCount());
		return MessageCode.OK;
	}
	
	/**
	 * 生产部队
	 * @param playerId
	 * @param cityId
	 * @param troops
	 * @param troopType
	 * @return
	 */
	public GameAction productionTroop(City city,GameProtos.Troops troops,int buildId,long nowTime,long overTime){
		GameAction gameAction = new GameAction();
		gameAction.setActionData(troops.toByteArray());
		if(buildId == 1010){
			gameAction.setActionType(GameActionType.PRODUCTION_LAND_VALUE);
		}else if(buildId == 1009){
			gameAction.setActionType(GameActionType.PRODUCTION_FLY_VALUE);
		}else{
			throw new RuntimeException("invalid buildId ,buildId ==" + buildId);
		}
		gameAction.setCreateTime(new Timestamp(nowTime));
		Timestamp ot = new Timestamp(overTime);
		gameAction.setOverTime(ot);
		gameAction.setSourceCityId(city.getCityId());
		gameAction.setSourcePlayerId(city.getPlayerId());
		gameAction.setTargetCityId(city.getCityId());
		gameAction.setTargetPlayerId(city.getPlayerId());
		this.gameActionMapper.createGameAction(gameAction);
		return gameAction;
	}
	
	public GameAction getGameAction(long gameActionId){
		GameAction ga = this.gameActionMapper.getGameAction(gameActionId);
		return ga;
	}
	
	/**
	 * 更新游戏动作信息
	 * @param gameAction
	 */
	public void updateGameAction(GameAction gameAction){
		this.gameActionMapper.updateGameAction(gameAction);
	}
	/**
	 * 删除掉一个游戏动作
	 * @param gameActionId
	 */
	public void delGameAction(long gameActionId){
		this.gameActionMapper.delGameAction(gameActionId);
	}
	
	/**
	 * 处理已经到期的生产队列，把生产的内容添加到部队信息里，并且返回null
	 * @param gameAction
	 * @param city
	 * @param type
	 * @param nowTime
	 * @return
	 * @throws InvalidProtocolBufferException
	 */
	private GameAction flushProductionTroop(GameAction gameAction,City city, int type,long nowTime) throws InvalidProtocolBufferException{
		if(gameAction == null){
			return null;
		}
		if(nowTime >= gameAction.getOverTime().getTime()){
			
			List<Troop> troops = troopMapper.getTroopsByCityIdAndType(city.getCityId(), type);
			Map<Integer,Troop> ts = new HashMap<Integer, Troop>();
			if(troops != null){
				for(Troop t : troops){
					ts.put(t.getTroopResId(), t);
				}
			}
			GameProtos.Troops gameActionTroops = GameProtos.Troops.parseFrom(gameAction.getActionData());
			for(GameProtos.Troop t : gameActionTroops.getTroopsList()){
				Troop troop = ts.get(t.getTroopResId());
				if(troop == null){
					troop = new Troop();
					troop.setCityId(city.getCityId());
					troop.setCount(t.getCount());
					troop.setPlayerId(city.getPlayerId());
					troop.setTroopResId(t.getTroopResId());
					troop.setTroopType(type);
					battleLogic.createTroop(troop);
				}else{
					troop.setCount(troop.getCount() + t.getCount());
					battleLogic.updateTroop(troop);
				}
			}
			this.delGameAction(gameAction.getActionId());
			return null;
		}else{
			return gameAction;
		}
	}
	
	/**
	 * 获取陆军的生产队列
	 * @param cityId
	 * @return
	 * @throws InvalidProtocolBufferException 
	 */
	public GameAction getProductionLandTroop(City city,long nowTime){
		List<GameAction> gs = this.gameActionMapper.getGameActionByType(city.getCityId(), GameActionType.PRODUCTION_LAND_VALUE);
		if(gs == null || gs.size() == 0){
			return null;
		}
		GameAction gameAction = gs.get(0);
		try {
			gameAction = this.flushProductionTroop(gameAction, city, 1, nowTime);
		} catch (InvalidProtocolBufferException e) {
			log.error("gameaction syn error",e);
			return null;
		}
		return gameAction;
	}
	/**
	 * 获取一个玩家的动作集合
	 * @param playerId
	 * @return
	 */
	public List<GameAction> getGameActions(long playerId){
		return this.gameActionMapper.getGameActions(playerId);
	}
	
	/**
	 * 获取空军的生产队列
	 * @param cityId
	 * @return
	 * @throws InvalidProtocolBufferException 
	 */
	public GameAction getProductionFlyTroop(City city, long nowTime){
		List<GameAction> gs = this.gameActionMapper.getGameActionByType(city.getCityId(), GameActionType.PRODUCTION_FLY_VALUE);
		if(gs == null || gs.size() == 0){
			return null;
		}
		GameAction gameAction = gs.get(0);
		try {
			gameAction = this.flushProductionTroop(gameAction, city, 2, nowTime);
		} catch (InvalidProtocolBufferException e) {
			log.error("gameaction syn error",e);
			return null;
		}
		return gameAction;
	}
	
	/**
	 * 获取所有的部队生产队列
	 * @param cityId
	 * @return
	 * @throws InvalidProtocolBufferException 
	 */
	public List<GameAction> getProductionTroop(City city,long nowTime) throws InvalidProtocolBufferException{
		List<GameAction> list = new ArrayList<GameAction>();
		list.add(this.getProductionFlyTroop(city,nowTime));
		list.add(this.getProductionLandTroop(city,nowTime));
		return list;
	}
	public List<TransportTask> getTransportTasksByPlayer(Long playerId) {
		ArrayList<TransportTask> tasks = new ArrayList<>();
		for(GameAction ga : this.getGameActions(playerId)){
			try {
				TransportTask.Builder t = TransportTask.parseFrom(ga.getActionData()).toBuilder();
				City sourceCity = cityLogic.getCity(ga.getSourceCityId());
				City targetCity = cityLogic.getCity(ga.getTargetCityId());
				Player sourcePlayer = playerLogic.getPlayer(sourceCity.getPlayerId());
				Player targetPlayer = playerLogic.getPlayer(targetCity.getPlayerId());
				
				t.setSourcePlayerId(ga.getSourcePlayerId());
				t.setTargetPlayerId(ga.getTargetPlayerId());
				t.setSourcePlayerName(sourcePlayer.getPlayerName());
				t.setTargetPlayerName(targetPlayer.getPlayerName());
				t.setSourceCityId(sourceCity.getCityId());
				t.setTargetCityId(targetCity.getCityId());
				t.setSourceCityName(sourceCity.getCityName());
				t.setTargetCityName(targetCity.getCityName());
				t.setSourceCityLevel(sourceCity.getLevel());
				t.setTargetCityLevel(targetCity.getLevel());

				t.setTaskId(ga.getActionId());
				t.setStatus(TransportStatus.valueOf(ga.getActionState()));
				t.setOverTime((int)((ga.getOverTime().getTime())/1000));
				
				tasks.add(t.build());
				
			} catch (InvalidProtocolBufferException e) {
				log.error("sync error", e);
				return new ArrayList<TransportTask>();
			}
		}
		return tasks;
	}
	
	public void createBattleRound(BattleField field, City sourceCity, City targetCity, TransportTask task) {
		GameAction gameAction = new GameAction();
		gameAction.setActionType(GameActionType.BATTLE_ROUND_VALUE);
		gameAction.setSourcePlayerId(sourceCity.getPlayerId());
		gameAction.setTargetPlayerId(targetCity.getPlayerId());
		gameAction.setTargetCityId(targetCity.getCityId());
		gameAction.setSourceCityId(sourceCity.getCityId());
		gameAction.setShipCount(task.getShipCount());
		
		long nowTime = System.currentTimeMillis();
		long overTime = field.getNextActionTime() + nowTime;
		gameAction.setCreateTime(new Timestamp(nowTime));
		gameAction.setOverTime(new Timestamp(overTime));
		gameAction.setLoadingTime(0);
		gameAction.setActionData(field.toByteArray());
		gameAction.setActionState(task.getStatus().getNumber());
		
		this.gameActionMapper.createGameAction(gameAction);
		gameActionCenter.addGameAction(gameAction.getActionId(), overTime);
		
	}
}
