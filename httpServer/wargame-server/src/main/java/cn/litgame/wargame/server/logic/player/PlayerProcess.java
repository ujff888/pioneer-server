package cn.litgame.wargame.server.logic.player;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.kriver.core.uuid.UUID64Tetris;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameProtos.CSChangeSystem;
import cn.litgame.wargame.core.auto.GameProtos.CSCreatePlayer;
import cn.litgame.wargame.core.auto.GameProtos.CSGmCommand;
import cn.litgame.wargame.core.auto.GameProtos.CSPayment;
import cn.litgame.wargame.core.auto.GameProtos.CSSetCapital;
import cn.litgame.wargame.core.auto.GameProtos.CSShowKingInfo;
import cn.litgame.wargame.core.auto.GameProtos.CSShowRank;
import cn.litgame.wargame.core.auto.GameProtos.CSStudyTech;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.RankItem;
import cn.litgame.wargame.core.auto.GameProtos.SCChangeSystem;
import cn.litgame.wargame.core.auto.GameProtos.SCCreatePlayer;
import cn.litgame.wargame.core.auto.GameProtos.SCGmCommand;
import cn.litgame.wargame.core.auto.GameProtos.SCPayment;
import cn.litgame.wargame.core.auto.GameProtos.SCSetCapital;
import cn.litgame.wargame.core.auto.GameProtos.SCShowKingInfo;
import cn.litgame.wargame.core.auto.GameProtos.SCShowRank;
import cn.litgame.wargame.core.auto.GameProtos.SCStudyTech;
import cn.litgame.wargame.core.auto.GameResProtos.PayInfo;
import cn.litgame.wargame.core.logic.RankLogic;
import cn.litgame.wargame.core.model.AppleVerify;
import cn.litgame.wargame.core.model.Bill;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Order;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.core.model.Troop;
import cn.litgame.wargame.log.model.PlayerLog;
import cn.litgame.wargame.server.logic.GameConfigManager;
import cn.litgame.wargame.server.message.KHttpMessageProcess;

@Service
public class PlayerProcess extends KHttpMessageProcess {
	
	/**
	 * 学习科技
	 * @param msg
	 */
	public void studyTech(CSStudyTech msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Player player =  httpMessageManager.getPlayer();
		SCStudyTech.Builder scStudyTech = SCStudyTech.newBuilder();
		
		MessageCode mc = playerLogic.studyTech(player.getPlayerId(),msg.getTechId(),scStudyTech);
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		builder.setScStudyTech(scStudyTech);
		httpMessageManager.send(builder);
	}
	
	/**
	 * 创建用户
	 * 
	 * @param msg
	 */
	public void createNewPlayer(CSCreatePlayer msg) {
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		
		// 获取角色名称
//		String roleName = StringUtils.deleteWhitespace(msg.getRoleName());
//		List<String> dictionaryList = configManager.getResDictionaryInfoList();
//		// 判断是否含有敏感词
//		if (KeywordFilterUtil.isContentKeyWords(dictionaryList,
//				roleName.toLowerCase())
//				|| KeywordFilterUtil.isContentKeyWords(dictionaryList,
//						roleName.toUpperCase())) {
//			builder.setMessageCode(MessageCode.HAS_SENSITIVITY_DICIONARY);
//			httpMessageManager.send(builder);
//			return;
//		}

		//TODO:oldId可以优化一下用redis来做计数器，递增的同时存储数据
		UUID64Tetris uuid = GameConfigManager.uuid;
		long playerId = uuid.getNextUUID();
		gameConfigManager.setOldPlayerId(uuid.getOid(playerId));

		Player player = playerLogic.createPlayer(playerId, msg.getDeviceType(),
				msg.getPlayerName(), "",msg.getPlatformUid(), msg.getPlatformType());
		if (player == null) {
			log.warn("create player error,msg=" + msg);
			builder.setMessageCode(MessageCode.ERR);
			httpMessageManager.send(builder);
			return;
		}
		//注册亲家用户
		chatLogic.activateChat(player);
				
		GameProtos.City city = playerLogic.getBornCity(playerId);
		
		httpMessageManager.initSessionKey(player.getPlayerId(),player.getPassword());
		SCCreatePlayer.Builder scCreatePlayer = SCCreatePlayer.newBuilder();
		scCreatePlayer.setPlayer(playerLogic.getPlayerMessage(player));
		scCreatePlayer.setCity(city);
		
		builder.setScCreatePlayer(scCreatePlayer);
		logManager.record(new PlayerLog(player.getPlayerId(), "创建角色",msg.toString(),player.getPlatformType(),player.getDeviceType(),MessageType.MSG_ID_CREATE_PLAYER_VALUE));
		
		httpMessageManager.send(builder);
	}

