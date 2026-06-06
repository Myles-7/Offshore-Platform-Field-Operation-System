package com.offshore.platform.dto.acceptance;

import jakarta.validation.constraints.NotBlank;

public class AcceptanceUnlockRequest {
    @NotBlank(message = "unlockReason is required")
    public String unlockReason;
    public String unlockScope;
}
