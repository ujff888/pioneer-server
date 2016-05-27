package cn.litgame.wargame.core.model.battle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.litgame.wargame.core.auto.GameProtos.Troop;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.auto.GameResProtos.TroopType;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.battle.unit.BattleUnit;
import cn.litgame.wargame.core.model.battle.unit.FireBattleUnit;
import cn.litgame.wargame.core.model.battle.unit.FlyAirBattleUnit;
import cn.litgame.wargame.core.model.battle.unit.FlyFireBattleUnit;
import cn.litgame.wargame.core.model.battle.unit.LightBattleUnit;
import cn.litgame.wargame.core.model.battle.unit.LogisticsBattleUnit;
import cn.litgame.wargame.core.model.battle.unit.NpcBattleUnit;
import cn.litgame.wargame.core.model.battle.unit.RemoteBattleUnit;
import cn.litgame.wargame.core.model.battle.unit.WeightBattleUnit;

/**
 * 军队的抽象
 * 
 * @author 熊纪元
 *
 */
public class Army implements Serializable{
	private static final long serialVersionUID = -4595006475981998252L;
	private long playerId;
	private int cityId;
	
	private static class FireComparator implements Comparator<BattleUnit>{
		static final FireComparator INSTANCE = new FireComparator();
		private FireComparator(){}
		@Override
		public int compare(BattleUnit o1, BattleUnit o2) {
			if(o2.getAmount() != 0){
				if(o1.getAmount() != 0)
					return o1.getAttack2() - o2.getAttack2();
				else
					return o1.getAttack() - o2.getAttack2();
			}else{
				if(o1.getAmount() != 0)
					return o1.getAttack2() - o2.getAttack();
				else
					return o1.getAttack() - o2.getAttack();
			}
		}
	} 
	
	private static class Attack2Comparator implements Comparator<BattleUnit>{
		static final Attack2Comparator INSTANCE = new Attack2Comparator();
		@Override
		public int compare(BattleUnit o1, BattleUnit o2) {
			return o1.getAttack2() - o2.getAttack2();
		}
	}

	private static class AttackComparator implements Comparator<BattleUnit>{
		static final AttackComparator INSTANCE = new AttackComparator();
		@Override
		public int compare(BattleUnit o1, BattleUnit o2) {
			return o1.getAttack() - o2.getAttack();
		}
	}

	private static class FlyComparator implements Comparator<BattleUnit>{
		static final FlyComparator INSTANCE = new FlyComparator();
		@Override
		public int compare(BattleUnit o1, BattleUnit o2) {
			if(o1.getAmount() != 0){
				if(o2.getAmount() != 0){
					return o1.getAttack2() - o2.getAttack2();
				}
				return 1;
			}else{
				if(o2.getAmount() != 0){
					return -1;
				}
				return 0;
			}
		}
	}

	//后备部队
	private Map<TroopType, List<BattleUnit>> backupTroops = new HashMap<>();
	//private Map<Integer, Integer> backupTroopsCount = new HashMap<>();

	public Army(long playerId, int cityId) {
		this.setPlayerId(playerId);
		this.setCityId(cityId);
		
		List<BattleUnit> fly = new ArrayList<>();
		List<BattleUnit> flyFire = new ArrayList<>();
		List<BattleUnit> fire = new ArrayList<>();
		List<BattleUnit> remote = new ArrayList<>();
		List<BattleUnit> weight = new ArrayList<>();
		List<BattleUnit> light = new ArrayList<>();
		List<BattleUnit> logistics = new ArrayList<>();
		List<BattleUnit> npc = new ArrayList<>();
		List<BattleUnit> remoteOutOfAmmo = new ArrayList<>();
			
		backupTroops.put(TroopType.FLY_AIR, fly);
		backupTroops.put(TroopType.FLY_FIRE, flyFire);
		backupTroops.put(TroopType.FIRE, fire);
		backupTroops.put(TroopType.REMOTE, remote);
		backupTroops.put(TroopType.WEIGHT, weight);
		backupTroops.put(TroopType.LIGHT, light);
		backupTroops.put(TroopType.LOGISTICS, logistics);
		backupTroops.put(TroopType.NPC, npc);
		backupTroops.put(TroopType.REMOTE_NO_AMMO, remoteOutOfAmmo);//没有弹药的远程
	}
	
