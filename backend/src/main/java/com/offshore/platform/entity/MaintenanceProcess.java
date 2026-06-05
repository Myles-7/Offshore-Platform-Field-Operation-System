package com.offshore.platform.entity;

import java.time.LocalDateTime;

/**
 * maintenance_process 表实体。
 */
public class MaintenanceProcess {
    private Long id;
    private String processCode;
    private String processName;
    private String equipmentType;
    private String processType;
    private String processSteps;
    private String toolsRequired;
    private String materialRequired;
    private String safetyMeasures;
    private String qualityStandard;
    private String durationEstimate;
    private String workType;
    private Integer enabledFlag;
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
    public String getProcessCode() { return processCode; }
    public void setProcessCode(String processCode) { this.processCode = processCode; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public String getEquipmentType() { return equipmentType; }
    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }
    public String getProcessType() { return processType; }
    public void setProcessType(String processType) { this.processType = processType; }
    public String getProcessSteps() { return processSteps; }
    public void setProcessSteps(String processSteps) { this.processSteps = processSteps; }
    public String getToolsRequired() { return toolsRequired; }
    public void setToolsRequired(String toolsRequired) { this.toolsRequired = toolsRequired; }
    public String getMaterialRequired() { return materialRequired; }
    public void setMaterialRequired(String materialRequired) { this.materialRequired = materialRequired; }
    public String getSafetyMeasures() { return safetyMeasures; }
    public void setSafetyMeasures(String safetyMeasures) { this.safetyMeasures = safetyMeasures; }
    public String getQualityStandard() { return qualityStandard; }
    public void setQualityStandard(String qualityStandard) { this.qualityStandard = qualityStandard; }
    public String getDurationEstimate() { return durationEstimate; }
    public void setDurationEstimate(String durationEstimate) { this.durationEstimate = durationEstimate; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public Integer getEnabledFlag() { return enabledFlag; }
    public void setEnabledFlag(Integer enabledFlag) { this.enabledFlag = enabledFlag; }
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
