package com.offshore.platform.vo.admin;

import java.time.LocalDateTime;

public class WorkOrderVO {
    public Long id;
    public String workOrderNo;
    public Long projectId;
    public String projectName;
    public Long templateId;
    public String workTitle;
    public String workType;
    public String workLocation;
    public String workContent;
    public String requiredMaterialDesc;
    public Long leaderId;
    public String leaderName;
    public Long maintainerId;
    public String maintainerName;
    public LocalDateTime plannedStartTime;
    public LocalDateTime plannedEndTime;
    public LocalDateTime actualStartTime;
    public LocalDateTime actualEndTime;
    public String status;
    public String priority;
    public String rejectReason;
    public String closeReason;
    public Integer acceptanceRequired;
    public String sourceType;
    public Integer version;
    public String syncStatus;
    public Integer abnormalFlag;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public String remark;
}
