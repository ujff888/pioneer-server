package cn.litgame.wargame.core.logic.queue.impl;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.logic.BattleLogic;
import cn.litgame.wargame.core.logic.queue.GameActionEvent;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.GameAction;
import cn.litgame.wargame.core.model.battle.Army;
import cn.litgame.wargame.core.model.battle.BattleField;
import cn.litgame.wargame.core.model.battle.BattleRound;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.log4j.Logger;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class BattleRoundActionEvent extends GameActionEvent{
	private static final Logger log = Logger.getLogger(BattleRoundActionEvent.class);
	
	@Resource(name = "jedisStoragePool")
	private JedisPool jedisStoragePool;
	
	@Resource(name = "battleLogic")
	private BattleLogic battleLogic;
	
	private BattleField field;
	
	@Override
	public void init(GameAction gameAction) throws InvalidProtocolBufferException{
		this.field = BattleField.parseFromByteArray(gameAction.getActionData());
		this.sourceCity = cityLogic.getCity(gameAction.getSourceCityId());
		this.targetCity = cityLogic.getCity(gameAction.getTargetCityId());
	}
	
	@Override
	public GameActionType getGameActionType() {
		return GameActionType.BATTLE_ROUND;
	}

	@Override
	public void doLogic(GameAction gameAction, long nowTime) throws InvalidProtocolBufferException {
		if(gameAction.getActionState() == TransportStatus.PK_VALUE) {
			if(field.getResult() == GameProtos.BattleResult.FIGHTING){
				field.nextRound();
				BattleRound round = field.saveRound();
				
				this.saveBattle(round);
				this.updateGameAction(gameAction);
			}
			
			if(field.getResult() != GameProtos.BattleResult.FIGHTING){
				TransportTask.Builder newTask = TransportTask.newBuilder();
				newTask.setType(GameActionType.TRANSPORT);
				newTask.setShipCount(gameAction.getShipCount());
				
				if(gameAction.getActionState() == TransportStatus.PLUNDER_VALUE){
					plunder(newTask, gameAction);
				}else if(gameAction.getActionState() == TransportStatus.GUARD_VALUE){
					
				}else if(gameAction.getActionState() == TransportStatus.INVADE_VALUE){
					
				}
				
				
				gameActionLogic.delGameAction(gameAction.getActionId());
				
			}
			
		}
	}
	
	private void plunder(TransportTask.Builder newTask, GameAction gameAction){
		if(field.getResult() == GameProtos.BattleResult.OFFENCE_WIN){
			//检查码头
			List<Building> wharfs = buildingLogic.getBuildings(targetCity.getCityId(), 1008);
			CityResource.Builder resource = CityResource.newBuilder();
			
			int wharfSpeed = 0;
			if(wharfs != null && wharfs.size() > 0){
				for(Building b : wharfs){
					wharfSpeed += configLogic.getResBuild(b.getBuildId(), b.getLevel()).getArg1();
				}
			}
			
			for(Army a : field.getArmysOffence()){
				resource = this.grabResource(targetCity, gameAction.getShipCount(), field.getArmysOffence().size());
				newTask.setResource(resource);
				cityLogic.removeCityResource(targetCity, resource.getWood(), resource.getStone(), resource.getCrystal(), resource.getMetal(), resource.getFood());
				
				long loadingTime = shipLogic.getResourceShipTime(resource.build(), 0, wharfSpeed);
				
				if(field.isLand()){
					newTask.addAllLandTroops(a.convertToTroops());
				}else{
					newTask.addAllShipTroops(a.convertToTroops());
				}
				City c = cityLogic.getCity(a.getCityId());
				shipLogic.useOverShip(c.getPlayerId(), newTask.getShipCount());
				gameActionLogic.createTransportShipTask(newTask, targetCity, cityLogic.getCity(a.getCityId()), loadingTime, true);		
			}
		}else{
			for(Army a : field.getArmysOffence()){
				if(field.isLand()){
					newTask.addAllLandTroops(a.convertToTroops());
				}else{
					newTask.addAllShipTroops(a.convertToTroops());
				}
				shipLogic.useOverShip(sourceCity.getPlayerId(), newTask.getShipCount());
				gameActionLogic.createTransportShipTaskSkipLoading(newTask, targetCity, cityLogic.getCity(a.getCityId()), true);	
			}
		}
	}
	
	private CityResource.Builder grabResource(City targetCity, int shipCount, int plunderCount) {
		int totalResourceCount = shipCount * 500;
		CityResource.Builder resource = CityResource.newBuilder();
		int safeCapacity = 0;
		List<Building> wareHouses = buildingLogic.getBuildings(targetCity.getCityId(), 1003);
		for(Building b : wareHouses){
			safeCapacity += b.getLevel() * 400;
		}
		
		int food = (targetCity.getFood() - safeCapacity) / plunderCount;
		int wood = (targetCity.getWood() - safeCapacity) / plunderCount;
		int stone = (targetCity.getStone() - safeCapacity) / plunderCount;
		int metal = (targetCity.getMetal() - safeCapacity) / plunderCount;
		int crystal = (targetCity.getCrystal() - safeCapacity) / plunderCount;
		
		int[] resourceArray = {food,wood,stone,metal,crystal};
		
		ArrayList<Integer> array = new ArrayList<>();
		for(int i : resourceArray){
			array.add(i);
		}
		
		Collections.sort(array);
		ListIterator<Integer> itr = array.listIterator(array.size());
		while(totalResourceCount > 0 && itr.hasPrevious()){
			int i = itr.previous();
			int grabCount = Math.min(i, totalResourceCount);

			if(i == food){
				resource.setFood(grabCount);
			}else if(i == wood){
				resource.setWood(grabCount);
			}else if(i == stone){
				resource.setStone(grabCount);
			}else if(i == metal){
				resource.setMetal(grabCount);
			}else if(i == crystal){
				resource.setCrystal(grabCount);
			}
			totalResourceCount -= grabCount;
		}
		return resource;
		
	}

	private void updateGameAction(GameAction gameAction) {
		gameAction.setActionData(field.toByteArray());
		
		long nowTime = System.currentTimeMillis();
		long nextTime = field.getNextActionTime();
		gameAction.setOverTime(new Timestamp(nowTime + nextTime));
		
		gameActionLogic.updateGameAction(gameAction);
	}

	private void saveBattle(BattleRound round) {
		

	}

	private byte[] buildPlayerBattleKey(Army a){
		String key = "battles_" + a.getPlayerId();
		try {
			return key.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			return new byte[0];
		}
	}

	private byte[] buildFieldKey(BattleField field){
		StringBuilder sb = new StringBuilder();
		sb.append(field.getFieldCityId());
		sb.append(field.getStartTime());
		sb.append(field.isLand());
		try {
			return sb.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			return new byte[0];
		}
	}

	private byte[] buildRedisKey(String key){
		try {
			return key.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			return new byte[0];
		}
	}

	private byte[] buildRoundKey(BattleField field){
		StringBuilder sb = new StringBuilder();
		sb.append(field.getFieldCityId());
		sb.append(field.getStartTime());
		sb.append(field.isLand());
		sb.append(field.getCurrentRoundNum()-1);
		try {
			return sb.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			return new byte[0];
		}
	}
}
