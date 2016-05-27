package cn.litgame.wargame.core.model.battle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos.BattleFieldType;
import cn.litgame.wargame.core.model.battle.round.BattleRoundDetail;
import cn.litgame.wargame.core.model.battle.round.BattleRoundInfo;
import cn.litgame.wargame.core.model.battle.round.RoundTroopDetail;
import cn.litgame.wargame.core.model.battle.round.RoundTroopInfo;
import cn.litgame.wargame.core.model.battle.unit.BattleUnit;

/**
 * 战斗回合，包含了每个回合的情报
 * 
 * @author 熊纪元
 *
 */
public class BattleRound implements Serializable {

	private static final long serialVersionUID = 5067244562806620768L;
	private static final Logger log = Logger.getLogger(BattleRound.class);

	private int seqNo;//回合数
	
	private Map<String, BattleRoundInfo> roundInfoOff = new HashMap<>();
	private Map<String, BattleRoundInfo> roundInfoDef = new HashMap<>();
	
	private BattleRoundDetail roundDetailOff = new BattleRoundDetail();
	private BattleRoundDetail roundDetailDef = new BattleRoundDetail(); 
	
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


	void save(BattleField field){
		log.info("==========Round("+seqNo+")============");
		log.info(this);
		
		for(BattleRoundInfo brif : roundInfoOff.values()){
			GameProtos.BattleRoundInfo.Builder pBattleRoundInfo = GameProtos.BattleRoundInfo.newBuilder();	
			pBattleRoundInfo.setPlayerId(brif.getPlayeId());
			pBattleRoundInfo.setCityId(brif.getCityId());
			for(RoundTroopInfo rtif : brif.getRoundTroopInfoMap().values()){
				GameProtos.RoundTroopInfo.Builder pRoundTroopInfo = GameProtos.RoundTroopInfo.newBuilder();
				pRoundTroopInfo.setCount(rtif.getCount());
				pRoundTroopInfo.setLost(rtif.getLost());
				pRoundTroopInfo.setTroopId(rtif.getTroopId());
				pBattleRoundInfo.addRoundTroopInfo(pRoundTroopInfo.build());
			}
			log.info(pBattleRoundInfo);
		}
		
		for(BattleRoundInfo brif : roundInfoDef.values()){
			GameProtos.BattleRoundInfo.Builder pBattleRoundInfo = GameProtos.BattleRoundInfo.newBuilder();	
			pBattleRoundInfo.setPlayerId(brif.getPlayeId());
			pBattleRoundInfo.setCityId(brif.getCityId());
			for(RoundTroopInfo rtif : brif.getRoundTroopInfoMap().values()){
				GameProtos.RoundTroopInfo.Builder pRoundTroopInfo = GameProtos.RoundTroopInfo.newBuilder();
				pRoundTroopInfo.setCount(rtif.getCount());
				pRoundTroopInfo.setLost(rtif.getLost());
				pRoundTroopInfo.setTroopId(rtif.getTroopId());
			
				pBattleRoundInfo.addRoundTroopInfo(pRoundTroopInfo.build());
			}
			log.info(pBattleRoundInfo);
		}
		
		GameProtos.BattleRoundDetail.Builder pBattleRoundDetailOff = GameProtos.BattleRoundDetail.newBuilder();
		pBattleRoundDetailOff.setMorale(field.getMoraleOff());
		for(RoundTroopDetail rtd : roundDetailOff.getRoundTroopDetailMap().values()){
			GameProtos.RoundTroopDetail.Builder pRoundTroopDetail = GameProtos.RoundTroopDetail.newBuilder();
			pRoundTroopDetail.setAmountRemain(rtd.getAmountRemain());
			pRoundTroopDetail.setCount(rtd.getCount());
			pRoundTroopDetail.setLost(rtd.getLost());
			pRoundTroopDetail.setFieldType(rtd.getFieldType());
			pRoundTroopDetail.setTroopId(rtd.getTroopId());
			
			pBattleRoundDetailOff.addRoundTroopDetail(pRoundTroopDetail.build());
		}
		log.info(pBattleRoundDetailOff);
		
		GameProtos.BattleRoundDetail.Builder pBattleRoundDetailDef = GameProtos.BattleRoundDetail.newBuilder();
		pBattleRoundDetailDef.setMorale(field.getMoraleDef());
		for(RoundTroopDetail rtd : roundDetailDef.getRoundTroopDetailMap().values()){
			GameProtos.RoundTroopDetail.Builder pRoundTroopDetail = GameProtos.RoundTroopDetail.newBuilder();
			pRoundTroopDetail.setAmountRemain(rtd.getAmountRemain());
			pRoundTroopDetail.setCount(rtd.getCount());
			pRoundTroopDetail.setLost(rtd.getLost());
			pRoundTroopDetail.setFieldType(rtd.getFieldType());
			pRoundTroopDetail.setTroopId(rtd.getTroopId());
			
			pBattleRoundDetailDef.addRoundTroopDetail(pRoundTroopDetail.build());
		}
		log.info(pBattleRoundDetailDef);
		
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

	
}
