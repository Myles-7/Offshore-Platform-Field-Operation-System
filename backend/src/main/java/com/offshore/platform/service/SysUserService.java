package com.offshore.platform.service;

import com.offshore.platform.entity.SysUser;
import java.util.List;

/**
 * sys_user 基础Service。
 */
public interface SysUserService {
    int create(SysUser sysUser);

    int update(SysUser sysUser);

    SysUser getById(Long id);

    List<SysUser> listAll();

    int removeById(Long id);
}
