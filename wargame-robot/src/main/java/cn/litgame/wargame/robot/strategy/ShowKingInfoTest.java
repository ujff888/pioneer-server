package cn.litgame.wargame.robot.strategy;

import cn.litgame.wargame.core.auto.GameProtos.CSShowKingInfo;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.robot.core.Robot;
import cn.litgame.wargame.robot.core.TestGameFactory;

public class ShowKingInfoTest extends TestGameFactory implements Runnable {
	private Robot robot;
	
	
	public ShowKingInfoTest(Robot robot) {
		super();
		this.robot = robot;
	}


	public Robot getRobot() {
		return robot;
	}


	public void setRobot(Robot robot) {
		this.robot = robot;
	}


	@Override
	public void run() {
		MessageBody.Builder body = MessageBody.newBuilder();
		body.setMessageCode(MessageCode.OK);
		body.setMessageType(MessageType.MSG_ID_SHOW_KING_INFO);
		
		System.out.println(MessageType.MSG_ID_SHOW_KING_INFO);
		
		CSShowKingInfo.Builder csShowKingInfo = CSShowKingInfo.newBuilder();
		body.setCsShowKingInfo(csShowKingInfo);
		try{
			this.send(body.build().toByteArray(), robot);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
