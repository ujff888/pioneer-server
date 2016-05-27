package cn.litgame.wargame.core.mapper;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.model.Troop;

public class TroopMapperTest {

	ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:application-config.xml");
	TroopMapper tm = context.getBean(TroopMapper.class);
	
	//@Test
	public void test(){
		Troop t = new Troop();
		t.setCityId(1);
		t.setPlayerId(1);
		//t.setTroopData(new byte[]{1,1,1});
		t.setTroopType(1);
		//tm.createTroop(t);
	//	t.setTroopData(new byte[]{2,2,2});
		System.out.println(tm.getTroop(1));
		System.out.println(tm.getTroopsByCityId(1));
		System.out.println(tm.getTroopsByCityIdAndType(1, 1));
		System.out.println(tm.getTroopsByPlayerId(1));
		//tm.updateTroop(t);
	}
}
