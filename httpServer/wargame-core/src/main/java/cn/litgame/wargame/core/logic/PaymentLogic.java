package cn.litgame.wargame.core.logic;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import cn.litgame.wargame.core.mapper.BillMapper;
import cn.litgame.wargame.core.mapper.OrderMapper;
import cn.litgame.wargame.core.model.AppleVerify;
import cn.litgame.wargame.core.model.Bill;
import cn.litgame.wargame.core.model.Order;

/**
 * @title 订单历史记录
 * 
 * @author wangshaojun
 * 
 * @version 0.00 2014-07-16 add
 */
@Service
public class PaymentLogic {
	
	private final static Logger log = Logger.getLogger(PaymentLogic.class);
	
	@Resource(name = "jedisStoragePool")
	private JedisPool jedisStoragePool;

	@Resource(name = "jedisCachePool")
	private JedisPool jedisCachePool;
	
	@Resource(name = "orderMapper")
	private OrderMapper orderMapper;

	@Resource(name = "billMapper")
	private BillMapper billMapper;
	
	public Bill getBillByOrderId(String orderId){
		return this.billMapper.getBill(orderId);
	}
	
	private final static String payment_records = "p_r_";
	
	private String buildKey(Long playerId){
		return payment_records + playerId;
	}
	
	public boolean haveProductId(long playerId, String productId){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			String key = this.buildKey(playerId);
			return jedis.sismember(key, productId);
		}finally{
			jedis.close();
		}
		
	}
	public Set<String> getProductIdsByPlayerId(long playerId){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			return jedis.smembers(this.buildKey(playerId));
		}finally{
			jedis.close();
		}
		
	}
	
	public void addBill(long playerId,String orderId,int rmb,String productId,int diamond){
		Jedis jedis = this.jedisStoragePool.getResource();
		try{
			Bill b = new Bill(playerId, orderId, rmb, productId, diamond);
			jedis.sadd(this.buildKey(playerId), productId);
			this.billMapper.insert(b);
		}finally{
			jedis.close();
		}
		
	}
	
	/**
	 * 更新历史订单信息
	 * 
	 * @param orderHistory
	 * @return
	 */
	public boolean updateOrderHistory(Order orderHistory) {
		return this.orderMapper.update(orderHistory) > 0 ? true : false;
	}

	/**
	 * 获取历史订单信息
	 * 
	 * @param playerId
	 * @return
	 */
	public List<Order> queryOrderHistoryList(long playerId) {
		return orderMapper.getOrderHistoryList(playerId);
	}

	/**
	 * 通过玩家ID和订单号ID获取历史订单信息
	 * 
	 * @param playerId
	 * @param orderId
	 * @return
	 */
	public Order queryOrderHistory(long playerId, String orderId) {
		return orderMapper.select(playerId, orderId);
	}

	/**
	 * 新添一条订单历史记录
	 * 
	 * @param playerId
	 * @param orderId
	 */
	public Order createOrderHistory(long playerId, String orderId) {
		Order orderHistory = new Order();
		orderHistory.setPlayerId(playerId);
		orderHistory.setOrderId(orderId);
		orderHistory.setCreateTime(new Timestamp(System.currentTimeMillis()));
		orderHistory.setStatus(0);
		orderMapper.insert(orderHistory);
		return orderHistory;
	}
	
	private static class TrustAnyTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	
	/**
	 * 苹果服务器验证
	 * 
	 * @param receipt 账单
	 * @return
	 */
	private String buyAppVerify(byte[] receipt,String url) {
		String buyCode = Base64.getEncoder().encodeToString(receipt);
		BufferedReader reader = null;
		HttpsURLConnection conn = null;
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },new java.security.SecureRandom());
			URL console = new URL(url);
			conn = (HttpsURLConnection) console.openConnection();
			conn.setSSLSocketFactory(sc.getSocketFactory());
			conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
			conn.setRequestMethod("POST");
			conn.setRequestProperty("content-type", "text/json;charset=utf-8");
			conn.setRequestProperty("Proxy-Connection", "Keep-Alive");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			BufferedOutputStream hurlBufOus = new BufferedOutputStream(conn.getOutputStream());

			String str = String.format(Locale.CHINA, "{\"receipt-data\":\""+ buyCode + "\"}");
			hurlBufOus.write(str.getBytes("UTF-8"));
			hurlBufOus.flush();

			InputStream is = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (Exception e) {
			log.error("payment error", e);
			return null;
		}finally{
			try {
				reader.close();
				conn.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * 
	 * @param receipt
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws IOException
	 */
	public AppleVerify iosVerify(byte[] receipt, String url,int appleId)
    {    
		AppleVerify verifyBean = new AppleVerify();
    	Long status = -1L;
    	String bid = "";
    	String orderId = "";
    	String productId = "";
    	try{
	    	String verifyResult = buyAppVerify(receipt,url);
	    	//System.out.println("verifyResult======>"+ verifyResult);
	        if(verifyResult!=null){   
	            //跟苹果验证有返回结果------------------  
	        	
	            JSONObject job = (JSONObject)JSONValue.parse(verifyResult);
	            status = (long)(job.get("status"));
	            if(status == 0){
		            JSONObject json  = (JSONObject)job.get("receipt");
		            bid = (String)json.get("bid");
		            orderId = (String)json.get("original_transaction_id"); 
		            productId = (String)json.get("product_id");
	            }
	        }
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	//放置校验返回结果
    	verifyBean.setStatus(status.intValue());
    	verifyBean.setBid(bid);
    	verifyBean.setAppid(appleId);
    	verifyBean.setOrderId(orderId);
    	verifyBean.setProductId(productId);
    	
        return verifyBean;
    }
}
