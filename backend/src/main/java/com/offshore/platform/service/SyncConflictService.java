package com.offshore.platform.service;

import com.offshore.platform.entity.SyncConflict;
import java.util.List;

/**
 * sync_conflict 基础Service。
 */
public interface SyncConflictService {
    int create(SyncConflict syncConflict);

    int update(SyncConflict syncConflict);

    SyncConflict getById(Long id);

    List<SyncConflict> listAll();

    int removeById(Long id);
}
