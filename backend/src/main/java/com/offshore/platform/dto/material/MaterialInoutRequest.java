package com.offshore.platform.dto.material;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class MaterialInoutRequest {
    @NotNull(message = "物料ID不能为空")
    public Long materialId;
    @NotNull(message = "数量不能为空")
    public BigDecimal quantity;
    public Long workOrderId;
    public Long qrcodeId;
    public String qrcodeValue;
    public String warehouseCode;
    public String warehouseName;
    public String locationCode;
    public String batchNo;
    public String businessReason;
    public String remark;
}
