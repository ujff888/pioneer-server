package cn.litgame.wargame.core.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.auto.GameProtos.CityResource;
import cn.litgame.wargame.core.logic.queue.impl.BattleRoundActionEvent;
import cn.litgame.wargame.core.model.Building;
import cn.litgame.wargame.core.model.City;


public class BattleActionTest {
	ApplicationContext context = new ClassPathXmlApplicationContext(  
            "classpath*:application-config.xml"); 

	static int totalResourceCount = 500*8;
	static int safeCapacity = 2_500;
	static int food = 22_587 - safeCapacity;
	static int wood = 17_463 - safeCapacity;
	static int stone = 170 - safeCapacity;
	static int metal = 936 - safeCapacity;
	static int crystal = 797 - safeCapacity;
	
	public static void main(String[] args){
		System.out.println(grabResource());
	}
	
	private static CityResource.Builder grabResource() {
		CityResource.Builder resource = CityResource.newBuilder();
		
		int[] resourceArray = {food,wood,stone,metal,crystal};
		
		ArrayList<Integer> array = new ArrayList<>();
		for(int i : resourceArray){
			array.add(i);
		}
		
		Collections.sort(array);
		ListIterator<Integer> itr = array.listIterator(array.size());
		while(totalResourceCount > 0 && itr.hasPrevious()){
			int i = itr.previous();
			if(i < 0)
				break;
			int grabCount = Math.min(i, totalResourceCount);

			if(i == food){
				resource.setFood(grabCount);
				
			}
			if(i == wood){
				resource.setWood(grabCount);
				
			}
			if(i == stone){
				resource.setStone(grabCount);
				
			}
			if(i == metal){
				resource.setMetal(grabCount);
			}
			if(i == crystal){
				resource.setCrystal(grabCount);
			}
			totalResourceCount -= grabCount;
		}
		return resource;
		
	}
}
