package com.offshore.platform.dto.workrecord;

import jakarta.validation.constraints.NotBlank;

public class WorkOrderRecordRejectRequest {
    @NotBlank(message = "驳回原因不能为空")
    public String rejectReason;
}
