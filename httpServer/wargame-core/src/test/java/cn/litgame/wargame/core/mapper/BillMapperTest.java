package cn.litgame.wargame.core.mapper;

import java.sql.Timestamp;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.model.Order;

public class BillMapperTest {
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	
	//@Test
	public void test(){
		BillMapper m = context.getBean(BillMapper.class);
//		m.insert(new Bill(1, "1", 1, "2", 1));
//		System.out.println(m.getBill("1"));
		OrderMapper om = context.getBean(OrderMapper.class);
		Order o = new Order();
		o.setOrderId("1");
		o.setPlayerId(1L);
		o.setStatus(0);
		o.setCreateTime(new Timestamp(System.currentTimeMillis()));
		//om.insert(o);
		System.out.println(om.getOrderHistoryList(1L));
		
	}
}
