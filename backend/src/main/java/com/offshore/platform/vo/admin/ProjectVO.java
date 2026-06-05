package com.offshore.platform.vo.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectVO {
    public Long id;
    public String projectCode;
    public String projectName;
    public String platformName;
    public String ownerUnit;
    public String contractorUnit;
    public Long projectManagerId;
    public String projectLocation;
    public LocalDate startDate;
    public LocalDate endDate;
    public String projectStatus;
    public Integer sortOrder;
    public Integer version;
    public String syncStatus;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public String remark;
}
