package com.offshore.platform.entity;

import java.time.LocalDateTime;

/**
 * sync_conflict 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class SyncConflict {
    public static final String TABLE_NAME = "sync_conflict";

    private Long id;
    private String conflictNo;
    private Long syncTaskId;
    private Long syncLogId;
    private String deviceId;
    private Long operatorId;
    private String moduleType;
    private String entityType;
    private Long entityId;
    private String localId;
    private Long serverId;
    private Long workOrderId;
    private String businessNo;
    private Integer baseVersion;
    private Integer clientVersion;
    private Integer serverVersion;
    private LocalDateTime clientUpdatedAt;
    private LocalDateTime serverUpdatedAt;
    private String conflictType;
    private String conflictFields;
    private String oldPayload;
    private String clientPayload;
    private String serverPayload;
    private String finalPayload;
    private String defaultStrategy;
    private String resolveStrategy;
    private String resolveStatus;
    private Long resolverId;
    private LocalDateTime resolveTime;
    private String resolveComment;
    private String lastWriteSide;
    private LocalDateTime lastWriteTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deletedFlag;
    private Long createdBy;
    private Long updatedBy;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConflictNo() {
        return conflictNo;
    }

    public void setConflictNo(String conflictNo) {
        this.conflictNo = conflictNo;
    }

    public Long getSyncTaskId() {
        return syncTaskId;
    }

    public void setSyncTaskId(Long syncTaskId) {
        this.syncTaskId = syncTaskId;
    }

    public Long getSyncLogId() {
        return syncLogId;
    }

    public void setSyncLogId(Long syncLogId) {
        this.syncLogId = syncLogId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(String businessNo) {
        this.businessNo = businessNo;
    }

    public Integer getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(Integer baseVersion) {
        this.baseVersion = baseVersion;
    }

    public Integer getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(Integer clientVersion) {
        this.clientVersion = clientVersion;
    }

    public Integer getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(Integer serverVersion) {
        this.serverVersion = serverVersion;
    }

    public LocalDateTime getClientUpdatedAt() {
        return clientUpdatedAt;
    }

    public void setClientUpdatedAt(LocalDateTime clientUpdatedAt) {
        this.clientUpdatedAt = clientUpdatedAt;
    }

    public LocalDateTime getServerUpdatedAt() {
        return serverUpdatedAt;
    }

    public void setServerUpdatedAt(LocalDateTime serverUpdatedAt) {
        this.serverUpdatedAt = serverUpdatedAt;
    }

    public String getConflictType() {
        return conflictType;
    }

    public void setConflictType(String conflictType) {
        this.conflictType = conflictType;
    }

    public String getConflictFields() {
        return conflictFields;
    }

    public void setConflictFields(String conflictFields) {
        this.conflictFields = conflictFields;
    }

    public String getOldPayload() {
        return oldPayload;
    }

    public void setOldPayload(String oldPayload) {
        this.oldPayload = oldPayload;
    }

    public String getClientPayload() {
        return clientPayload;
    }

    public void setClientPayload(String clientPayload) {
        this.clientPayload = clientPayload;
    }

    public String getServerPayload() {
        return serverPayload;
    }

    public void setServerPayload(String serverPayload) {
        this.serverPayload = serverPayload;
    }

    public String getFinalPayload() {
        return finalPayload;
    }

    public void setFinalPayload(String finalPayload) {
        this.finalPayload = finalPayload;
    }

    public String getDefaultStrategy() {
        return defaultStrategy;
    }

    public void setDefaultStrategy(String defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
    }

    public String getResolveStrategy() {
        return resolveStrategy;
    }

    public void setResolveStrategy(String resolveStrategy) {
        this.resolveStrategy = resolveStrategy;
    }

    public String getResolveStatus() {
        return resolveStatus;
    }

    public void setResolveStatus(String resolveStatus) {
        this.resolveStatus = resolveStatus;
    }

    public Long getResolverId() {
        return resolverId;
    }

    public void setResolverId(Long resolverId) {
        this.resolverId = resolverId;
    }

    public LocalDateTime getResolveTime() {
        return resolveTime;
    }

    public void setResolveTime(LocalDateTime resolveTime) {
        this.resolveTime = resolveTime;
    }

    public String getResolveComment() {
        return resolveComment;
    }

    public void setResolveComment(String resolveComment) {
        this.resolveComment = resolveComment;
    }

    public String getLastWriteSide() {
        return lastWriteSide;
    }

    public void setLastWriteSide(String lastWriteSide) {
        this.lastWriteSide = lastWriteSide;
    }

    public LocalDateTime getLastWriteTime() {
        return lastWriteTime;
    }

    public void setLastWriteTime(LocalDateTime lastWriteTime) {
        this.lastWriteTime = lastWriteTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDeletedFlag() {
        return deletedFlag;
    }

    public void setDeletedFlag(Integer deletedFlag) {
        this.deletedFlag = deletedFlag;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
