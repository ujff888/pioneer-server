package cn.litgame.wargame.robot.strategy;

import cn.litgame.wargame.core.auto.GameProtos.CSShowRank;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.core.auto.GameProtos.RankType;
import cn.litgame.wargame.robot.core.Robot;
import cn.litgame.wargame.robot.core.TestGameFactory;

public class ShowRankTest extends TestGameFactory implements Runnable {
	private Robot robot;
	
	public ShowRankTest(Robot robot) {
		super();
		this.robot = robot;
	}
	
	@Override
	public void run() {
		MessageBody.Builder body = MessageBody.newBuilder();
		body.setMessageCode(MessageCode.OK);
		body.setMessageType(MessageType.MSG_ID_SHOW_RANK);
		
		CSShowRank.Builder csShowRank = CSShowRank.newBuilder();
		//csShowRank.setRankType(RankType.BEAT_RECORD_RANK);
		
		csShowRank.setRankType(RankType.TOTAL_RANK);
		body.setCsShowRank(csShowRank);
		try {
			this.send(body.build().toByteArray(), robot);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		csShowRank.setRankType(RankType.BUILDING_RANK);
		body.setCsShowRank(csShowRank);
		try {
			this.send(body.build().toByteArray(), robot);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		csShowRank.setRankType(RankType.GOLD_RANK);
		body.setCsShowRank(csShowRank);
		try {
			this.send(body.build().toByteArray(), robot);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		csShowRank.setRankType(RankType.SCIENCE_RANK);
		body.setCsShowRank(csShowRank);
		try {
			this.send(body.build().toByteArray(), robot);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		csShowRank.setRankType(RankType.WARFARE_RANK);
		body.setCsShowRank(csShowRank);
		try {
			this.send(body.build().toByteArray(), robot);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
