package cn.litgame.wargame.core.mapper;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.model.LandDonation;

import org.junit.*;

public class LandDonationTest {
	ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:application-config.xml");
	LandDonationMapper ldm = context.getBean(LandDonationMapper.class);
	
	//@Before
	public void Before(){
		//ldm.deleteLandDonation(1, 1234);
		//ldm.deleteLandDonation(1, 3456);
	}
	//TODO:这个测试用例的函数更改了，需要更改
	//@Test
	public void Test(){
		LandDonation landDonation = new LandDonation();
		LandDonation landDonation1 = new LandDonation();
		
		landDonation.setLandId(1);
		landDonation.setCityId(1234);
		
		landDonation1.setLandId(1);
		landDonation1.setCityId(3456);
		
		Assert.assertEquals(1,ldm.createLandDonation(landDonation));
		Assert.assertEquals(1,ldm.createLandDonation(landDonation1));
		
		System.out.println("======================");
		System.out.println(ldm.getLandDonations(1));
		System.out.println("======================");
		
		landDonation.setResourceDonationCount(1000);
		landDonation1.setResourceDonationCount(2000);
		
		Assert.assertEquals(1, ldm.updateLandDonation(landDonation));
		Assert.assertEquals(1, ldm.updateLandDonation(landDonation1));
		
		System.out.println("======================");
		System.out.println(ldm.getLandDonations(1));
		System.out.println("======================");
	}
}
