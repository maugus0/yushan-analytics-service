package com.yushan.analytics_service.entity;

import java.util.Date;
import java.util.UUID;

public class History {
    private Integer id;
    private UUID uuid;
    private UUID userId;
    private Integer novelId;
    private Integer chapterId;
    private Date createTime;
    private Date updateTime;

    // No-arg constructor (required for MyBatis)
    public History() {
        super();
    }

    // All-args constructor (optional, for convenience)
    public History(Integer id, UUID uuid, UUID userId, Integer novelId, Integer chapterId, Date createTime, Date updateTime) {
        this.id = id;
        this.uuid = uuid;
        this.userId = userId;
        this.novelId = novelId;
        this.chapterId = chapterId;
        this.createTime = createTime != null ? new Date(createTime.getTime()) : null;
        this.updateTime = updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getUserId() {
        return userId;
    }

    public Integer getNovelId() {
        return novelId;
    }

    public Integer getChapterId() {
        return chapterId;
    }

    public Date getCreateTime() {
        return createTime != null ? new Date(createTime.getTime()) : null;
    }

    public Date getUpdateTime() {
        return updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    // Setters (required for MyBatis result mapping)
    public void setId(Integer id) {
        this.id = id;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setNovelId(Integer novelId) {
        this.novelId = novelId;
    }

    public void setChapterId(Integer chapterId) {
        this.chapterId = chapterId;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime != null ? new Date(createTime.getTime()) : null;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime != null ? new Date(updateTime.getTime()) : null;
    }
}
