package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

public class Player {
	private Long playerId;
	private String deviceType;
	private String playerName;
	private String icon;
	private int gold;
	private int diamond;
	private int level;
	private String bindAccount;
	private String platformUid;//平台id
	private int platformType;//平台类型
	private int status;//0:正常,1:删除,2:非活跃3天，3：非活跃7天，4是非活跃1个月以上
	private Timestamp createTime;
	private Timestamp lastLoginTime;//最后一次登录的时间
	private int vip;
	private Timestamp vipTime;//vip的结束时间点
	private String deviceToken;
	private boolean needSave;//如果逻辑操作的时候需要存储对象，先打标记，整个请求处理完成后存储一次
	private Timestamp lastFlushGoldTime;//TODO:字段还未添加
	private int chatOn;
	private String password;//新用户随机生成6位数的密码

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Timestamp getLastFlushGoldTime() {
		return lastFlushGoldTime;
	}
	public void setLastFlushGoldTime(Timestamp lastFlushGoldTime) {
		this.lastFlushGoldTime = lastFlushGoldTime;
	}
	
	public int getChatOn() {
		return chatOn;
	}
	public void setChatOn(int chatOn) {
		this.chatOn = chatOn;
	}
	public boolean isNeedSave() {
		return needSave;
	}
	public void setNeedSave(boolean needSave) {
		this.needSave = needSave;
	}
	public Long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public int getGold() {
		return gold;
	}
	public void setGold(int gold) {
		this.gold = gold;
	}
	public int getDiamond() {
		return diamond;
	}
	public void setDiamond(int diamond) {
		this.diamond = diamond;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getBindAccount() {
		return bindAccount;
	}
	public void setBindAccount(String bindAccount) {
		this.bindAccount = bindAccount;
	}
	public String getPlatformUid() {
		return platformUid;
	}
	public void setPlatformUid(String platformUid) {
		this.platformUid = platformUid;
	}
	public int getPlatformType() {
		return platformType;
	}
	public void setPlatformType(int platformType) {
		this.platformType = platformType;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Timestamp getLastLoginTime() {
		return lastLoginTime;
	}
	public void setLastLoginTime(Timestamp lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	public int getVip() {
		return vip;
	}
	public void setVip(int vip) {
		this.vip = vip;
	}
	public Timestamp getVipTime() {
		return vipTime;
	}
	public void setVipTime(Timestamp vipTime) {
		this.vipTime = vipTime;
	}
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
	
	@Override
	public String toString() {
		return "Player [playerId=" + playerId + ", deviceType=" + deviceType
				+ ", playerName=" + playerName + ", icon=" + icon + ", gold="
				+ gold + ", diamond=" + diamond + ", level=" + level
				+ ", bindAccount=" + bindAccount + ", platformUid="
				+ platformUid + ", platformType=" + platformType + ", status="
				+ status + ", createTime=" + createTime + ", lastLoginTime="
				+ lastLoginTime + ", vip=" + vip + ", vipTime=" + vipTime
				+ ", deviceToken=" + deviceToken + ", needSave=" + needSave
				+ ", lastFlushGoldTime=" + lastFlushGoldTime + ", chatOn="
				+ chatOn + ", password=" + password + "]";
	}

}
