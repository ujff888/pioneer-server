package cn.litgame.wargame.core.model.battle;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.battle.round.BattleRoundDetail;
import cn.litgame.wargame.core.model.battle.round.BattleRoundInfo;
import cn.litgame.wargame.core.model.battle.round.RoundTroopDetail;
import cn.litgame.wargame.core.model.battle.round.RoundTroopInfo;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * 战斗回合，包含了每个回合的情报
 * 
 * @author 熊纪元
 *
 */
public class BattleRound{
	private static final Logger log = Logger.getLogger(BattleRound.class);

	private int seqNo;//回合数
	
	private Map<String, BattleRoundInfo> roundInfoOff = new HashMap<>();
	private Map<String, BattleRoundInfo> roundInfoDef = new HashMap<>();
	
	private BattleRoundDetail roundDetailOff = new BattleRoundDetail();
	private BattleRoundDetail roundDetailDef = new BattleRoundDetail();

	private int offenceLost = 0;
	private int defenceLost = 0;
	
	public Map<String, BattleRoundInfo> getRoundInfoOff() {
		return roundInfoOff;
	}

	public Map<String, BattleRoundInfo> getRoundInfoDef() {
		return roundInfoDef;
	}

	public BattleRoundDetail getRoundDetailOff() {
		return roundDetailOff;
	}

	public BattleRoundDetail getRoundDetailDef() {
		return roundDetailDef;
	}

