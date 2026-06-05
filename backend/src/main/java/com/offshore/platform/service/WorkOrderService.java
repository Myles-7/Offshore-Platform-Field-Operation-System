package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrder;
import java.util.List;

/**
 * work_order 基础Service。
 */
public interface WorkOrderService {
    int create(WorkOrder workOrder);

    int update(WorkOrder workOrder);

    WorkOrder getById(Long id);

    List<WorkOrder> listAll();

    int removeById(Long id);
}
