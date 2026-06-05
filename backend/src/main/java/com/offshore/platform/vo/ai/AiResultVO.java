package com.offshore.platform.vo.ai;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiResultVO {
    public Long id;
    public String aiResultNo;
    public Long workOrderId;
    public String workOrderNo;
    public Long projectId;
    public Long recordId;
    public Long attachmentId;
    public String fileId;
    public Long modelId;
    public String modelCode;
    public String modelVersion;
    public Integer inferCostMs;
    public String defectType;
    public BigDecimal confidence;
    public Integer suspectedDefectFlag;
    public Integer defectCount;
    public String resultSummary;
    public String reviewStatus;
    public Integer reviewedFlag;
    public Integer version;
    public String syncStatus;
    public LocalDateTime updatedAt;
    public List<AiDefectBoxVO> boxes = new ArrayList<>();
    public String auxiliaryNotice = "AI result is auxiliary only and does not change final work-order acceptance status.";
}
