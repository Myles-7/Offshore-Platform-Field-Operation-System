package com.offshore.platform.dto.ai;

import java.math.BigDecimal;

public class AiDefectBoxRequest {
    public String localId;
    public String defectType;
    public BigDecimal confidence;
    public BigDecimal x;
    public BigDecimal y;
    public BigDecimal width;
    public BigDecimal height;
    public Integer imageWidth;
    public Integer imageHeight;
    public Integer normalizedFlag;
    public String boxLabel;
    public String boxColor;
    public Integer sortOrder;
}
