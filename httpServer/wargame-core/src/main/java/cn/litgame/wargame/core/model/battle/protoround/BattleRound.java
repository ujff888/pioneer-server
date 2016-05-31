package cn.litgame.wargame.core.model.battle.protoround;

import cn.litgame.wargame.core.auto.GameProtos;
import cn.litgame.wargame.core.auto.GameResProtos;
import cn.litgame.wargame.core.model.BattleTroop;
import cn.litgame.wargame.core.model.battle.Army;
import cn.litgame.wargame.core.model.battle.BattleField;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * Created by 熊纪元 on 2016/5/29.
 */
public class BattleRound {
    private static final Logger log = Logger.getLogger(BattleRound.class);

    private int seqNo;//回合数

    private GameProtos.BattleRoundInfo roundInfoOff = GameProtos.BattleRoundInfo.getDefaultInstance();
    private GameProtos.BattleRoundInfo roundInfoDef = GameProtos.BattleRoundInfo.getDefaultInstance();

    private GameProtos.BattleRoundDetail roundDetailOff = GameProtos.BattleRoundDetail.getDefaultInstance();
    private GameProtos.BattleRoundDetail roundDetailDef = GameProtos.BattleRoundDetail.getDefaultInstance();

    private int offenceLost = 0;
    private int defenceLost = 0;

    public BattleRound(int seqNo) {
        this.seqNo = seqNo;
    }

