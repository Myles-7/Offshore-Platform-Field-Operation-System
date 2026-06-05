package com.offshore.platform.mapper;

import com.offshore.platform.entity.SyncTask;
import java.util.List;

/**
 * sync_task 基础Mapper。
 */
public interface SyncTaskMapper {
    int insert(SyncTask syncTask);

    int updateById(SyncTask syncTask);

    SyncTask selectById(Long id);

    List<SyncTask> selectAll();

    int softDeleteById(Long id);
}
