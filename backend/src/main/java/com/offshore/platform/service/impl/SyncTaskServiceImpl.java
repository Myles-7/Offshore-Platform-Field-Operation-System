package com.offshore.platform.service.impl;

import com.offshore.platform.entity.SyncTask;
import com.offshore.platform.mapper.SyncTaskMapper;
import com.offshore.platform.service.SyncTaskService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * sync_task 基础Service实现。
 */
@Service
public class SyncTaskServiceImpl implements SyncTaskService {
    private final SyncTaskMapper syncTaskMapper;

    public SyncTaskServiceImpl(SyncTaskMapper syncTaskMapper) {
        this.syncTaskMapper = syncTaskMapper;
    }

    @Override
    public int create(SyncTask syncTask) {
        return syncTaskMapper.insert(syncTask);
    }

    @Override
    public int update(SyncTask syncTask) {
        return syncTaskMapper.updateById(syncTask);
    }

    @Override
    public SyncTask getById(Long id) {
        return syncTaskMapper.selectById(id);
    }

    @Override
    public List<SyncTask> listAll() {
        return syncTaskMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return syncTaskMapper.softDeleteById(id);
    }
}
