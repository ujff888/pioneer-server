package cn.litgame.wargame.server.logic.login;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import redis.clients.jedis.JedisPool;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.VersionStatus;
import cn.litgame.wargame.core.auto.GameProtos.CSBindAccount;
import cn.litgame.wargame.core.auto.GameProtos.CSCheckVersion;
import cn.litgame.wargame.core.auto.GameProtos.CSLogin;
import cn.litgame.wargame.core.auto.GameProtos.CSPing;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.SCBindAccount;
import cn.litgame.wargame.core.auto.GameProtos.SCCheckVersion;
import cn.litgame.wargame.core.auto.GameProtos.SCLogin;
import cn.litgame.wargame.core.auto.GameProtos.SCPing;
import cn.litgame.wargame.core.logic.BattleLogic;
import cn.litgame.wargame.core.logic.ChatLogic;
import cn.litgame.wargame.core.logic.CityLogic;
import cn.litgame.wargame.core.logic.GameNoticeLogic;
import cn.litgame.wargame.core.logic.PlayerLogic;
import cn.litgame.wargame.core.logic.RankLogic;
import cn.litgame.wargame.core.logic.ShipLogic;
import cn.litgame.wargame.core.model.Account;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.core.model.PlayerTech;
import cn.litgame.wargame.log.LogManager;
import cn.litgame.wargame.log.model.PlayerLog;
import cn.litgame.wargame.server.logic.GameConfigManager;
import cn.litgame.wargame.server.logic.HttpMessageManager;
import cn.litgame.wargame.server.message.KHttpMessageProcess;

@Service
public class LoginProcess extends KHttpMessageProcess{

	/**
	 * 触发数据同步逻辑
	 * 
	 * @param msg
	 */
	public void ping(CSPing msg) {
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCPing.Builder scPing = SCPing.newBuilder();
		scPing.setServerTime((int)(System.currentTimeMillis()/1000));
		builder.setScPing(scPing);
		httpMessageManager.send(builder);
	}
	public void checkVersion(CSCheckVersion msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCCheckVersion.Builder scCheckVersion = SCCheckVersion.newBuilder();
		VersionStatus versionStatus = gameConfigManager.getClientVersionStatus(msg.getVersion());
		scCheckVersion.setVersionStatus(versionStatus);
		if(versionStatus == VersionStatus.UPDATE || versionStatus == VersionStatus.STOP){
			scCheckVersion.setClientUrl(gameConfigManager.getReleaseUrl(msg.getPlatformType()));
		}
		builder.setScCheckVersion(scCheckVersion);
		httpMessageManager.send(builder);
	}
	
