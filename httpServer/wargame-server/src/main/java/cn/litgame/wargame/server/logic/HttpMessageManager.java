package cn.litgame.wargame.server.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageCode;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MessageType;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.core.auto.GameProtos.MessageBody.Builder;
import cn.litgame.wargame.core.logic.ConfigLogic;
import cn.litgame.wargame.core.logic.PlayerLogic;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Player;
import cn.litgame.wargame.server.message.KHttpMessageContext;
import cn.litgame.wargame.server.message.KHttpMessageHandler;


@Service
public class HttpMessageManager {
	private final static Logger log = Logger.getLogger(HttpMessageManager.class);
	
	@Resource(name = "jedisStoragePool")
	private JedisPool jedisStoragePool;
	
	@Resource(name = "playerLogic")
	private PlayerLogic playerLogic;
	
	@Resource(name = "configLogic")
	private ConfigLogic configLogic;
	
//	@Resource(name = "cityHandler")
//	private CityHandler cityHandler;
//	
//	@Resource(name = "mapHandler")
//	private MapHandler mapHandler;
//	
//	@Resource(name = "loginHandler")
//	private LoginHandler loginHandler;
//	
//	@Resource(name = "playerHandler")
//	private PlayerHandler playerHandler;
//	
//	@Resource(name = "friendHandler")
//	private FriendHandler friendHandler;
//	
//	@Resource(name = "mailHandler")
//	private MailHandler mailHandler;
//	
//	@Resource(name = "battleHandler")
//	private BattleHandler battleHandler;
//
//	@Resource(name = "shipHandler")
//	private ShipHandler shipHandler;

//	
//	@Resource(name = "packItemHandler")
//	private PackItemHandler packItemHandler;
//	
//	@Resource(name = "shopItemHandler")
//	private ShopItemHandler shopHandler;
	
	private List<KHttpMessageHandler> handlers = new ArrayList<KHttpMessageHandler>();
	
	public void registHandler(KHttpMessageHandler handler){
		handlers.add(handler);
	}
	
	/**
	 * 所有的handler都需要在这里注册
	 */
	@PostConstruct
	public void init(){
		boolean a;
//		SAXReader saxReader = new SAXReader();
//		String classpath = this.getClass().getClassLoader().getResource("").getPath();
//        Document document = null;
//        Element status = null;
//        XMLWriter xmlWriter = null;
//		try {
//			document = saxReader.read(new File( classpath + "/configLoaded.xml"));
//			Element root = document.getRootElement();
//	        status = root.element("status");
//	        
//			status.setText("0");//0表示尚未开始读取配置文件
//			OutputFormat format = new OutputFormat("", false);// 设置缩进为0个空格，并且另起一行为false
//	       
//	        
			a = configLogic.loadConfig(HttpMessageManager.class.getResource("/pb.bytes").getPath());

//			status.setText("1");//1表示读取成功
//			if(!a){
//				status.setText("2");//2表示读取失败
//			}
//			xmlWriter = new XMLWriter(new FileOutputStream(classpath + "/configLoaded.xml"), format);
//			xmlWriter.write(document);
//			
//			
//		} catch (Exception e1) {
//			log.error("configLoaded.xml读取失败！",e1);
//			status.setText("2");
//		}finally{
//			try {
//				if(xmlWriter != null){
//					xmlWriter.close();
//				}
//				
//			} catch (IOException e) {
//				log.error("xml write error", e);
//			}
//		}
//		
//		if(status.getText().compareTo("1")!=0){
//			throw new RuntimeException("load config error");
//		}
	}
	
	public City getCityContext(){
		return contexts.get().getCity();
	}
	
	/**
	 * 打上这个标记，就会自动刷新客户端的资源
	 * @param city
	 */
	public void changeCityResource(City city){
		contexts.get().setCity(city);
	}
	public long getPlayerId(){
		long playerId = contexts.get().getPlayerId();
		if(playerId < 0){
			playerId = 0;
		} 
		return playerId;
	}
	
	public Player getPlayer(){
		Player player = contexts.get().getPlayer();
		if(player == null){
			player = playerLogic.getPlayer(this.getPlayerId());
			contexts.get().setPlayer(player);
		}
		return player;
	}
	/**
	 * 用来存储本线程通讯的上下文信息
	 */
	private static ThreadLocal<KHttpMessageContext> contexts = new ThreadLocal<KHttpMessageContext>();
	