	public BattleRound(int seq) {
		this.seqNo = seq;
	}

	
	public int getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}

	public void setRoundInfoOff(Map<String, BattleRoundInfo> roundInfoOff) {
		this.roundInfoOff = roundInfoOff;
	}

	public void setRoundInfoDef(Map<String, BattleRoundInfo> roundInfoDef) {
		this.roundInfoDef = roundInfoDef;
	}

	public void setRoundDetailOff(BattleRoundDetail roundDetailOff) {
		this.roundDetailOff = roundDetailOff;
	}

	public void setRoundDetailDef(BattleRoundDetail roundDetailDef) {
		this.roundDetailDef = roundDetailDef;
	}

	public int getDefenceLost() {
		return defenceLost;
	}

	public void setDefenceLost(int defenceLost) {
		this.defenceLost = defenceLost;
	}

	public int getOffenceLost() {
		return offenceLost;
	}

	public void setOffenceLost(int offenceLost) {
		this.offenceLost = offenceLost;
	}

	void save(BattleField field){
		log.info("==========Round("+seqNo+")============");
		log.info(this);
		generateBattleRoundInfo(true);
		generateBattleRoundInfo(false);

		generateRoundDetail(field, true);
		generateRoundDetail(field, false);

		generateSimpleBattleInfo(field);
		generateBattleDetail(field);
	}

	@Override
	public String toString() {
		return "BattleRound [\nseqNo=" + seqNo + ", \nroundInfoOff=" + roundInfoOff + ", \nroundInfoDef=" + roundInfoDef
				+ ", \nroundDetailOff=" + roundDetailOff + ", \nroundDetailDef=" + roundDetailDef + "]";
	}

	public int getTotalLost(boolean offence) {
		int lost = 0;
		for(RoundTroopDetail info : 
			offence ? roundDetailOff.getRoundTroopDetailMap().values()
					: roundDetailDef.getRoundTroopDetailMap().values()){
			lost += info.getLost();
		}
		return lost;
	}

	public byte[] toByteArray() {
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);){
			oos.writeObject(this);
			return bos.toByteArray();
		} catch (IOException e) {
			log.error(e);
			return new byte[0];
		}
	}

	private void generateBattleRoundInfo(boolean isOffence){
		Map<String, BattleRoundInfo> battleRoundInfoMap = isOffence ? roundInfoOff : roundInfoDef;
		for(BattleRoundInfo brif : battleRoundInfoMap.values()){
			GameProtos.BattleRoundInfo.Builder pBattleRoundInfo = GameProtos.BattleRoundInfo.newBuilder();
			pBattleRoundInfo.setPlayerId(brif.getPlayeId());
			pBattleRoundInfo.setCityId(brif.getCityId());
			for(RoundTroopInfo rtif : brif.getRoundTroopInfoMap().values()){
				GameProtos.RoundTroopInfo.Builder pRoundTroopInfo = GameProtos.RoundTroopInfo.newBuilder();
				pRoundTroopInfo.setCount(rtif.getCount());
				pRoundTroopInfo.setTroopId(rtif.getTroopId());

				pBattleRoundInfo.addRoundTroopInfo(pRoundTroopInfo.build());
			}
			log.info(pBattleRoundInfo);
		}
	}

	private void generateRoundDetail(BattleField field, boolean isOffence){
		BattleRoundDetail battleRoundDetail = isOffence ? roundDetailOff : roundDetailDef;
		GameProtos.BattleRoundDetail.Builder pBattleRoundDetail = GameProtos.BattleRoundDetail.newBuilder();
		pBattleRoundDetail.setMorale(isOffence ? field.getMoraleOff() : field.getMoraleDef());
		for(RoundTroopDetail rtd : battleRoundDetail.getRoundTroopDetailMap().values()){
			GameProtos.RoundTroopDetail.Builder pRoundTroopDetail = GameProtos.RoundTroopDetail.newBuilder();
			pRoundTroopDetail.setAmountRemain(rtd.getAmountRemain());
			pRoundTroopDetail.setCount(rtd.getCount());
			pRoundTroopDetail.setLost(rtd.getLost());
			pRoundTroopDetail.setFieldType(rtd.getFieldType());
			pRoundTroopDetail.setTroopId(rtd.getTroopId());

			pBattleRoundDetail.addRoundTroopDetail(pRoundTroopDetail.build());
		}
		//log.info(pBattleRoundDetail);
	}

	private GameProtos.SimpleBattleInfo generateSimpleBattleInfo(BattleField field){
		GameProtos.SimpleBattleInfo.Builder simpleBattleInfo = GameProtos.SimpleBattleInfo.newBuilder();
		simpleBattleInfo.setIsOver(field.getResult() != GameProtos.BattleResult.FIGHTING);
		City city = field.cityLogic.getCity(field.getFieldCityId());
		String playerName = field.playerLogic.getPlayer(city.getPlayerId()).getPlayerName();
		simpleBattleInfo.setCityId(city.getCityId());
		simpleBattleInfo.setCityLevel(city.getLevel());
		simpleBattleInfo.setCityName(city.getCityName());
		simpleBattleInfo.setPlayerName(playerName);
		long lastRoundTime = field.getStartTime() + (field.getCurrentRoundNum()-2)* field.getNextActionTime();
		simpleBattleInfo.setLastRoundTime((int)(lastRoundTime/1000));
		if(field.isLand())
			simpleBattleInfo.setType(GameProtos.BattleType.LAND_WAR);
		else
			simpleBattleInfo.setType(GameProtos.BattleType.AIR_WAR);
		if(simpleBattleInfo.getIsOver()){
			if(field.getResult() == GameProtos.BattleResult.OFFENCE_WIN){
				for(Army a : field.getArmysOffence()){
					simpleBattleInfo.addWinnerId(a.getPlayerId());
				}
			}else if(field.getResult() == GameProtos.BattleResult.DEFENCE_WIN){
				for(Army a : field.getArmysDefence()){
					simpleBattleInfo.addWinnerId(a.getPlayerId());
				}
			}
		}
		simpleBattleInfo.setBattleId(field.getUUID());
		simpleBattleInfo.setRoundNum(field.getCurrentRoundNum()-1);
		//log.info(simpleBattleInfo.build());
		return simpleBattleInfo.build();
	}

	private GameProtos.BattleDetail generateBattleDetail(BattleField field) {
		GameProtos.BattleDetail.Builder battleDetail = GameProtos.BattleDetail.newBuilder();
		battleDetail.setSimpleInfo(this.generateSimpleBattleInfo(field));
		battleDetail.addAllOffence(getProtoArmyList(field, true));
		battleDetail.addAllDefence(getProtoArmyList(field, false));

		GameProtos.SimpleArmyInfo[] simpleArmyInfos = generateSimpleArmyInfo(field);
		battleDetail.setOffenceInfo(simpleArmyInfos[0]);
		battleDetail.setDefenceInfo(simpleArmyInfos[1]);

		//log.info(battleDetail.build());
		return battleDetail.build();
	}

	private List<GameProtos.Army> getProtoArmyList(BattleField field, boolean isOffence){
		List<Army> armies = isOffence ? field.getArmysOffence() : field.getArmysDefence();
		List<GameProtos.Army> parmies = new ArrayList<>();
		for(Army a : armies){
			GameProtos.Army.Builder builder = GameProtos.Army.newBuilder();
			City city = field.cityLogic.getCity(a.getCityId());
			String playerName = field.playerLogic.getPlayer(city.getPlayerId()).getPlayerName();
			builder.setCityName(city.getCityName());
			builder.setPlayerName(playerName);
			parmies.add(builder.build());
		}
		return parmies;
	}

	private GameProtos.SimpleArmyInfo[] generateSimpleArmyInfo(BattleField field){
		GameProtos.SimpleArmyInfo.Builder simpleArmyInfoOff = GameProtos.SimpleArmyInfo.newBuilder();
		GameProtos.SimpleArmyInfo.Builder simpleArmyInfoDef = GameProtos.SimpleArmyInfo.newBuilder();
		Collection<BattleRoundInfo> InfoOff = roundInfoOff.values();
		Collection<BattleRoundInfo> InfoDef = roundInfoDef.values();

		simpleArmyInfoOff.setPlayerCount(field.getArmysOffence().size());
		simpleArmyInfoDef.setPlayerCount(field.getArmysDefence().size());

		int lost = 0,remain = 0;

		for(BattleRoundInfo info : InfoOff){
			for(RoundTroopInfo troopInfo : info.getRoundTroopInfoMap().values()){
				lost += troopInfo.getLost();
				remain += troopInfo.getCount();
			}
		}
		simpleArmyInfoOff.setLost(lost).setRemain(remain);

		this.setOffenceLost(lost);

		lost = remain = 0;
		for(BattleRoundInfo info : InfoDef){
			for(RoundTroopInfo troopInfo : info.getRoundTroopInfoMap().values()){
				lost += troopInfo.getLost();
				remain += troopInfo.getCount();
			}
		}
		simpleArmyInfoDef.setLost(lost).setRemain(remain);

		simpleArmyInfoOff.setKill(simpleArmyInfoDef.getLost());
		simpleArmyInfoDef.setKill(simpleArmyInfoOff.getLost());

		this.setDefenceLost(lost);

		for(BattleRound round : field.getRoundHistory()){
			simpleArmyInfoOff.setLost(simpleArmyInfoOff.getLost() + round.offenceLost);
			simpleArmyInfoDef.setLost(simpleArmyInfoDef.getLost() + round.defenceLost);

			simpleArmyInfoOff.setKill(simpleArmyInfoOff.getKill() + round.defenceLost);
			simpleArmyInfoDef.setKill(simpleArmyInfoDef.getKill() + round.offenceLost);
		}

		return new GameProtos.SimpleArmyInfo[]{simpleArmyInfoOff.build(), simpleArmyInfoDef.build()};
	}

	private int generateLost(BattleRound round, boolean isOffence){
		Collection<BattleRoundInfo> infoCollection = isOffence ? round.getRoundInfoOff().values() : round.getRoundInfoDef().values();
		int lost = 0;
		for(BattleRoundInfo info : infoCollection){
			for(RoundTroopInfo troopInfo : info.getRoundTroopInfoMap().values()){
				lost += troopInfo.getLost();
			}
		}
		return lost;
	}
}
