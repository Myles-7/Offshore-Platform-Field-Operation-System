package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.page.PageRequestDTO;
import com.offshore.platform.common.page.PageResult;
import com.offshore.platform.common.util.PasswordHashUtil;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.admin.AdminUserCreateRequest;
import com.offshore.platform.dto.admin.AdminUserUpdateRequest;
import com.offshore.platform.dto.admin.PermissionIdsRequest;
import com.offshore.platform.dto.admin.RoleIdsRequest;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.SysPermission;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysRolePermission;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.SysPermissionMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysRolePermissionMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
import com.offshore.platform.service.AdminSystemService;
import com.offshore.platform.service.PermissionCheckService;
import com.offshore.platform.vo.admin.AdminUserVO;
import com.offshore.platform.vo.admin.PermissionVO;
import com.offshore.platform.vo.admin.RoleVO;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminSystemServiceImpl implements AdminSystemService {
    private static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final OperationLogMapper operationLogMapper;
    private final PermissionCheckService permissionCheckService;
    private final PasswordHashUtil passwordHashUtil;

    public AdminSystemServiceImpl(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper,
            SysPermissionMapper sysPermissionMapper, SysUserRoleMapper sysUserRoleMapper,
            SysRolePermissionMapper sysRolePermissionMapper, OperationLogMapper operationLogMapper,
            PermissionCheckService permissionCheckService, PasswordHashUtil passwordHashUtil) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.operationLogMapper = operationLogMapper;
        this.permissionCheckService = permissionCheckService;
        this.passwordHashUtil = passwordHashUtil;
    }

    @Override
    public PageResult<AdminUserVO> listUsers(PageRequestDTO pageRequest) {
        requireSystemAdmin();
        int pageNum = pageRequest.getPageNum() == null ? 1 : pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize() == null ? 10 : pageRequest.getPageSize();
        int offset = (pageNum - 1) * pageSize;
        String keyword = normalizeKeyword(pageRequest.getKeyword());
        long total = sysUserMapper.countPage(keyword);
        List<AdminUserVO> records = sysUserMapper.selectPage(keyword, offset, pageSize)
                .stream()
                .map(this::toUserVO)
                .toList();
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    public AdminUserVO getUser(Long id) {
        requireSystemAdmin();
        return toUserVO(requireUser(id));
    }

    @Override
    @Transactional
    public AdminUserVO createUser(AdminUserCreateRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = requireSystemAdmin();
        ensureUsernameAvailable(request.getUsername(), null);
        ensurePhoneAvailable(request.getPhone(), null);

        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordHashUtil.hash(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(emptyToNull(request.getPhone()));
        user.setEmail(emptyToNull(request.getEmail()));
        user.setEmployeeNo(emptyToNull(request.getEmployeeNo()));
        user.setAccountStatus(defaultText(request.getAccountStatus(), "ACTIVE"));
        user.setPcEnabled(defaultInt(request.getPcEnabled(), 1));
        user.setMobileEnabled(defaultInt(request.getMobileEnabled(), 1));
        user.setPrimaryProjectId(request.getPrimaryProjectId());
        user.setDepartmentId(request.getDepartmentId());
        user.setPasswordUpdatedAt(now);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeletedFlag(0);
        user.setCreatedBy(currentUser.getUserId());
        user.setUpdatedBy(currentUser.getUserId());
        user.setRemark(request.getRemark());
        sysUserMapper.insert(user);
        writeOperationLog(currentUser, servletRequest, "CREATE_USER", "SYS_USER", user.getId(), user.getUsername());
        return toUserVO(user);
    }

    @Override
    @Transactional
    public AdminUserVO updateUser(Long id, AdminUserUpdateRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = requireSystemAdmin();
        SysUser user = requireUser(id);
        ensurePhoneAvailable(request.getPhone(), id);

        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            user.setPhone(emptyToNull(request.getPhone()));
        }
        if (request.getEmail() != null) {
            user.setEmail(emptyToNull(request.getEmail()));
        }
        if (request.getEmployeeNo() != null) {
            user.setEmployeeNo(emptyToNull(request.getEmployeeNo()));
        }
        user.setAccountStatus(defaultText(request.getAccountStatus(), user.getAccountStatus()));
        user.setPcEnabled(defaultInt(request.getPcEnabled(), user.getPcEnabled()));
        user.setMobileEnabled(defaultInt(request.getMobileEnabled(), user.getMobileEnabled()));
        if (request.getPrimaryProjectId() != null) {
            user.setPrimaryProjectId(request.getPrimaryProjectId());
        }
        if (request.getDepartmentId() != null) {
            user.setDepartmentId(request.getDepartmentId());
        }
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUser.getUserId());
        user.setRemark(request.getRemark());
        sysUserMapper.updateById(user);
        writeOperationLog(currentUser, servletRequest, "UPDATE_USER", "SYS_USER", id, user.getUsername());
        return toUserVO(sysUserMapper.selectById(id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id, HttpServletRequest servletRequest) {
        CurrentUser currentUser = requireSystemAdmin();
        SysUser user = requireUser(id);
        if (currentUser.getUserId().equals(id)) {
            throw new BusinessException(ErrorCode.USER_ERROR, "不能删除当前登录用户");
        }
        sysUserMapper.softDeleteById(id);
        writeOperationLog(currentUser, servletRequest, "DELETE_USER", "SYS_USER", id, user.getUsername());
    }

    @Override
    public List<RoleVO> listRoles() {
        requireSystemAdmin();
        return sysRoleMapper.selectAll().stream().map(this::toRoleVO).toList();
    }

    @Override
    public List<PermissionVO> listPermissions() {
        requireSystemAdmin();
        return sysPermissionMapper.selectAll().stream().map(this::toPermissionVO).toList();
    }

    @Override
    @Transactional
    public void assignUserRoles(Long userId, RoleIdsRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = requireSystemAdmin();
        SysUser user = requireUser(userId);
        Set<Long> roleIds = distinctIds(request.getRoleIds());
        for (Long roleId : roleIds) {
            if (sysRoleMapper.selectById(roleId) == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在：" + roleId);
            }
        }
        sysUserRoleMapper.softDeleteByUserId(userId, currentUser.getUserId());
        for (Long roleId : roleIds) {
            upsertUserRole(userId, roleId, currentUser.getUserId());
        }
        writeOperationLog(currentUser, servletRequest, "ASSIGN_USER_ROLE", "SYS_USER", userId, user.getUsername());
    }

    @Override
    @Transactional
    public void assignRolePermissions(Long roleId, PermissionIdsRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = requireSystemAdmin();
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        Set<Long> permissionIds = distinctIds(request.getPermissionIds());
        for (Long permissionId : permissionIds) {
            if (sysPermissionMapper.selectById(permissionId) == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "权限不存在：" + permissionId);
            }
        }
        sysRolePermissionMapper.softDeleteByRoleId(roleId, currentUser.getUserId());
        for (Long permissionId : permissionIds) {
            upsertRolePermission(roleId, permissionId, currentUser.getUserId());
        }
        writeOperationLog(currentUser, servletRequest, "ASSIGN_ROLE_PERMISSION", "SYS_ROLE", roleId, role.getRoleCode());
    }

    private CurrentUser requireSystemAdmin() {
        CurrentUser currentUser = CurrentUserContext.require();
        permissionCheckService.requireRole(currentUser, SYSTEM_ADMIN);
        return currentUser;
    }

    private SysUser requireUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void ensureUsernameAvailable(String username, Long selfId) {
        SysUser exists = sysUserMapper.selectByUsername(username);
        if (exists != null && !exists.getId().equals(selfId)) {
            throw new BusinessException(ErrorCode.USER_ERROR, "用户名已存在");
        }
    }

    private void ensurePhoneAvailable(String phone, Long selfId) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        SysUser exists = sysUserMapper.selectByPhone(phone);
        if (exists != null && !exists.getId().equals(selfId)) {
            throw new BusinessException(ErrorCode.USER_ERROR, "手机号已存在");
        }
    }

    private void upsertUserRole(Long userId, Long roleId, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        SysUserRole exists = sysUserRoleMapper.selectByUserIdAndRoleId(userId, roleId);
        if (exists == null) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setCreatedAt(now);
            relation.setUpdatedAt(now);
            relation.setDeletedFlag(0);
            relation.setCreatedBy(operatorId);
            relation.setUpdatedBy(operatorId);
            sysUserRoleMapper.insert(relation);
            return;
        }
        exists.setDeletedFlag(0);
        exists.setUpdatedAt(now);
        exists.setUpdatedBy(operatorId);
        sysUserRoleMapper.updateById(exists);
    }

    private void upsertRolePermission(Long roleId, Long permissionId, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        SysRolePermission exists = sysRolePermissionMapper.selectByRoleIdAndPermissionId(roleId, permissionId);
        if (exists == null) {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            relation.setCreatedAt(now);
            relation.setUpdatedAt(now);
            relation.setDeletedFlag(0);
            relation.setCreatedBy(operatorId);
            relation.setUpdatedBy(operatorId);
            sysRolePermissionMapper.insert(relation);
            return;
        }
        exists.setDeletedFlag(0);
        exists.setUpdatedAt(now);
        exists.setUpdatedBy(operatorId);
        sysRolePermissionMapper.updateById(exists);
    }

    private AdminUserVO toUserVO(SysUser user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setEmployeeNo(user.getEmployeeNo());
        vo.setAccountStatus(user.getAccountStatus());
        vo.setPcEnabled(user.getPcEnabled());
        vo.setMobileEnabled(user.getMobileEnabled());
        vo.setPrimaryProjectId(user.getPrimaryProjectId());
        vo.setDepartmentId(user.getDepartmentId());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setRemark(user.getRemark());
        List<SysRole> roles = sysRoleMapper.selectByUserId(user.getId());
        vo.setRoleIds(roles.stream().map(SysRole::getId).toList());
        vo.setRoleCodes(roles.stream().map(SysRole::getRoleCode).toList());
        return vo;
    }

    private RoleVO toRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setRoleType(role.getRoleType());
        vo.setDataScope(role.getDataScope());
        vo.setPcEnabled(role.getPcEnabled());
        vo.setMobileEnabled(role.getMobileEnabled());
        vo.setSortOrder(role.getSortOrder());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        return vo;
    }

    private PermissionVO toPermissionVO(SysPermission permission) {
        PermissionVO vo = new PermissionVO();
        vo.setId(permission.getId());
        vo.setParentId(permission.getParentId());
        vo.setPermissionCode(permission.getPermissionCode());
        vo.setPermissionName(permission.getPermissionName());
        vo.setPermissionType(permission.getPermissionType());
        vo.setPlatform(permission.getPlatform());
        vo.setRoutePath(permission.getRoutePath());
        vo.setApiMethod(permission.getApiMethod());
        vo.setApiPath(permission.getApiPath());
        vo.setComponentPath(permission.getComponentPath());
        vo.setIcon(permission.getIcon());
        vo.setSortOrder(permission.getSortOrder());
        vo.setVisibleFlag(permission.getVisibleFlag());
        vo.setStatus(permission.getStatus());
        vo.setRemark(permission.getRemark());
        return vo;
    }

    private void writeOperationLog(CurrentUser currentUser, HttpServletRequest request, String operationType,
            String businessType, Long businessId, String businessNo) {
        OperationLog log = new OperationLog();
        log.setTraceId(TraceIdUtils.currentTraceId());
        log.setOperatorId(currentUser.getUserId());
        log.setOperatorName(currentUser.getRealName());
        log.setRoleCode(String.join(",", currentUser.getRoleCodes()));
        log.setPlatform("PC");
        log.setModuleName("SYSTEM");
        log.setOperationType(operationType);
        log.setBusinessType(businessType);
        log.setBusinessId(String.valueOf(businessId));
        log.setBusinessNo(businessNo);
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(request.getRequestURI());
        log.setRequestIp(clientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setResultStatus("SUCCESS");
        log.setOperationTime(LocalDateTime.now());
        log.setDeletedFlag(0);
        log.setCreatedBy(currentUser.getUserId());
        log.setUpdatedBy(currentUser.getUserId());
        operationLogMapper.insert(log);
    }

    private Set<Long> distinctIds(List<Long> ids) {
        if (ids == null) {
            return Set.of();
        }
        List<Long> validIds = new ArrayList<>();
        for (Long id : ids) {
            if (id != null) {
                validIds.add(id);
            }
        }
        return new LinkedHashSet<>(validIds);
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private Integer defaultInt(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
