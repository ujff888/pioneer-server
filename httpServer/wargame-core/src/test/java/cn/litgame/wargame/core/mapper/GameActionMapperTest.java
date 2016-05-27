package cn.litgame.wargame.core.mapper;

import java.util.List;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.model.GameAction;

public class GameActionMapperTest {
	ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:application-config.xml");
	GameActionMapper gm = context.getBean(GameActionMapper.class);
	
	@Test
	public void test(){
		//List<GameAction> ga = gm.getGameActionsByCity(0,0,1);
		//System.out.println("ga====" + ga);
//		GameAction ga = new GameAction();
//		byte[] actionData = {1,1,1};
//		ga.setActionData(actionData);
//		ga.setActionType(GameActionType.PRODUCTION_LAND.getValue());
//		ga.setCreateTime(new Timestamp(System.currentTimeMillis()));
//		ga.setOverTime(new Timestamp(System.currentTimeMillis()));
//		ga.setLoadingTime(new Timestamp(System.currentTimeMillis() + 100000));
//		ga.setSourceCityId(1);
//		ga.setTargetCityId(2);
//		ga.setSourcePlayerId(11);
//		ga.setTargetPlayerId(22);
//		gm.createGameAction(ga);
//		List<GameAction> gas = gm.getLoadingGameAction(1, System.currentTimeMillis());
//		System.out.println("===================================");
//		System.out.println(gas);
	}
	
}
