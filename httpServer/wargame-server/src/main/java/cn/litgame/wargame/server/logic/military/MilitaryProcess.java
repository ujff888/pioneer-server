package cn.litgame.wargame.server.logic.military;

import java.util.List;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.core.auto.GameProtos.CSMilitaryAction;
import cn.litgame.wargame.core.auto.GameProtos.CSMilitaryInfo;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.MilitaryActionType;
import cn.litgame.wargame.core.auto.GameProtos.SCMilitaryAction;
import cn.litgame.wargame.core.auto.GameProtos.SCMilitaryInfo;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.auto.GameProtos.TroopInfo;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.server.message.KHttpMessageProcess;
@Service
public class MilitaryProcess extends KHttpMessageProcess {

	public void militaryInfo(CSMilitaryInfo csMilitaryInfo) {
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		builder.setMessageType(MessageType.MSG_ID_MILITARY_INFO);
		Player player = httpMessageManager.getPlayer();
		if(player == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_PLAYER);
			httpMessageManager.send(builder);
			return;
		}
		SCMilitaryInfo.Builder scMilitaryInfo = SCMilitaryInfo.newBuilder();
		List<TransportTask> tasks = gameActionLogic.getTransportTasksByPlayer(player.getPlayerId());
		scMilitaryInfo.addAllTasks(tasks);
		
		builder.setScMilitaryInfo(scMilitaryInfo);
		builder.setMessageCode(MessageCode.OK);
		httpMessageManager.send(builder);
	
	}

	public void militaryAction(CSMilitaryAction csMilitaryAction) {
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		builder.setMessageCode(MessageCode.OK);
		builder.setMessageType(MessageType.MSG_ID_MILITARY_ACTION);
		Player player = httpMessageManager.getPlayer();
		SCMilitaryAction.Builder scMilitaryAction = SCMilitaryAction.newBuilder();
		TroopInfo troopInfo = csMilitaryAction.getTroopInfo();
		
		int troopWeight = battleLogic.getTroopWeight(troopInfo);
		
		if(csMilitaryAction.getShipNum() > shipLogic.getFreeShip(player.getPlayerId())){
			builder.setMessageCode(MessageCode.NOT_ENOUGH_TRANSPORTATION);
			builder.setScMilitaryAction(scMilitaryAction);
			httpMessageManager.send(builder);
		}
		
		MessageCode mc = battleLogic.checkTroop(csMilitaryAction.getSourceCityId(), troopInfo);
		
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			builder.setScMilitaryAction(scMilitaryAction);
			httpMessageManager.send(builder);
		}
		
		battleLogic.removeTroopFromCity(troopInfo);
		
		TransportTask.Builder task = TransportTask.newBuilder();
		task.setType(GameActionType.BATTLE);
		task.setSourceCityId(csMilitaryAction.getSourceCityId());
		task.setTargetCityId(csMilitaryAction.getTargetCityId());
		task.addAllLandTroops(troopInfo.getLandTroopList());
		task.addAllShipTroops(troopInfo.getFlyTroopList());
		task.setShipCount(csMilitaryAction.getShipNum());
		
		if(csMilitaryAction.getActionType() == MilitaryActionType.PLUNDER_CITY)
			task.setStatus(TransportStatus.PLUNDER);
		else if(csMilitaryAction.getActionType() == MilitaryActionType.INVADE_CITY || csMilitaryAction.getActionType() == MilitaryActionType.INVADE_PIER)
			task.setStatus(TransportStatus.INVADE);
		else if(csMilitaryAction.getActionType() == MilitaryActionType.GUARD_CITY || csMilitaryAction.getActionType() == MilitaryActionType.GUARD_PIER)
			task.setStatus(TransportStatus.GUARD);

		//检查码头
		List<Building> wharfs = buildingLogic.getBuildings(csMilitaryAction.getSourceCityId(), 1008);
		if(wharfs == null || wharfs.size() == 0){
			builder.setMessageCode(MessageCode.NOT_FOUND_WHARF);
			httpMessageManager.send(builder);
			return;
		}
		int wharfSpeed = 0;
		for(Building b : wharfs){
			wharfSpeed += configLogic.getResBuild(b.getBuildId(), b.getLevel()).getArg1();
		}
		
		CityResource resource = task.getResource();
		
		
		
		long loadingTime = shipLogic.getResourceShipTime(resource, troopWeight, wharfSpeed);
		
		gameActionLogic.createTransportShipTask(task, cityLogic.getCity(task.getSourceCityId()), cityLogic.getCity(task.getTargetCityId()), loadingTime);
		
		builder.setMessageCode(MessageCode.OK);
		builder.setScMilitaryAction(scMilitaryAction);
		httpMessageManager.send(builder);
		
	}

}
