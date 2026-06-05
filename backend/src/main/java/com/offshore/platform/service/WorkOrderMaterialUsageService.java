package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderMaterialUsage;
import java.util.List;

/**
 * work_order_material_usage 基础Service。
 */
public interface WorkOrderMaterialUsageService {
    int create(WorkOrderMaterialUsage workOrderMaterialUsage);

    int update(WorkOrderMaterialUsage workOrderMaterialUsage);

    WorkOrderMaterialUsage getById(Long id);

    List<WorkOrderMaterialUsage> listAll();

    int removeById(Long id);
}
