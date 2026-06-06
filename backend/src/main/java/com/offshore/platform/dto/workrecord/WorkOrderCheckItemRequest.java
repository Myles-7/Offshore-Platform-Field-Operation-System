package com.offshore.platform.dto.workrecord;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class WorkOrderCheckItemRequest {
    public String localId;
    public Integer version;
    public String itemCode;
    @NotBlank(message = "检查项名称不能为空")
    public String itemName;
    public String itemType;
    public String itemDesc;
    public Integer requiredFlag;
    public String checkResult;
    public String checkValue;
    public String checkUnit;
    public Integer abnormalFlag;
    public String abnormalDesc;
    public LocalDateTime checkedAt;
    public Integer attachmentRequiredFlag;
    public Integer attachmentCount;
    public Integer aiRequiredFlag;
    public Integer aiResultCount;
    public Integer sortOrder;
    public LocalDateTime clientCreatedAt;
    public LocalDateTime clientUpdatedAt;
    public String deviceId;
    public String remark;
}
