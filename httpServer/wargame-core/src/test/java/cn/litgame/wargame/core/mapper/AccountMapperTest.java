package cn.litgame.wargame.core.mapper;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.litgame.wargame.core.model.Account;

import org.junit.*;

public class AccountMapperTest {
	ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:application-config.xml"); 
	AccountMapper am = context.getBean(AccountMapper.class);
	
	@Before
	public void Before(){
		am.delAccount(10234);
	}
	
	@Test
	public void Test(){
		Account account = new Account();
		account.setId(10234);
		account.setAccount("account");
		account.setPlatformType(1);
		account.setPlayerId(10104L);
		
		Assert.assertEquals(1,am.addAccount(account));
		
		Account newAccount = am.getAccount("account", 1);
		
		Assert.assertEquals(10234,newAccount.getId());
		Assert.assertEquals(10104L,newAccount.getPlayerId());
	}
}
