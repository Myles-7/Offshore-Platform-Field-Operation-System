package com.offshore.platform.vo.ai;

import java.math.BigDecimal;

public class AiDefectBoxVO {
    public Long id;
    public Long aiResultId;
    public String defectType;
    public BigDecimal confidence;
    public BigDecimal x;
    public BigDecimal y;
    public BigDecimal width;
    public BigDecimal height;
    public String boxLabel;
}
