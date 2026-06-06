package com.offshore.platform.dto.ai;

import java.time.LocalDateTime;

public class AiResultQueryRequest {
    public Long projectId;
    public Long workOrderId;
    public Long recordId;
    public String defectType;
    public String reviewStatus;
    public String modelVersion;
    public LocalDateTime createdTimeStart;
    public LocalDateTime createdTimeEnd;
}
