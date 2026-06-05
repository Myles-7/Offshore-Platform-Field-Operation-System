package com.offshore.platform.service;

import com.offshore.platform.entity.SyncLog;
import java.util.List;

/**
 * sync_log 基础Service。
 */
public interface SyncLogService {
    int create(SyncLog syncLog);

    int update(SyncLog syncLog);

    SyncLog getById(Long id);

    List<SyncLog> listAll();

    int removeById(Long id);
}
