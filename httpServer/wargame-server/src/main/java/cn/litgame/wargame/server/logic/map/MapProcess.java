package cn.litgame.wargame.server.logic.map;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameProtos.CSLandDonation;
import cn.litgame.wargame.core.auto.GameProtos.CSSetWorker;
import cn.litgame.wargame.core.auto.GameProtos.CSShowCity;
import cn.litgame.wargame.core.auto.GameProtos.CSShowLand;
import cn.litgame.wargame.core.auto.GameProtos.CSShowLandResource;
import cn.litgame.wargame.core.auto.GameProtos.CSShowWorld;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.SCLandDonation;
import cn.litgame.wargame.core.auto.GameProtos.SCSetWorker;
import cn.litgame.wargame.core.auto.GameProtos.SCShowCity;
import cn.litgame.wargame.core.auto.GameProtos.SCShowLand;
import cn.litgame.wargame.core.auto.GameProtos.SCShowLandResource;
import cn.litgame.wargame.core.auto.GameProtos.SCShowWorld;
import cn.litgame.wargame.core.logic.BuildingLogic;
import cn.litgame.wargame.core.logic.CityLogic;
import cn.litgame.wargame.core.logic.MapLogic;
import cn.litgame.wargame.core.logic.PlayerLogic;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.server.logic.HttpMessageManager;
import cn.litgame.wargame.server.message.KHttpMessageProcess;

@Service
public class MapProcess  extends KHttpMessageProcess  {
	
	public void setWorker(CSSetWorker msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCSetWorker.Builder scSetWorker = SCSetWorker.newBuilder();
		
		MessageCode mc = mapLogic.setWorker(msg.getCityId(),msg.getWorkersList());
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		builder.setScSetWorker(scSetWorker);
		httpMessageManager.send(builder);
		
	}
	
	public void showLandResource(CSShowLandResource msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCShowLandResource.Builder scShowLandResource = SCShowLandResource.newBuilder();
		
		MessageCode mc = mapLogic.showLandResource(msg.getLandId(), msg.getType(), msg.getCityId(),scShowLandResource);
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		builder.setScShowLandResource(scShowLandResource);
		httpMessageManager.send(builder);
	}
	
	public void landDonation(CSLandDonation msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCLandDonation.Builder scLandDonation = SCLandDonation.newBuilder();
		City myCity = cityLogic.getCity(msg.getCityId());
		if(myCity == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		httpMessageManager.changeCityResource(myCity);
		MessageCode mc = mapLogic.landDonation(myCity, msg.getType(), msg.getCount(), scLandDonation);
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		builder.setScLandDonation(scLandDonation);
		builder.setNeedUpdateResource(true);
		httpMessageManager.send(builder);
		
	}
	
	public void showWorld(CSShowWorld msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();

		SCShowWorld.Builder scShowWorld = SCShowWorld.newBuilder();
		scShowWorld.addAllLands(cityLogic.getLandData(msg.getLandIdList()));
		builder.setScShowWorld(scShowWorld);
		httpMessageManager.send(builder);
		
	}
	
	public void showLand(CSShowLand msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();

		SCShowLand.Builder scShowLand = SCShowLand.newBuilder();
		scShowLand.setLand(mapLogic.getLandInfo(msg.getLandId()));
		builder.setScShowLand(scShowLand);
		httpMessageManager.send(builder);
	}
	
	public void showCity(CSShowCity msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();

		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		
		List<Building> bs = buildingLogic.getBuildings( msg.getCityId());
		if(bs != null){
			SCShowCity.Builder scShowCity =  SCShowCity.newBuilder();
			for(Building b : bs){
				scShowCity.addBuildings(buildingLogic.convert(city,b));
			}
			builder.setScShowCity(scShowCity);
		}
		httpMessageManager.send(builder);
	}
	
}