	public void bindAccount(CSBindAccount msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();

		
		SCBindAccount.Builder scBindAccount = SCBindAccount.newBuilder();
		Account checkPlayer = playerLogic.getAccount(msg.getAccount(), msg.getPlatformType());
		if(checkPlayer != null){
			scBindAccount.setStatus(3);
			builder.setScBindAccount(scBindAccount);
			httpMessageManager.send(builder);
			return;
		}
		
		Player player = httpMessageManager.getPlayer();
		if(player == null){
			scBindAccount.setStatus(4);
			builder.setScBindAccount(scBindAccount);
			httpMessageManager.send(builder);
			return;
		}
		
		if(StringUtils.isNotBlank(player.getPlatformUid())){
			scBindAccount.setStatus(2);
			builder.setScBindAccount(scBindAccount);
			httpMessageManager.send(builder);
			return;
		}
		
		player.setPlatformUid(msg.getAccount());
		player.setPlatformType(msg.getPlatformType().getNumber());
		playerLogic.updatePlayer(player);
		playerLogic.addAccount(msg.getAccount(), msg.getPlatformType(), player.getPlayerId());
		scBindAccount.setStatus(1);
		builder.setScBindAccount(scBindAccount);
		httpMessageManager.send(builder);
	}
//	public void checkGameCenter(CSCheckGameCenterId msg){
//		MessageContent.Builder builder = HttpMessageManager
//				.getMessageContentBuilder();
//		builder.setMessageType(MessageType.MESSAGE_ID_CHECK_GAMECENTER);
//		String checkPlayerId = this.getPlayerIdByGameCenterAccount(msg.getCheckGameCenterId());
//		SCCheckGameCenterId.Builder scCheckGameCenterId = SCCheckGameCenterId.newBuilder();
//		if(StringUtils.isNotBlank(checkPlayerId)){
//			Player checkPlayer = playerLogic.getPlayer(Long.parseLong(checkPlayerId));
//			if(checkPlayer != null){
//				scCheckGameCenterId.setCheckGameCenterRoleInfo(playerLogic.convert(checkPlayer, null));
//			}
//		}
//		builder.setScCheckGameCenterId(scCheckGameCenterId);
//		httpMessageManager.send(builder);
//	}
//	
//	/**
//	 * 检查版本
//	 * @param msg
//	 */
//	public void checkVersion(CSCheckVersion msg){
//		MessageContent.Builder builder = HttpMessageManager
//				.getMessageContentBuilder();
//		builder.setMessageType(MessageType.MESSAGE_ID_CHECK_VERSION);
//		SCCheckVersion.Builder scCheckVersion = SCCheckVersion.newBuilder();
//		VersionStatus versionStatus = gameConfigManager
//				.getClientVersionStatus(msg.getVersion());
//		scCheckVersion.setVersionStatus(versionStatus);
//		if(versionStatus == VersionStatus.PROHIBIT){
//			scCheckVersion.setReleaseUrl(gameConfigManager.getReleaseUrl());
//			builder.setScCheckVersion(scCheckVersion);
//			httpMessageManager.send(builder);
//			return;
//		}
//		if (versionStatus == VersionStatus.EXPIRE
//				|| versionStatus == VersionStatus.NEED_UPDATE_ONLINE_AND_EXPIRE) {
//			scCheckVersion.setReleaseUrl(gameConfigManager.getReleaseUrl());
//		}
//		checkVersionStatus(builder,versionStatus,msg.getVersion());
//		
//		builder.setScCheckVersion(scCheckVersion);
//		
//		httpMessageManager.send(builder);
//	}
//	
	public void login(CSLogin msg) {
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCLogin.Builder scLogin = SCLogin.newBuilder();
		scLogin.setServerTime((int) (System.currentTimeMillis() / 1000));
		
		Player player = null;
		if(msg.hasPlatformUid() && StringUtils.isNotBlank(msg.getPlatformUid())){
			Account account = playerLogic.getAccount(msg.getPlatformUid(), msg.getPlatformType());
			player = playerLogic.getPlayer(account.getPlayerId());
		}else{
			 player = playerLogic.getPlayer(httpMessageManager.getPlayerId());
		}

		if (player == null) {
			scLogin.setIsNewPlayer(true);
			builder.setScLogin(scLogin);
			httpMessageManager.send(builder);
			return;
		}
		
		httpMessageManager.initSessionKey(player.getPlayerId(),player.getPassword());
		//检查用户是否已经注册为亲家用户
		if(playerLogic.getPlayer(player.getPlayerId()).getChatOn()==0){
			chatLogic.activateChat(player);
		}
		
		scLogin.setPlayer(playerLogic.getPlayerMessage(player));
		scLogin.addAllCity(cityLogic.getCitysProto(player.getPlayerId()));
		scLogin.setFreeShipCount(shipLogic.getFreeShip(player.getPlayerId()));
		scLogin.setTotalShipCount(shipLogic.getShipCount(player.getPlayerId()));
		
		PlayerTech pt = playerLogic.getPlayerTech(player.getPlayerId());
		scLogin.addTechProgress(pt.getFlyProgress());
		scLogin.addTechProgress(pt.getEconomicProgress());
		scLogin.addTechProgress(pt.getScienceProgress());
		scLogin.addTechProgress(pt.getMilitaryProgress());
		
		//添加玩家总分
		scLogin.setTotalRankScore((int) rankLogic.getPlayerScore(player.getPlayerId(), RankLogic.RankType.TOTAL_RANK));
		builder.setScLogin(scLogin);
		
		logManager.record(new PlayerLog(player.getPlayerId(), "登录游戏",msg.toString(),player.getPlatformType(),player.getDeviceType(),MessageType.MSG_ID_LOGIN_VALUE));
		
		httpMessageManager.send(builder);
	}
}
