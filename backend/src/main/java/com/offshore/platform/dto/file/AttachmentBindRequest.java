package com.offshore.platform.dto.file;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AttachmentBindRequest {
    private String localId;
    private Long recordId;

    @NotBlank(message = "文件ID不能为空")
    private String fileId;

    @NotBlank(message = "附件类型不能为空")
    private String attachmentType;

    private String attachmentName;
    private String attachmentDesc;
    private String businessScene;
    private LocalDateTime captureTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationName;
    private Integer watermarkFlag;
    private String watermarkText;
    private Integer durationSeconds;
    private Integer mediaWidth;
    private Integer mediaHeight;
    private String deviceId;
    private String remark;

    public String getLocalId() { return localId; }
    public void setLocalId(String localId) { this.localId = localId; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    public String getAttachmentDesc() { return attachmentDesc; }
    public void setAttachmentDesc(String attachmentDesc) { this.attachmentDesc = attachmentDesc; }
    public String getBusinessScene() { return businessScene; }
    public void setBusinessScene(String businessScene) { this.businessScene = businessScene; }
    public LocalDateTime getCaptureTime() { return captureTime; }
    public void setCaptureTime(LocalDateTime captureTime) { this.captureTime = captureTime; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public Integer getWatermarkFlag() { return watermarkFlag; }
    public void setWatermarkFlag(Integer watermarkFlag) { this.watermarkFlag = watermarkFlag; }
    public String getWatermarkText() { return watermarkText; }
    public void setWatermarkText(String watermarkText) { this.watermarkText = watermarkText; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public Integer getMediaWidth() { return mediaWidth; }
    public void setMediaWidth(Integer mediaWidth) { this.mediaWidth = mediaWidth; }
    public Integer getMediaHeight() { return mediaHeight; }
    public void setMediaHeight(Integer mediaHeight) { this.mediaHeight = mediaHeight; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
