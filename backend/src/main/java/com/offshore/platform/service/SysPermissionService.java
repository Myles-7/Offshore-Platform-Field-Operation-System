package com.offshore.platform.service;

import com.offshore.platform.entity.SysPermission;
import java.util.List;

/**
 * sys_permission 基础Service。
 */
public interface SysPermissionService {
    int create(SysPermission sysPermission);

    int update(SysPermission sysPermission);

    SysPermission getById(Long id);

    List<SysPermission> listAll();

    int removeById(Long id);
}
