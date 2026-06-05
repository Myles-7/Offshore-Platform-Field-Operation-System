package com.offshore.platform.service.impl;

import com.offshore.platform.entity.SysRolePermission;
import com.offshore.platform.mapper.SysRolePermissionMapper;
import com.offshore.platform.service.SysRolePermissionService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * sys_role_permission 基础Service实现。
 */
@Service
public class SysRolePermissionServiceImpl implements SysRolePermissionService {
    private final SysRolePermissionMapper sysRolePermissionMapper;

    public SysRolePermissionServiceImpl(SysRolePermissionMapper sysRolePermissionMapper) {
        this.sysRolePermissionMapper = sysRolePermissionMapper;
    }

    @Override
    public int create(SysRolePermission sysRolePermission) {
        return sysRolePermissionMapper.insert(sysRolePermission);
    }

    @Override
    public int update(SysRolePermission sysRolePermission) {
        return sysRolePermissionMapper.updateById(sysRolePermission);
    }

    @Override
    public SysRolePermission getById(Long id) {
        return sysRolePermissionMapper.selectById(id);
    }

    @Override
    public List<SysRolePermission> listAll() {
        return sysRolePermissionMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return sysRolePermissionMapper.softDeleteById(id);
    }
}
