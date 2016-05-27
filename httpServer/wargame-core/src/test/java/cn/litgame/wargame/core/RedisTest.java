package cn.litgame.wargame.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kriver.core.common.MathUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class RedisTest {
	private final static Logger log = Logger.getLogger(RedisTest.class);
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	JedisPool pool = (JedisPool) context.getBean("jedisStoragePool");
	
	@Test
	public void test(){
		int x = Math.abs(1 - 3);
		int z = Math.abs(1 - 1);
		int distance = (int)Math.sqrt(x * x + z * z);
		System.out.println(distance);

		try(Jedis jedis = pool.getResource();){
			Transaction tx = jedis.multi();
			tx.hset("hashsetTest".getBytes(), "key".getBytes(), "value".getBytes());
			tx.hset("hashsetTest".getBytes(), "key".getBytes(), "value-a".getBytes());
			Response<byte[]> bytes = tx.hget("hashsetTest".getBytes(), "key".getBytes());
			tx.exec();
			System.out.println(new String(bytes.get()));
		}
		
	}
	/**
	 * 世界地图的生成
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("开始======");
		for(int l = 0; l<100;l++){
			List<Integer> ids = new ArrayList<Integer>();
			List<Integer> rs = new ArrayList<Integer>();
			
			for(int i = 1;i< 101 ;i++){
				ids.add(i);
			}
			for(int i = 1;i< 51 ;i++){
				int r = ids.remove(MathUtils.random(0, ids.size()-1));
				rs.add(r);
			}
			rs.sort(new Comparator<Integer>() {

				@Override
				public int compare(Integer o1, Integer o2) {
					return o1 < o2 ? -1:1;
				}
			});
			for(Integer ii : rs){
				System.out.println(ii);
			}
		}
		System.out.println("结束======");
		
	}
}
