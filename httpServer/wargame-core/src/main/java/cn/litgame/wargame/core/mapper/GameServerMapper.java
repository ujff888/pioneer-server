package cn.litgame.wargame.core.mapper;

import java.util.Map;

public interface GameServerMapper {
	public String getConfig(String key);
	public int updateConfig(Map<String,Object> args);
	public void insertConfig(Map<String,Object> args);
	
}
