package com.offshore.platform.service;

import com.offshore.platform.entity.SysRole;
import java.util.List;

/**
 * sys_role 基础Service。
 */
public interface SysRoleService {
    int create(SysRole sysRole);

    int update(SysRole sysRole);

    SysRole getById(Long id);

    List<SysRole> listAll();

    int removeById(Long id);
}
