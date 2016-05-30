package cn.litgame.wargame.core.model.battle;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.auto.GameResProtos.ResTroop;
import cn.litgame.wargame.core.auto.GameResProtos.TroopType;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.battle.unit.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;

/**
 * 军队的抽象
 * 
 * @author 熊纪元
 *
 */
public class Army implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3516389017448464560L;

	@Resource(name = "configLogic")
	private static ConfigLogic configLogic;
	
	private long playerId;
	private int cityId;

	public GameProtos.ArmyDetail convertToProto() {
		GameProtos.ArmyDetail.Builder builder = GameProtos.ArmyDetail.newBuilder();
		builder.setPlayerId(this.playerId);
		builder.setCityId(this.cityId);
		for(Map.Entry<TroopType, List<BattleTroop>> entry : this.backupBattleTroops.entrySet()){
			GameProtos.BattleTroopList.Builder battleTroopList = GameProtos.BattleTroopList.newBuilder();
			battleTroopList.setTroopType(entry.getKey());
			for(BattleTroop battleTroop : entry.getValue()){
				GameProtos.BattleTroop.Builder battleTroopBuilder = GameProtos.BattleTroop.newBuilder();
				battleTroopBuilder.setCount(battleTroop.getCount()).setResTroop(battleTroop.getResTroop());
				battleTroopList.addBattleTroop(battleTroopBuilder);
			}
			builder.addBackupTroops(battleTroopList);
		}
		return builder.build();
	}

	private static class FireComparator implements Comparator<BattleTroop>{
		static final FireComparator INSTANCE = new FireComparator();
		@Override
		public int compare(BattleTroop o1, BattleTroop o2) {
			if(o2.getResTroop().getAmount() != 0){
				if(o1.getResTroop().getAmount() != 0)
					return o1.getResTroop().getAttack2() - o2.getResTroop().getAttack2();
				else
					return o1.getResTroop().getAttack() - o2.getResTroop().getAttack2();
			}else{
				if(o1.getResTroop().getAmount() != 0)
					return o1.getResTroop().getAttack2() - o2.getResTroop().getAttack();
				else
					return o1.getResTroop().getAttack() - o2.getResTroop().getAttack();
			}
		}
	} 
	
	private static class Attack2Comparator implements Comparator<BattleTroop>{
		static final Attack2Comparator INSTANCE = new Attack2Comparator();
		@Override
		public int compare(BattleTroop o1, BattleTroop o2) {
			return o1.getResTroop().getAttack2() - o2.getResTroop().getAttack2();
		}
	}

	private static class AttackComparator implements Comparator<BattleTroop>{
		static final AttackComparator INSTANCE = new AttackComparator();
		@Override
		public int compare(BattleTroop o1, BattleTroop o2) {
			return o1.getResTroop().getAttack() - o2.getResTroop().getAttack();
		}
	}

	private static class FlyComparator implements Comparator<BattleTroop>{
		static final FlyComparator INSTANCE = new FlyComparator();
		@Override
		public int compare(BattleTroop o1, BattleTroop o2) {
			if(o1.getResTroop().getAmount() != 0){
				if(o2.getResTroop().getAmount() != 0){
					return o1.getResTroop().getAttack2() - o2.getResTroop().getAttack2();
				}
				return 1;
			}else{
				if(o2.getResTroop().getAmount() != 0){
					return -1;
				}
				return 0;
			}
		}
	}
	
	//后备部队
	private Map<TroopType, List<BattleTroop>> backupBattleTroops = new HashMap<>();
	//private Map<Integer, Integer> backupTroopsCount = new HashMap<>();

	public Army(GameProtos.ArmyDetail detail){
		this(detail.getPlayerId(), detail.getCityId());
		for(GameProtos.BattleTroopList pbBattleTroops : detail.getBackupTroopsList()){
			List<BattleTroop> battleTroopList = new ArrayList<>();
			for(GameProtos.BattleTroop battleTroop : pbBattleTroops.getBattleTroopList()){
				BattleTroop temp = new BattleTroop();
				temp.setCount(battleTroop.getCount());
				temp.setResTroop(battleTroop.getResTroop());
				battleTroopList.add(temp);
			}
			this.backupBattleTroops.put(pbBattleTroops.getTroopType(), battleTroopList);
		}
	}

	public Army(long playerId, int cityId) {
		this.setPlayerId(playerId);
		this.setCityId(cityId);
	}
	
	public Army(long playerId, int cityId, List<BattleTroop> battleTroops) {
		this(playerId, cityId);
		
		for(BattleTroop bt : battleTroops) {
			TroopType troopType = bt.getResTroop().getTroopType();
			
			//把军队分队
			List<BattleTroop> battleTroopList = this.getBackupBattleTroopsByType(troopType);

			if(bt.getCount() >0)
				battleTroopList.add(new BattleTroop(bt));
			this.backupBattleTroops.put(troopType, battleTroopList);
			
			//TODO：BattleTroop这个对象多态掉，不同的士兵，应该自己知道去哪里，多态的实现方式参照server服务器里面的handler自动注册，自动多态调用的方式，不需要switch函数
		}
	}
	
	public Map<TroopType, List<BattleTroop>> getBackupBattleTroops() {
		return backupBattleTroops;
	}

	public void setBackupBattleTroops(Map<TroopType, List<BattleTroop>> backupBattleTroops) {
		this.backupBattleTroops = backupBattleTroops;
	}
	
	public List<BattleTroop> getBackupBattleTroopsByType(TroopType troopType) {
		List<BattleTroop> troops = this.getBackupBattleTroops().get(troopType);
		if(troops == null){
			troops = new ArrayList<>();
			this.getBackupBattleTroops().put(troopType, troops);
		}
		return troops;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	/**
	 * 判断指定战场位置是否还有后备作战单位
	 * 
	 * @param position
	 * @return
	 */
	boolean hasNextUnit(BattleFieldType position, Slot slot) {
		if(slot.isFull())
			return false;
		switch(position){
		case FIELD_FIRE:
			if(this.getBackupBattleTroopsByType(TroopType.FIRE).size() > 0){
				if(slot.isEmpty()){
					return true;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FIRE)){
						if(bt.getResTroop().getId() == slot.getResTroopId()){
							return true;
						}
					}
				}
			}
			return false;
		case FIELD_FLY_FIRE:
			if(this.getBackupBattleTroopsByType(TroopType.FLY_FIRE).size() > 0){
				if(slot.isEmpty()){
					return true;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FLY_FIRE)){
						if(bt.getResTroop().getId() == slot.getResTroopId()){
							return true;
						}
					}
				}
			}
			return false;
		case FIELD_REMOTE:
			if(this.getBackupBattleTroopsByType(TroopType.REMOTE).size() > 0){
				if(slot.isEmpty()){
					return true;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.REMOTE)){
						if(bt.getResTroop().getId() == slot.getResTroopId()){
							return true;
						}
					}
				}
			}
			return false;
		case FIELD_CLOSE:
			if(this.getBackupBattleTroopsByType(TroopType.WEIGHT).size() > 0){
				if(slot.isEmpty()){
					return true;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.WEIGHT)){
						if(bt.getResTroop().getId() == slot.getResTroopId()){
							return true;
						}
					}
				}
			}else{
				if(this.getBackupBattleTroopsByType(TroopType.LIGHT).size() > 0){
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.LIGHT)){
						if(bt.getResTroop().getId() == slot.getResTroopId()){
							return true;
						}
					}
				}else{
					//没有弹药的远程
					if(this.getBackupBattleTroopsByType(TroopType.REMOTE_NO_AMMO).size() > 0){
						for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.REMOTE_NO_AMMO)){
							if(bt.getResTroop().getId() == slot.getResTroopId()){
								return true;
							}
						}
					}
				}
			}
			return false;
		case FIELD_SIDE:
			if(this.getBackupBattleTroopsByType(TroopType.LIGHT).size() > 0){
				if(slot.isEmpty()){
					return true;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.LIGHT)){
						if(bt.getResTroop().getId() == slot.getResTroopId()){
							return true;
						}
					}
				}
			}
			return false;
		case FIELD_FLY:
			if( this.getBackupBattleTroopsByType(TroopType.FLY_AIR).size() > 0){
				if(slot.isEmpty()){
					return true;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FLY_AIR)){
						if(bt.getResTroop().getId() == slot.getResTroopId()){
							return true;
						}
					}
				}
			}
			return false;
		case FIELD_SUPPORT:
		case FIELD_BACKUP:
			return false;
		default:
			throw new RuntimeException("unkown BattleFieldType: " + position);
		}
	}

	BattleUnit getNextUnit(BattleFieldType position, Slot slot) {
		List<BattleTroop> backups = null;
		BattleUnit nextUnit = null;
		int count;
		if(!this.hasNextUnit(position, slot) || slot.isFull())
			return nextUnit;
		
		switch(position){
		case FIELD_FLY_FIRE:
			backups = this.getBackupBattleTroopsByType(TroopType.FLY_FIRE);
			Collections.sort(backups, FlyComparator.INSTANCE);
			if(backups.size() > 0){
				if(slot.isEmpty()){
					BattleTroop temp = backups.get(backups.size()-1);
					count = slot.getFreeSpace()/temp.getResTroop().getSpace();
					count = Math.min(count, temp.getCount());
					temp.setCount(temp.getCount() - count);
					if(temp.getCount() == 0)
						backups.remove(temp);
					
					nextUnit = new FlyFireBattleUnit(temp, count, this.playerId, this.cityId);
					return nextUnit;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FLY_FIRE)){
						if(bt.getResTroop().getId() == slot.getResTroopId()
								&& (count = slot.getFreeSpace()/bt.getResTroop().getSpace()) > 0){
							count = Math.min(count, bt.getCount());
							bt.setCount(bt.getCount() - count);
							if(bt.getCount() == 0)
								backups.remove(bt);

							nextUnit = new FlyFireBattleUnit(bt, count, this.playerId, this.cityId);
							return nextUnit;
						}
					}
				}
			}
			return null;
		case FIELD_REMOTE:
			backups = this.getBackupBattleTroopsByType(TroopType.REMOTE);
			Collections.sort(backups, Attack2Comparator.INSTANCE);
			if(backups.size() > 0){
				if(slot.isEmpty()){
					BattleTroop temp = backups.get(backups.size()-1);
					count = slot.getFreeSpace()/temp.getResTroop().getSpace();
					count = Math.min(count, temp.getCount());
					temp.setCount(temp.getCount() - count);
					if(temp.getCount() == 0)
						backups.remove(temp);
					
					nextUnit = new RemoteBattleUnit(temp, count, this.playerId, this.cityId);
					return nextUnit;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FLY_FIRE)){
						if(bt.getResTroop().getId() == slot.getResTroopId()
								&& (count = slot.getFreeSpace()/bt.getResTroop().getSpace()) > 0){
							count = Math.min(count, bt.getCount());
							bt.setCount(bt.getCount() - count);
							if(bt.getCount() == 0)
								backups.remove(bt);

							nextUnit = new RemoteBattleUnit(bt, count, this.playerId, this.cityId);
							return nextUnit;
						}
					}
				}
			}
			return null;
		case FIELD_SIDE:
			backups = this.getBackupBattleTroopsByType(TroopType.LIGHT);
			Collections.sort(backups, AttackComparator.INSTANCE);
			if(backups.size() > 0){
				if(slot.isEmpty()){
					BattleTroop temp = backups.get(backups.size()-1);
					count = slot.getFreeSpace()/temp.getResTroop().getSpace();
					count = Math.min(count, temp.getCount());
					temp.setCount(temp.getCount() - count);
					if(temp.getCount() == 0)
						backups.remove(temp);
					
					nextUnit = new LightBattleUnit(temp, count, this.playerId, this.cityId);
					return nextUnit;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FLY_FIRE)){
						if(bt.getResTroop().getId() == slot.getResTroopId()
								&& (count = slot.getFreeSpace()/bt.getResTroop().getSpace()) > 0){
							count = Math.min(count, bt.getCount());
							bt.setCount(bt.getCount() - count);
							if(bt.getCount() == 0)
								backups.remove(bt);

							nextUnit = new LightBattleUnit(bt, count, this.playerId, this.cityId);
							return nextUnit;
						}
					}
				}
			}
			return null;
		case FIELD_FLY:
			backups = this.getBackupBattleTroopsByType(TroopType.FLY_AIR);
			Collections.sort(backups, FlyComparator.INSTANCE);
			if(backups.size() > 0){
				if(slot.isEmpty()){
					BattleTroop temp = backups.get(backups.size()-1);
					count = slot.getFreeSpace()/temp.getResTroop().getSpace();
					count = Math.min(count, temp.getCount());
					temp.setCount(temp.getCount() - count);
					if(temp.getCount() == 0)
						backups.remove(temp);
					
					nextUnit = new FlyAirBattleUnit(temp, count, this.playerId, this.cityId);
					return nextUnit;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FLY_FIRE)){
						if(bt.getResTroop().getId() == slot.getResTroopId()
								&& (count = slot.getFreeSpace()/bt.getResTroop().getSpace()) > 0){
							count = Math.min(count, bt.getCount());
							bt.setCount(bt.getCount() - count);
							if(bt.getCount() == 0)
								backups.remove(bt);

							nextUnit = new FlyAirBattleUnit(bt, count, this.playerId, this.cityId);
							return nextUnit;
						}
					}
				}
			}
			return null;
		case FIELD_FIRE:
			backups = this.getBackupBattleTroopsByType(TroopType.FIRE);
			Collections.sort(backups, FireComparator.INSTANCE);
			if(backups.size() > 0){
				if(slot.isEmpty()){
					BattleTroop temp = backups.get(backups.size()-1);
					count = slot.getFreeSpace()/temp.getResTroop().getSpace();
					count = Math.min(count, temp.getCount());
					temp.setCount(temp.getCount() - count);
					if(temp.getCount() == 0)
						backups.remove(temp);
					
					nextUnit = new FireBattleUnit(temp, count, this.playerId, this.cityId);
					return nextUnit;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FLY_FIRE)){
						if(bt.getResTroop().getId() == slot.getResTroopId()
								&& (count = slot.getFreeSpace()/bt.getResTroop().getSpace()) > 0){
							count = Math.min(count, bt.getCount());
							bt.setCount(bt.getCount() - count);
							if(bt.getCount() == 0)
								backups.remove(bt);

							nextUnit = new FireBattleUnit(bt, count, this.playerId, this.cityId);
							return nextUnit;
						}
					}
				}
			}
			return null;
		case FIELD_CLOSE:
			backups = this.getBackupBattleTroopsByType(TroopType.WEIGHT);
			if(backups.size() > 0){
				if(slot.isEmpty()){
					BattleTroop temp = backups.get(backups.size()-1);
					count = slot.getFreeSpace()/temp.getResTroop().getSpace();
					count = Math.min(count, temp.getCount());
					temp.setCount(temp.getCount() - count);
					if(temp.getCount() == 0)
						backups.remove(temp);
					
					nextUnit = new WeightBattleUnit(temp, count, this.playerId, this.cityId);
					return nextUnit;
				}else{
					for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FLY_FIRE)){
						if(bt.getResTroop().getId() == slot.getResTroopId()
								&& (count = slot.getFreeSpace()/bt.getResTroop().getSpace()) > 0){
							count = Math.min(count, bt.getCount());
							bt.setCount(bt.getCount() - count);
							if(bt.getCount() == 0)
								backups.remove(bt);

							nextUnit = new WeightBattleUnit(bt, count, this.playerId, this.cityId);
							return nextUnit;
						}
					}
				}
			}else{
				backups = this.getBackupBattleTroopsByType(TroopType.LIGHT);
				if(backups.size() > 0){
					if(slot.isEmpty()){
						BattleTroop temp = backups.get(backups.size()-1);
						count = slot.getFreeSpace()/temp.getResTroop().getSpace();
						count = Math.min(count, temp.getCount());
						temp.setCount(temp.getCount() - count);
						if(temp.getCount() == 0)
							backups.remove(temp);
						
						nextUnit = new LightBattleUnit(temp, count, this.playerId, this.cityId);
						return nextUnit;
					}else{
						for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FLY_FIRE)){
							if(bt.getResTroop().getId() == slot.getResTroopId()
									&& (count = slot.getFreeSpace()/bt.getResTroop().getSpace()) > 0){
								count = Math.min(count, bt.getCount());
								bt.setCount(bt.getCount() - count);
								if(bt.getCount() == 0)
									backups.remove(bt);
								
								nextUnit = new LightBattleUnit(bt, count, this.playerId, this.cityId);
								return nextUnit;
							}
						}
					}
				}else{
					backups = this.getBackupBattleTroopsByType(TroopType.REMOTE_NO_AMMO);//没有弹药的远程
					if(slot.isEmpty()){
						BattleTroop temp = backups.get(backups.size()-1);
						count = slot.getFreeSpace()/temp.getResTroop().getSpace();
						count = Math.min(count, temp.getCount());
						temp.setCount(temp.getCount() - count);
						if(temp.getCount() == 0)
							backups.remove(temp);
						
						nextUnit = new RemoteBattleUnit(temp, count, this.playerId, this.cityId);
						return nextUnit;
					}else{
						for(BattleTroop bt : this.getBackupBattleTroopsByType(TroopType.FLY_FIRE)){
							if(bt.getResTroop().getId() == slot.getResTroopId()
									&& (count = slot.getFreeSpace()/bt.getResTroop().getSpace()) > 0){
								count = Math.min(count, bt.getCount());
								bt.setCount(bt.getCount() - count);
								if(bt.getCount() == 0)
									backups.remove(bt);
								
								nextUnit = new RemoteBattleUnit(bt, count, this.playerId, this.cityId);
								return nextUnit;
							}
						}
					}
				}
			}
			return null;
		case FIELD_SUPPORT:
		case FIELD_BACKUP:
			return null;
		default:
			throw new RuntimeException("unkown BattleFieldType: " + position);
		}
	}

	void addAUnit(TroopType troopType, BattleUnit bu){
		List<BattleTroop> troops = this.getBackupBattleTroopsByType(troopType);
		for(BattleTroop bt : troops){
			if(bt.getResTroop().getId() == bu.getTroopId()){
				bt.setCount(bt.getCount() + bu.getCount());
				return;
			}
		}
		
		BattleTroop temp = new BattleTroop();
		temp.setCount(bu.getCount());
		ResTroop.Builder res = configLogic.getResTroop(bu.getTroopId()).toBuilder();
		res.setAttack(bu.getAttack()).setAttack2(bu.getAttack2()).setDefense(bu.getDefense());
		temp.setResTroop(res.build());
		
		troops.add(temp);
	}

	@Override
	public String toString() {
		return "Army{" +
				"playerId=" + playerId +
				", cityId=" + cityId +
				", backupBattleTroops=" + backupBattleTroops +
				'}';
	}

	public List<GameProtos.Troop> convertToTroops() {
		List<GameProtos.Troop> troops = new ArrayList<>();
		for(List<BattleTroop> list : this.backupBattleTroops.values()){
			for(BattleTroop bt : list){
				GameProtos.Troop.Builder builder = GameProtos.Troop.newBuilder();
				builder.setTroopResId(bt.getResTroop().getId());
				builder.setCount(bt.getCount());
				builder.setAttack(bt.getResTroop().getAttack());
				builder.setAttack2(bt.getResTroop().getAttack2());
				builder.setDefense(bt.getResTroop().getDefense());
				
				troops.add(builder.build());
			}
		}
		return troops;
	}

}