	/**
	 * 设置新的首都
	 * @param msg
	 */
	public void setCapital(CSSetCapital msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		City city = cityLogic.getCity(msg.getCityId());
		if(city == null){
			builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
			httpMessageManager.send(builder);
			return;
		}
		SCSetCapital.Builder scSetCapital = SCSetCapital.newBuilder();
		MessageCode mc = cityLogic.setCapital(city, scSetCapital);
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		builder.setScSetCapital(scSetCapital);
		httpMessageManager.send(builder);
	}
	
	/**
	 * 改革政体
	 * @param msg
	 */
	public void changeSystem(CSChangeSystem msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Player player = httpMessageManager.getPlayer();
		SCChangeSystem.Builder scChangeSystem = SCChangeSystem.newBuilder();
		MessageCode mc = playerLogic.changeSystem(player, msg.getSystemId(),scChangeSystem);
		if(mc != MessageCode.OK){
			builder.setMessageCode(mc);
			httpMessageManager.send(builder);
			return;
		}
		builder.setScChangeSystem(scChangeSystem);
		httpMessageManager.send(builder);
	}
	
	/**
	 * 支付处理
	 * 
	 * @param msg
	 * @throws UnsupportedEncodingException 
	 */
	public void payment(CSPayment msg){
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		Player player = httpMessageManager.getPlayer();
		
		PayInfo info = configLogic.getPayInfo(msg.getProductId());
		if(info == null){
			log.error("not found productId,msg="+msg);
			return;
		}
		
		Order orderHistory = paymentLogic.queryOrderHistory(player.getPlayerId(),msg.getOrderId());
		// orderHistory为空时，新添一条订单历史记录
		if(orderHistory == null){
			orderHistory = paymentLogic.createOrderHistory(player.getPlayerId(),msg.getOrderId());
		}
		Bill bill = paymentLogic.getBillByOrderId(msg.getOrderId());
		if(bill != null || orderHistory.getStatus() == 1){
			log.error("this orderId have in bills,msg="+msg);
			return;
		}
		
		AppleVerify iosVerify = null;
		try{
			iosVerify = paymentLogic.iosVerify(msg.getReceipt().toByteArray(),gameConfigManager.getAppleUrl(),gameConfigManager.getAppId());
		}catch(Exception e){
			log.error("verify error,msg="+msg, e);
			return;
		}
		if(iosVerify.getStatus() == -1){
			log.error("访问服务器校验失败!");
			return;
		}
		if(iosVerify.getStatus() != 0){
			log.error("苹果校验失败!iosVerify[" + iosVerify.getStatus() + "]");
			return;
		}
		
		String bundleId = gameConfigManager.getBundleId();
		long appId = gameConfigManager.getAppId();
		SCPayment.Builder scPayment = SCPayment.newBuilder();
		scPayment.setOrderId(msg.getOrderId());
		scPayment.setProductId(msg.getProductId());
		
		//判断请求数据是否合法性
		if(iosVerify.getBid().equals(bundleId) && iosVerify.getAppid() == appId 
				&& iosVerify.getOrderId().equals(msg.getOrderId()) 
				&& iosVerify.getProductId().equals(msg.getProductId())){
			boolean isFirst = !paymentLogic.haveProductId(player.getPlayerId(), msg.getProductId());
			scPayment.setStatus(1);
			int giftId = isFirst ? info.getFristGiftId() : info.getGiftId();
			
			playerLogic.updatePlayer(player);
			
			//添加bill
			orderHistory.setStatus(1);
			paymentLogic.updateOrderHistory(orderHistory);
			paymentLogic.addBill(player.getPlayerId(), msg.getOrderId(), info.getRmb(), info.getProducId(),1);
			//发奖
		}else{
			log.error("非法请求数据校验!,msg="+msg);
			return;
		}

		// 设置信息
		builder.setScPayment(scPayment);

		httpMessageManager.send(builder);
	}

	/**
	 * 显示国王信息
	 * 
	 * @param csKingInfo
	 */
	public void showKingInfo(CSShowKingInfo csKingInfo) {
		Long playerId = httpMessageManager.getPlayerId();
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCShowKingInfo.Builder scShowKingInfo = SCShowKingInfo.newBuilder();
		
		scShowKingInfo.setTotalScore((int) rankLogic.getPlayerScore(playerId, RankLogic.RankType.TOTAL_RANK));
		scShowKingInfo.setGoldScore((int) rankLogic.getPlayerScore(playerId, RankLogic.RankType.GOLD_RANK));
		scShowKingInfo.setBuildingScore((int) rankLogic.getPlayerScore(playerId, RankLogic.RankType.BUILDING_RANK));
		scShowKingInfo.setScienceScore((int) rankLogic.getPlayerScore(playerId, RankLogic.RankType.SCIENCE_RANK));
		scShowKingInfo.setWarfareScore((int) rankLogic.getPlayerScore(playerId, RankLogic.RankType.WARFARE_RANK));
		
		builder.setScShowKingInfo(scShowKingInfo);
		httpMessageManager.send(builder);
	}