	public static void setKHttpMessageContext(KHttpMessageContext context){
		contexts.set(context);
	}
	
	/**
	 * 处理具体消息所对应的逻辑
	 * @param messageContent
	 */
	public void handler(MessageBody messagebody){
		MessageBody.Builder builder = MessageBody.newBuilder();
		builder.setMessageType(messagebody.getMessageType());
		contexts.get().init(builder);
		
		Player player = this.getPlayer();
		
		//会话处理 
		if(messagebody.getMessageType() != MessageType.MSG_ID_LOGIN
				&& messagebody.getMessageType() != MessageType.MSG_ID_CREATE_PLAYER
				&& messagebody.getMessageType() != MessageType.MSG_ID_BIND_ACCOUNT){
			
			if(player == null){
				builder.setMessageCode(MessageCode.NOT_FOUND_PLAYER);
				this.send(builder);
				return;
			}
			if(!player.getPassword().equals(getContext().getSessionPassword())){
				builder.setMessageCode(MessageCode.DATA_EXPIRE);
				this.send(builder);
				return;
			}
			
			if(!hadSession(this.getPlayerId(), messagebody.getSessionKey())){
				builder.setMessageCode(MessageCode.SESSION_EXPIRE);
				String newSessionKey = this.initSessionKey(player.getPlayerId() , player.getPassword());
				builder.setSessionKey(newSessionKey);
				this.send(builder);
				return;
			}
		}
		if(StringUtils.isNotBlank(messagebody.getSessionKey()) && messagebody.getMessageType() != MessageType.MSG_ID_CREATE_PLAYER){
			if(player != null && !player.getPassword().equals(getContext().getSessionPassword())){
				builder.setMessageCode(MessageCode.DATA_EXPIRE);
				this.send(builder);
				return;
			}
			this.expireSessionKey(this.buildSK(this.getPlayerId()));
		}
		
		for(KHttpMessageHandler handler : handlers){
			if(handler.handler(messagebody) == KHttpMessageHandler.CATCH_HANDLER){
				return;
			}
		}
		throw new RuntimeException("not catch handler,messageType="+messagebody.getMessageType());
	}
	
	/**
	 * 获取MessageContent的builder
	 * TODO:这里可以优化成一个builder池子,不必为每个请求都创建新的builder，在send后回收
	 * @return
	 */
	public MessageBody.Builder getMessageContentBuilder(){
		return contexts.get().getMessageBody();
	}

	@Resource(name = "commonsProcess")
	private CommonsProcess commonsProcess;
	
	private final static String SK = "sk_";
	private String buildSK(long playerId){
		return SK + playerId;
	}
	public boolean hadSession(long playerId,String oldSessionValue){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			if(StringUtils.isBlank(oldSessionValue)){
				return false;
			}
			String v = jedis.get(this.buildSK(playerId));
			if(StringUtils.isBlank(v)){
				return false;
			}
			return v.equals(oldSessionValue);
		}finally{
			jedis.close();
		}

	}
	
	public void expireSessionKey(String sessionKey){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			jedis.expire(sessionKey, 1800);
		}finally{
			jedis.close();
		}
		
	}
	
	public String initSessionKey(long playerId,String password){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			String sessionKey = contexts.get().generateSessionKey(playerId,password);
			if(sessionKey != null){
				String sk = this.buildSK(playerId);
				jedis.set(sk, sessionKey);
				this.expireSessionKey(sk);
			}
			return sessionKey;
		}finally{
			jedis.close();
		}

	}
	
	/**
	 * 发送消息
	 * @param builder
	 * @throws IOException 
	 */
	public void send(Builder builder){
		Player player = this.getPlayer();

		if(builder.getMessageType() == MessageType.MSG_ID_LOGIN
				|| builder.getMessageType() == MessageType.MSG_ID_CREATE_PLAYER){
			String sessionKey = contexts.get().getSessionKey();
			
			if(StringUtils.isNotBlank(sessionKey)){
				builder.setSessionKey(sessionKey);
			}
		}
		
		commonsProcess.process(player,builder);
		getContext().send(builder);
	}
	
	public static KHttpMessageContext getContext(){
		return contexts.get();
	}
	public static void main(String[] args) {
		System.out.println(137573172566L % 12);
	}
}
