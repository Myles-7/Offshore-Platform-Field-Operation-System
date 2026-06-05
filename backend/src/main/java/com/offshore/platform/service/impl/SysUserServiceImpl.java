package com.offshore.platform.service.impl;

import com.offshore.platform.entity.SysUser;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.service.SysUserService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * sys_user 基础Service实现。
 */
@Service
public class SysUserServiceImpl implements SysUserService {
    private final SysUserMapper sysUserMapper;

    public SysUserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public int create(SysUser sysUser) {
        return sysUserMapper.insert(sysUser);
    }

    @Override
    public int update(SysUser sysUser) {
        return sysUserMapper.updateById(sysUser);
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    public List<SysUser> listAll() {
        return sysUserMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return sysUserMapper.softDeleteById(id);
    }
}
