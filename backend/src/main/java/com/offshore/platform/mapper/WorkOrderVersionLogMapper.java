package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderVersionLog;
import java.util.List;

/**
 * work_order_version_log 基础Mapper。
 */
public interface WorkOrderVersionLogMapper {
    int insert(WorkOrderVersionLog workOrderVersionLog);

    int updateById(WorkOrderVersionLog workOrderVersionLog);

    WorkOrderVersionLog selectById(Long id);

    List<WorkOrderVersionLog> selectAll();

    int softDeleteById(Long id);
}
