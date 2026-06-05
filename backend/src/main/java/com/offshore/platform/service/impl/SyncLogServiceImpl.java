package com.offshore.platform.service.impl;

import com.offshore.platform.entity.SyncLog;
import com.offshore.platform.mapper.SyncLogMapper;
import com.offshore.platform.service.SyncLogService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * sync_log 基础Service实现。
 */
@Service
public class SyncLogServiceImpl implements SyncLogService {
    private final SyncLogMapper syncLogMapper;

    public SyncLogServiceImpl(SyncLogMapper syncLogMapper) {
        this.syncLogMapper = syncLogMapper;
    }

    @Override
    public int create(SyncLog syncLog) {
        return syncLogMapper.insert(syncLog);
    }

    @Override
    public int update(SyncLog syncLog) {
        return syncLogMapper.updateById(syncLog);
    }

    @Override
    public SyncLog getById(Long id) {
        return syncLogMapper.selectById(id);
    }

    @Override
    public List<SyncLog> listAll() {
        return syncLogMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return syncLogMapper.softDeleteById(id);
    }
}
