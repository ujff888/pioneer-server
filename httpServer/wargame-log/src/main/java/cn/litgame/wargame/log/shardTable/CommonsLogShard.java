package cn.litgame.wargame.log.shardTable;

import java.util.Calendar;

import cn.litgame.wargame.log.LogManager;

import com.google.code.shardbatis.strategy.ShardStrategy;

public class CommonsLogShard implements ShardStrategy{

	@Override
	public String getTargetTableName(String baseTableName, Object params, String mapperId) {
		return baseTableName.replace("template", LogManager.DATEFORMAT.format(Calendar.getInstance().getTime()));
	}
}
