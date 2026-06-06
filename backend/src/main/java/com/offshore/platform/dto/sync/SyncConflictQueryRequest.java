package com.offshore.platform.dto.sync;

import java.time.LocalDateTime;

public class SyncConflictQueryRequest {
    public Long workOrderId;
    public String businessNo;
    public String entityType;
    public String conflictStatus;
    public String deviceId;
    public Long operatorId;
    public LocalDateTime createdTimeStart;
    public LocalDateTime createdTimeEnd;
}
