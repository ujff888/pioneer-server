package cn.litgame.wargame.core.mapper;

import java.util.List;

import cn.litgame.wargame.core.model.Battle;

public interface BattleMapper {
	public int insert(Battle battle);
	public int update(Battle battle);
	public int delBattleById(long playerId,int modelType,int modelId);
	public Battle getBattleById(long playerId,int modelType,int modelId);
	public List<Battle> getAllBattles(long playerId);
	public List<Battle> getBattlesByType(long playerId,int modelType);
	public int delBattleByType(long playerId,int modelType);
}
