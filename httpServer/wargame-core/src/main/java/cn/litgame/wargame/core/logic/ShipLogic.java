package cn.litgame.wargame.core.logic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.google.protobuf.InvalidProtocolBufferException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.TroopId;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.ShipType;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.auto.GameProtos.Troop;
import cn.litgame.wargame.core.auto.GameResProtos.ResourceType;
import cn.litgame.wargame.core.logic.queue.GameActionCenter;
import cn.litgame.wargame.core.logic.queue.GameActionLogic;
import cn.litgame.wargame.core.logic.queue.impl.NextStateGameAction;
import cn.litgame.wargame.core.mapper.ShipMapper;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.GameAction;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.core.model.Ship;

@Service
public class ShipLogic {

	@Resource(name = "shipMapper")
	private ShipMapper shipMapper;
	
	@Resource(name = "jedisStoragePool")
	private JedisPool jedisStoragePool;
	
	@Resource(name = "gameActionLogic")
	private GameActionLogic gameActionLogic;
	
	@Resource(name = "cityLogic")
	private CityLogic cityLogic;
	
	@Resource(name = "mapLogic")
	private MapLogic mapLogic;
	
	@Resource(name = "configLogic")
	protected ConfigLogic configLogic;
	
	@Resource(name = "gameActionCenter")
	protected GameActionCenter gameActionCenter;
	
	private final static String transport_ship_key = "transport_ship";
	
	private final static Logger log = Logger.getLogger(ShipLogic.class);
	
	/**
	 * 取消运输船
	 * @param playerId
	 * @param actionId
	 * @return
	 */
	public MessageCode cancelTransportTask(long playerId,long actionId){
		GameAction gameAction = gameActionLogic.getGameAction(actionId);
		if(gameAction == null){
			return MessageCode.NOT_FOUND_GAME_ACTION;
		}
		if(playerId != gameAction.getSourcePlayerId()){
			return MessageCode.ONLY_OPER_SELF_DATA;
		}
		
		if(gameAction.getActionState() == TransportStatus.LOADING_VALUE
				||gameAction.getActionState() == TransportStatus.WAITING_VALUE){
			
			//装载和等待状态下，直接取消、返还给主城市资源
			
			TransportTask task = null;
			try {
				task = TransportTask.parseFrom(gameAction.getActionData());
			} catch (InvalidProtocolBufferException e) {
				log.error("sync error", e);
				return MessageCode.ERR;
			}
			
			if(task.getType() == GameActionType.CREATE_CITY){
				cityLogic.deleteCity(gameAction.getTargetCityId());
				City sourceCity = cityLogic.getCity(gameAction.getSourceCityId());
				sourceCity.addResource(ResourceType.WOOD, configLogic.getGlobalConfig().getCreateCityWood());
				sourceCity.setTotalPerson(sourceCity.getTotalPerson() + configLogic.getGlobalConfig().getCreateCityPerson());
				
				cityLogic.updateCity(sourceCity);
			}
			
			CityResource resource = task.getResource();
			City sourceCity = cityLogic.getCity(gameAction.getSourceCityId());
			sourceCity.setWood(sourceCity.getWood() + resource.getWood());
			sourceCity.setFood(sourceCity.getFood() + resource.getFood());
			sourceCity.setMetal(sourceCity.getMetal() + resource.getMetal());
			sourceCity.setStone(sourceCity.getStone() + resource.getStone());
			sourceCity.setCrystal(sourceCity.getCrystal() + resource.getCrystal());
			
			cityLogic.updateCity(sourceCity);
			
			this.useOverShip(playerId, gameAction.getShipCount());
			gameActionLogic.delGameAction(gameAction.getActionId());
			
			if(gameAction.getActionState() == TransportStatus.LOADING_VALUE){
				//检查一下是否有等待状态的动作，如果有的话，就进行装载
				GameAction waitGameAction = gameActionLogic.getWaitingGameAction(sourceCity.getCityId());
				
				if(waitGameAction != null){
					TransportTask newTask;
					try {
						newTask = TransportTask.parseFrom(waitGameAction.getActionData());
					} catch (InvalidProtocolBufferException e) {
						log.error("sync error", e);
						return MessageCode.ERR;
					}
					long nowTime = System.currentTimeMillis();
					newTask = newTask.toBuilder().setLoadingStartTime((int) (nowTime/1000)).build();
					
					waitGameAction.setActionData(newTask.toByteArray());
					waitGameAction.setActionState(TransportStatus.LOADING_VALUE);
					waitGameAction.setOverTime(new Timestamp(nowTime + waitGameAction.getLoadingTime()));
					gameActionLogic.updateGameAction(waitGameAction);
					gameActionCenter.addGameAction(waitGameAction.getActionId(), gameAction.getOverTime().getTime());
			}
			}
		}else if(gameAction.getActionState() == TransportStatus.TRANSIT_VALUE){
			//运输状态，直接返回逻辑，更改队列的计时器，等返回到城市后，执行到达的逻辑
			TransportTask originalTask = null;
			try {
				originalTask = TransportTask.parseFrom(gameAction.getActionData());
			} catch (InvalidProtocolBufferException e) {
				log.error("sync error", e);
				return MessageCode.ERR;
			}
			
			if(originalTask.getType() == GameActionType.CREATE_CITY){
				cityLogic.deleteCity(gameAction.getTargetCityId());
			}

			TransportTask.Builder backTask = TransportTask.newBuilder();
			int nowTime = (int) System.currentTimeMillis()/1000;
			City sourceCity = cityLogic.getCity(originalTask.getSourceCityId());
			
			backTask.setCreateTime(nowTime);
			backTask.setShipCount(originalTask.getShipCount());
			
			backTask.setTargetCityId(originalTask.getSourceCityId());
			backTask.setTargetCityName(cityLogic.getCity(originalTask.getSourceCityId()).getCityName());
			
			backTask.setSourceCityId(originalTask.getTargetCityId());
			backTask.setResource(originalTask.getResource());
			
			backTask.addAllLandTroops(originalTask.getLandTroopsList());
			backTask.addAllShipTroops(originalTask.getShipTroopsList());
			
			backTask.setOverTime(2*nowTime - originalTask.getShipoutTime());
			
			backTask.setStatus(TransportStatus.BACKING);
			backTask.setType(originalTask.getType());
			
			this.useOverShip(playerId, originalTask.getShipCount());
			
			//TODO:行动点减一
			gameActionLogic.createTransportShipTask(backTask, sourceCity, sourceCity, 0);
			gameActionLogic.delGameAction(gameAction.getActionId());
			
		}else{
			throw new RuntimeException("action state error,can't cancel,gameAction="+gameAction);
		}
		return MessageCode.OK;
	}
	
