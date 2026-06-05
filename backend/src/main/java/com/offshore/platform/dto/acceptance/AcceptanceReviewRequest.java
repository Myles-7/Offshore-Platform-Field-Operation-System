package com.offshore.platform.dto.acceptance;

import jakarta.validation.constraints.NotBlank;

public class AcceptanceReviewRequest {
    @NotBlank(message = "验收状态不能为空")
    public String acceptanceStatus;
    public String acceptanceResult;
    public String acceptanceOpinion;
    public String rejectReason;
}
