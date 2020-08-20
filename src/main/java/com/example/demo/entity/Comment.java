package com.example.demo.entity;

import java.util.Date;

public class Comment {
    private int id;
    private int userId;
    private int entityType; // 回复的类型 1是帖子
    private int entityId;   // 回复内容在其表中的ID 如果entityType是1 代表的是帖子的ID
    private int targetId; // 被回复者的ID 比如 B回复了A 则targetId是用户A的ID
    private int status; // 回复内容的状态 0正常 1删除
    private String content;
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", targetId=" + targetId +
                ", status=" + status +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