	/**
	 * 立即结束运输任务（加速）
	 * 
	 * @param taskId
	 * @return
	 */
	public MessageCode overTransportTask(long playerId, long actionId) {
		GameAction gameAction = gameActionLogic.getGameAction(actionId);
		if(gameAction == null){
			return MessageCode.NOT_FOUND_GAME_ACTION;
		}
		if(playerId != gameAction.getSourcePlayerId()){
			return MessageCode.ONLY_OPER_SELF_DATA;
		}
		if(gameAction.getActionState() == TransportStatus.WAITING_VALUE){
			return MessageCode.ERR;
		}
		if(gameAction.getActionState() == TransportStatus.LOADING_VALUE){
			City sourceCity = cityLogic.getCity(gameAction.getSourceCityId());
			City targetCity = cityLogic.getCity(gameAction.getTargetCityId());
			long nowTime = System.currentTimeMillis();
			TransportTask transportTask;
			
			try {
				transportTask = TransportTask.parseFrom(gameAction.getActionData());
			} catch (InvalidProtocolBufferException e) {
				log.error("sync error", e);
				return MessageCode.ERR;
			}
			
			//将装载状态的动作变成运输状态，并且重新放入定时器里
			long transportTime = mapLogic.getLandTimeDistance(sourceCity.getLandId(), targetCity.getLandId()
					,configLogic.getResTroop(TroopId.ship_VALUE).getSpeed());
			
			gameAction.setActionState(TransportStatus.TRANSIT_VALUE);
			gameAction.setOverTime(new Timestamp(nowTime + transportTime * 1000));
			gameAction.setActionType(transportTask.getType().getNumber());
			transportTask = transportTask.toBuilder().setShipoutTime((int) nowTime/1000).build();
			
			gameActionLogic.updateGameAction(gameAction);
			gameActionCenter.addGameAction(gameAction.getActionId(), gameAction.getOverTime().getTime());
			//检查一下是否有等待状态的动作，如果有的话，就进行装载
			GameAction waitGameAction = gameActionLogic.getWaitingGameAction(sourceCity.getCityId());
			
			if(waitGameAction != null){
				TransportTask task;
				try {
					task = TransportTask.parseFrom(waitGameAction.getActionData());
				} catch (InvalidProtocolBufferException e) {
					log.error("sync error", e);
					return MessageCode.ERR;
				}
				nowTime = System.currentTimeMillis();
				task = task.toBuilder().setLoadingStartTime((int) (nowTime/1000)).build();
				
				waitGameAction.setActionData(task.toByteArray());
				waitGameAction.setActionState(TransportStatus.LOADING_VALUE);
				waitGameAction.setOverTime(new Timestamp(nowTime + waitGameAction.getLoadingTime()));
				gameActionLogic.updateGameAction(waitGameAction);
				gameActionCenter.addGameAction(waitGameAction.getActionId(), gameAction.getOverTime().getTime());
			}
		}
		if(gameAction.getActionState() == TransportStatus.TRANSIT_VALUE ||
				gameAction.getActionState() == TransportStatus.BACKING_VALUE){
			TransportTask transportTask;
			try {
				transportTask = TransportTask.parseFrom(gameAction.getActionData());
			} catch (InvalidProtocolBufferException e) {
				log.error("sync error", e);
				return MessageCode.ERR;
			}
			//将完成时间设为当前时间，并重新放入定时队列里
			long nowTime = System.currentTimeMillis();
			transportTask = transportTask.toBuilder().setOverTime((int) nowTime/1000).build();
			gameAction.setActionData(transportTask.toByteArray());
			gameAction.setOverTime(new Timestamp(nowTime));
			
			gameActionLogic.updateGameAction(gameAction);
			gameActionCenter.addGameAction(gameAction.getActionId(), gameAction.getOverTime().getTime());
		}
		return MessageCode.OK;
	}
	
