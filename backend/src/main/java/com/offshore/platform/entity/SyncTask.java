package com.offshore.platform.entity;

import java.time.LocalDateTime;

/**
 * sync_task 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class SyncTask {
    public static final String TABLE_NAME = "sync_task";

    private Long id;
    private String syncTaskNo;
    private String batchId;
    private String deviceId;
    private Long operatorId;
    private String syncDirection;
    private String syncType;
    private String taskStatus;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private Integer conflictCount;
    private Integer retryCount;
    private Integer maxRetryCount;
    private String requestCursor;
    private String responseCursor;
    private LocalDateTime clientTime;
    private LocalDateTime serverStartTime;
    private LocalDateTime serverEndTime;
    private String errorCode;
    private String errorMessage;
    private String idempotencyKey;
    private String requestSummary;
    private String responseSummary;
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

    public String getSyncTaskNo() {
        return syncTaskNo;
    }

    public void setSyncTaskNo(String syncTaskNo) {
        this.syncTaskNo = syncTaskNo;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
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

    public String getSyncDirection() {
        return syncDirection;
    }

    public void setSyncDirection(String syncDirection) {
        this.syncDirection = syncDirection;
    }

    public String getSyncType() {
        return syncType;
    }

    public void setSyncType(String syncType) {
        this.syncType = syncType;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public Integer getConflictCount() {
        return conflictCount;
    }

    public void setConflictCount(Integer conflictCount) {
        this.conflictCount = conflictCount;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public String getRequestCursor() {
        return requestCursor;
    }

    public void setRequestCursor(String requestCursor) {
        this.requestCursor = requestCursor;
    }

    public String getResponseCursor() {
        return responseCursor;
    }

    public void setResponseCursor(String responseCursor) {
        this.responseCursor = responseCursor;
    }

    public LocalDateTime getClientTime() {
        return clientTime;
    }

    public void setClientTime(LocalDateTime clientTime) {
        this.clientTime = clientTime;
    }

    public LocalDateTime getServerStartTime() {
        return serverStartTime;
    }

    public void setServerStartTime(LocalDateTime serverStartTime) {
        this.serverStartTime = serverStartTime;
    }

    public LocalDateTime getServerEndTime() {
        return serverEndTime;
    }

    public void setServerEndTime(LocalDateTime serverEndTime) {
        this.serverEndTime = serverEndTime;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRequestSummary() {
        return requestSummary;
    }

    public void setRequestSummary(String requestSummary) {
        this.requestSummary = requestSummary;
    }

    public String getResponseSummary() {
        return responseSummary;
    }

    public void setResponseSummary(String responseSummary) {
        this.responseSummary = responseSummary;
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
