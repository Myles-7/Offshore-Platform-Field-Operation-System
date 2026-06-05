package com.offshore.platform.entity;

import java.time.LocalDateTime;

/**
 * knowledge_case 表实体。
 */
public class KnowledgeCase {
    private Long id;
    private String caseNo;
    private String title;
    private String equipmentType;
    private String faultType;
    private String faultPhenomenon;
    private String faultCause;
    private String solution;
    private String preventiveMeasures;
    private String workType;
    private Integer enabledFlag;
    private Long sourceWorkOrderId;
    private String attachmentIds;
    private String localId;
    private Long serverId;
    private Integer version;
    private String syncStatus;
    private String deviceId;
    private Long operatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deletedFlag;
    private Long createdBy;
    private Long updatedBy;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCaseNo() { return caseNo; }
    public void setCaseNo(String caseNo) { this.caseNo = caseNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getEquipmentType() { return equipmentType; }
    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }
    public String getFaultType() { return faultType; }
    public void setFaultType(String faultType) { this.faultType = faultType; }
    public String getFaultPhenomenon() { return faultPhenomenon; }
    public void setFaultPhenomenon(String faultPhenomenon) { this.faultPhenomenon = faultPhenomenon; }
    public String getFaultCause() { return faultCause; }
    public void setFaultCause(String faultCause) { this.faultCause = faultCause; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
    public String getPreventiveMeasures() { return preventiveMeasures; }
    public void setPreventiveMeasures(String preventiveMeasures) { this.preventiveMeasures = preventiveMeasures; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public Integer getEnabledFlag() { return enabledFlag; }
    public void setEnabledFlag(Integer enabledFlag) { this.enabledFlag = enabledFlag; }
    public Long getSourceWorkOrderId() { return sourceWorkOrderId; }
    public void setSourceWorkOrderId(Long sourceWorkOrderId) { this.sourceWorkOrderId = sourceWorkOrderId; }
    public String getAttachmentIds() { return attachmentIds; }
    public void setAttachmentIds(String attachmentIds) { this.attachmentIds = attachmentIds; }
    public String getLocalId() { return localId; }
    public void setLocalId(String localId) { this.localId = localId; }
    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(Integer deletedFlag) { this.deletedFlag = deletedFlag; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
