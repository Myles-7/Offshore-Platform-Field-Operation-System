package com.offshore.platform.dto.qualification;

import jakarta.validation.constraints.NotBlank;

public class EmployeeRequest {
    public Long userId;
    @NotBlank(message = "员工编号不能为空")
    public String employeeNo;
    @NotBlank(message = "员工姓名不能为空")
    public String realName;
    public String phone;
    public String idCardHash;
    public Long departmentId;
    public String positionName;
    public String employeeStatus;
    public String remark;
}
