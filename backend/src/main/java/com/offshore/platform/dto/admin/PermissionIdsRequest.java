package com.offshore.platform.dto.admin;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class PermissionIdsRequest {
    @NotNull(message = "权限ID列表不能为空")
    private List<Long> permissionIds = new ArrayList<>();

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
