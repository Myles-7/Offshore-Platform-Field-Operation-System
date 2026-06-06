package com.offshore.platform.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class AiBatchReviewRequest {
    @NotEmpty(message = "resultIds is required")
    public List<Long> resultIds = new ArrayList<>();
    @NotBlank(message = "reviewStatus is required")
    public String reviewStatus;
    public String reviewConclusion;
    public String reviewComment;
}
