package cn.litgame.wargame.core.mapper;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.logic.CityLogic;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Player;

import java.util.List;

import org.junit.*;

public class CityMapperTest {
	ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:application-config.xml");
	CityMapper cm = context.getBean(CityMapper.class);
	ConfigLogic conf = context.getBean(ConfigLogic.class);
	CityLogic cityLogic = context.getBean(CityLogic.class);
	
	@Before
	public void Before(){
		conf.loadConfig(this.getClass().getResource("/pb.bytes").getPath());
	}
	
	@Test
	public void Test(){
		/*cm.getCityByPos(1, 1);

		int count = cm.count();
		List<City> cities = cm.selectByRange(0, count);
		
		Assert.assertEquals(count, cities.size());*/
		System.out.println(cityLogic.getCity(88));
		System.out.println(cityLogic.getCity(99));
	}
}


