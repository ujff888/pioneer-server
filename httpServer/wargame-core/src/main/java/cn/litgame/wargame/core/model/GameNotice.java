package cn.litgame.wargame.core.model;

import java.sql.Timestamp;

public class GameNotice {
	private int id;
	private String title;
	private String content;
	private Timestamp startTime;
	private Timestamp endTime;
	private Timestamp createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "GameNotice [id=" + id + ", title=" + title + ", content="
				+ content + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", createTime=" + createTime + "]";
	}
}
