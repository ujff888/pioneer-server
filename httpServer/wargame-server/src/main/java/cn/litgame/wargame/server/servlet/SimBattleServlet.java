package cn.litgame.wargame.server.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import cn.litgame.wargame.core.auto.GameProtos.BattleResult;
import cn.litgame.wargame.core.auto.GameResProtos.BattleGround;
import cn.litgame.wargame.core.logic.BattleLogic;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.model.battle.Army;
import cn.litgame.wargame.core.model.battle.BattleField;
import cn.litgame.wargame.core.model.battle.BattleRound;
import cn.litgame.wargame.core.model.BattleTroop;
import junit.framework.Assert;

@Controller
public class SimBattleServlet {
	@Resource(name = "configLogic")
	private ConfigLogic configLogic;
	
	@Resource(name = "battleLogic")
	private BattleLogic battleLogic;
	
	@RequestMapping(value="sim.lc",method=RequestMethod.POST)
	public ModelAndView simBattle(
			@RequestParam("battleFieldLevel") Integer battleFieldLevel,
			@RequestParam("moraleDownPercent") Double moraleDownPercent,
			@RequestParam("weakMoraleExtra") Double weakMoraleExtra,
			@RequestParam("moraleBuffIndex") Double moraleBuffIndex,
			@RequestParam("2001o") Integer o2001, @RequestParam("2001d") Integer d2001, 
			@RequestParam("2002o") Integer o2002, @RequestParam("2002d") Integer d2002,
			@RequestParam("2003o") Integer o2003, @RequestParam("2003d") Integer d2003,
			@RequestParam("2004o") Integer o2004, @RequestParam("2004d") Integer d2004,
			@RequestParam("2005o") Integer o2005, @RequestParam("2005d") Integer d2005,
			@RequestParam("2006o") Integer o2006, @RequestParam("2006d") Integer d2006,
			@RequestParam("2007o") Integer o2007, @RequestParam("2007d") Integer d2007,
			@RequestParam("2008o") Integer o2008, @RequestParam("2008d") Integer d2008,
			@RequestParam("2009o") Integer o2009, @RequestParam("2009d") Integer d2009,
			@RequestParam("2010o") Integer o2010, @RequestParam("2010d") Integer d2010,
			@RequestParam("2011o") Integer o2011, @RequestParam("2010d") Integer d2011,
			@RequestParam("2012o") Integer o2012, @RequestParam("2010d") Integer d2012,
			HttpServletRequest request, HttpServletResponse response){
		ModelAndView m =  new ModelAndView("SimBattle");
		StringBuilder sb = new StringBuilder();
		try{
			BattleGround bg = configLogic.getBattleGround(1, battleFieldLevel);
			
			List<BattleTroop> offence = new ArrayList<BattleTroop>();
			List<BattleTroop> defence = new ArrayList<BattleTroop>();
			List<Army> armysOffence = new ArrayList<>();
			List<Army> armysDefence = new ArrayList<>();
			
			BattleTroop bt2001 = new BattleTroop();
			bt2001.setResTroop(configLogic.getResTroop(2001));
			BattleTroop bt2002 = new BattleTroop();
			bt2002.setResTroop(configLogic.getResTroop(2002));
			BattleTroop bt2003 = new BattleTroop();
			bt2003.setResTroop(configLogic.getResTroop(2003));
			BattleTroop bt2004 = new BattleTroop();
			bt2004.setResTroop(configLogic.getResTroop(2004));
			BattleTroop bt2005 = new BattleTroop();
			bt2005.setResTroop(configLogic.getResTroop(2005));
			BattleTroop bt2006 = new BattleTroop();
			bt2006.setResTroop(configLogic.getResTroop(2006));
			BattleTroop bt2007 = new BattleTroop();
			bt2007.setResTroop(configLogic.getResTroop(2007));
			BattleTroop bt2008 = new BattleTroop();
			bt2008.setResTroop(configLogic.getResTroop(2008));
			BattleTroop bt2009 = new BattleTroop();
			bt2009.setResTroop(configLogic.getResTroop(2009));
			BattleTroop bt2010 = new BattleTroop();
			bt2010.setResTroop(configLogic.getResTroop(2010));
			BattleTroop bt2011 = new BattleTroop();
			bt2011.setResTroop(configLogic.getResTroop(2011));
			BattleTroop bt2012 = new BattleTroop();
			bt2012.setResTroop(configLogic.getResTroop(2012));

			bt2001.setCount(o2001);
			offence.add(bt2001);
			
			bt2002.setCount(o2002);
			offence.add(bt2002);
			
			bt2003.setCount(o2003);
			offence.add(bt2003);
			
			bt2004.setCount(o2004);
			offence.add(bt2004);

			bt2005.setCount(o2005);
			offence.add(bt2005);

			bt2006.setCount(o2006);
			offence.add(bt2006);
			
			bt2007.setCount(o2007);
			offence.add(bt2007);
			
			bt2008.setCount(o2008);
			offence.add(bt2008);
			
			bt2009.setCount(o2009);
			offence.add(bt2009);
			
			bt2010.setCount(o2010);
			offence.add(bt2010);
			
			bt2011.setCount(o2011);
			offence.add(bt2011);
			
			bt2012.setCount(o2012);
			offence.add(bt2012);
			
			Army aaa = new Army(1L, 1, offence);
			Army a = new Army(1L, 1, offence);

			
			armysOffence.add(a);
		
			bt2001.setCount(d2001);
			defence.add(bt2001);
			
			bt2002.setCount(d2002);
			defence.add(bt2002);
			
			bt2003.setCount(d2003);
			defence.add(bt2003);
			
			bt2004.setCount(d2004);
			defence.add(bt2004);

			bt2005.setCount(d2005);
			defence.add(bt2005);
			
			bt2006.setCount(d2006);
			defence.add(bt2006);

			bt2007.setCount(d2007);
			defence.add(bt2007);
			
			bt2008.setCount(d2008);
			defence.add(bt2008);
			
			bt2009.setCount(d2009);
			defence.add(bt2009);
			
			bt2010.setCount(d2010);
			defence.add(bt2010);
			
			bt2011.setCount(d2011);
			defence.add(bt2011);
			
			bt2012.setCount(d2012);
			defence.add(bt2012);
		
			Army b = new Army(2L, 2, defence);
			
			armysDefence.add(b);
			
			battleLogic.saveBattleField(battleLogic.initBattleField(armysOffence, armysDefence, bg, BattleField.LAND, 1));
			BattleField field = battleLogic.loadBattleField();
			field.setMoraleDownPercent(moraleDownPercent/100);
			field.setWeakMoraleExtra(weakMoraleExtra/100);
			battleLogic.saveBattleField(field);
			int i = 1;
			List<BattleRound> roundList = new ArrayList<>();
			while(field.getResult() == BattleResult.FIGHTING){
				sb.append("第"+i+"回合");
				field = battleLogic.loadBattleField();
				battleLogic.nextRound(field);
				roundList.add(battleLogic.saveRound(field));
				battleLogic.saveBattleField(field);
				i++;
			}
			
			m.addObject("roundList", roundList);
			m.addObject("result", field.getResult());
		}catch (NullPointerException e){
			m.addObject("error", "参数不能为空");
		}
		
		return m;
	}
}
