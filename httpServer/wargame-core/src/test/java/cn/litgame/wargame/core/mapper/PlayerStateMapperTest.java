package cn.litgame.wargame.core.mapper;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.model.PlayerState;

public class PlayerStateMapperTest {
	private final static Logger log = Logger.getLogger(PlayerMapperTest.class);
	
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	PlayerStateMapper pm = context.getBean(PlayerStateMapper.class);
	
	@Test
	public void test(){
		PlayerState ps = new PlayerState();
		ps.setPlayerId(11L);
		ps.setAddPopulationCount(1);
		ps.setAddPopulationCountInCapital(1);
		ps.setAddPublicOpinion(1);
		ps.setAddPublicOpinionInCapital(1);
		ps.setAddTechPoint(1);
		ps.setBuildCost(1);
		ps.setFunction(1);
		ps.setLandCost(1);
		ps.setShipCost(1);
		ps.setShipSpeed(1);
		ps.setSystem(1);
		ps.setTechCost(1);
		
		pm.createPlayerState(ps);
		PlayerState p = pm.getPlayerState(11L);
		log.debug(p);
		p.setAddPopulationCountInCapital(2);
		pm.updatePlayerState(p);
		pm.delPlayerState(11L);
		System.out.println(pm.getPlayerState(11L));
		
	}
}
