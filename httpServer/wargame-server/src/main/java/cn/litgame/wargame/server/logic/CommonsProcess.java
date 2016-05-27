package cn.litgame.wargame.server.logic;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.SCUpdatePlayerInfo;
import cn.litgame.wargame.core.auto.GameProtos.SCUpdateResource;
import cn.litgame.wargame.core.logic.BattleLogic;
import cn.litgame.wargame.core.logic.CityLogic;
import cn.litgame.wargame.core.logic.PlayerLogic;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Player;

/**
 * 对发给客户端的请求做一些通用的附加值计算
 * 
 * @author bear
 * 
 */
@Service
public class CommonsProcess {

	@Resource(name = "playerLogic")
	private PlayerLogic playerLogic;

//	@Resource(name = "friendLogic")
//	private FriendLogic friendLogic;

	@Resource(name = "cityLogic")
	private CityLogic cityLogic;

	@Resource(name = "battleLogic")
	private BattleLogic battleLogic;
	
	@Resource(name = "httpMessageManager")
	private HttpMessageManager httpMessageManager;
	
	private final static Logger log = Logger.getLogger(CommonsProcess.class);

	
	/**
	 * 处理邮件的同步问题
	 * 
	 * @param builder
	 * @param player
	 */
	private void doMail(MessageBody.Builder builder, Player player) {
//		SCUncheckMailIds.Builder scUncheckMailIds = SCUncheckMailIds.newBuilder();
//		List<Long> ids = mailLogic.getMailIds(player.getPlayerId(), false);
//		ids.addAll(mailLogic.getSystemMailIds(player.getPlayerId()));
//		scUncheckMailIds.addAllMailIds(ids);
//		builder.setScUncheckMailIds(scUncheckMailIds);
	}


	private void doCityResource(Player player,MessageBody.Builder builder){
		
	}
	
	private void doNotice(MessageBody.Builder builder, Player player){
		// 添加公告通知
//		List<String> notices = new ArrayList<String>();
//		Timestamp time = new Timestamp(System.currentTimeMillis());
//		List<GameNotice> gameNotices = gameNoticeLogic
//				.getGameNoticeByTime(time);
//		for (GameNotice gn : gameNotices) {
//			notices.add(gn.getContent());
//		}
		
//		SCGameNotice.Builder scGameNoticeBuilder = SCGameNotice.newBuilder();
//		scGameNoticeBuilder.addAllNotices(notices);
//		builder.setScGameNotice(scGameNoticeBuilder);
	}
	
	/**
	 * 同步各种数据
	 * 
	 * @param builder
	 * @param playerId
	 */
	public void process(Player player,MessageBody.Builder builder) {
		if (player != null && builder.getMessageCode() == MessageCode.OK) {
			if(player.isNeedSave()){
				SCUpdatePlayerInfo.Builder scUpdatePlayerInfo = SCUpdatePlayerInfo.newBuilder();
				scUpdatePlayerInfo.setDiamond(player.getDiamond());
				scUpdatePlayerInfo.setGold(player.getGold());
				builder.setScUpdatePlayerInfo(scUpdatePlayerInfo);
				playerLogic.updatePlayer(player);
			}
			if(builder.getNeedUpdateResource()){
				City city = httpMessageManager.getCityContext();
				if(city != null){
					SCUpdateResource.Builder scUpdateResource = SCUpdateResource.newBuilder();
					CityResource.Builder cityResource = CityResource.newBuilder();
					cityResource.setCityId(city.getCityId());
					cityResource.setWood(city.getWood());
					cityResource.setFood(city.getFood());
					cityResource.setCrystal(city.getCrystal());
					cityResource.setMetal(city.getMetal());
					cityResource.setStone(city.getStone());
					cityResource.setPerson((int)(city.getTotalPerson()));
					scUpdateResource.setCityResource(cityResource);
					builder.setScUpdateResource(scUpdateResource);
					
					cityLogic.updateCity(city);
				}
			}
		}
	}
}
