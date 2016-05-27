package cn.litgame.wargame.server.logic;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BattleProcessTest {

	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	private final static Logger log = Logger.getLogger(BattleProcessTest.class);
	
	//@Test
	public void reliveShow(){
//		BattleProcess battleProcess = context.getBean(BattleProcess.class);
//		PlayerLogic player = context.getBean(PlayerLogic.class);
		//System.out.println("==="+BattleType.ANCIENT);
		//battleProcess.reLive(player.getPlayer(137573171254L),String.valueOf(BattleType.ANCIENT), 11111);
	}
}
