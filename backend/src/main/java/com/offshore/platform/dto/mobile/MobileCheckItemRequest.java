package com.offshore.platform.dto.mobile;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class MobileCheckItemRequest {
    private String localId;
    private String itemCode;

    @NotBlank(message = "检查项名称不能为空")
    private String itemName;

    private String itemType;
    private String itemDesc;
    private Integer requiredFlag;
    private String checkResult;
    private String checkValue;
    private String checkUnit;
    private Integer abnormalFlag;
    private String abnormalDesc;
    private LocalDateTime checkedAt;
    private Integer attachmentRequiredFlag;
    private Integer attachmentCount;
    private Integer aiRequiredFlag;
    private Integer aiResultCount;
    private Integer sortOrder;
    private String deviceId;
    private String remark;

    public String getLocalId() { return localId; }
    public void setLocalId(String localId) { this.localId = localId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getItemDesc() { return itemDesc; }
    public void setItemDesc(String itemDesc) { this.itemDesc = itemDesc; }
    public Integer getRequiredFlag() { return requiredFlag; }
    public void setRequiredFlag(Integer requiredFlag) { this.requiredFlag = requiredFlag; }
    public String getCheckResult() { return checkResult; }
    public void setCheckResult(String checkResult) { this.checkResult = checkResult; }
    public String getCheckValue() { return checkValue; }
    public void setCheckValue(String checkValue) { this.checkValue = checkValue; }
    public String getCheckUnit() { return checkUnit; }
    public void setCheckUnit(String checkUnit) { this.checkUnit = checkUnit; }
    public Integer getAbnormalFlag() { return abnormalFlag; }
    public void setAbnormalFlag(Integer abnormalFlag) { this.abnormalFlag = abnormalFlag; }
    public String getAbnormalDesc() { return abnormalDesc; }
    public void setAbnormalDesc(String abnormalDesc) { this.abnormalDesc = abnormalDesc; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
    public Integer getAttachmentRequiredFlag() { return attachmentRequiredFlag; }
    public void setAttachmentRequiredFlag(Integer attachmentRequiredFlag) { this.attachmentRequiredFlag = attachmentRequiredFlag; }
    public Integer getAttachmentCount() { return attachmentCount; }
    public void setAttachmentCount(Integer attachmentCount) { this.attachmentCount = attachmentCount; }
    public Integer getAiRequiredFlag() { return aiRequiredFlag; }
    public void setAiRequiredFlag(Integer aiRequiredFlag) { this.aiRequiredFlag = aiRequiredFlag; }
    public Integer getAiResultCount() { return aiResultCount; }
    public void setAiResultCount(Integer aiResultCount) { this.aiResultCount = aiResultCount; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
