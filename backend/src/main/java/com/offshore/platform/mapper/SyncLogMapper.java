package com.offshore.platform.mapper;

import com.offshore.platform.entity.SyncLog;
import java.util.List;

/**
 * sync_log 基础Mapper。
 */
public interface SyncLogMapper {
    int insert(SyncLog syncLog);

    int updateById(SyncLog syncLog);

    SyncLog selectById(Long id);

    List<SyncLog> selectAll();

    int softDeleteById(Long id);
}
