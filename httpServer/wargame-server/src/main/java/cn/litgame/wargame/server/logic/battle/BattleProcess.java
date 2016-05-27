package cn.litgame.wargame.server.logic.battle;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameProtos.CSBattleList;
import cn.litgame.wargame.core.auto.GameProtos.CSCancelProductionTroop;
import cn.litgame.wargame.core.auto.GameProtos.CSDisbandTroop;
import cn.litgame.wargame.core.auto.GameProtos.CSProductionTroop;
import cn.litgame.wargame.core.auto.GameProtos.CSRoundDetail;
import cn.litgame.wargame.core.auto.GameProtos.CSRoundList;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.SCBattleList;
import cn.litgame.wargame.core.auto.GameProtos.SCCancelProductionTroop;
import cn.litgame.wargame.core.auto.GameProtos.SCDisbandTroop;
import cn.litgame.wargame.core.auto.GameProtos.SCProductionTroop;
import cn.litgame.wargame.core.auto.GameProtos.SCRoundDetail;
import cn.litgame.wargame.core.auto.GameProtos.SCRoundList;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Troop;
import cn.litgame.wargame.server.message.KHttpMessageProcess;

@Service
public class BattleProcess extends KHttpMessageProcess{

	/**
	 * 生产部队
	 * @param msg
	 */
	public void productionTroop(CSProductionTroop msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCProductionTroop.Builder scProductionTroop = SCProductionTroop.newBuilder();
		
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		httpMessageManager.changeCityResource(city);
		MessageCode mc = battleLogic.productionTroop(city,msg.getTroops(),msg.getPosition(),scProductionTroop);
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		builder.setScProductionTroop(scProductionTroop);
		builder.setNeedUpdateResource(true);
		httpMessageManager.send(builder);
	}
	
	/**
	 * 取消生产部队
	 * @param msg
	 */
	public void cancelProductionTroop(CSCancelProductionTroop msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		httpMessageManager.changeCityResource(city);
		
		Building building = buildingLogic.getBuilding(msg.getCityId(), msg.getPosition());
		MessageCode mc = battleLogic.cancelProductionTroop(city,building);
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		SCCancelProductionTroop.Builder scCancelProductionTroop = SCCancelProductionTroop.newBuilder();
		builder.setScCancelProductionTroop(scCancelProductionTroop);
		httpMessageManager.send(builder);
		
	}

	/**
	 * 解散部队
	 * @param msg
	 */
	public void disbandTroop(CSDisbandTroop msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Troop troop = battleLogic.getTroop(msg.getId());
		if(troop == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_TROOP);
			httpMessageManager.send(builder);
			return;
		}
		troop.setCount(troop.getCount() - msg.getCount());
		if(troop.getCount() <= 0){
			troop.setCount(0);
			battleLogic.delTroop(msg.getId());
		}else{
			battleLogic.updateTroop(troop);
		}
		SCDisbandTroop.Builder scDisbandTroop = SCDisbandTroop.newBuilder();
		scDisbandTroop.setId(msg.getId());
		scDisbandTroop.setCount(troop.getCount());
		builder.setScDisbandTroop(scDisbandTroop);
		httpMessageManager.send(builder);
	}

	public void battleList(CSBattleList csBattleList) {
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCBattleList.Builder scBattleList = SCBattleList.newBuilder();
		
		builder.setScBattleList(scBattleList);
		httpMessageManager.send(builder);
		
	}

	public void roundList(CSRoundList csRoundList) {
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCRoundList.Builder scRoundList = SCRoundList.newBuilder();
		
		builder.setScRoundList(scRoundList);
		httpMessageManager.send(builder);
	}

	public void roundDetail(CSRoundDetail csRoundDetail) {
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCRoundDetail.Builder scRoundDetail = SCRoundDetail.newBuilder();
		
		builder.setScRoundDetail(scRoundDetail);
		httpMessageManager.send(builder);
	}
}
