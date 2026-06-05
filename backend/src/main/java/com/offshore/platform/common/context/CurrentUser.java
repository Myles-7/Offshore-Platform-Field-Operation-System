package com.offshore.platform.common.context;

import java.util.Collections;
import java.util.List;

public class CurrentUser {
    private Long userId;
    private String username;
    private String realName;
    private List<String> roleCodes = Collections.emptyList();
    private List<String> permissionCodes = Collections.emptyList();
    private String dataScope;
    private Long primaryProjectId;
    private Integer pcEnabled;
    private Integer mobileEnabled;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public Long getPrimaryProjectId() {
        return primaryProjectId;
    }

    public void setPrimaryProjectId(Long primaryProjectId) {
        this.primaryProjectId = primaryProjectId;
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
}
