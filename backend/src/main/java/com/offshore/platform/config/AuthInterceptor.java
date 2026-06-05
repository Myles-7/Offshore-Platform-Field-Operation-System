package com.offshore.platform.config;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.entity.DeviceInfo;
import com.offshore.platform.mapper.DeviceInfoMapper;
import com.offshore.platform.service.AuthService;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.PermissionCheckService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;
    private final PermissionCheckService permissionCheckService;
    private final DataScopeService dataScopeService;
    private final DeviceInfoMapper deviceInfoMapper;

    public AuthInterceptor(AuthService authService, PermissionCheckService permissionCheckService,
            DataScopeService dataScopeService, DeviceInfoMapper deviceInfoMapper) {
        this.authService = authService;
        this.permissionCheckService = permissionCheckService;
        this.dataScopeService = dataScopeService;
        this.deviceInfoMapper = deviceInfoMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        CurrentUser currentUser = authService.loadCurrentUserByToken(authorization.substring("Bearer ".length()));
        CurrentUserContext.set(currentUser);
        checkEndpointPermission(request, currentUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserContext.clear();
    }

    private void checkEndpointPermission(HttpServletRequest request, CurrentUser currentUser) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/")) {
            requirePcEnabled(currentUser);
            requireAdminModuleRole(path, currentUser);
        } else if (path.startsWith("/api/mobile/")) {
            requireMobileEnabled(currentUser);
        } else if (path.startsWith("/api/sync/")) {
            requireMobileEnabled(currentUser);
            validateSyncDevice(request, currentUser);
        } else if (path.startsWith("/api/files/")) {
            requireAnyEndpointEnabled(currentUser);
        } else if (path.startsWith("/api/ai/")) {
            requireAnyEndpointEnabled(currentUser);
        } else if (path.startsWith("/api/auth/")) {
            requireAnyEndpointEnabled(currentUser);
        }
    }

    private void requirePcEnabled(CurrentUser currentUser) {
        if (!Integer.valueOf(1).equals(currentUser.getPcEnabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is not enabled for PC backend");
        }
    }

    private void requireMobileEnabled(CurrentUser currentUser) {
        if (!Integer.valueOf(1).equals(currentUser.getMobileEnabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is not enabled for mobile client");
        }
    }

    private void requireAnyEndpointEnabled(CurrentUser currentUser) {
        if (!Integer.valueOf(1).equals(currentUser.getPcEnabled())
                && !Integer.valueOf(1).equals(currentUser.getMobileEnabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account endpoint is disabled");
        }
    }

    private void requireAdminModuleRole(String path, CurrentUser currentUser) {
        if (dataScopeService.canAccessAll(currentUser)) {
            return;
        }
        if (path.startsWith("/api/admin/users") || path.startsWith("/api/admin/roles")
                || path.startsWith("/api/admin/permissions") || path.startsWith("/api/admin/operation-logs")) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (path.startsWith("/api/admin/materials")) {
            permissionCheckService.requireAnyRole(currentUser, "MATERIAL_MANAGER");
            return;
        }
        if (path.startsWith("/api/admin/employees") || path.startsWith("/api/admin/certificates")) {
            permissionCheckService.requireAnyRole(currentUser, "QUALIFICATION_MANAGER");
            return;
        }
        if (path.startsWith("/api/admin/dashboard") || path.startsWith("/api/admin/reports")) {
            permissionCheckService.requireAnyRole(currentUser, "BUSINESS_USER", "PROJECT_MANAGER");
            return;
        }
        if (path.startsWith("/api/admin/ai")) {
            permissionCheckService.requireAnyRole(currentUser, "PROJECT_MANAGER", "ACCEPTOR");
            return;
        }
        if (path.startsWith("/api/admin/sync")) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (path.contains("/material-usage")) {
            permissionCheckService.requireAnyRole(currentUser, "MATERIAL_MANAGER", "PROJECT_MANAGER", "DISPATCHER",
                    "SITE_LEADER", "ACCEPTOR");
            return;
        }
        if (path.contains("/qualification-candidates")) {
            permissionCheckService.requireAnyRole(currentUser, "QUALIFICATION_MANAGER", "PROJECT_MANAGER",
                    "DISPATCHER");
            return;
        }
        if (path.startsWith("/api/admin/projects") || path.startsWith("/api/admin/work-orders")
                || path.startsWith("/api/admin/work-order-templates") || path.startsWith("/api/admin/work-records")) {
            permissionCheckService.requireAnyRole(currentUser, "PROJECT_MANAGER", "DISPATCHER", "SITE_LEADER", "ACCEPTOR");
        }
    }

    private void validateSyncDevice(HttpServletRequest request, CurrentUser currentUser) {
        String deviceId = request.getParameter("deviceId");
        if (!StringUtils.hasText(deviceId)) {
            deviceId = request.getHeader("X-Device-Id");
        }
        if (!StringUtils.hasText(deviceId) && request.getContentType() != null
                && request.getContentType().contains("application/json")) {
            return;
        }
        if (!StringUtils.hasText(deviceId)) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "deviceId is required");
        }
        validateDeviceOwner(deviceId, currentUser, request.getRequestURI().endsWith("/device/register"));
    }

    private void validateDeviceOwner(String deviceId, CurrentUser currentUser, boolean allowNewDevice) {
        DeviceInfo device = deviceInfoMapper.selectAll().stream()
                .filter(item -> deviceId.equals(item.getDeviceId()))
                .findFirst()
                .orElse(null);
        if (device == null && allowNewDevice) {
            return;
        }
        if (device == null) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "Device is not registered");
        }
        if (!currentUser.getUserId().equals(device.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Device does not belong to current user");
        }
        if (!"ACTIVE".equals(device.getDeviceStatus()) || !Integer.valueOf(1).equals(device.getSyncEnabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Device sync is disabled");
        }
    }
}