	/**
	 * 获取运输这些物资，需要多少搜的运输船
	 * @param resource
	 * @return
	 */
	public int getResourceShipCount(CityResource resource,int otherCount){
		double count = resource.getWood() + resource.getFood() + resource.getCrystal() + resource.getMetal()
				+ resource.getStone() + resource.getPerson() + otherCount;
		
		return (int)Math.ceil(count/500);
	}
	
	/**
	 * 获取装载这些物资，需要多久的时间
	 * @param resource
	 * @return 返回的是需要多少豪秒
	 */
	public long getResourceShipTime(CityResource resource,int otherCount,double speed){
		double count = resource.getWood() + resource.getFood() + resource.getCrystal() + resource.getMetal()
				+ resource.getStone() + resource.getPerson() + otherCount;
		
		return (int)((count / (speed/60)) * 1000);
	}
	
	/**
	 * 获取当前可用的运输船数量
	 * @param playerId
	 * @return
	 */
	public int getFreeShip(long playerId){
		List<Ship> ships = this.getShips(playerId);
		int count = 0;
		for(Ship ship : ships){
			count += ship.getCount();
		}
		
		return count - this.getUseShipCount(playerId);
	}
	
	/**
	 * 使用运输船，要在这里计数
	 * @param playerId
	 * @param count
	 */
	public void useShip(long playerId,int count){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			int c = this.getUseShipCount(playerId);
			jedis.hset(transport_ship_key, String.valueOf(playerId), String.valueOf(c + count));
		}finally{
			jedis.close();
		}
	}
	
	/**
	 * 使用完成后，在这里通知一下
	 * @param playerId
	 * @param count
	 */
	public void useOverShip(long playerId,int count){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			int c = this.getUseShipCount(playerId) - count;
			if(c < 0){
				c = 0;
			}
			jedis.hset(transport_ship_key, String.valueOf(playerId), String.valueOf(c));
		}finally{
			jedis.close();
		}
	}
	
	/**
	 * 获取当前有几艘运输船在使用中
	 * @param playerId
	 * @return
	 */
	public int getUseShipCount(long playerId){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			String v = jedis.hget(transport_ship_key, String.valueOf(playerId));
			if(StringUtils.isBlank(v)){
				return 0;
			}
			return Integer.valueOf(v);
		}finally{
			jedis.close();
		}
	}
	
	/**
	 * 获取金币购买运输船的价格
	 * @param count
	 * @return
	 */
	public int getGoldShipPrice(int count){
		return 800 + (count -1) * 400;
	}
	
	/**
	 * 获取钻石购买运输船的价格
	 * @param count
	 * @return
	 */
	public int getDiamondShipPrice(int count){
		return 180 - count * 4;
	}
	
	/**
	 * 获取运输船的总数
	 * @param playerId
	 * @return
	 */
	public int getShipCount(long playerId){
		List<Ship> ships = this.getShips(playerId);
		int count = 0;
		for(Ship ship : ships){
			count += ship.getCount();
		}
		return count;
	}
	
	/**
	 * 获取全部运输船
	 * @param playerId
	 * @return
	 */
	public List<Ship> getShips(long playerId){
		List<Ship> ships = this.shipMapper.getShipsByPlayerId(playerId);
		if(ships == null){
			return new ArrayList<Ship>();
		}
		return ships;
	}
	/**
	 * 获取某种类型的运输船
	 * @param playerId
	 * @param type
	 * @return
	 */
	public Ship getShipByType(long playerId,ShipType type){
		return this.shipMapper.getShipByType(playerId, type.getNumber());
	}
	
	/**
	 * 创建运输船
	 * @param playerId
	 * @param shipType
	 * @param count
	 * @return
	 */
	public Ship addShip(long playerId,ShipType shipType,int count){
		Ship s = this.getShipByType(playerId, shipType);
		if(s == null){
			s = new Ship(playerId,shipType.getNumber(),count);
			this.shipMapper.addShip(s);
			return s;
		}
		s.setCount(s.getCount() + count);
		this.shipMapper.updateShip(s);
		return s;
	}
	
	/**
	 * 删除运输船
	 * @param playerId
	 * @param shipType
	 * @param count
	 */
	public void removeShip(long playerId,ShipType shipType,int count){
		Ship s = this.getShipByType(playerId, shipType);
		if(s == null){
			return;
		}
		s.setCount(s.getCount() - count);
		if(s.getCount() > 0){
			this.shipMapper.updateShip(s);
		}else{
			this.shipMapper.delShip(s.getShipId());
		}
	}
}
