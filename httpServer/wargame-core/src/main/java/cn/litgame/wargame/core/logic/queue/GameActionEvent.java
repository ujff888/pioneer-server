package cn.litgame.wargame.core.logic.queue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.logic.BuildingLogic;
import cn.litgame.wargame.core.logic.CityLogic;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.logic.MapLogic;
import cn.litgame.wargame.core.logic.PlayerLogic;
import cn.litgame.wargame.core.logic.ShipLogic;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.GameAction;

import com.google.protobuf.InvalidProtocolBufferException;


public abstract class GameActionEvent {
	
	@Resource(name = "shipLogic")
	protected ShipLogic shipLogic;
	
	@Resource(name = "cityLogic")
	protected CityLogic cityLogic;
	
	@Resource(name = "configLogic")
	protected ConfigLogic configLogic;
	
	@Resource(name = "buildingLogic")
	protected BuildingLogic buildingLogic;
	
	@Resource(name = "mapLogic")
	protected MapLogic mapLogic;
	
	@Resource(name = "gameActionLogic")
	protected GameActionLogic gameActionLogic;
	
	@Resource(name = "gameActionCenter")
	protected GameActionCenter gameActionCenter;
	
	@Resource(name = "playerLogic")
	protected PlayerLogic playerLogic;
	
	protected final static Logger log = Logger.getLogger(GameActionEvent.class);
	
	protected TransportTask transportTask;
	protected City sourceCity;
	protected City targetCity;
	
	/**
	 * 初始化数据
	 * @param ga
	 * @throws InvalidProtocolBufferException 
	 */
	public void init(GameAction ga) throws InvalidProtocolBufferException{
		this.transportTask = TransportTask.parseFrom(ga.getActionData());
		this.sourceCity = cityLogic.getCity(ga.getSourceCityId());
		this.targetCity = cityLogic.getCity(ga.getTargetCityId());
	}
	
	public City getSourceCity() {
		return sourceCity;
	}

	public void setSourceCity(City sourceCity) {
		this.sourceCity = sourceCity;
	}

	public City getTargetCity() {
		return targetCity;
	}

	public void setTargetCity(City targetCity) {
		this.targetCity = targetCity;
	}

	@PostConstruct
	public void regiest(){
		gameActionCenter.regiestGameActionEvent(this.getGameActionType().getNumber(), this);
	}
	
	public void setTransportTask(TransportTask task){
		this.transportTask = task;
	}
	
	public abstract GameActionType getGameActionType();
	public abstract void doLogic(GameAction gameAction,long nowTime) throws InvalidProtocolBufferException ;
}
