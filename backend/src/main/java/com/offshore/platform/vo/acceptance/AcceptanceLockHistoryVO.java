package com.offshore.platform.vo.acceptance;

import java.time.LocalDateTime;

public class AcceptanceLockHistoryVO {
    public String sourceType;
    public Long sourceId;
    public String operationType;
    public String description;
    public Long operatorId;
    public String operatorName;
    public LocalDateTime operationTime;
}
