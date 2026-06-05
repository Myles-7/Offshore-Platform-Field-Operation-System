package com.offshore.platform.vo.mobile;

import java.time.LocalDateTime;

public class MobileCheckItemVO {
    public Long id;
    public Long serverId;
    public String localId;
    public Long workOrderId;
    public Long recordId;
    public String itemCode;
    public String itemName;
    public String itemType;
    public String itemDesc;
    public Integer requiredFlag;
    public String checkResult;
    public String checkValue;
    public String checkUnit;
    public Integer abnormalFlag;
    public String abnormalDesc;
    public Long checkedBy;
    public LocalDateTime checkedAt;
    public Integer attachmentRequiredFlag;
    public Integer attachmentCount;
    public Integer aiRequiredFlag;
    public Integer aiResultCount;
    public Integer sortOrder;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
    public Integer conflictFlag;
}
