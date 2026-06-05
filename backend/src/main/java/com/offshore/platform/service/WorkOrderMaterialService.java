package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderMaterial;
import java.util.List;

/**
 * work_order_material 基础Service。
 */
public interface WorkOrderMaterialService {
    int create(WorkOrderMaterial workOrderMaterial);

    int update(WorkOrderMaterial workOrderMaterial);

    WorkOrderMaterial getById(Long id);

    List<WorkOrderMaterial> listAll();

    int removeById(Long id);
}
