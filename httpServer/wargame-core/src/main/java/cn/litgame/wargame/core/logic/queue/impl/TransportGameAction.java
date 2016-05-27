package cn.litgame.wargame.core.logic.queue.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.auto.GameProtos.Troop;
import cn.litgame.wargame.core.logic.BattleLogic;
import cn.litgame.wargame.core.logic.queue.GameActionEvent;
import cn.litgame.wargame.core.model.GameAction;

@Service
public class TransportGameAction  extends GameActionEvent {
	@Resource(name = "battleLogic")
	private BattleLogic battleLogic;
	
	@Override
	public GameActionType getGameActionType() {
		return GameActionType.TRANSPORT;
	}

	@Override
	public void doLogic(GameAction gameAction, long nowTime) {
		if( gameAction.getActionState() == TransportStatus.TRANSIT_VALUE){
			CityResource cityResource = null;
			try {
				cityResource = TransportTask.parseFrom(gameAction.getActionData()).getResource();
			} catch (InvalidProtocolBufferException e) {
				log.error("syn error",e);
				return;
			}
			if(transportTask.hasResource()){
				targetCity.setWood(targetCity.getWood() + cityResource.getWood());
				targetCity.setFood(targetCity.getFood() + cityResource.getFood());
				targetCity.setStone(targetCity.getStone() + cityResource.getStone());
				targetCity.setCrystal(targetCity.getCrystal() + cityResource.getCrystal());
				targetCity.setMetal(targetCity.getMetal() + cityResource.getMetal());
				
				cityLogic.updateCity(targetCity);
			}
			
			if(transportTask.getLandTroopsCount() > 0){
				for(Troop t : transportTask.getLandTroopsList()){
					battleLogic.increaseTroop(targetCity, t);
				}
			}
			
			if(transportTask.getShipTroopsCount() > 0){
				for(Troop t : transportTask.getShipTroopsList()){
					battleLogic.increaseTroop(targetCity, t);
				}
			}
			
			shipLogic.useOverShip(gameAction.getSourcePlayerId(), gameAction.getShipCount());
			gameActionLogic.delGameAction(gameAction.getActionId());
		}else{
			throw new RuntimeException("gameaction state error,gameAction=="+gameAction);
		}
	}

}
