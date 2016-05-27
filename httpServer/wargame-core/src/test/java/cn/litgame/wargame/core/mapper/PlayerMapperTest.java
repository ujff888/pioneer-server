package cn.litgame.wargame.core.mapper;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.model.Player;

public class PlayerMapperTest {
	private final static Logger log = Logger.getLogger(PlayerMapperTest.class);
	
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	PlayerMapper pm = context.getBean(PlayerMapper.class);
	//@Test
	public void testGameServer(){
		//
//		GameServerMapper gm = context.getBean(GameServerMapper.class);
//		Map<String,Object> ms = new HashMap<String, Object>();
//		ms.put("key", "t");
//		ms.put("value", "12333");
//		String r = gm.getConfig("t");
//		gm.updateConfig(ms);
//		System.out.println(r);
	}
	private long playerId = 10104L;
	@Before
	public void before(){
		 pm.delete(playerId);
		
	}
	
	@After
	public void after(){
		
	}
	
	@Test
	public void test(){
		
		Player player = new Player();
		player.setPlayerId(playerId);
		player.setPlayerName("李川");
		
		player.setCreateTime(new Timestamp(System.currentTimeMillis()));
		Assert.assertEquals(1, pm.insert(player));
		
		Player p = pm.select(playerId);
		Assert.assertEquals("李川", p.getPlayerName());
		Assert.assertEquals((Long)playerId,(Long)p.getPlayerId());
		
		
		p.setPlayerName("李川-改");
		pm.update(p);
		player = pm.select(playerId);
		Assert.assertEquals("李川-改", player.getPlayerName());
		
		List<Player> players = pm.selectAll();
		
		int length = players.size();
		
		List<Player> anotherPlayers = pm.selectByRange(0, pm.count());
		
		Assert.assertEquals(length, anotherPlayers.size());
		
	}
	
	//@Test
	public void getTest(){
		System.out.println(10104L%12);
		
	}
}
