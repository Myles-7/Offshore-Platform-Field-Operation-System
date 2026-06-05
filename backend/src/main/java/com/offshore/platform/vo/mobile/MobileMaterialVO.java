package com.offshore.platform.vo.mobile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MobileMaterialVO {
    public Long id;
    public Long serverId;
    public String localId;
    public Long workOrderId;
    public String materialCode;
    public String materialName;
    public String materialSpec;
    public String unit;
    public BigDecimal plannedQty;
    public BigDecimal actualQty;
    public String prepareStatus;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
}
