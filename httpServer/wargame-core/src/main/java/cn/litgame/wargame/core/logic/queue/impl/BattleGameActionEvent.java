package cn.litgame.wargame.core.logic.queue.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.auto.GameResProtos.BattleGround;
import cn.litgame.wargame.core.auto.GameResProtos.ResTroop;
import cn.litgame.wargame.core.logic.BattleLogic;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.logic.queue.GameActionEvent;
import cn.litgame.wargame.core.model.GameAction;
import cn.litgame.wargame.core.model.Troop;
import cn.litgame.wargame.core.model.battle.Army;
import cn.litgame.wargame.core.model.battle.BattleField;
import cn.litgame.wargame.core.model.battle.troop.BattleTroop;

@Service
public class BattleGameActionEvent extends GameActionEvent {
	@Resource(name = "battleLogic")
	private BattleLogic battleLogic;
	
	@Resource(name = "configLogic")
	private ConfigLogic configLogic;
	
	@Override
	public void doLogic(GameAction gameAction,long nowTime) {
		if(transportTask.getStatus() == TransportStatus.INVADE){
			if(transportTask.getShipTroopsCount() > 0){
				
			}else if(transportTask.getLandTroopsCount() > 0){
				
			}
		}
		
		if(transportTask.getStatus() == TransportStatus.PLUNDER){
			if(transportTask.getLandTroopsCount() > 0){
				List<Army> armyOffence = new ArrayList<>();
				List<Army> armyDefence = new ArrayList<>();
				List<BattleTroop> troopsOff = new ArrayList<>();
				
				for(GameProtos.Troop t : transportTask.getLandTroopsList()){
					ResTroop.Builder rt = configLogic.getResTroop(t.getTroopResId()).toBuilder();
					rt.setAttack(t.getAttack()).setAttack2(t.getAttack2()).setDefense(t.getDefense());
					BattleTroop bt = new BattleTroop(rt.build(), t.getCount());
					troopsOff.add(bt);
				}
				armyOffence.add(new Army(sourceCity.getPlayerId(), sourceCity.getCityId(), troopsOff));
				
				List<BattleTroop> troopsDef = new ArrayList<>();

				for(Troop t : battleLogic.getTroopsByCityId(targetCity)){
					ResTroop.Builder rt = configLogic.getResTroop(t.getTroopResId()).toBuilder();
					BattleTroop bt = new BattleTroop(rt.build(), t.getCount());
					troopsDef.add(bt);
				}
				armyDefence.add(new Army(targetCity.getPlayerId(), targetCity.getCityId(), troopsDef));
				
				BattleGround ground = configLogic.getBattleGround(1, 1); 
				BattleField field = null;
				List<Troop> targetFlyTroop = battleLogic.getFlyTroopsByCityId(targetCity, true);
				
				if(transportTask.getLandTroopsCount() > 0){
					if(targetFlyTroop.size() == 0){
						field = battleLogic.initBattleField(armyOffence, armyDefence, ground, BattleField.LAND, targetCity.getCityId());	
					}
					else{
						TransportTask.Builder backTask = TransportTask.newBuilder();
						backTask.addAllLandTroops(transportTask.getLandTroopsList());
						backTask.setSourceCityId(targetCity.getCityId());
						backTask.setTargetCityId(targetCity.getCityId());
						backTask.setShipCount(transportTask.getShipCount());
						shipLogic.useOverShip(sourceCity.getPlayerId(), transportTask.getShipCount());
						gameActionLogic.createTransportShipTaskSkipLoading(backTask, targetCity, sourceCity, true);
					}
						
				}else if(transportTask.getShipTroopsCount() > 0){
					field = battleLogic.initBattleField(armyOffence, armyDefence, ground, BattleField.FLY, targetCity.getCityId());
				}
				
				gameActionLogic.createBattleRound(field, sourceCity, targetCity, transportTask);
			}
		}
		
		
		if(transportTask.getStatus() == TransportStatus.GUARD){
			if(transportTask.getShipTroopsCount() > 0){
				
			}else if(transportTask.getLandTroopsCount() > 0){
				
			}
		}
		
	}

	@Override
	public GameActionType getGameActionType() {
		return GameActionType.BATTLE;
	}

}

