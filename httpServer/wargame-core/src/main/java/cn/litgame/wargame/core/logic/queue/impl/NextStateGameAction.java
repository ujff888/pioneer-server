package cn.litgame.wargame.core.logic.queue.impl;

import java.sql.Timestamp;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.TroopId;
import cn.litgame.wargame.core.auto.GameProtos.TransportStatus;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.logic.queue.GameActionEvent;
import cn.litgame.wargame.core.model.GameAction;

import com.google.protobuf.InvalidProtocolBufferException;


/**
 * 这个类目前负责处理状态切换，触发新的定时任务
 * @author Administrator
 *
 */
@Service
public class NextStateGameAction  extends GameActionEvent {

	@Override
	public GameActionType getGameActionType() {
		
		return GameActionType.NEXT_STATE_ACTION;
	}

	@Override
	public void doLogic(GameAction gameAction, long nowTime) throws InvalidProtocolBufferException {
		
		if(gameAction.getActionState() == TransportStatus.LOADING_VALUE || gameAction.getActionState() == TransportStatus.TRANSIT_VALUE){
			
			//将装载状态的动作变成运输状态，并且重新放入定时器里
			long transportTime = mapLogic.getLandTimeDistance(sourceCity.getLandId(), targetCity.getLandId()
					,configLogic.getResTroop(TroopId.ship_VALUE).getSpeed());
			
			gameAction.setActionState(TransportStatus.TRANSIT_VALUE);
			gameAction.setOverTime(new Timestamp(nowTime + transportTime * 1000));
			gameAction.setActionType(transportTask.getType().getNumber());
			transportTask = transportTask.toBuilder().setShipoutTime((int) nowTime/1000).build();
			
			gameActionLogic.updateGameAction(gameAction);
			gameActionCenter.addGameAction(gameAction.getActionId(), gameAction.getOverTime().getTime());
			
			if(gameAction.getActionState() == TransportStatus.TRANSIT_VALUE)
				return;
			
			//检查一下是否有等待状态的动作，如果有的话，就进行装载
			GameAction waitGameAction = gameActionLogic.getWaitingGameAction(sourceCity.getCityId());
			log.info(waitGameAction.toString());
			if(waitGameAction != null){
				TransportTask task = TransportTask.parseFrom(waitGameAction.getActionData());
				nowTime = System.currentTimeMillis();
				task = task.toBuilder().setLoadingStartTime((int) (nowTime/1000)).build();
				
				waitGameAction.setActionData(task.toByteArray());
				waitGameAction.setActionState(TransportStatus.LOADING_VALUE);
				waitGameAction.setOverTime(new Timestamp(waitGameAction.getLoadingTime()));
				gameActionLogic.updateGameAction(waitGameAction);
				gameActionCenter.addGameAction(waitGameAction.getActionId(), gameAction.getOverTime().getTime());
			}
		}
	}

}
