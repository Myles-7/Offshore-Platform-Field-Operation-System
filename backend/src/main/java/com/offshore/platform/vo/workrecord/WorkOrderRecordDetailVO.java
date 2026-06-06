package com.offshore.platform.vo.workrecord;

import java.time.LocalDateTime;

public class WorkOrderRecordDetailVO {
    public Long id;
    public Long serverId;
    public String localId;
    public Long workOrderId;
    public Long recordId;
    public String detailType;
    public String detailTitle;
    public String detailContent;
    public Integer stepNo;
    public String itemCode;
    public String itemName;
    public String itemValue;
    public String itemUnit;
    public Integer normalFlag;
    public String abnormalDesc;
    public Integer attachmentRefFlag;
    public Integer aiRefFlag;
    public Integer sortOrder;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
    public Integer conflictFlag;
}
