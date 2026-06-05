package com.offshore.platform.vo.admin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminUserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String employeeNo;
    private String accountStatus;
    private Integer pcEnabled;
    private Integer mobileEnabled;
    private Long primaryProjectId;
    private Long departmentId;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private String remark;
    private List<Long> roleIds = new ArrayList<>();
    private List<String> roleCodes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}
