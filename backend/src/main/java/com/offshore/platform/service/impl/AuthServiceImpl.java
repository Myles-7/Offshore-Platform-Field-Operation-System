package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.JwtTokenUtil;
import com.offshore.platform.common.util.PasswordHashUtil;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.SysPermission;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.SysPermissionMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.service.AuthService;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.vo.auth.CurrentUserVO;
import com.offshore.platform.vo.auth.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final OperationLogMapper operationLogMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final DataScopeService dataScopeService;
    private final PasswordHashUtil passwordHashUtil;

    public AuthServiceImpl(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper,
            SysPermissionMapper sysPermissionMapper, OperationLogMapper operationLogMapper,
            JwtTokenUtil jwtTokenUtil, DataScopeService dataScopeService, PasswordHashUtil passwordHashUtil) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.operationLogMapper = operationLogMapper;
        this.jwtTokenUtil = jwtTokenUtil;
        this.dataScopeService = dataScopeService;
        this.passwordHashUtil = passwordHashUtil;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        SysUser user = sysUserMapper.selectByUsernameOrPhone(request.getLoginName());
        if (user == null || !passwordHashUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.USER_ERROR, "用户名或密码错误");
        }
        if (!"ACTIVE".equals(user.getAccountStatus())) {
            throw new BusinessException(ErrorCode.USER_ERROR, "账号状态不可用");
        }
        String platform = normalizePlatform(request.getPlatform());
        if ("MOBILE".equals(platform) && !Integer.valueOf(1).equals(user.getMobileEnabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号不允许登录移动端");
        }
        if (!"MOBILE".equals(platform) && !Integer.valueOf(1).equals(user.getPcEnabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号不允许登录PC后台");
        }

        CurrentUser currentUser = buildCurrentUser(user);
        currentUser.setDataScope(dataScopeService.resolveDataScope(currentUser));
        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername());

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(clientIp(servletRequest));
        sysUserMapper.updateById(user);
        writeLoginLog(user, currentUser, platform, servletRequest, "SUCCESS", null);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRoleCodes(currentUser.getRoleCodes());
        response.setPermissionCodes(currentUser.getPermissionCodes());
        response.setDataScope(currentUser.getDataScope());
        response.setPrimaryProjectId(user.getPrimaryProjectId());
        return response;
    }

    @Override
    public void logout(HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.get();
        if (currentUser != null) {
            OperationLog log = baseLog(servletRequest, "BOTH");
            log.setOperatorId(currentUser.getUserId());
            log.setOperatorName(currentUser.getRealName());
            log.setRoleCode(String.join(",", currentUser.getRoleCodes()));
            log.setModuleName("AUTH");
            log.setOperationType("LOGOUT");
            log.setBusinessType("AUTH");
            log.setBusinessId(String.valueOf(currentUser.getUserId()));
            log.setBusinessNo(currentUser.getUsername());
            log.setResultStatus("SUCCESS");
            operationLogMapper.insert(log);
        }
    }

    @Override
    public CurrentUserVO current() {
        return toCurrentUserVO(CurrentUserContext.require());
    }

    @Override
    public CurrentUser loadCurrentUserByToken(String token) {
        Long userId = jwtTokenUtil.parseUserId(token);
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !"ACTIVE".equals(user.getAccountStatus())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        CurrentUser currentUser = buildCurrentUser(user);
        currentUser.setDataScope(dataScopeService.resolveDataScope(currentUser));
        return currentUser;
    }

    private CurrentUser buildCurrentUser(SysUser user) {
        List<SysRole> roles = sysRoleMapper.selectByUserId(user.getId());
        List<SysPermission> permissions = sysPermissionMapper.selectByUserId(user.getId());
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(user.getId());
        currentUser.setUsername(user.getUsername());
        currentUser.setRealName(user.getRealName());
        currentUser.setPrimaryProjectId(user.getPrimaryProjectId());
        currentUser.setPcEnabled(user.getPcEnabled());
        currentUser.setMobileEnabled(user.getMobileEnabled());
        currentUser.setRoleCodes(roles.stream().map(SysRole::getRoleCode).toList());
        currentUser.setPermissionCodes(permissions.stream().map(SysPermission::getPermissionCode).toList());
        currentUser.setDataScope(roles.stream().map(SysRole::getDataScope).filter(StringUtils::hasText).findFirst().orElse("SELF"));
        return currentUser;
    }

    private CurrentUserVO toCurrentUserVO(CurrentUser currentUser) {
        CurrentUserVO vo = new CurrentUserVO();
        vo.setUserId(currentUser.getUserId());
        vo.setUsername(currentUser.getUsername());
        vo.setRealName(currentUser.getRealName());
        vo.setRoleCodes(currentUser.getRoleCodes());
        vo.setPermissionCodes(currentUser.getPermissionCodes());
        vo.setDataScope(currentUser.getDataScope());
        vo.setPrimaryProjectId(currentUser.getPrimaryProjectId());
        return vo;
    }

    private void writeLoginLog(SysUser user, CurrentUser currentUser, String platform, HttpServletRequest request,
            String resultStatus, String errorMessage) {
        OperationLog log = baseLog(request, platform);
        log.setOperatorId(user.getId());
        log.setOperatorName(user.getRealName());
        log.setRoleCode(String.join(",", currentUser.getRoleCodes()));
        log.setModuleName("AUTH");
        log.setOperationType("LOGIN");
        log.setBusinessType("AUTH");
        log.setBusinessId(String.valueOf(user.getId()));
        log.setBusinessNo(user.getUsername());
        log.setResultStatus(resultStatus);
        log.setErrorMessage(errorMessage);
        operationLogMapper.insert(log);
    }

    private OperationLog baseLog(HttpServletRequest request, String platform) {
        OperationLog log = new OperationLog();
        log.setTraceId(TraceIdUtils.currentTraceId());
        log.setPlatform(platform);
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(request.getRequestURI());
        log.setRequestIp(clientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setOperationTime(LocalDateTime.now());
        log.setDeletedFlag(0);
        return log;
    }

    private String normalizePlatform(String platform) {
        if (!StringUtils.hasText(platform)) {
            return "PC";
        }
        return platform.toUpperCase(Locale.ROOT);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
