package com.offshore.platform.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WorkOrderTemplateVO {
    public Long id;
    public String templateCode;
    public String templateName;
    public String workType;
    public String defaultPriority;
    public String defaultWorkContent;
    public String defaultMaterialDesc;
    public BigDecimal defaultDurationHours;
    public Integer enabledFlag;
    public Integer version;
    public String syncStatus;
    public LocalDateTime createdAt;
    public String remark;
}
