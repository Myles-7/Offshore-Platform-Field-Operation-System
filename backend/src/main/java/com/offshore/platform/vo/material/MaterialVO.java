package com.offshore.platform.vo.material;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MaterialVO {
    public Long id;
    public String materialCode;
    public String materialName;
    public String materialCategory;
    public String materialSpec;
    public String unit;
    public BigDecimal safetyStockQty;
    public Integer enabledFlag;
    public Integer traceEnabled;
    public Integer qrcodeRequired;
    public LocalDateTime updatedAt;
}