    public BattleRound(GameProtos.BattleRound round){
        this.seqNo = round.getSeqNo();
        this.roundInfoOff = round.getRoundInfoOff();
        this.roundInfoDef = round.getRoundInfoDef();
        this.roundDetailOff = round.getRoundDetailOff();
        this.roundDetailDef = round.getRoundDetailDef();
        this.offenceLost = round.getOffenceLost();
        this.defenceLost = round.getDefenceLost();
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public GameProtos.BattleRoundInfo getRoundInfoOff() {
        return roundInfoOff;
    }

    public void setRoundInfoOff(GameProtos.BattleRoundInfo roundInfoOff) {
        this.roundInfoOff = roundInfoOff;
    }

    public GameProtos.BattleRoundInfo getRoundInfoDef() {
        return roundInfoDef;
    }

    public void setRoundInfoDef(GameProtos.BattleRoundInfo roundInfoDef) {
        this.roundInfoDef = roundInfoDef;
    }

    public GameProtos.BattleRoundDetail getRoundDetailOff() {
        return roundDetailOff;
    }

    public void setRoundDetailOff(GameProtos.BattleRoundDetail roundDetailOff) {
        this.roundDetailOff = roundDetailOff;
    }

    public GameProtos.BattleRoundDetail getRoundDetailDef() {
        return roundDetailDef;
    }

    public void setRoundDetailDef(GameProtos.BattleRoundDetail roundDetailDef) {
        this.roundDetailDef = roundDetailDef;
    }

    public int getOffenceLost() {
        return offenceLost;
    }

    public void setOffenceLost(int offenceLost) {
        this.offenceLost = offenceLost;
    }

    public int getDefenceLost() {
        return defenceLost;
    }

    public void setDefenceLost(int defenceLost) {
        this.defenceLost = defenceLost;
    }

    public int getTotalLost(boolean offence) {
        int lost = 0;
        for(GameProtos.RoundTroopDetail info :
                offence ? roundDetailOff.getRoundTroopDetailList()
                        : roundDetailDef.getRoundTroopDetailList()){
            lost += info.getLost();
        }
        return lost;
    }

    public GameProtos.BattleRoundInfo generateRoundInfo(BattleField field, boolean isOffence){
        List<Army> armies = isOffence ? field.getArmysOffence() : field.getArmysDefence();
        GameProtos.BattleRoundInfo.Builder battleRoundInfo = GameProtos.BattleRoundInfo.newBuilder();
        for(Army army : armies){
            for(List<BattleTroop> battleTroops : army.getBackupBattleTroops().values()){
                for(BattleTroop battleTroop : battleTroops){
                    GameProtos.RoundTroopInfo.Builder roundTroopInfo = null;
                    int index = this.getRoundTroopInfoIndex(isOffence, battleTroop.getResTroop().getId());

                    if(index == -1){
                        roundTroopInfo = GameProtos.RoundTroopInfo.newBuilder();
                    }else{
                        roundTroopInfo = battleRoundInfo.getRoundTroopInfo(index).toBuilder();
                        battleRoundInfo.removeRoundTroopInfo(index);
                    }
                    roundTroopInfo.setTroopId(battleTroop.getResTroop().getId());
                    roundTroopInfo.setCount(battleTroop.getCount());
                    battleRoundInfo.addRoundTroopInfo(roundTroopInfo);
                }
            }
        }
        GameProtos.BattleRoundInfo value = battleRoundInfo.build();
        if(isOffence){
            this.roundInfoOff = value;
        }else{
            this.roundInfoDef = value;
        }
        log.info(isOffence);
        log.info(value);
        return value;
    }

    private int getRoundTroopInfoIndex(boolean isOffence, int troopId){
        GameProtos.BattleRoundInfo battleRoundInfo = isOffence ? roundInfoOff : roundInfoDef;
        for(GameProtos.RoundTroopInfo roundTroopInfo : battleRoundInfo.getRoundTroopInfoList()){
            if(roundTroopInfo.getTroopId() == troopId){
                return battleRoundInfo.getRoundTroopInfoList().indexOf(roundTroopInfo);
            }
        }
        return -1;
    }

    public GameProtos.BattleRoundDetail generateRoundDetail(BattleField field, boolean isOffence){
//        Map<GameResProtos.BattleFieldType, List<BattleUnit>> troopsInfield = isOffence ?
//                field.getTroopsInFieldOffence() : field.getTroopsInFieldDefence();
//
        GameProtos.BattleRoundDetail.Builder battleRoundDetail = isOffence ? roundDetailOff.toBuilder() : roundDetailDef.toBuilder();
//        battleRoundDetail.setBattleId(field.getUUID()).setRoundNum(this.seqNo);
//        battleRoundDetail.setMorale(isOffence ? field.getMoraleOff() : field.getMoraleDef());
//        for(Map.Entry<GameResProtos.BattleFieldType, List<BattleUnit>> entry : troopsInfield.entrySet()){
//            for(BattleUnit battleUnit : entry.getValue()){
//                boolean exists = false;
//                int index = -1;
//                for(GameProtos.RoundTroopDetail roundTroopDetail : battleRoundDetail.getRoundTroopDetailList()){
//                    index++;
//                    if(roundTroopDetail.getTroopId() == battleUnit.getTroopId() && roundTroopDetail.getFieldType() == entry.getKey()){
//                        GameProtos.RoundTroopDetail.Builder builder = roundTroopDetail.toBuilder();
//                        builder.setCount(builder.getCount() + battleUnit.getCount());
//                        battleRoundDetail.removeRoundTroopDetail(index);
//                        battleRoundDetail.addRoundTroopDetail(builder);
//                        exists = true;
//                        break;
//                    }
//                }
//                if(!exists){
//                    GameProtos.RoundTroopDetail.Builder builder = GameProtos.RoundTroopDetail.newBuilder();
//                    builder.setTroopId(battleUnit.getTroopId()).setFieldType(entry.getKey()).setCount(builder.getCount());
//                    battleRoundDetail.addRoundTroopDetail(builder);
//                }
//            }
//        }
        GameProtos.BattleRoundDetail value = battleRoundDetail.build();
//        if(isOffence){
//            this.roundDetailOff = value;
//        }else{
//            this.roundDetailDef = value;
//        }
//        log.info(isOffence);
//        log.info(value);
        return value;
    }

    public void  addLost(int troopId, int lostNo, GameResProtos.BattleFieldType type, boolean isOffence) {
        GameProtos.BattleRoundDetail.Builder roundDetail = isOffence ? roundDetailOff.toBuilder() : roundDetailDef.toBuilder();

        boolean exists = false;
        int index = -1;
        for(GameProtos.RoundTroopDetail roundTroopDetail : roundDetail.getRoundTroopDetailList()){
            index++;
            if(roundTroopDetail.getTroopId() == troopId && roundTroopDetail.getFieldType() == type){
                GameProtos.RoundTroopDetail.Builder builder = roundTroopDetail.toBuilder();
                builder.setLost(builder.getLost() + lostNo);
                roundDetail.removeRoundTroopDetail(index);
                roundDetail.addRoundTroopDetail(builder);
                exists = true;
                break;
            }
        }
        if(!exists){
            GameProtos.RoundTroopDetail.Builder builder = GameProtos.RoundTroopDetail.newBuilder();
            builder.setTroopId(troopId).setFieldType(type).setLost(lostNo);
            roundDetail.addRoundTroopDetail(builder);
        }

        if(isOffence){
            roundDetailOff = roundDetail.build();
        }else{
            roundDetailDef = roundDetail.build();
        }
    }


    @Override
    public String toString() {
        return "BattleRound{" +
                "seqNo=" + seqNo +
                ", roundInfoOff=" + roundInfoOff +
                ", roundInfoDef=" + roundInfoDef +
                ", roundDetailOff=" + roundDetailOff +
                ", roundDetailDef=" + roundDetailDef +
                ", offenceLost=" + offenceLost +
                ", defenceLost=" + defenceLost +
                '}';
    }


    public GameProtos.BattleRound convertToProto() {
        GameProtos.BattleRound.Builder builder = GameProtos.BattleRound.newBuilder();
        builder.setSeqNo(this.seqNo);
        builder.setRoundInfoOff(this.roundInfoOff);
        builder.setRoundInfoDef(this.roundInfoDef);
        builder.setRoundDetailOff(this.roundDetailOff);
        builder.setRoundDetailDef(this.roundDetailDef);
        builder.setOffenceLost(this.offenceLost);
        builder.setDefenceLost(this.defenceLost);
        return builder.build();
    }
}
