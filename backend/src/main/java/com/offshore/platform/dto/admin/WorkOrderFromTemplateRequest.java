package com.offshore.platform.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class WorkOrderFromTemplateRequest {
    public String workOrderNo;

    @NotNull(message = "项目ID不能为空")
    public Long projectId;

    @NotBlank(message = "工单标题不能为空")
    public String workTitle;

    @NotBlank(message = "作业地点不能为空")
    public String workLocation;

    public Long leaderId;
    public Long maintainerId;
    public LocalDateTime plannedStartTime;
    public LocalDateTime plannedEndTime;
    public Integer acceptanceRequired;
    public String remark;
}
