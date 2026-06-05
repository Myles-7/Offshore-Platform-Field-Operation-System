package com.offshore.platform.entity;

import java.time.LocalDateTime;

/**
 * ai_review_record 表实体。
 * 根据 db/init_schema.sql 生成。
 */
public class AiReviewRecord {
    public static final String TABLE_NAME = "ai_review_record";

    private Long id;
    private String reviewNo;
    private Long aiResultId;
    private Long workOrderId;
    private Long recordId;
    private Long attachmentId;
    private Long reviewerId;
    private String reviewerName;
    private String reviewStatus;
    private String confirmedDefectType;
    private String reviewOpinion;
    private String acceptanceSuggestion;
    private LocalDateTime reviewTime;
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

    public String getReviewNo() {
        return reviewNo;
    }

    public void setReviewNo(String reviewNo) {
        this.reviewNo = reviewNo;
    }

    public Long getAiResultId() {
        return aiResultId;
    }

    public void setAiResultId(Long aiResultId) {
        this.aiResultId = aiResultId;
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

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getConfirmedDefectType() {
        return confirmedDefectType;
    }

    public void setConfirmedDefectType(String confirmedDefectType) {
        this.confirmedDefectType = confirmedDefectType;
    }

    public String getReviewOpinion() {
        return reviewOpinion;
    }

    public void setReviewOpinion(String reviewOpinion) {
        this.reviewOpinion = reviewOpinion;
    }

    public String getAcceptanceSuggestion() {
        return acceptanceSuggestion;
    }

    public void setAcceptanceSuggestion(String acceptanceSuggestion) {
        this.acceptanceSuggestion = acceptanceSuggestion;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
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
