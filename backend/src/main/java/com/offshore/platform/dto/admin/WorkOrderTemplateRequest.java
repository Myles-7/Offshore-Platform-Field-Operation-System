package com.offshore.platform.dto.admin;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class WorkOrderTemplateRequest {
    @NotBlank(message = "模板编号不能为空")
    public String templateCode;

    @NotBlank(message = "模板名称不能为空")
    public String templateName;

    public String workType;
    public String defaultPriority;
    public String defaultWorkContent;
    public String defaultMaterialDesc;
    public BigDecimal defaultDurationHours;
    public Integer enabledFlag;
    public String remark;
}
