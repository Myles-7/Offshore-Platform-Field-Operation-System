package com.offshore.platform.dto.admin;

import jakarta.validation.constraints.NotBlank;

public class WorkOrderStatusRequest {
    @NotBlank(message = "目标状态不能为空")
    public String status;

    public String operationDesc;
    public String rejectReason;
    public String closeReason;
}