	/**
	 * 显示排行
	 * 
	 * @param csShowRank
	 */
	public void showRank(CSShowRank csShowRank) {
		GameProtos.RankType csRankType = csShowRank.getRankType();

		RankLogic.RankType sRankType = null;
		Player player = httpMessageManager.getPlayer();
		MessageBody.Builder builder = httpMessageManager.getMessageContentBuilder();
		SCShowRank.Builder scShowRank = SCShowRank.newBuilder();
		
		switch(csRankType){
		case BEAT_RECORD_RANK:
			//TODO:实现消灭记录
			break;
		case BUILDING_RANK:
			sRankType = RankLogic.RankType.BUILDING_RANK;
			break;
		case SCIENCE_RANK:
			sRankType = RankLogic.RankType.SCIENCE_RANK;
			break;
		case GOLD_RANK:
			sRankType = RankLogic.RankType.GOLD_RANK;
			break;
		case TOTAL_RANK:
			sRankType = RankLogic.RankType.TOTAL_RANK;
			break;
		case WARFARE_RANK:
			sRankType = RankLogic.RankType.WARFARE_RANK;
			break;
		default:
			throw new RuntimeException("unkown rank type. csRanktype = " + csRankType );
		}
		
		Set<Tuple> rankList = rankLogic.getRankTop(sRankType, 100);
		
		ArrayList<RankItem> rankItemList = new ArrayList<>();
		Iterator<Tuple> it = rankList.iterator();
		
		Jedis jedis;
		while(it.hasNext()){
			Tuple t = it.next();
			
			RankItem.Builder rankItemBuilder = RankItem.newBuilder();
			Player playerInRank = playerLogic.getPlayer(Long.valueOf(t.getElement()));
			
			if(playerInRank == null){
				jedis = jedisStoragePool.getResource();
				try{
					jedis.zrem(rankLogic.getRankKey(sRankType), t.getElement());
					continue;
				}finally {
					jedis.close();
				}
			}
			
			rankItemBuilder.setPlayerName(playerInRank.getPlayerName());
			rankItemBuilder.setPlayerSocre((int) t.getScore());
			rankItemBuilder.setPlayerRank((int)rankLogic.getPlayerRank(playerInRank.getPlayerId(), sRankType));
			
			rankItemList.add(rankItemBuilder.build());
		}
		int playerRank = (int) rankLogic.getPlayerRank(player.getPlayerId(), sRankType);
		scShowRank.setRankType(csRankType);
		scShowRank.setMyRank(playerRank);
		scShowRank.addAllRankItem(rankItemList);
		
		builder.setScShowRank(scShowRank);
		httpMessageManager.send(builder);
	}

