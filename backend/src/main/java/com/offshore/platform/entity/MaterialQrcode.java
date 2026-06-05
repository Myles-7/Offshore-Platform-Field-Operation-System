package com.offshore.platform.entity;

import java.time.LocalDateTime;

/**
 * material_qrcode 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class MaterialQrcode {
    public static final String TABLE_NAME = "material_qrcode";

    private Long id;
    private Long materialId;
    private String materialCode;
    private String qrcodeValue;
    private String qrcodeFileId;
    private String batchNo;
    private String serialNo;
    private Long generateUserId;
    private LocalDateTime generateTime;
    private String bindStatus;
    private String qrcodeStatus;
    private LocalDateTime lastScanTime;
    private Long lastScanUserId;
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

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getQrcodeValue() {
        return qrcodeValue;
    }

    public void setQrcodeValue(String qrcodeValue) {
        this.qrcodeValue = qrcodeValue;
    }

    public String getQrcodeFileId() {
        return qrcodeFileId;
    }

    public void setQrcodeFileId(String qrcodeFileId) {
        this.qrcodeFileId = qrcodeFileId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public Long getGenerateUserId() {
        return generateUserId;
    }

    public void setGenerateUserId(Long generateUserId) {
        this.generateUserId = generateUserId;
    }

    public LocalDateTime getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(LocalDateTime generateTime) {
        this.generateTime = generateTime;
    }

    public String getBindStatus() {
        return bindStatus;
    }

    public void setBindStatus(String bindStatus) {
        this.bindStatus = bindStatus;
    }

    public String getQrcodeStatus() {
        return qrcodeStatus;
    }

    public void setQrcodeStatus(String qrcodeStatus) {
        this.qrcodeStatus = qrcodeStatus;
    }

    public LocalDateTime getLastScanTime() {
        return lastScanTime;
    }

    public void setLastScanTime(LocalDateTime lastScanTime) {
        this.lastScanTime = lastScanTime;
    }

    public Long getLastScanUserId() {
        return lastScanUserId;
    }

    public void setLastScanUserId(Long lastScanUserId) {
        this.lastScanUserId = lastScanUserId;
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
