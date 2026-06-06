package com.offshore.platform.dto.acceptance;

import jakarta.validation.constraints.NotBlank;

public class AcceptanceRelockRequest {
    @NotBlank(message = "relockReason is required")
    public String relockReason;
    public Boolean regeneratePdf;
}
