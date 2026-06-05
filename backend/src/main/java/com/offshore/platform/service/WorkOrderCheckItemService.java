package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderCheckItem;
import java.util.List;

/**
 * work_order_check_item 基础Service。
 */
public interface WorkOrderCheckItemService {
    int create(WorkOrderCheckItem workOrderCheckItem);

    int update(WorkOrderCheckItem workOrderCheckItem);

    WorkOrderCheckItem getById(Long id);

    List<WorkOrderCheckItem> listAll();

    int removeById(Long id);
}
