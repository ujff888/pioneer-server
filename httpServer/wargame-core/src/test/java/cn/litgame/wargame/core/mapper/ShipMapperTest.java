package cn.litgame.wargame.core.mapper;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.model.Ship;

public class ShipMapperTest {

	ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:application-config.xml"); 
	
	@Test
	public void test(){
		ShipMapper sm = context.getBean(ShipMapper.class);
		Ship ship = new Ship();
		ship.setShipId(4);
		ship.setCount(22);
		ship.setPlayerId(22);
		ship.setShipType(22);
		
		sm.delShip(4);
//		
//		sm.addShip(ship);
//		Ship s = sm.getShipByType(1111, 11);
//		System.out.println(s);
		
		
		
	}
}
