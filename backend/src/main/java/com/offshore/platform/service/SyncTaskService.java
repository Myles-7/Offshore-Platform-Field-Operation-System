package com.offshore.platform.service;

import com.offshore.platform.entity.SyncTask;
import java.util.List;

/**
 * sync_task 基础Service。
 */
public interface SyncTaskService {
    int create(SyncTask syncTask);

    int update(SyncTask syncTask);

    SyncTask getById(Long id);

    List<SyncTask> listAll();

    int removeById(Long id);
}
