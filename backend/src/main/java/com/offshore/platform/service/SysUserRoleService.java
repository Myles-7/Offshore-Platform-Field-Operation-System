package com.offshore.platform.service;

import com.offshore.platform.entity.SysUserRole;
import java.util.List;

/**
 * sys_user_role 基础Service。
 */
public interface SysUserRoleService {
    int create(SysUserRole sysUserRole);

    int update(SysUserRole sysUserRole);

    SysUserRole getById(Long id);

    List<SysUserRole> listAll();

    int removeById(Long id);
}
