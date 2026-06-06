package com.offshore.platform.dto.ai;

import jakarta.validation.constraints.NotBlank;

public class AiReviewRequest {
    @NotBlank
    public String reviewStatus;
    public String confirmedDefectType;
    public String reviewConclusion;
    public String reviewComment;
    public String reviewOpinion;
    public String acceptanceSuggestion;
}
