package cn.litgame.wargame.core.mapper;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PlayerHistoryMapperTest {
	
	private final static Logger log = Logger.getLogger(PlayerHistoryMapperTest.class);
	
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	PlayerHistoryMapper pm = context.getBean(PlayerHistoryMapper.class);
	
	//@Test
	public void test(){
		
	}
}
