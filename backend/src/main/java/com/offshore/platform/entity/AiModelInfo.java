package com.offshore.platform.entity;

import java.math.BigDecimal;

import java.time.LocalDateTime;

/**
 * ai_model_info 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class AiModelInfo {
    public static final String TABLE_NAME = "ai_model_info";

    private Long id;
    private String modelCode;
    private String modelName;
    private String modelVersion;
    private String modelType;
    private String runtimeType;
    private String deploySide;
    private String modelFileId;
    private String modelHash;
    private String inputSize;
    private String defectTypes;
    private BigDecimal confidenceThreshold;
    private Integer activeFlag;
    private String modelStatus;
    private LocalDateTime releasedAt;
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

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getRuntimeType() {
        return runtimeType;
    }

    public void setRuntimeType(String runtimeType) {
        this.runtimeType = runtimeType;
    }

    public String getDeploySide() {
        return deploySide;
    }

    public void setDeploySide(String deploySide) {
        this.deploySide = deploySide;
    }

    public String getModelFileId() {
        return modelFileId;
    }

    public void setModelFileId(String modelFileId) {
        this.modelFileId = modelFileId;
    }

    public String getModelHash() {
        return modelHash;
    }

    public void setModelHash(String modelHash) {
        this.modelHash = modelHash;
    }

    public String getInputSize() {
        return inputSize;
    }

    public void setInputSize(String inputSize) {
        this.inputSize = inputSize;
    }

    public String getDefectTypes() {
        return defectTypes;
    }

    public void setDefectTypes(String defectTypes) {
        this.defectTypes = defectTypes;
    }

    public BigDecimal getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(BigDecimal confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public Integer getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Integer activeFlag) {
        this.activeFlag = activeFlag;
    }

    public String getModelStatus() {
        return modelStatus;
    }

    public void setModelStatus(String modelStatus) {
        this.modelStatus = modelStatus;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
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
