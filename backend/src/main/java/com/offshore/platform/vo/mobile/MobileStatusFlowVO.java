package com.offshore.platform.vo.mobile;

import java.time.LocalDateTime;

public class MobileStatusFlowVO {
    public Long id;
    public Long serverId;
    public String localId;
    public Long workOrderId;
    public String fromStatus;
    public String toStatus;
    public String operationType;
    public String operationDesc;
    public Long operatorId;
    public LocalDateTime operationTime;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
}
