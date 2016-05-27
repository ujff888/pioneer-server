package cn.litgame.wargame.core.logic.queue.impl;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.TroopId;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.CityStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.logic.BuildingLogic;
import cn.litgame.wargame.core.logic.CityLogic;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.logic.MapLogic;
import cn.litgame.wargame.core.logic.ShipLogic;
import cn.litgame.wargame.core.logic.queue.GameActionCenter;
import cn.litgame.wargame.core.logic.queue.GameActionEvent;
import cn.litgame.wargame.core.logic.queue.GameActionLogic;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.GameAction;

import com.google.protobuf.InvalidProtocolBufferException;

@Service
public class CreateCityGameAction extends GameActionEvent {

	@Override
	public GameActionType getGameActionType() {
		return GameActionType.CREATE_CITY;
	}

	@Override
	public void doLogic(GameAction gameAction, long nowTime) {
		if(gameAction.getActionState() == TransportStatus.TRANSIT_VALUE){
			targetCity.setCityStatus(CityStatus.CITY_NORMAL_VALUE);
			CityResource cityResource = null;
			try {
				cityResource = TransportTask.parseFrom(gameAction.getActionData()).getResource();
			} catch (InvalidProtocolBufferException e) {
				log.error("syn error",e);
				return;
			}
			targetCity.setWood(cityResource.getWood() + 250);
			targetCity.setFood(cityResource.getFood());
			targetCity.setCrystal(cityResource.getCrystal());
			targetCity.setMetal(cityResource.getMetal());
			targetCity.setStone(cityResource.getStone());
			targetCity.setTotalPerson(configLogic.getGlobalConfig().getCreateCityPerson());
			cityLogic.updateCity(targetCity);
			buildingLogic.createBuilding(targetCity.getPlayerId(), targetCity.getCityId(), 1001, 0,false);
			shipLogic.useOverShip(gameAction.getSourcePlayerId(), gameAction.getShipCount());
			gameActionLogic.delGameAction(gameAction.getActionId());
		}else{
			throw new RuntimeException("gameaction state error,gameAction=="+gameAction);
		}
	}
}
