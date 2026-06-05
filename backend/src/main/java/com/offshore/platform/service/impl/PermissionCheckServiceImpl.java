package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.service.PermissionCheckService;
import org.springframework.stereotype.Service;

@Service
public class PermissionCheckServiceImpl implements PermissionCheckService {
    @Override
    public boolean hasRole(CurrentUser currentUser, String roleCode) {
        return currentUser != null && currentUser.getRoleCodes().stream().anyMatch(item -> sameRole(item, roleCode));
    }

    @Override
    public boolean hasAnyRole(CurrentUser currentUser, String... roleCodes) {
        if (currentUser == null || roleCodes == null) {
            return false;
        }
        for (String roleCode : roleCodes) {
            if (hasRole(currentUser, roleCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(CurrentUser currentUser, String permissionCode) {
        return currentUser != null && currentUser.getPermissionCodes().contains(permissionCode);
    }

    @Override
    public void requireRole(CurrentUser currentUser, String roleCode) {
        if (!hasRole(currentUser, roleCode)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    @Override
    public void requireAnyRole(CurrentUser currentUser, String... roleCodes) {
        if (!hasAnyRole(currentUser, roleCodes)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    @Override
    public void requirePermission(CurrentUser currentUser, String permissionCode) {
        if (!hasPermission(currentUser, permissionCode)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean sameRole(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        if (actual.equals(expected)) {
            return true;
        }
        return ("SYSTEM_ADMIN".equals(actual) && "SYS_ADMIN".equals(expected))
                || ("SYS_ADMIN".equals(actual) && "SYSTEM_ADMIN".equals(expected))
                || ("MAINTAINER".equals(actual) && "WORKER".equals(expected))
                || ("WORKER".equals(actual) && "MAINTAINER".equals(expected));
    }
}
