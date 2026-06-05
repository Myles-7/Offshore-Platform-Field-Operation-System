package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderTemplate;
import java.util.List;

/**
 * work_order_template 基础Service。
 */
public interface WorkOrderTemplateService {
    int create(WorkOrderTemplate workOrderTemplate);

    int update(WorkOrderTemplate workOrderTemplate);

    WorkOrderTemplate getById(Long id);

    List<WorkOrderTemplate> listAll();

    int removeById(Long id);
}