	public void gmCommand(CSGmCommand csGmCommand) {
		MessageBody.Builder builder = MessageBody.newBuilder();
		builder.setMessageType(MessageType.MSG_ID_GM_COMMAND);
		
		SCGmCommand.Builder scGmCommand = SCGmCommand.newBuilder();
		
		Player player = httpMessageManager.getPlayer();
		String[] args = csGmCommand.getCommand().split(" ");
		if(args.length < 2){
			builder.setMessageCode(MessageCode.ERR);
			httpMessageManager.send(builder);
			return;
		}
		String command = args[0];
		int commandArg;
		try{
			commandArg = Integer.valueOf(args[1]);
		}catch(NumberFormatException unsed){
			builder.setMessageCode(MessageCode.ERR);
			httpMessageManager.send(builder);
			return;
		}
		
		boolean hasCityId = csGmCommand.hasCityId();
		City city = null;
		if(hasCityId)
			city = cityLogic.getCity(csGmCommand.getCityId());
		
		switch(command){
		case "GM_GOLD":
			if(player.getGold() + commandArg < 0)
				commandArg = 0;
			player.setGold(player.getGold()+commandArg);
			playerLogic.updatePlayerGold(player.getPlayerId(), player.getGold()+commandArg);
			player.setNeedSave(true);
			break;
		case "GM_DIAMOND":
			if(player.getDiamond() + commandArg < 0)
				commandArg = 0;
			player.setDiamond(player.getDiamond() + commandArg);
			player.setNeedSave(true);
			break;
		case "GM_WOOD":
			if(!hasCityId){
				builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
				httpMessageManager.send(builder);
				return;
			}
			
			if(city.getWood() + commandArg < 0)
				commandArg = 0;
			city.setWood(city.getWood()+commandArg);
			cityLogic.updateCity(city);
			httpMessageManager.changeCityResource(city);
			break;
		case "GM_STONE":
			if(!hasCityId){
				builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
				httpMessageManager.send(builder);
				return;
			}
			
			if(city.getStone() + commandArg < 0)
				commandArg = 0;
			city.setStone(city.getStone()+commandArg);
			cityLogic.updateCity(city);
			httpMessageManager.changeCityResource(city);
			break;
		case "GM_CRYSTAL":
			if(!hasCityId){
				builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
				httpMessageManager.send(builder);
				return;
			}
			
			if(city.getCrystal() + commandArg < 0)
				commandArg = 0;
			city.setCrystal(city.getCrystal()+commandArg);
			cityLogic.updateCity(city);
			httpMessageManager.changeCityResource(city);
			break;
		case "GM_FOOD":
			if(!hasCityId){
				builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
				httpMessageManager.send(builder);
				return;
			}
			
			if(city.getFood() + commandArg < 0)
				commandArg = 0;
			city.setFood(city.getFood()+commandArg);
			cityLogic.updateCity(city);
			httpMessageManager.changeCityResource(city);
			break;
		case "GM_METAL":
			if(!hasCityId){
				builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
				httpMessageManager.send(builder);
				return;
			}
			
			if(city.getMetal() + commandArg < 0)
				commandArg = 0;
			city.setMetal(city.getMetal()+commandArg);
			cityLogic.updateCity(city);
			httpMessageManager.changeCityResource(city);
			break;
		case "GM_ALL":
			if(!hasCityId){
				builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
				httpMessageManager.send(builder);
				return;
			}
			if(city.getWood() + commandArg < 0)
				commandArg = 0;
			city.setWood(city.getWood()+commandArg);
			if(city.getStone() + commandArg < 0)
				commandArg = 0;
			city.setStone(city.getStone()+commandArg);
			if(city.getCrystal() + commandArg < 0)
				commandArg = 0;
			city.setCrystal(city.getCrystal()+commandArg);
			if(city.getFood() + commandArg < 0)
				commandArg = 0;
			city.setFood(city.getFood()+commandArg);
			if(city.getMetal() + commandArg < 0)
				commandArg = 0;
			city.setMetal(city.getMetal()+commandArg);
			cityLogic.updateCity(city);
			httpMessageManager.changeCityResource(city);
			break;
		case "GM_TROOP":
			if(!hasCityId){
				builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
				httpMessageManager.send(builder);
				return;
			}
			if(args.length < 2){
				builder.setMessageCode(MessageCode.ERR);
				httpMessageManager.send(builder);
				return;
			}
			int commandCount;
			try{
				commandCount = Integer.parseInt(args[2]);
			}catch(NumberFormatException e){
				builder.setMessageCode(MessageCode.NOT_FOUND_CITY);
				httpMessageManager.send(builder);
				return;
			}
			Troop troop = new Troop();
			troop.setCityId(csGmCommand.getCityId());
			troop.setPlayerId(player.getPlayerId());
			
			troop.setTroopType(1);
			troop.setTroopResId(commandArg);
			troop.setCount(commandCount>0?commandCount:0);
			boolean troopExists = false;
			long troopId = 0;
			for(Troop tp : battleLogic.getTroopsByCityId(cityLogic.getCity(csGmCommand.getCityId()))){
				if(tp.getTroopResId() == commandArg){
					troopExists = true;
					troopId = tp.getTroopId();
					break;
				}
			}
			
			if(troopExists){
				troop.setTroopId(troopId);
				battleLogic.updateTroop(troop);
			}else{
				battleLogic.createTroop(troop);
			}
			break;
		default:
			break;
		}
		builder.setNeedUpdateResource(true);
		builder.setScGmCommand(scGmCommand);
		builder.setMessageCode(MessageCode.OK);
		httpMessageManager.send(builder);
	}


//	/**
//	 * 分享处理
//	 * 
//	 * @param msg
//	 */
//	public void shareProcess(CSShare msg) {
//		MessageContent.Builder builder = HttpMessageManager
//				.getMessageContentBuilder();
//		builder.setMessageType(MessageType.MESSAGE_ID_SHARE);
//		Player player = httpMessageManager.getPlayer();
//		// 分享处理
//		int rewardDiamond = playerLogic.shareProcess(builder, player);
//		SCShare.Builder scShare = SCShare.newBuilder();
//		scShare.setDiamond(rewardDiamond);
//		builder.setScShare(scShare);
//
//		// 记录分享日志
//		logManager.record(new PlayerLog(player == null ? -1 : player
//				.getPlayerId(), "分享处理",
//				("玩家ID:" + player.getPlayerId() + "进行分享处理"),
//				player == null ? 1 : player.getPlatformType(), player
//						.getDeviceType(), MessageType.MESSAGE_ID_SHARE
//						.getNumber(), msg.getShareEnum().getNumber()));
//
//		httpMessageManager.send(builder);
//	}
}
