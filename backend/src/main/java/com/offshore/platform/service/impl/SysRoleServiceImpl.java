package com.offshore.platform.service.impl;

import com.offshore.platform.entity.SysRole;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.service.SysRoleService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * sys_role 基础Service实现。
 */
@Service
public class SysRoleServiceImpl implements SysRoleService {
    private final SysRoleMapper sysRoleMapper;

    public SysRoleServiceImpl(SysRoleMapper sysRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public int create(SysRole sysRole) {
        return sysRoleMapper.insert(sysRole);
    }

    @Override
    public int update(SysRole sysRole) {
        return sysRoleMapper.updateById(sysRole);
    }

    @Override
    public SysRole getById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    @Override
    public List<SysRole> listAll() {
        return sysRoleMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return sysRoleMapper.softDeleteById(id);
    }
}
