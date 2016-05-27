package cn.litgame.wargame.core.mapper;

import java.util.List;

import cn.litgame.wargame.core.model.GameAction;
//TODO:这张表要做性能测试，如果有问题要优化IO
public interface GameActionMapper {
	public GameAction getGameAction(long gameActionId);
	public int createGameAction(GameAction gameAction);
	public int delGameAction(long actionId);
	public int updateGameAction(GameAction gameAction);
	/**
	 * 获取一个玩家的行动集合
	 * @param playerId
	 * @return
	 */
	public List<GameAction> getGameActions(long playerId);
	/**
	 * 按照创建时间取最早的记录,这个函数取得是state包含低级state
	 * @param cityId
	 * @param type
	 * @return
	 */
	public List<GameAction> getGameActionsByCity(int cityId,int state);
	
	/**
	 * 获取城市里某种类型的action
	 * @param cityId
	 * @param type
	 * @return
	 */
	public List<GameAction> getGameActionByType(int cityId,int type);
	
	/**
	 * 根据状态取action,获取的是指定状态的action
	 * @param cityId
	 * @param state
	 * @return
	 */
	public List<GameAction> getGameActionsByState(int cityId,int state,int limit);
}
