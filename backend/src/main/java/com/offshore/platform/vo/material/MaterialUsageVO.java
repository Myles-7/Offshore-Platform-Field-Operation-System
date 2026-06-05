package com.offshore.platform.vo.material;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MaterialUsageVO {
    public Long id;
    public Long serverId;
    public String localId;
    public String usageNo;
    public Long workOrderId;
    public Long materialId;
    public String materialCode;
    public String materialName;
    public BigDecimal usedQty;
    public BigDecimal wasteQty;
    public BigDecimal returnQty;
    public LocalDateTime usageTime;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
}
