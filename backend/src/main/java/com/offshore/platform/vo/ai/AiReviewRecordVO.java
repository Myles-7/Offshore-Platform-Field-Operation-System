package com.offshore.platform.vo.ai;

import java.time.LocalDateTime;

public class AiReviewRecordVO {
    public Long id;
    public String reviewNo;
    public Long aiResultId;
    public Long reviewerId;
    public String reviewerName;
    public String reviewStatus;
    public String confirmedDefectType;
    public String reviewOpinion;
    public String acceptanceSuggestion;
    public LocalDateTime reviewTime;
}
