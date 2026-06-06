package com.offshore.platform.dto.ai;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiResultRequest {
    public String localId;
    @NotNull
    public Long workOrderId;
    public Long recordId;
    @NotNull
    public Long attachmentId;
    public String fileId;
    public String resultImageFileId;
    public Long modelId;
    public String modelCode;
    public String modelVersion;
    public Integer inferenceTimeMs;
    public String inferSide;
    public LocalDateTime inferTime;
    public Integer inferCostMs;
    public String defectType;
    public BigDecimal confidence;
    public Integer suspectedDefectFlag;
    public String resultSummary;
    public String rawResult;
    public String deviceId;
    public List<AiDefectBoxRequest> boxes = new ArrayList<>();
}
