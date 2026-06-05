package com.offshore.platform.vo.ai;

import java.math.BigDecimal;

public class AiModelVO {
    public Long id;
    public String modelCode;
    public String modelName;
    public String modelVersion;
    public String modelType;
    public String runtimeType;
    public String deploySide;
    public BigDecimal confidenceThreshold;
    public Integer activeFlag;
    public String modelStatus;
}
