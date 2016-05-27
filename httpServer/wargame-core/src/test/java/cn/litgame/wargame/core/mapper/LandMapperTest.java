package cn.litgame.wargame.core.mapper;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.model.Land;
import junit.framework.Assert;

import org.junit.*;

public class LandMapperTest {
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	LandMapper lm = context.getBean(LandMapper.class);
	
	//@Before
	public void Before(){
		 lm.deleteLand(1234);
	}
	
	//@Test
	public void Test(){
		LandMapper lm = context.getBean(LandMapper.class);
		Land land = new Land();
		land.setLandId(1234);
		land.setWoodExp(1);
		land.setWoodLevel(2);
		land.setResourceExp(3);
		land.setResourceLevel(4);
		
		Assert.assertEquals(1, lm.createLand(land));
		Land newLand = lm.getLand(1234);
		
		Assert.assertEquals(1234,newLand.getLandId());
		Assert.assertEquals(1, newLand.getWoodExp());
		Assert.assertEquals(2, newLand.getWoodLevel());
		Assert.assertEquals(3, newLand.getResourceExp());
		Assert.assertEquals(4, newLand.getResourceLevel());
		
		newLand.setResourceLevel(5);
		lm.updateLand(newLand);
		Land newnewLand=lm.getLand(1234);
		
		Assert.assertEquals(5, newnewLand.getResourceLevel());
		
		
	}
}
