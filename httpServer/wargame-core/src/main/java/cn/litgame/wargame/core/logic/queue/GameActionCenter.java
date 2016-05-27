package cn.litgame.wargame.core.logic.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.kriver.core.common.ExecutorUtil;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;
import cn.litgame.wargame.core.model.GameAction;

import com.google.protobuf.InvalidProtocolBufferException;

//
/*****
 * 把所有的定时任务存放到队列中，以时间戳来排序，每次取第一条记录，时间最小的那个。
 * 处理完的话，就把本记录删除掉，以心跳的方式，一直查询队列中的第一条，看是否满足触发时间
 * 本类是为了解决游戏中会有大量的定时任务所造成的性能开销
 * @author Administrator
 */
@Service
public class GameActionCenter {
	private final static Logger log = Logger.getLogger(GameActionCenter.class);
	
	@Resource(name = "jedisStoragePool")
	private JedisPool jedisStoragePool;
	
	@Resource(name = "gameActionLogic")
	private GameActionLogic gameActionLogic;
	
	private final static Logger logger = Logger.getLogger(GameActionCenter.class);
	private Map<Integer,GameActionEvent> gameActionEvents = new HashMap<Integer, GameActionEvent>();
	private ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(2); 
	private static final int period = 100;//毫秒
	private static final int delay = 10;//毫秒
	
	/**
	 * 注册定时动作触发后的业务逻辑
	 * @param type
	 * @param event
	 */
	public void regiestGameActionEvent(int type,GameActionEvent event){
		gameActionEvents.put(type, event);
	}

	/**
	 * waring：这里没有用redis加逻辑锁，因为线上环境是一个server对应一个redis实例
	 * 但是内网环境中，会有多个server连接一个redis实例，可能会有问题，但是也先不加锁
	 */
	public void tick(){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			Tuple t = null;
			Set<Tuple> ss = jedis.zrangeWithScores(queue_key, 0, 0);
			if(ss == null || ss.size() != 1){
				return;
			}else{
				t = (Tuple)ss.toArray()[0];
			}

			long time = (long)t.getScore();
			long nowTime = System.currentTimeMillis();
			if( nowTime > time){
				long actionId = Long.parseLong(t.getElement());
				GameAction ga = gameActionLogic.getGameAction(actionId);
				jedis.zrem(queue_key, String.valueOf(actionId));
				//gameActionLogic.delGameAction(actionId);
				if(ga == null){
					return;
				}
				
				GameActionEvent event = gameActionEvents.get(ga.getActionType());
				event.init(ga);
				event.doLogic(ga,nowTime);
				tick();
			}
		} catch (InvalidProtocolBufferException e) {
			log.error("syn error", e);
		}finally{
			jedis.close();
		}

	}

	@PostConstruct
	public void start(){
		//注册jvm关闭后的钩子,确保定时任务执行完
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				ExecutorUtil.shutdownAndAwaitTermination(executor);
			}
		}));

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try{
					tick();
				}catch(Exception e){
					//TODO:这里以后加个发短信通知的功能，防止一直在抛异常
					logger.error("定时器异常", e);
				}
			}
		};
		executor.scheduleAtFixedRate(runnable, delay, period, TimeUnit.MILLISECONDS); 
	}
	/**
	 * 游戏定时器的时间队列
	 */
	private final static String queue_key = "wargame_time_queue";
	
	/**
	 * 添加进队列一个gameaction
	 * @param actionId
	 * @param time
	 */
	public void addGameAction(long actionId,long time){
		Jedis jedis = jedisStoragePool.getResource();
		try{
			jedis.zadd(queue_key, (double)time, String.valueOf(actionId));
		}finally{
			jedis.close();
		}
	}
	
	
	/**
	 * 获取队列中的总数
	 * @return
	 */
	public long getGameActionQueueCount(){
		Jedis jedis = jedisStoragePool.getResource();
		try{
			return jedis.zcard(queue_key);
		}finally{
			jedis.close();
		}
	}
	
}
