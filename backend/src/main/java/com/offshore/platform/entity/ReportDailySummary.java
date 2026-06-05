package com.offshore.platform.entity;

import java.time.LocalDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * report_daily_summary 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class ReportDailySummary {
    public static final String TABLE_NAME = "report_daily_summary";

    private Long id;
    private LocalDate summaryDate;
    private Long projectId;
    private String projectName;
    private Integer workOrderTotal;
    private Integer workOrderInProgress;
    private Integer workOrderCompleted;
    private Integer workOrderPendingAcceptance;
    private Integer workOrderRejected;
    private BigDecimal completionRate;
    private Integer attendanceCount;
    private Integer recordCount;
    private Integer attachmentCount;
    private BigDecimal materialUsedAmount;
    private BigDecimal materialUsedQty;
    private BigDecimal outputValue;
    private Integer aiSuspectedCount;
    private Integer conflictPendingCount;
    private LocalDateTime generatedAt;
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

    public LocalDate getSummaryDate() {
        return summaryDate;
    }

    public void setSummaryDate(LocalDate summaryDate) {
        this.summaryDate = summaryDate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getWorkOrderTotal() {
        return workOrderTotal;
    }

    public void setWorkOrderTotal(Integer workOrderTotal) {
        this.workOrderTotal = workOrderTotal;
    }

    public Integer getWorkOrderInProgress() {
        return workOrderInProgress;
    }

    public void setWorkOrderInProgress(Integer workOrderInProgress) {
        this.workOrderInProgress = workOrderInProgress;
    }

    public Integer getWorkOrderCompleted() {
        return workOrderCompleted;
    }

    public void setWorkOrderCompleted(Integer workOrderCompleted) {
        this.workOrderCompleted = workOrderCompleted;
    }

    public Integer getWorkOrderPendingAcceptance() {
        return workOrderPendingAcceptance;
    }

    public void setWorkOrderPendingAcceptance(Integer workOrderPendingAcceptance) {
        this.workOrderPendingAcceptance = workOrderPendingAcceptance;
    }

    public Integer getWorkOrderRejected() {
        return workOrderRejected;
    }

    public void setWorkOrderRejected(Integer workOrderRejected) {
        this.workOrderRejected = workOrderRejected;
    }

    public BigDecimal getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(BigDecimal completionRate) {
        this.completionRate = completionRate;
    }

    public Integer getAttendanceCount() {
        return attendanceCount;
    }

    public void setAttendanceCount(Integer attendanceCount) {
        this.attendanceCount = attendanceCount;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public Integer getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(Integer attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public BigDecimal getMaterialUsedAmount() {
        return materialUsedAmount;
    }

    public void setMaterialUsedAmount(BigDecimal materialUsedAmount) {
        this.materialUsedAmount = materialUsedAmount;
    }

    public BigDecimal getMaterialUsedQty() {
        return materialUsedQty;
    }

    public void setMaterialUsedQty(BigDecimal materialUsedQty) {
        this.materialUsedQty = materialUsedQty;
    }

    public BigDecimal getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(BigDecimal outputValue) {
        this.outputValue = outputValue;
    }

    public Integer getAiSuspectedCount() {
        return aiSuspectedCount;
    }

    public void setAiSuspectedCount(Integer aiSuspectedCount) {
        this.aiSuspectedCount = aiSuspectedCount;
    }

    public Integer getConflictPendingCount() {
        return conflictPendingCount;
    }

    public void setConflictPendingCount(Integer conflictPendingCount) {
        this.conflictPendingCount = conflictPendingCount;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
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
