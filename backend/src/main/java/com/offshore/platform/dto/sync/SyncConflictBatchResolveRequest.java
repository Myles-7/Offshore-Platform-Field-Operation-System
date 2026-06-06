package com.offshore.platform.dto.sync;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class SyncConflictBatchResolveRequest {
    @NotEmpty(message = "conflictIds is required")
    public List<Long> conflictIds = new ArrayList<>();
    @NotBlank(message = "resolveStrategy is required")
    public String resolveStrategy;
    public String resolveComment;
}
