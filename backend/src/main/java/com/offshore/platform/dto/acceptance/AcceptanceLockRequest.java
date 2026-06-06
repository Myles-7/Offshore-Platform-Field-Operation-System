package com.offshore.platform.dto.acceptance;

import jakarta.validation.constraints.NotBlank;

public class AcceptanceLockRequest {
    @NotBlank(message = "lockReason is required")
    public String lockReason;
}
