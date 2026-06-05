package com.offshore.platform.dto.material;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MaterialUsageRequest {
    @NotNull(message = "物料ID不能为空")
    public Long materialId;
    @NotNull(message = "使用数量不能为空")
    public BigDecimal usedQty;
    public BigDecimal wasteQty;
    public BigDecimal returnQty;
    public BigDecimal plannedQty;
    public Long qrcodeId;
    public String qrcodeValue;
    public String usageLocation;
    public String usageDesc;
    public BigDecimal costPrice;
    public LocalDateTime usageTime;
    public String localId;
    public String deviceId;
    public String remark;
}
