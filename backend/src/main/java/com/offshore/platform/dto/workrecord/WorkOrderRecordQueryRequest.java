package com.offshore.platform.dto.workrecord;

import java.time.LocalDateTime;

public class WorkOrderRecordQueryRequest {
    public Long constructionUserId;
    public LocalDateTime constructionTimeStart;
    public LocalDateTime constructionTimeEnd;
    public Integer abnormalFlag;
    public String recordStatus;
    public String syncStatus;
}
