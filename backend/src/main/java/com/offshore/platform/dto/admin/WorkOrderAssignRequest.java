package com.offshore.platform.dto.admin;

import jakarta.validation.constraints.NotNull;

public class WorkOrderAssignRequest {
    public Long leaderId;

    @NotNull(message = "维修工ID不能为空")
    public Long maintainerId;

    public String assignmentRole;
    public String remark;
}
