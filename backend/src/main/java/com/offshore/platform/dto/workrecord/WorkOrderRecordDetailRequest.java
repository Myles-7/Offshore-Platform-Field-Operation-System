package com.offshore.platform.dto.workrecord;

import java.time.LocalDateTime;

public class WorkOrderRecordDetailRequest {
    public String localId;
    public Integer version;
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
    public LocalDateTime clientCreatedAt;
    public LocalDateTime clientUpdatedAt;
    public String deviceId;
    public String remark;
}
