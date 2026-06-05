package com.offshore.platform.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class ProjectRequest {
    @NotBlank(message = "项目编号不能为空")
    @Size(max = 64, message = "项目编号长度不能超过64")
    public String projectCode;

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 128, message = "项目名称长度不能超过128")
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
    public String remark;
}
