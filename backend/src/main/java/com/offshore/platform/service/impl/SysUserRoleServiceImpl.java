package com.offshore.platform.service.impl;

import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.mapper.SysUserRoleMapper;
import com.offshore.platform.service.SysUserRoleService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * sys_user_role 基础Service实现。
 */
@Service
public class SysUserRoleServiceImpl implements SysUserRoleService {
    private final SysUserRoleMapper sysUserRoleMapper;

    public SysUserRoleServiceImpl(SysUserRoleMapper sysUserRoleMapper) {
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    @Override
    public int create(SysUserRole sysUserRole) {
        return sysUserRoleMapper.insert(sysUserRole);
    }

    @Override
    public int update(SysUserRole sysUserRole) {
        return sysUserRoleMapper.updateById(sysUserRole);
    }

    @Override
    public SysUserRole getById(Long id) {
        return sysUserRoleMapper.selectById(id);
    }

    @Override
    public List<SysUserRole> listAll() {
        return sysUserRoleMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return sysUserRoleMapper.softDeleteById(id);
    }
}
