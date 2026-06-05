package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderVersionLog;
import java.util.List;

/**
 * work_order_version_log 基础Service。
 */
public interface WorkOrderVersionLogService {
    int create(WorkOrderVersionLog workOrderVersionLog);

    int update(WorkOrderVersionLog workOrderVersionLog);

    WorkOrderVersionLog getById(Long id);

    List<WorkOrderVersionLog> listAll();

    int removeById(Long id);
}
