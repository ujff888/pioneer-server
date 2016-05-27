package cn.litgame.wargame.core.mapper;

import java.sql.Timestamp;
import java.util.List;

import cn.litgame.wargame.core.model.GameNotice;

public interface GameNoticeMapper {
	public List<GameNotice> getGameNoticeByTime(String time);
	public List<GameNotice> select();
	public GameNotice getGameNoticeById(int id);
	public int insert(GameNotice gameNotice);
	public int update(GameNotice gameNotice);
	public int delete(int id);
}
