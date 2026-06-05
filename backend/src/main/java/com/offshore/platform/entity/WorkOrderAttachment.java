package com.offshore.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * work_order_attachment 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class WorkOrderAttachment {
    public static final String TABLE_NAME = "work_order_attachment";

    private Long id;
    private Long workOrderId;
    private Long recordId;
    private String fileId;
    private String attachmentType;
    private String attachmentName;
    private String attachmentDesc;
    private String businessScene;
    private LocalDateTime captureTime;
    private Long captureUserId;
    private String captureUserName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationName;
    private Integer watermarkFlag;
    private String watermarkText;
    private LocalDateTime watermarkTime;
    private String watermarkWorkOrderNo;
    private String watermarkUserName;
    private BigDecimal watermarkLatitude;
    private BigDecimal watermarkLongitude;
    private Integer durationSeconds;
    private Integer mediaWidth;
    private Integer mediaHeight;
    private Long aiResultId;
    private String aiBindStatus;
    private String previewStatus;
    private String mobileCacheStatus;
    private String uploadStatus;
    private Integer retryCount;
    private LocalDateTime lastRetryTime;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime clientCreatedAt;
    private LocalDateTime clientUpdatedAt;
    private String localId;
    private Long serverId;
    private Integer version;
    private String syncStatus;
    private String deviceId;
    private Long operatorId;
    private Integer conflictFlag;
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

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentDesc() {
        return attachmentDesc;
    }

    public void setAttachmentDesc(String attachmentDesc) {
        this.attachmentDesc = attachmentDesc;
    }

    public String getBusinessScene() {
        return businessScene;
    }

    public void setBusinessScene(String businessScene) {
        this.businessScene = businessScene;
    }

    public LocalDateTime getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(LocalDateTime captureTime) {
        this.captureTime = captureTime;
    }

    public Long getCaptureUserId() {
        return captureUserId;
    }

    public void setCaptureUserId(Long captureUserId) {
        this.captureUserId = captureUserId;
    }

    public String getCaptureUserName() {
        return captureUserName;
    }

    public void setCaptureUserName(String captureUserName) {
        this.captureUserName = captureUserName;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Integer getWatermarkFlag() {
        return watermarkFlag;
    }

    public void setWatermarkFlag(Integer watermarkFlag) {
        this.watermarkFlag = watermarkFlag;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public void setWatermarkText(String watermarkText) {
        this.watermarkText = watermarkText;
    }

    public LocalDateTime getWatermarkTime() {
        return watermarkTime;
    }

    public void setWatermarkTime(LocalDateTime watermarkTime) {
        this.watermarkTime = watermarkTime;
    }

    public String getWatermarkWorkOrderNo() {
        return watermarkWorkOrderNo;
    }

    public void setWatermarkWorkOrderNo(String watermarkWorkOrderNo) {
        this.watermarkWorkOrderNo = watermarkWorkOrderNo;
    }

    public String getWatermarkUserName() {
        return watermarkUserName;
    }

    public void setWatermarkUserName(String watermarkUserName) {
        this.watermarkUserName = watermarkUserName;
    }

    public BigDecimal getWatermarkLatitude() {
        return watermarkLatitude;
    }

    public void setWatermarkLatitude(BigDecimal watermarkLatitude) {
        this.watermarkLatitude = watermarkLatitude;
    }

    public BigDecimal getWatermarkLongitude() {
        return watermarkLongitude;
    }

    public void setWatermarkLongitude(BigDecimal watermarkLongitude) {
        this.watermarkLongitude = watermarkLongitude;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getMediaWidth() {
        return mediaWidth;
    }

    public void setMediaWidth(Integer mediaWidth) {
        this.mediaWidth = mediaWidth;
    }

    public Integer getMediaHeight() {
        return mediaHeight;
    }

    public void setMediaHeight(Integer mediaHeight) {
        this.mediaHeight = mediaHeight;
    }

    public Long getAiResultId() {
        return aiResultId;
    }

    public void setAiResultId(Long aiResultId) {
        this.aiResultId = aiResultId;
    }

    public String getAiBindStatus() {
        return aiBindStatus;
    }

    public void setAiBindStatus(String aiBindStatus) {
        this.aiBindStatus = aiBindStatus;
    }

    public String getPreviewStatus() {
        return previewStatus;
    }

    public void setPreviewStatus(String previewStatus) {
        this.previewStatus = previewStatus;
    }

    public String getMobileCacheStatus() {
        return mobileCacheStatus;
    }

    public void setMobileCacheStatus(String mobileCacheStatus) {
        this.mobileCacheStatus = mobileCacheStatus;
    }

    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getLastRetryTime() {
        return lastRetryTime;
    }

    public void setLastRetryTime(LocalDateTime lastRetryTime) {
        this.lastRetryTime = lastRetryTime;
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

    public LocalDateTime getClientCreatedAt() {
        return clientCreatedAt;
    }

    public void setClientCreatedAt(LocalDateTime clientCreatedAt) {
        this.clientCreatedAt = clientCreatedAt;
    }

    public LocalDateTime getClientUpdatedAt() {
        return clientUpdatedAt;
    }

    public void setClientUpdatedAt(LocalDateTime clientUpdatedAt) {
        this.clientUpdatedAt = clientUpdatedAt;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
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

    public Integer getConflictFlag() {
        return conflictFlag;
    }

    public void setConflictFlag(Integer conflictFlag) {
        this.conflictFlag = conflictFlag;
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
