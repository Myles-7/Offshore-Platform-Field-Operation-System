package com.offshore.platform.vo.material;

import java.math.BigDecimal;

public class InventoryVO {
    public Long id;
    public Long materialId;
    public String materialCode;
    public String warehouseCode;
    public String warehouseName;
    public String batchNo;
    public BigDecimal currentQty;
    public BigDecimal availableQty;
    public String inventoryStatus;
}
