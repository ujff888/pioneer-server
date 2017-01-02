package cn.litgame.wargame.server.message;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.logic.HttpMessageManager;

public abstract class KHttpMessageHandler {
	
	@Resource(name = "httpMessageManager")
	private HttpMessageManager httpMessageManager;
	
	@PostConstruct
	public void init(){
		httpMessageManager.registHandler(this);
	}
	/**
	 * 被处理器捕获到了
	 */
	public static final boolean CATCH_HANDLER = true;
	/**
	 * 没有被处理器捕获到
	 */
	public static final boolean NOT_CATCH_HANDLER = false;
	/**
	 * 捕获并处理的话返回1
	 * @param messageBody
	 * @return
	 */
	public abstract boolean handle(MessageBody messageBody);
}
