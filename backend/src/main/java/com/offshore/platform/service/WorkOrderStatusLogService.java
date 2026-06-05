package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderStatusLog;
import java.util.List;

/**
 * work_order_status_log 基础Service。
 */
public interface WorkOrderStatusLogService {
    int create(WorkOrderStatusLog workOrderStatusLog);

    int update(WorkOrderStatusLog workOrderStatusLog);

    WorkOrderStatusLog getById(Long id);

    List<WorkOrderStatusLog> listAll();

    int removeById(Long id);
}
