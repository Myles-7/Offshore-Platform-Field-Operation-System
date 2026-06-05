package com.offshore.platform.dto.admin;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class RoleIdsRequest {
    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds = new ArrayList<>();

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
