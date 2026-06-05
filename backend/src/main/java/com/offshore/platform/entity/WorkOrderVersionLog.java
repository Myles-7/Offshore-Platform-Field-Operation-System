package com.offshore.platform.entity;

import java.time.LocalDateTime;

/**
 * work_order_version_log 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class WorkOrderVersionLog {
    public static final String TABLE_NAME = "work_order_version_log";

    private Long id;
    private Long workOrderId;
    private String workOrderNo;
    private Integer version;
    private Integer previousVersion;
    private String changeSource;
    private String changeType;
    private String changedFields;
    private String oldPayload;
    private String newPayload;
    private String localId;
    private Long serverId;
    private String deviceId;
    private Long operatorId;
    private Long syncTaskId;
    private Long syncLogId;
    private Long conflictId;
    private LocalDateTime clientUpdatedAt;
    private LocalDateTime serverUpdatedAt;
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

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(Integer previousVersion) {
        this.previousVersion = previousVersion;
    }

    public String getChangeSource() {
        return changeSource;
    }

    public void setChangeSource(String changeSource) {
        this.changeSource = changeSource;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }

    public String getOldPayload() {
        return oldPayload;
    }

    public void setOldPayload(String oldPayload) {
        this.oldPayload = oldPayload;
    }

    public String getNewPayload() {
        return newPayload;
    }

    public void setNewPayload(String newPayload) {
        this.newPayload = newPayload;
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

    public Long getConflictId() {
        return conflictId;
    }

    public void setConflictId(Long conflictId) {
        this.conflictId = conflictId;
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
