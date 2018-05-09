package cn.litgame.wargame.robot.main;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.robot.core.Robot;
import cn.litgame.wargame.robot.strategy.*;
/**
 * 测试机器人入口类
 * @author admin
 *
 */
public class Main 
{
	private static final int ALL_STRATEGY = 0;
	private static final int LOGIN_STRATEGY = 1;
	private static final int CREATE_PLAYER_STRATEGY = 2;
	private static final int BATTLE_START_STRATEGY = 3;
	private static final int BATTLE_END_STRATEGY = 4;
	private static final int SHOW_RANK_STRATEGY = 5;
	private static final int ANCILENT_SHOW_STRATEGY = 6;
	private static final int ANCILENT_START_STRATEGY = 7;
	private static final int ANCILENT_END_STRATEGY = 8;
	private static final int MESSAGE_ID_BUY_RELIVE_SHOW = 9;
	private static final int MESSAGE_ID_BIND_ACCOUNT = 10;
	private static final int MESSAGE_ID_SHOW_PUB = 11;
	private static final int MESSAGE_ID_DAILY_QUEST_FLUSH = 12;
	private static final int MESSAGE_ID_GENERALS_UP = 13;
	private static final int MESSAGE_ID_BATTLE_SWEEP = 14;
	private static final int MESSAGE_ID_PAYMENT = 15;
	private static final int MESSAGE_ID_SHARE = 16;
	private static final int MSG_ID_SHOW_KING_INFO = 17;
	private static final int MSG_ID_SHOW_RANK = 18;
	private static final int MSG_ID_START_TRANSPORT_TASK = 19;
	private static final int MSG_ID_CANCEL_TRANSPORT_TASK = 20;
	


	public byte[] convert(MessageBody mc){
		return null;
	}
	/**
	 * 参数说明：
	 * 192.168.0.202 8080 1 300 20
	 * 服务器ip  端口 策略id 执行次数 间隔时间(毫秒)
	 * @param args
	 * @throws Exception
	 */
    public static void main( String[] args ) throws Exception
    {
    	//ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:application-config.xml");
//    	//LoginTest loginTest = context.getBean(LoginTest.class);
//    	//CreateNewRoleTest createNewRoleTest = context.getBean(CreateNewRoleTest.class);
    	
    	args = new String[5];
    	args[0] = "127.0.0.1" ;
    	args[1] = "8080";
    	args[2] = ""+LOGIN_STRATEGY;
    	args[3] = "1";
    	args[4] = "1000";
    	
    	if(args == null || args.length != 5){
    		throw new RuntimeException("参数错误，正确格式为：服务器ip  端口 策略id 执行次数 间隔时间(毫秒)");
    	}
    	
    	String ip = args[0];
    	int port = Integer.valueOf(args[1]);
    	int strategyId = Integer.valueOf(args[2]);
    	int times = Integer.valueOf(args[3]);
    	int interruptTime = Integer.valueOf(args[4]);

    	int i=0;
    	while(i<times){
    		System.out.println("----------------------启动第"+i+"个机器人-----------------------");
        	Robot robot = new Robot(ip,port,interruptTime);
			switch(strategyId){
		//	case ALL_STRATEGY:robot.addStrategy(new SimulateUserTest(robot));break;
			case LOGIN_STRATEGY:robot.addStrategy(new LoginTest(robot));break;
//			case BATTLE_START_STRATEGY:robot.addStrategy(new BattleTest(robot));break;
//			case BATTLE_END_STRATEGY:robot.addStrategy(new BattleTest(robot));break;
//			case SHOW_RANK_STRATEGY:robot.addStrategy(new RankTest(robot));break;
//			case ANCILENT_SHOW_STRATEGY:robot.addStrategy(new AncilentShowTest(robot));break;
//			case ANCILENT_START_STRATEGY:robot.addStrategy(new AncilentStartTest(robot)); break;
//			case ANCILENT_END_STRATEGY:robot.addStrategy(new AncilentEndTest(robot)); break;
//			case MESSAGE_ID_BUY_RELIVE_SHOW:
//					robot.addStrategy(new BattleTest(robot)); 
//					robot.addStrategy(new ReliveShowTest(robot)); 
//			break;
//			case MESSAGE_ID_BIND_ACCOUNT:robot.addStrategy(new BindAccountTest(robot)); break;
//			case MESSAGE_ID_SHOW_PUB:robot.addStrategy(new ShowPubTest(robot)); break;
//			case MESSAGE_ID_DAILY_QUEST_FLUSH:robot.addStrategy(new DailyQuestFlushTest(robot)); break;
//			case MESSAGE_ID_GENERALS_UP:robot.addStrategy(new GeneralsUpTest(robot)); break;
//			case MESSAGE_ID_BATTLE_SWEEP:robot.addStrategy(new BattleSweeTest(robot)); break;
			//case MESSAGE_ID_PAYMENT:robot.addStrategy(new PaymentTest(robot));break;
//			case MESSAGE_ID_SHARE:robot.addStrategy(new ShareTest(robot));break;
			case CREATE_PLAYER_STRATEGY:robot.addStrategy(new CreatePlayerTest(robot));break;
			case MSG_ID_SHOW_KING_INFO:robot.addStrategy(new ShowKingInfoTest(robot));break;
			case MSG_ID_SHOW_RANK:robot.addStrategy(new ShowRankTest(robot));break;
			case MSG_ID_START_TRANSPORT_TASK:robot.addStrategy(new StartTransportTaskTest(robot));break;
			case MSG_ID_CANCEL_TRANSPORT_TASK:robot.addStrategy(new CancelTransportTaskTest(robot));break;
			}
			robot.start();
			Thread.sleep(interruptTime);
			i++;
    	}
    	
    }

}
