package com.offshore.platform.mapper;

import com.offshore.platform.entity.SyncConflict;
import java.util.List;

/**
 * sync_conflict 基础Mapper。
 */
public interface SyncConflictMapper {
    int insert(SyncConflict syncConflict);

    int updateById(SyncConflict syncConflict);

    SyncConflict selectById(Long id);

    List<SyncConflict> selectAll();

    int softDeleteById(Long id);
}
