package com.offshore.platform.dto.workrecord;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WorkOrderRecordCreateRequest {
    public String localId;
    @NotBlank(message = "记录类型不能为空")
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
    public LocalDateTime clientCreatedAt;
    public LocalDateTime clientUpdatedAt;
    public String deviceId;
    public String remark;
}
