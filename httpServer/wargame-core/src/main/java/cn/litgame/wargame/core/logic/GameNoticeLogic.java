package cn.litgame.wargame.core.logic;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.litgame.wargame.core.mapper.GameNoticeMapper;
import cn.litgame.wargame.core.model.GameNotice;

@Service
public class GameNoticeLogic {
	@Resource(name = "gameNoticeMapper")
	GameNoticeMapper gameNoticeMapper;

	public List<GameNotice> getGameNoticeByTime(Timestamp time) {
		return gameNoticeMapper.getGameNoticeByTime(time.toString());
	}
	/**
	 * TODO:该函数为实现
	 * @return
	 */
	public List<GameNotice> getCurrentGameNotice(){
		return null;
	}
	
	public List<GameNotice> select(){
		return gameNoticeMapper.select();
	}

	public boolean insert(GameNotice gameNotice) {
		return gameNoticeMapper.insert(gameNotice) > 0;
	}

	public boolean update(GameNotice gameNotice) {
		GameNotice old =getGameNoticeById(gameNotice.getId());
		old.setId(gameNotice.getId());
		old.setTitle(gameNotice.getTitle());
		old.setContent(gameNotice.getContent());
		old.setStartTime(gameNotice.getStartTime());
		old.setEndTime(gameNotice.getEndTime());
		return gameNoticeMapper.update(old) > 0 ;
	}

	public boolean delete(int id) {
		return gameNoticeMapper.delete(id) > 0;
	}
	
	public GameNotice getGameNoticeById(int id){
		GameNotice gameNotice =gameNoticeMapper.getGameNoticeById(id);
		if(null == gameNotice){
			throw new RuntimeException("id is not fount ! id = "+ id);
		}
		return gameNotice;
	}

}
