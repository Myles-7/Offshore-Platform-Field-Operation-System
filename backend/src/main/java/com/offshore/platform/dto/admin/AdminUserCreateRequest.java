package com.offshore.platform.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminUserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过64")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须在6到64之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 64, message = "真实姓名长度不能超过64")
    private String realName;

    @Size(max = 32, message = "手机号长度不能超过32")
    private String phone;

    @Size(max = 128, message = "邮箱长度不能超过128")
    private String email;

    @Size(max = 64, message = "员工编号长度不能超过64")
    private String employeeNo;

    private String accountStatus;
    private Integer pcEnabled;
    private Integer mobileEnabled;
    private Long primaryProjectId;
    private Long departmentId;
    private String remark;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Integer getPcEnabled() {
        return pcEnabled;
    }

    public void setPcEnabled(Integer pcEnabled) {
        this.pcEnabled = pcEnabled;
    }

    public Integer getMobileEnabled() {
        return mobileEnabled;
    }

    public void setMobileEnabled(Integer mobileEnabled) {
        this.mobileEnabled = mobileEnabled;
    }

    public Long getPrimaryProjectId() {
        return primaryProjectId;
    }

    public void setPrimaryProjectId(Long primaryProjectId) {
        this.primaryProjectId = primaryProjectId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
