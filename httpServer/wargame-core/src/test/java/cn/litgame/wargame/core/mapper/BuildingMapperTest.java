package cn.litgame.wargame.core.mapper;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.model.Building;

import java.sql.Timestamp;

import org.junit.*;

public class BuildingMapperTest {
	ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:application-config.xml");
	BuildingMapper bm = context.getBean(BuildingMapper.class);
	
	//@Before
	public void Before(){
	//	bm.delBuilding(1111, 1);
		//bm.delBuilding(1111, 2);
	}
	
	//@Test
	public void Test(){
		Building building = new Building();
		building.setBuildId(1);
		building.setBuilding(true);
		building.setBuildTime(new Timestamp(System.currentTimeMillis()));
		building.setCityId(1111);
		building.setLevel(1);
		building.setPosition(1);
		
		Building building1 = new Building();
		building1.setBuildId(2);
		building1.setBuilding(false);
		building1.setBuildTime(new Timestamp(System.currentTimeMillis()));
		building1.setCityId(1111);
		building1.setLevel(1);
		building1.setPosition(2);
		
		Assert.assertEquals(1, bm.createBuilding(building1));
		Assert.assertEquals(1, bm.createBuilding(building));
		
		System.out.println("======================");
		System.out.println(bm.getBuildings( 1111));
		System.out.println("======================");
		System.out.println("======================");
		System.out.println(bm.getBuilding( 1111,1));
		System.out.println("======================");
		System.out.println("======================");
		System.out.println(bm.getBuilding( 1111,2));
		System.out.println("======================");
		
		building.setLevel(111);
		building1.setLevel(222);
		
		Assert.assertEquals(1, bm.updateBuilding(building1));
		Assert.assertEquals(1, bm.updateBuilding(building));
		
		System.out.println("======================");
		System.out.println(bm.getBuildings(1111));
		System.out.println("======================");
		System.out.println("======================");
		System.out.println(bm.getBuilding( 1111,1));
		System.out.println("======================");
		System.out.println("======================");
		System.out.println(bm.getBuilding( 1111,2));
		System.out.println("======================");
	}
	
}
