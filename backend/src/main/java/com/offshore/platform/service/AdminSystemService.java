package com.offshore.platform.service;

import com.offshore.platform.common.page.PageRequestDTO;
import com.offshore.platform.common.page.PageResult;
import com.offshore.platform.dto.admin.AdminUserCreateRequest;
import com.offshore.platform.dto.admin.AdminUserUpdateRequest;
import com.offshore.platform.dto.admin.PermissionIdsRequest;
import com.offshore.platform.dto.admin.RoleIdsRequest;
import com.offshore.platform.vo.admin.AdminUserVO;
import com.offshore.platform.vo.admin.PermissionVO;
import com.offshore.platform.vo.admin.RoleVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface AdminSystemService {
    PageResult<AdminUserVO> listUsers(PageRequestDTO pageRequest);

    AdminUserVO getUser(Long id);

    AdminUserVO createUser(AdminUserCreateRequest request, HttpServletRequest servletRequest);

    AdminUserVO updateUser(Long id, AdminUserUpdateRequest request, HttpServletRequest servletRequest);

    void deleteUser(Long id, HttpServletRequest servletRequest);

    List<RoleVO> listRoles();

    List<PermissionVO> listPermissions();

    void assignUserRoles(Long userId, RoleIdsRequest request, HttpServletRequest servletRequest);

    void assignRolePermissions(Long roleId, PermissionIdsRequest request, HttpServletRequest servletRequest);
}
