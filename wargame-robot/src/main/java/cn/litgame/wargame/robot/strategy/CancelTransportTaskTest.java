package cn.litgame.wargame.robot.strategy;

import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.core.auto.GameProtos.CSCancelTransportTask;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.robot.core.Robot;
import cn.litgame.wargame.robot.core.TestGameFactory;

public class CancelTransportTaskTest extends TestGameFactory implements Runnable {
	private Robot robot;
	
	public CancelTransportTaskTest() {
		super();
	}

	public CancelTransportTaskTest(Robot robot) {
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
		//body.setSessionKey("");
		body.setMessageCode(MessageCode.OK);
		body.setMessageType(MessageType.MSG_ID_CANCEL_TRANSPORT_TASK);
		
		CSCancelTransportTask.Builder csCancelTransportTask = CSCancelTransportTask.newBuilder();
		csCancelTransportTask.setTaskId(45);
		
		try{
			this.send(body.build().toByteArray(), robot);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
