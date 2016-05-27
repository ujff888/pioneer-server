package cn.litgame.wargame.server.logic;

import org.apache.commons.configuration.ConfigurationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.server.logic.GameConfigManager;

public class ConfigManagerTest {
	
	ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:application-config.xml");
	
//	@Test
	public void testResources(){
//		ConfigManager cm = context.getBean(ConfigManager.class);
//		TetrisResources tr = cm.getTetrisResources();
//		List<ResGeneralsCard> l1 = tr.getGeneralsCardsList();
//		System.out.println(l1.size());
//		for(ResGeneralsCard g : l1){
//			System.out.println(g.getCardName());
//		}
//		List<ResGeneralsLevelByElement> l2 = tr.getGeneralsElementLevelsList();
//		List<ResGeneralsLevel> l3 = tr.getGeneralsLevelsList();
//		System.out.println(l3.size());
//		for(ResGeneralsLevel g : l3){
//			System.out.println(g);
//		}

	}
	
	//@Test
	public void createPlayer(){
//		PlayerProcess pp = context.getBean(PlayerProcess.class);
//		CSCreateNewRole.Builder c = CSCreateNewRole.newBuilder();
//		c.setDeviceType("iphone");
//		c.setRoleName("xuhao1");
//		c.setRoleIcon(2);
//		c.setGeneralsCardId(50004);
//		c.setBindAccount("no");
//		pp.createNewPlayer(c.build());
	}
	
	//@Test
	public void configuration(){
//		String url = Configuration.getProperty("KuaiYongUrl");
//		System.out.println(url);
	}
}
