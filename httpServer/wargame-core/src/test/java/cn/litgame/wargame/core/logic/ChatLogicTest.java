package cn.litgame.wargame.core.logic;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.litgame.wargame.core.auto.GameProtos.TransportTask;
import cn.litgame.wargame.core.model.Player;
import junit.framework.Assert;

public class ChatLogicTest{

	ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:application-config.xml");
	ChatLogic cl = context.getBean(ChatLogic.class);
	ConfigLogic configLogic = context.getBean(ConfigLogic.class);

	private final static Logger log = Logger.getLogger(ChatLogicTest.class);

    
 
	//@Test
    public void test() throws InvalidProtocolBufferException{
		TransportTask.Builder b = TransportTask.newBuilder();
		b.setSourceCityId(111);
		TransportTask.Builder a = TransportTask.parseFrom(b.build().toByteArray()).toBuilder();
		System.out.println("=============================");
		System.out.println(a.getSourceCityId());
    }
	
	//@Test
	public void chatLogicTest() throws ClientProtocolException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException{
		
		boolean a = configLogic.loadConfig(ChatLogicTest.class.getResource("/pb.bytes").getPath());

//		Player player = new Player();
//		player.setPassword("password");
//		player.setPlayerId(111199L);
//		player.setPlayerName("test name");
//		boolean result = cl.activateChat(player);
//		log.info("result ====" + result);
		
		String roomId = cl.createChatRoom("testRoom", 5000);
		log.info("roomId ==== " + roomId);
		
		cl.deleteChatRoom(roomId);
		
		String groupId = cl.createGroup("test_group", false, "test_acount1", true, "test group info");
		log.info("groupId ==== " + groupId);
		
		HashMap<String, Object> groupDetails = cl.getGroupDetails(groupId);
		Assert.assertEquals("test_group", (String)(groupDetails.get("groupName")));
		Assert.assertEquals(0, (long)(groupDetails.get("isPrivate")));
		Assert.assertEquals("test_acount1", (String)(groupDetails.get("ownerAccount")));
		Assert.assertEquals(1, (long)(groupDetails.get("needVerify")));
		Assert.assertEquals("test group info", (String)(groupDetails.get("groupInfo")));

		cl.addGroupMember(groupId, "test_acount2");
		cl.modifyGroup(groupId, "test_group_mod", true, "test_acount2", false, "test group info mod");
		groupDetails = cl.getGroupDetails(groupId);
		Assert.assertEquals("test_group_mod", (String)(groupDetails.get("groupName")));
		Assert.assertEquals(1, (long)(groupDetails.get("isPrivate")));
		Assert.assertEquals("test_acount2", (String)(groupDetails.get("ownerAccount")));
		Assert.assertEquals(0, (long)(groupDetails.get("needVerify")));
		Assert.assertEquals("test group info mod", (String)(groupDetails.get("groupInfo")));
		
		
		cl.addGroupMember(groupId, "test_acount3");
		cl.delGroupMember(groupId, "test_acount3");
		
		ArrayList<String> members = cl.getGroupMembers(groupId, 0, 2);
		Assert.assertEquals("test_acount1", members.get(0));
		Assert.assertEquals("test_acount2", members.get(1));
		Assert.assertEquals(2, members.size());
		
		Assert.assertEquals(groupId, cl.getGroupsByUserAccount("test_acount2", 0, 1).get(0));
		Assert.assertEquals(groupId, cl.getAllGroups(0, 1).get(0));
		
		cl.dismissGroup(groupId);
	}
	
}
