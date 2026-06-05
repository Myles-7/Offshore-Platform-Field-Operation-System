package com.offshore.platform.vo.mobile;

import java.time.LocalDateTime;

public class MobileWorkOrderListVO {
    public Long id;
    public Long serverId;
    public String localId;
    public String workOrderNo;
    public String projectName;
    public String workTitle;
    public String workLocation;
    public String workContentSummary;
    public String status;
    public String priority;
    public LocalDateTime plannedStartTime;
    public LocalDateTime plannedEndTime;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
    public Integer conflictFlag;
}
