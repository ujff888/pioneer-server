package cn.litgame.wargame.core.model;
   import java.util.List;

public class Battle {
	private long playerId;
	/**
	 * 进攻方的部队
	 */
	private List<BattleTroop> a;
	/**
	 * 防守方的部队
	 */
	private List<BattleTroop> b;
	
	
//	private List<Troop> 
//	private List<Troop> 
//	private List<Troop> 
//	private List<Troop> 
//	private List<Troop> 
//	private List<Troop> 
//	private List<Troop> 
//	private List<Troop> 
	
	public Battle(){
		
	}
	
	public Battle(List<BattleTroop> a, List<BattleTroop> b){
		this.a = a ;
		this.b = b;
	}
	
	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	public List<BattleTroop> getA() {
		return a;
	}
	public void setA(List<BattleTroop> a) {
		this.a = a;
	}
	public List<BattleTroop> getB() {
		return b;
	}
	public void setB(List<BattleTroop> b) {
		this.b = b;
	}
	
	@Override
	public String toString() {
		return "Battle [playerId=" + playerId + ", a=" + a + ", b=" + b + "]";
	}
	
}
