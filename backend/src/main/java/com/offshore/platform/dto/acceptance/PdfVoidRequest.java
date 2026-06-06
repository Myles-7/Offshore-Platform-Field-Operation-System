package com.offshore.platform.dto.acceptance;

import jakarta.validation.constraints.NotBlank;

public class PdfVoidRequest {
    @NotBlank(message = "voidReason is required")
    public String voidReason;
}
