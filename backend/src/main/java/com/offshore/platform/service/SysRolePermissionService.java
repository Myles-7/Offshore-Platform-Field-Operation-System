package com.offshore.platform.service;

import com.offshore.platform.entity.SysRolePermission;
import java.util.List;

/**
 * sys_role_permission 基础Service。
 */
public interface SysRolePermissionService {
    int create(SysRolePermission sysRolePermission);

    int update(SysRolePermission sysRolePermission);

    SysRolePermission getById(Long id);

    List<SysRolePermission> listAll();

    int removeById(Long id);
}
