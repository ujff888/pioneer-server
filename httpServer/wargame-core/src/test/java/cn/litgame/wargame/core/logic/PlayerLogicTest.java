package cn.litgame.wargame.core.logic;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class PlayerLogicTest {
	private final static Logger log = Logger.getLogger(PlayerLogicTest.class);
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	
	BattleLogic battleLogic = context.getBean(BattleLogic.class);
	ConfigLogic configLogic = context.getBean(ConfigLogic.class);

	//@Before
	public void before(){
		//configLogic.loadConfig(ConfigLogic.class.getResource("/pb.bytes").getPath());
		//System.out.println("基础民意奖励===="+ configLogic.getGlobalConfig().getBasePublicOpinion());
	}
	
	//@Test
	public void test(){
		JedisPool pool = (JedisPool)context.getBean("jedisStoragePool");
		
		for(int i = 0;i<10000;i++){
			Jedis jedis = pool.getResource();
			try{
				jedis.set("test", "lichuan");
				String r = jedis.get("test");
				System.out.println(r);
			}finally{
				jedis.close();
			}
		}

	}
	
	//@After
	public void after(){
		
	}
	public static void main(String[] args) {
		long startTime = System.nanoTime();
		//2901213
		//5915949
		List<Integer> a = new LinkedList<Integer>();
		List<Integer> b = new LinkedList<Integer>();
		List<Integer> c = new LinkedList<Integer>();
		List<Integer> d = new LinkedList<Integer>();
		for(int i = 0 ;i<10000;i++){
			a.add(i);
			b.add(i);
			c.add(i);
			d.add(i);
		}
		List<Integer> result = new LinkedList<Integer>();
		result.addAll(a);
		result.addAll(b);
		result.addAll(c);
		result.addAll(d);
		System.out.println(System.nanoTime() - startTime);
	}
}
