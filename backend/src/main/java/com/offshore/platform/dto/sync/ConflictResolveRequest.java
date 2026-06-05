package com.offshore.platform.dto.sync;

import jakarta.validation.constraints.NotBlank;

public class ConflictResolveRequest {
    @NotBlank(message = "处理策略不能为空")
    public String resolveStrategy;
    public String finalPayload;
    public String resolveComment;
}
