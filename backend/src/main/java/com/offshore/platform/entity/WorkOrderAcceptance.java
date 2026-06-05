package com.offshore.platform.entity;

import java.time.LocalDateTime;

/**
 * work_order_acceptance 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class WorkOrderAcceptance {
    public static final String TABLE_NAME = "work_order_acceptance";

    private Long id;
    private String acceptanceNo;
    private Long workOrderId;
    private Long projectId;
    private String workOrderNo;
    private String projectName;
    private Long constructionUserId;
    private String constructionUserName;
    private Long acceptanceUserId;
    private String acceptanceUserName;
    private LocalDateTime acceptanceTime;
    private String acceptanceStatus;
    private String acceptanceResult;
    private String acceptanceOpinion;
    private String problemDesc;
    private Integer rectificationRequired;
    private String recordSummary;
    private String attachmentSummary;
    private Integer signatureCount;
    private Integer pdfGeneratedFlag;
    private Integer lockedFlag;
    private LocalDateTime lockedAt;
    private Long lockedBy;
    private String lockReason;
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

    public String getAcceptanceNo() {
        return acceptanceNo;
    }

    public void setAcceptanceNo(String acceptanceNo) {
        this.acceptanceNo = acceptanceNo;
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

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public Long getAcceptanceUserId() {
        return acceptanceUserId;
    }

    public void setAcceptanceUserId(Long acceptanceUserId) {
        this.acceptanceUserId = acceptanceUserId;
    }

    public String getAcceptanceUserName() {
        return acceptanceUserName;
    }

    public void setAcceptanceUserName(String acceptanceUserName) {
        this.acceptanceUserName = acceptanceUserName;
    }

    public LocalDateTime getAcceptanceTime() {
        return acceptanceTime;
    }

    public void setAcceptanceTime(LocalDateTime acceptanceTime) {
        this.acceptanceTime = acceptanceTime;
    }

    public String getAcceptanceStatus() {
        return acceptanceStatus;
    }

    public void setAcceptanceStatus(String acceptanceStatus) {
        this.acceptanceStatus = acceptanceStatus;
    }

    public String getAcceptanceResult() {
        return acceptanceResult;
    }

    public void setAcceptanceResult(String acceptanceResult) {
        this.acceptanceResult = acceptanceResult;
    }

    public String getAcceptanceOpinion() {
        return acceptanceOpinion;
    }

    public void setAcceptanceOpinion(String acceptanceOpinion) {
        this.acceptanceOpinion = acceptanceOpinion;
    }

    public String getProblemDesc() {
        return problemDesc;
    }

    public void setProblemDesc(String problemDesc) {
        this.problemDesc = problemDesc;
    }

    public Integer getRectificationRequired() {
        return rectificationRequired;
    }

    public void setRectificationRequired(Integer rectificationRequired) {
        this.rectificationRequired = rectificationRequired;
    }

    public String getRecordSummary() {
        return recordSummary;
    }

    public void setRecordSummary(String recordSummary) {
        this.recordSummary = recordSummary;
    }

    public String getAttachmentSummary() {
        return attachmentSummary;
    }

    public void setAttachmentSummary(String attachmentSummary) {
        this.attachmentSummary = attachmentSummary;
    }

    public Integer getSignatureCount() {
        return signatureCount;
    }

    public void setSignatureCount(Integer signatureCount) {
        this.signatureCount = signatureCount;
    }

    public Integer getPdfGeneratedFlag() {
        return pdfGeneratedFlag;
    }

    public void setPdfGeneratedFlag(Integer pdfGeneratedFlag) {
        this.pdfGeneratedFlag = pdfGeneratedFlag;
    }

    public Integer getLockedFlag() {
        return lockedFlag;
    }

    public void setLockedFlag(Integer lockedFlag) {
        this.lockedFlag = lockedFlag;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Long getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(Long lockedBy) {
        this.lockedBy = lockedBy;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
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
