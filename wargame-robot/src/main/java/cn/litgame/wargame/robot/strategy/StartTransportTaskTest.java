package cn.litgame.wargame.robot.strategy;

import cn.litgame.wargame.core.auto.GameGlobalProtos.GameActionType;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.core.auto.GameProtos.CSCancelTransportTask;
import cn.litgame.wargame.core.auto.GameProtos.CSStartTransportTask;
import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.robot.core.Robot;
import cn.litgame.wargame.robot.core.TestGameFactory;

public class StartTransportTaskTest extends TestGameFactory implements Runnable{
	private Robot robot;
	
	public StartTransportTaskTest(Robot robot) {
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
		body.setMessageType(MessageType.MSG_ID_START_TRANSPORT_TASK);
		
		CSStartTransportTask.Builder csStartTransportTask = CSStartTransportTask.newBuilder();
		TransportTask.Builder task = TransportTask.newBuilder();
		task.setType(GameActionType.TRANSPORT);
		task.setSourceCityId(1);
		task.setTargetCityId(2);
		CityResource.Builder resource = CityResource.newBuilder();
		resource.setWood(1000);
		task.setResource(resource);
		csStartTransportTask.setTask(task);
		body.setCsStartTransportTask(csStartTransportTask);
		
		try{
			this.send(body.build().toByteArray(), robot);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
