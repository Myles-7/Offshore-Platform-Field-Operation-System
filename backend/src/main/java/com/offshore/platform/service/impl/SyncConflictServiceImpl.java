package com.offshore.platform.service.impl;

import com.offshore.platform.entity.SyncConflict;
import com.offshore.platform.mapper.SyncConflictMapper;
import com.offshore.platform.service.SyncConflictService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * sync_conflict 基础Service实现。
 */
@Service
public class SyncConflictServiceImpl implements SyncConflictService {
    private final SyncConflictMapper syncConflictMapper;

    public SyncConflictServiceImpl(SyncConflictMapper syncConflictMapper) {
        this.syncConflictMapper = syncConflictMapper;
    }

    @Override
    public int create(SyncConflict syncConflict) {
        return syncConflictMapper.insert(syncConflict);
    }

    @Override
    public int update(SyncConflict syncConflict) {
        return syncConflictMapper.updateById(syncConflict);
    }

    @Override
    public SyncConflict getById(Long id) {
        return syncConflictMapper.selectById(id);
    }

    @Override
    public List<SyncConflict> listAll() {
        return syncConflictMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return syncConflictMapper.softDeleteById(id);
    }
}
