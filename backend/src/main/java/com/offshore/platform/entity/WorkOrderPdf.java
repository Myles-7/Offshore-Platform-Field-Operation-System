package com.offshore.platform.entity;

import java.time.LocalDateTime;

/**
 * work_order_pdf 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class WorkOrderPdf {
    public static final String TABLE_NAME = "work_order_pdf";

    private Long id;
    private String pdfNo;
    private Long workOrderId;
    private Long acceptanceId;
    private String fileId;
    private String workOrderNo;
    private String projectName;
    private String constructionUserName;
    private String acceptanceUserName;
    private LocalDateTime acceptanceTime;
    private String signatureFileIds;
    private String recordSummary;
    private String pdfContentSnapshot;
    private String pdfStatus;
    private Long generatedBy;
    private LocalDateTime generatedAt;
    private Integer lockedFlag;
    private String archiveStatus;
    private Integer previewEnabled;
    private Integer downloadEnabled;
    private String localFilePath;
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

    public String getPdfNo() {
        return pdfNo;
    }

    public void setPdfNo(String pdfNo) {
        this.pdfNo = pdfNo;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getAcceptanceId() {
        return acceptanceId;
    }

    public void setAcceptanceId(Long acceptanceId) {
        this.acceptanceId = acceptanceId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
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

    public String getConstructionUserName() {
        return constructionUserName;
    }

    public void setConstructionUserName(String constructionUserName) {
        this.constructionUserName = constructionUserName;
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

    public String getSignatureFileIds() {
        return signatureFileIds;
    }

    public void setSignatureFileIds(String signatureFileIds) {
        this.signatureFileIds = signatureFileIds;
    }

    public String getRecordSummary() {
        return recordSummary;
    }

    public void setRecordSummary(String recordSummary) {
        this.recordSummary = recordSummary;
    }

    public String getPdfContentSnapshot() {
        return pdfContentSnapshot;
    }

    public void setPdfContentSnapshot(String pdfContentSnapshot) {
        this.pdfContentSnapshot = pdfContentSnapshot;
    }

    public String getPdfStatus() {
        return pdfStatus;
    }

    public void setPdfStatus(String pdfStatus) {
        this.pdfStatus = pdfStatus;
    }

    public Long getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(Long generatedBy) {
        this.generatedBy = generatedBy;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Integer getLockedFlag() {
        return lockedFlag;
    }

    public void setLockedFlag(Integer lockedFlag) {
        this.lockedFlag = lockedFlag;
    }

    public String getArchiveStatus() {
        return archiveStatus;
    }

    public void setArchiveStatus(String archiveStatus) {
        this.archiveStatus = archiveStatus;
    }

    public Integer getPreviewEnabled() {
        return previewEnabled;
    }

    public void setPreviewEnabled(Integer previewEnabled) {
        this.previewEnabled = previewEnabled;
    }

    public Integer getDownloadEnabled() {
        return downloadEnabled;
    }

    public void setDownloadEnabled(Integer downloadEnabled) {
        this.downloadEnabled = downloadEnabled;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
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
