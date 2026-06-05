package com.offshore.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * work_order_record 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class WorkOrderRecord {
    public static final String TABLE_NAME = "work_order_record";

    private Long id;
    private Long workOrderId;
    private Long projectId;
    private String recordNo;
    private String recordType;
    private LocalDateTime constructionTime;
    private Long constructionUserId;
    private String constructionUserName;
    private String constructionDesc;
    private String siteCondition;
    private Integer abnormalFlag;
    private String abnormalDesc;
    private String weather;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String locationName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal altitude;
    private Integer attachmentCount;
    private Integer aiResultCount;
    private String recordStatus;
    private LocalDateTime submittedAt;
    private Long confirmedBy;
    private LocalDateTime confirmedAt;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getRecordNo() {
        return recordNo;
    }

    public void setRecordNo(String recordNo) {
        this.recordNo = recordNo;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public LocalDateTime getConstructionTime() {
        return constructionTime;
    }

    public void setConstructionTime(LocalDateTime constructionTime) {
        this.constructionTime = constructionTime;
    }

    public Long getConstructionUserId() {
        return constructionUserId;
    }

    public void setConstructionUserId(Long constructionUserId) {
        this.constructionUserId = constructionUserId;
    }

    public String getConstructionUserName() {
        return constructionUserName;
    }

    public void setConstructionUserName(String constructionUserName) {
        this.constructionUserName = constructionUserName;
    }

    public String getConstructionDesc() {
        return constructionDesc;
    }

    public void setConstructionDesc(String constructionDesc) {
        this.constructionDesc = constructionDesc;
    }

    public String getSiteCondition() {
        return siteCondition;
    }

    public void setSiteCondition(String siteCondition) {
        this.siteCondition = siteCondition;
    }

    public Integer getAbnormalFlag() {
        return abnormalFlag;
    }

    public void setAbnormalFlag(Integer abnormalFlag) {
        this.abnormalFlag = abnormalFlag;
    }

    public String getAbnormalDesc() {
        return abnormalDesc;
    }

    public void setAbnormalDesc(String abnormalDesc) {
        this.abnormalDesc = abnormalDesc;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public BigDecimal getHumidity() {
        return humidity;
    }

    public void setHumidity(BigDecimal humidity) {
        this.humidity = humidity;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
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

    public BigDecimal getAltitude() {
        return altitude;
    }

    public void setAltitude(BigDecimal altitude) {
        this.altitude = altitude;
    }

    public Integer getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(Integer attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public Integer getAiResultCount() {
        return aiResultCount;
    }

    public void setAiResultCount(Integer aiResultCount) {
        this.aiResultCount = aiResultCount;
    }

    public String getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(String recordStatus) {
        this.recordStatus = recordStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Long getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(Long confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
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
