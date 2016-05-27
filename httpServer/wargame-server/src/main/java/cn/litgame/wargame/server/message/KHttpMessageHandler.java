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
	public static final int CATCH_HANDLER = 1;
	/**
	 * 没有被处理器捕获到
	 */
	public static final int NOT_CATCH_HANDLER = 0;
	/**
	 * 捕获并处理的话返回1
	 * @param messageContent
	 * @return
	 */
	public abstract int handler(MessageBody messageBody);
}
