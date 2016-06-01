package cn.litgame.wargame.core.logic;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.BattleGround;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.battle.Army;
import cn.litgame.wargame.core.model.battle.BattleField;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

public class BattleLogicTest {

	private final static Logger log = Logger.getLogger(BattleLogicTest.class);
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 
	
	BattleLogic battleLogic = context.getBean(BattleLogic.class);
	ConfigLogic configLogic = context.getBean(ConfigLogic.class);
	JedisPool pool = (JedisPool) context.getBean("jedisStoragePool");
	
	@Before
	public void init(){
		configLogic.loadConfig(this.getClass().getResource("/pb.bytes").getPath());
	}

	
	@Test
	public void test(){
		BattleGround bg = configLogic.getBattleGround(1, 1);
		
		List<BattleTroop> offence = new ArrayList<>();
		List<BattleTroop> defence = new ArrayList<>();
		List<Army> armysOffence = new ArrayList<>();
		List<Army> armysDefence = new ArrayList<>();
		
		BattleTroop bt = new BattleTroop();
		bt.setCount(1);
		bt.setResTroop(configLogic.getResTroop(2001));
		BattleTroop bt1 = new BattleTroop();
		bt1.setCount(1);
		bt1.setResTroop(configLogic.getResTroop(2002));
		BattleTroop bt2 = new BattleTroop();
		bt2.setCount(22);
		bt2.setResTroop(configLogic.getResTroop(2003));
		BattleTroop bt3 = new BattleTroop();
		bt3.setCount(1);
		bt3.setResTroop(configLogic.getResTroop(2004));
		BattleTroop bt4 = new BattleTroop();
		bt4.setCount(1);
		bt4.setResTroop(configLogic.getResTroop(2005));
		BattleTroop bt5 = new BattleTroop();
		bt5.setCount(1);
		bt5.setResTroop(configLogic.getResTroop(2006));
		BattleTroop bt6 = new BattleTroop();
		bt6.setCount(1);
		bt6.setResTroop(configLogic.getResTroop(2007));
		BattleTroop bt7 = new BattleTroop();
		bt7.setCount(1);
		bt7.setResTroop(configLogic.getResTroop(2008));
		BattleTroop bt8 = new BattleTroop();
		bt8.setCount(1);
		bt8.setResTroop(configLogic.getResTroop(2009));
		BattleTroop bt9 = new BattleTroop();
		bt9.setCount(1);
		bt9.setResTroop(configLogic.getResTroop(2010));
		BattleTroop bt10 = new BattleTroop();
		bt10.setCount(1);
		bt10.setResTroop(configLogic.getResTroop(2011));
		BattleTroop bt11 = new BattleTroop();
		bt11.setCount(1);
		bt11.setResTroop(configLogic.getResTroop(2012));

//		offence.add(bt);
//		offence.add(bt1);
		offence.add(bt2);
//		offence.add(bt6);
//		offence.add(bt5);
//		offence.add(bt4);
//		offence.add(bt3);
//		offence.add(bt7);
//		offence.add(bt8);
//		offence.add(bt9);
//		offence.add(bt10);
//		offence.add(bt11);

		Army a = new Army(1L, 1, offence);
//		offence.clear();
//		offence.add(bt4);
//		Army a1 = new Army(3L, 3, offence);

		armysOffence.add(a);
		//armysOffence.add(a1);
	
//		defence.add(bt);
//		defence.add(bt1);
		bt2.setCount(30);
		defence.add(bt2);
//		defence.add(bt6);
//		defence.add(bt5);
//		defence.add(bt4);
//		defence.add(bt3);
//		defence.add(bt7);
//		defence.add(bt8);
//		defence.add(bt9);
//		defence.add(bt10);
//		defence.add(bt11);
	
		Army b = new Army(2L, 2, defence);
//		defence.clear();
//		defence.add(bt4);
//		Army b1 = new Army(4L, 4, defence);
		
		armysDefence.add(b);
		//armysDefence.add(b1);


		BattleField field = battleLogic.initBattleField(armysOffence, armysDefence, bg, BattleField.LAND, 1);
		//battleLogic.saveBattleField(field);
		GameProtos.BattleResult result = battleLogic.fight(field);
		Assert.assertEquals(GameProtos.BattleResult.DEFENCE_WIN, result);
		//log.info(battleLogic.loadBattleField());
	}
}
