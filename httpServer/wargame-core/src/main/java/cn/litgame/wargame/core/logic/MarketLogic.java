package cn.litgame.wargame.core.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.kriver.core.common.BaseUtil;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import cn.litgame.wargame.core.auto.GameGlobalProtos.MarketType;
import cn.litgame.wargame.core.auto.GameProtos.CityOrderInfo;
import cn.litgame.wargame.core.auto.GameProtos.MarketOrderInfo;
import cn.litgame.wargame.core.auto.GameResProtos.ResLand;
import cn.litgame.wargame.core.auto.GameResProtos.ResourceType;
import cn.litgame.wargame.core.model.City;
import cn.litgame.wargame.core.model.Player;

@Service
public class MarketLogic {

	@Resource(name = "jedisStoragePool")
	private JedisPool jedisStoragePool;
	
	@Resource(name = "configLogic")
	private ConfigLogic configLogic;
	
	@Resource(name = "cityLogic")
	private CityLogic cityLogic;
	
	@Resource(name = "playerLogic")
	private PlayerLogic playerLogic;
	
	@Resource(name = "mapLogic")
	private MapLogic mapLogic;
	
	private final static Logger log = Logger.getLogger(MarketLogic.class);
	
	private void test(){
		Jedis jedis = jedisStoragePool.getResource();
		try{
			
		}finally{
			jedis.close();
		}
	}
	
	private byte[] buildLandKey(int landId){
		return BaseUtil.intToByteArray(landId);
	}
	
	private byte[] buildCityKey(int cityId){
		return BaseUtil.intToByteArray(cityId);
	}
	

	private void toCity(Player player,MarketOrderInfo order,City city){
		if(order.getMarketType() == MarketType.MARKET_BUY){
			player.setGold(player.getGold() + order.getCount() * order.getPrice());
		}else if(order.getMarketType() == MarketType.MARKET_SELL){
			city.addResource(order.getResourceType(), order.getCount());
		}
	}
	
	private void toOrder(Player player,MarketOrderInfo order,City city){
		if(order.getMarketType() == MarketType.MARKET_BUY){
			player.setGold(player.getGold() - order.getCount() * order.getPrice());
		}else if(order.getMarketType() == MarketType.MARKET_SELL){
			city.addResource(order.getResourceType(), -order.getCount());
		}
	}
	
	/**
	 * 更新订单，如果城市资源有变化，返回true
	 * @param landId
	 * @param cityId
	 * @param info
	 */
	public void saveOrderInfo(Player player,City city,CityOrderInfo newOrderInfo, CityOrderInfo oldOrderInfo,boolean save){
		Jedis jedis = jedisStoragePool.getResource();
		
		int oldGold = player.getGold();
		try{
			if(oldOrderInfo != null){
				//先把老的订单上的资源数据都还原到城市上
				for(MarketOrderInfo market : oldOrderInfo.getMarketOrderInfoList()){
					this.toCity(player,market, city);
				}
				//根据新的订单把资源从城市移动到订单上
				for(MarketOrderInfo market : newOrderInfo.getMarketOrderInfoList()){
					this.toOrder(player,market, city);
				}
			}

			jedis.hset(this.buildLandKey(city.getLandId()), this.buildCityKey(city.getCityId()), newOrderInfo.toByteArray());
		}finally{
			jedis.close();
		}
		if(player.getGold() != oldGold){
			player.setNeedSave(true);
		}
		if(save){
			playerLogic.updatePlayer(player);
			cityLogic.updateCity(city);
		}
	}
	
	public void updateCityOrderInfo(City city, CityOrderInfo order){
		Jedis jedis = jedisStoragePool.getResource();
		try{
			Transaction tx = jedis.multi();
			tx.hset(this.buildLandKey(city.getLandId()), this.buildCityKey(city.getCityId()), order.toByteArray());
			tx.exec();
		}finally {
			jedis.close();
		}
	}
	
	public CityOrderInfo getOrderInfo(int landId,int cityId){
		Jedis jedis = jedisStoragePool.getResource();
		try{
			byte[] data = jedis.hget(this.buildLandKey(landId), this.buildCityKey(cityId));
			if(data == null){
				return null;
			}
			return CityOrderInfo.parseFrom(data);
		}catch(Exception e ){
			log.error("syn error", e);
			return null;
		}
		finally{
			jedis.close();
		}
	}
	
	public List<CityOrderInfo> showCityOrderInfo(int distance,int landId,int cityId){
		List<CityOrderInfo> result = new ArrayList<CityOrderInfo>();
		Jedis jedis = jedisStoragePool.getResource();
		try{
			ResLand res = configLogic.getResLand(landId);
			int minX = res.getX() - distance;
			int maxX = res.getX() + distance;
			int minZ = res.getZ() - distance;
			int maxZ = res.getZ() + distance;
			if(minX < 1){
				minX = 1;
			}
			if(maxX > 100){
				maxX = 100;
			}
			if(minZ < 1){
				minZ = 1;
			}
			if(maxZ > 100){
				maxZ = 100;
			}
			
			List<Integer> landIds = new ArrayList<Integer>();
			for(int x = minX ; x < maxX + 1 ; x++){
				for(int z = minZ ; z < maxZ; z++){
					ResLand l = configLogic.getResLand(x, z);
					if(l != null){
						landIds.add(l.getLandId());
					}
				}
			}
			
			for(Integer id : landIds){
				Map<byte[],byte[]> datas = jedis.hgetAll(this.buildLandKey(id));
				if(datas != null){
					for(Map.Entry<byte[], byte[]> entry : datas.entrySet()){
						CityOrderInfo info = CityOrderInfo.parseFrom(entry.getValue());
						
						CityOrderInfo.Builder b = CityOrderInfo.newBuilder(info);
						City city = cityLogic.getCity(info.getCityId());
						b.setCityName(city.getCityName());
						b.setDistance(mapLogic.getLandDistance(landId, city.getLandId()));
						b.clearMarketOrderInfo();
						for(MarketOrderInfo moi : info.getMarketOrderInfoList()){
							if(moi.getCount() > 0 && moi.getPrice() > 0)
								b.addMarketOrderInfo(moi);
						}
						
						if(info.getCityId() != cityId){
							result.add(b.build());
						}
					}
				}
			}
			
		}catch(Exception e){
			log.error("syn error", e);
		}finally{
			jedis.close();
		}
		return result;
	}
	
	
}
