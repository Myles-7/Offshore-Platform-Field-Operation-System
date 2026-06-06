package com.offshore.platform.dto.workrecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WorkOrderRecordUpdateRequest {
    public Integer version;
    public String recordType;
    public LocalDateTime constructionTime;
    public String constructionDesc;
    public String siteCondition;
    public Integer abnormalFlag;
    public String abnormalDesc;
    public String weather;
    public BigDecimal temperature;
    public BigDecimal humidity;
    public String locationName;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public BigDecimal altitude;
    public LocalDateTime clientUpdatedAt;
    public String deviceId;
    public String remark;
}