	public Army(long playerId, int cityId, List<BattleTroop> battleTroops) {
		this(playerId, cityId);

		for(BattleTroop bt : battleTroops) {
			//把军队实例化成具体的作战单位并分队
			int count = bt.getCount();
			TroopType troopType = bt.getResTroop().getTroopType();
			switch(troopType){
			case REMOTE:
				for(int i=0;i<count;i++){
					BattleUnit bu = new RemoteBattleUnit(bt);
					
					bu.setPlayerId(this.getPlayerId());
					bu.setCityId(this.getCityId());
					
					this.getBackupTroopsByType(troopType).add(bu);
				}
				break;
			case LIGHT:
				for(int i=0;i<count;i++){
					BattleUnit bu = new LightBattleUnit(bt);
					
					bu.setPlayerId(this.getPlayerId());
					bu.setCityId(this.getCityId());
					
					this.getBackupTroopsByType(troopType).add(bu);
				}
				break;
			case WEIGHT:
				for(int i=0;i<count;i++){
					BattleUnit bu = new WeightBattleUnit(bt);
					
					bu.setPlayerId(this.getPlayerId());
					bu.setCityId(this.getCityId());
					
					this.getBackupTroopsByType(troopType).add(bu);
				}
				break;
			case FLY_AIR:
				for(int i=0;i<count;i++){
					BattleUnit bu = new FlyAirBattleUnit(bt);
					
					bu.setPlayerId(this.getPlayerId());
					bu.setCityId(this.getCityId());
					
					this.getBackupTroopsByType(troopType).add(bu);
				}
				break;
			case FLY_FIRE:
				for(int i=0;i<count;i++){
					BattleUnit bu = new FlyFireBattleUnit(bt);
					
					bu.setPlayerId(this.getPlayerId());
					bu.setCityId(this.getCityId());
					
					this.getBackupTroopsByType(troopType).add(bu);
				}
				break;
			case FIRE:
				for(int i=0;i<count;i++){
					BattleUnit bu = new FireBattleUnit(bt);
					
					bu.setPlayerId(this.getPlayerId());
					bu.setCityId(this.getCityId());
					
					this.getBackupTroopsByType(troopType).add(bu);
				}
				break;
			case LOGISTICS:
				for(int i=0;i<count;i++){
					BattleUnit bu = new LogisticsBattleUnit(bt);
					
					bu.setPlayerId(this.getPlayerId());
					bu.setCityId(this.getCityId());
					
					this.getBackupTroopsByType(troopType).add(bu);
				}
				break;
			case NPC:
				for(int i=0;i<count;i++){
					BattleUnit bu = new NpcBattleUnit(bt);
					
					bu.setPlayerId(this.getPlayerId());
					bu.setCityId(this.getCityId());
					
					this.getBackupTroopsByType(troopType).add(bu);
				}
				break;
			default:
				break;
			}
		}
	}
	
	Map<TroopType, List<BattleUnit>> getBackupTroops() {
		return this.backupTroops;
	}
	
