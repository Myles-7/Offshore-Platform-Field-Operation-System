package com.offshore.platform.dto.ai;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class AiModelRequest {
    @NotBlank
    public String modelCode;
    @NotBlank
    public String modelName;
    @NotBlank
    public String modelVersion;
    public String modelType;
    public String runtimeType;
    public String deploySide;
    public String modelFileId;
    public String modelHash;
    public String inputSize;
    public String defectTypes;
    public BigDecimal confidenceThreshold;
    public String remark;
}
