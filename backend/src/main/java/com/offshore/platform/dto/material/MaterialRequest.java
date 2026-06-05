package com.offshore.platform.dto.material;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class MaterialRequest {
    @NotBlank(message = "物料编码不能为空")
    public String materialCode;
    @NotBlank(message = "物料名称不能为空")
    public String materialName;
    public String materialCategory;
    public String materialSpec;
    public String materialModel;
    public String unit;
    public String brand;
    public String manufacturer;
    public BigDecimal safetyStockQty;
    public Integer enabledFlag;
    public Integer traceEnabled;
    public Integer qrcodeRequired;
    public String remark;
}
