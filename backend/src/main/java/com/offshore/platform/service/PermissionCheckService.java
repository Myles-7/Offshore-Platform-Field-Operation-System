package com.offshore.platform.service;

import com.offshore.platform.common.context.CurrentUser;

public interface PermissionCheckService {
    boolean hasRole(CurrentUser currentUser, String roleCode);

    boolean hasAnyRole(CurrentUser currentUser, String... roleCodes);

    boolean hasPermission(CurrentUser currentUser, String permissionCode);

    void requireRole(CurrentUser currentUser, String roleCode);

    void requireAnyRole(CurrentUser currentUser, String... roleCodes);

    void requirePermission(CurrentUser currentUser, String permissionCode);
}
