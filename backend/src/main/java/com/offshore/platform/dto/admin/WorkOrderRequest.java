package com.offshore.platform.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class WorkOrderRequest {
    public String workOrderNo;

    @NotNull(message = "项目ID不能为空")
    public Long projectId;

    public Long templateId;

    @NotBlank(message = "工单标题不能为空")
    public String workTitle;

    public String workType;

    @NotBlank(message = "作业地点不能为空")
    public String workLocation;

    @NotBlank(message = "作业内容不能为空")
    public String workContent;

    public String requiredMaterialDesc;
    public Long leaderId;
    public Long maintainerId;
    public LocalDateTime plannedStartTime;
    public LocalDateTime plannedEndTime;
    public String priority;
    public Integer acceptanceRequired;
    public String remark;
}
