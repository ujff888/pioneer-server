package cn.litgame.wargame.core.mapper;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.model.Battle;

public class BattleMapperTest {
	private final static Logger log = Logger.getLogger(PlayerMapperTest.class);
	
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	
	//@Test
	public void test(){
		BattleMapper m = context.getBean(BattleMapper.class);
		Battle battle = new Battle();
		
		//m.insert(battle);
		
	}
	
	
}
