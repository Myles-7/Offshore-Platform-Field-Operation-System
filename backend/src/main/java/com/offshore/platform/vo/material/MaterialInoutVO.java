package com.offshore.platform.vo.material;

import java.math.BigDecimal;

public class MaterialInoutVO {
    public Long id;
    public String recordNo;
    public Long materialId;
    public String materialCode;
    public String materialName;
    public String inoutType;
    public BigDecimal quantity;
    public BigDecimal beforeQty;
    public BigDecimal afterQty;
}
