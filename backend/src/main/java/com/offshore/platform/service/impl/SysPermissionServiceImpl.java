package com.offshore.platform.service.impl;

import com.offshore.platform.entity.SysPermission;
import com.offshore.platform.mapper.SysPermissionMapper;
import com.offshore.platform.service.SysPermissionService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * sys_permission 基础Service实现。
 */
@Service
public class SysPermissionServiceImpl implements SysPermissionService {
    private final SysPermissionMapper sysPermissionMapper;

    public SysPermissionServiceImpl(SysPermissionMapper sysPermissionMapper) {
        this.sysPermissionMapper = sysPermissionMapper;
    }

    @Override
    public int create(SysPermission sysPermission) {
        return sysPermissionMapper.insert(sysPermission);
    }

    @Override
    public int update(SysPermission sysPermission) {
        return sysPermissionMapper.updateById(sysPermission);
    }

    @Override
    public SysPermission getById(Long id) {
        return sysPermissionMapper.selectById(id);
    }

    @Override
    public List<SysPermission> listAll() {
        return sysPermissionMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return sysPermissionMapper.softDeleteById(id);
    }
}