	List<BattleUnit> getBackupTroopsByType(TroopType troopType) {
		return this.backupTroops.get(troopType);
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
	 * 将指定战场位置的后备作战单位移出
	 * 
	 * @param position
	 */
	void popAUnitFromBackup(FieldPosition position) {
		int index;
		switch(position.getType()){
		case FIELD_FIRE:
			index = this.getBackupTroopsByType(TroopType.FIRE).size()-1;
			this.getBackupTroopsByType(TroopType.FIRE).remove(index);
			break;
		case FIELD_FLY_FIRE:
			index = this.getBackupTroopsByType(TroopType.FLY_FIRE).size()-1;
			this.getBackupTroopsByType(TroopType.FLY_FIRE).remove(index);
			break;
		case FIELD_REMOTE:
			index = this.getBackupTroopsByType(TroopType.REMOTE).size()-1;
			this.getBackupTroopsByType(TroopType.REMOTE).remove(index);
			break;
		case FIELD_SIDE:
			index = this.getBackupTroopsByType(TroopType.LIGHT).size()-1;
			this.getBackupTroopsByType(TroopType.LIGHT).remove(index);
			break;
		case FIELD_FLY:
			index = this.getBackupTroopsByType(TroopType.FLY_AIR).size()-1;
			this.getBackupTroopsByType(TroopType.FLY_AIR).remove(index);
			break;
		case FIELD_CLOSE:
			if(backupTroops.get(TroopType.WEIGHT).size() > 0){
				index = this.getBackupTroopsByType(TroopType.WEIGHT).size()-1;
				this.getBackupTroopsByType(TroopType.WEIGHT).remove(index);
			}else{
				if(backupTroops.get(TroopType.LIGHT).size() > 0){
					index = this.getBackupTroopsByType(TroopType.LIGHT).size()-1;
					this.getBackupTroopsByType(TroopType.LIGHT).remove(index);
				}else{
					//没有弹药的远程
					if(backupTroops.get(TroopType.REMOTE_NO_AMMO).size() > 0){
						index = this.getBackupTroopsByType(TroopType.REMOTE_NO_AMMO).size()-1;
						this.getBackupTroopsByType(TroopType.REMOTE_NO_AMMO).remove(index);
					}
				}
			}
			break;
		case FIELD_SUPPORT:
		case FIELD_BACKUP:
			break;
		default:
			throw new RuntimeException("unkown BattleFieldType: " + position.getType());
		}
	}
	
	/**
	 * 判断指定战场位置是否还有后备作战单位
	 * 
	 * @param position
	 * @return
	 */
	boolean hasNextUnit(BattleFieldType position) {
		switch(position){
		case FIELD_FIRE:
			if(backupTroops.get(TroopType.FIRE).size() > 0){
				return true;
			}
			return false;
		case FIELD_FLY_FIRE:
			if(backupTroops.get(TroopType.FLY_FIRE).size() > 0){
				return true;
			}
			return false;
		case FIELD_REMOTE:
			if(backupTroops.get(TroopType.REMOTE).size() > 0){
				return true;
			}
			return false;
		case FIELD_CLOSE:
			if(backupTroops.get(TroopType.WEIGHT).size() > 0){
				return true;
			}else{
				if(backupTroops.get(TroopType.LIGHT).size() > 0){
					return true;
				}else{
					//没有弹药的远程
					if(backupTroops.get(TroopType.REMOTE_NO_AMMO).size() > 0){
						return true;
					}
				}
			}
			return false;
		case FIELD_SIDE:
			if(backupTroops.get(TroopType.LIGHT).size() > 0){
				return true;
			}
			return false;
		case FIELD_FLY:
			if(backupTroops.get(TroopType.FLY_AIR).size() > 0){
				return true;
			}
			return false;
		case FIELD_SUPPORT:
		case FIELD_BACKUP:
			return false;
		default:
			throw new RuntimeException("unkown BattleFieldType: " + position);
		}
	}
	
	
	BattleUnit getNextUnit(BattleFieldType position) {
		List<BattleUnit> backups = null;
		BattleUnit nextUnit = null;
		switch(position){
		case FIELD_FLY_FIRE:
			backups = backupTroops.get(TroopType.FLY_FIRE);
			Collections.sort(backups, FlyComparator.INSTANCE);
			if(backups.size() > 0){
				nextUnit = backups.get(backups.size()-1);
				return nextUnit;
			}
			return null;
		case FIELD_REMOTE:
			backups = backupTroops.get(TroopType.REMOTE);
			Collections.sort(backups, Attack2Comparator.INSTANCE);
			if(backups.size() > 0){
				nextUnit = backups.get(backups.size()-1);
				return nextUnit;
			}
			return null;
		case FIELD_SIDE:
			backups = backupTroops.get(TroopType.LIGHT);
			Collections.sort(backups, AttackComparator.INSTANCE);
			if(backups.size() > 0){
				nextUnit = backups.get(backups.size()-1);
				return nextUnit;
			}
			return null;
		case FIELD_FLY:
			backups = backupTroops.get(TroopType.FLY_AIR);
			Collections.sort(backups, FlyComparator.INSTANCE);
			if(backups.size() > 0){
				nextUnit = backups.get(backups.size()-1);
				return nextUnit;
			}
			return null;
		case FIELD_FIRE:
			backups = backupTroops.get(TroopType.FIRE);
			Collections.sort(backups, FireComparator.INSTANCE);
			if(backups.size() > 0){
				nextUnit = backups.get(backups.size()-1);
				return nextUnit;
			}
			return null;
		case FIELD_CLOSE:
			backups = backupTroops.get(TroopType.WEIGHT);
			if(backups.size() > 0){
				nextUnit = backups.get(backups.size()-1);
				return nextUnit;
			}else{
				backups = backupTroops.get(TroopType.LIGHT);
				if(backups.size() > 0){
					nextUnit = backups.get(backups.size()-1);
					return nextUnit;
				}else{
					backups = backupTroops.get(TroopType.REMOTE_NO_AMMO);//没有弹药的远程
					if(backups.size() > 0){
						nextUnit = backups.get(backups.size()-1);
						return nextUnit;
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

	@Override
	public String toString() {
		return "Army [playerId=" + playerId + ", cityId=" + cityId + ", backupTroops=" + backupTroops
				+ "]";
	}

	public List<Troop> convertToTroops() {
		Map<Integer,Troop> troops = new HashMap<>();
		for(List<BattleUnit> units : this.backupTroops.values()){
			for(BattleUnit unit : units){
				Troop temp = troops.get(unit.getTroopId());
				if(temp == null){
					Troop.Builder builder = Troop.newBuilder();
				
					builder.setAttack(unit.getAttack());
					builder.setAttack2(unit.getAttack2());
					builder.setCount(1);
					builder.setDefense(unit.getDefense());
					builder.setTroopResId(unit.getTroopId());
					temp = builder.build();
				}else{
					Troop.Builder builder = temp.toBuilder();
					builder.setCount(temp.getCount()+1);
					temp = builder.build();
				}
				troops.put(temp.getTroopResId(), temp);
			}
		}
		return new ArrayList<Troop>(troops.values());
	}

}



