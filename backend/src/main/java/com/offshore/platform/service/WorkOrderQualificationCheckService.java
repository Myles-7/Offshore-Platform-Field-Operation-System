package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderQualificationCheck;
import java.util.List;

/**
 * work_order_qualification_check 基础Service。
 */
public interface WorkOrderQualificationCheckService {
    int create(WorkOrderQualificationCheck workOrderQualificationCheck);

    int update(WorkOrderQualificationCheck workOrderQualificationCheck);

    WorkOrderQualificationCheck getById(Long id);

    List<WorkOrderQualificationCheck> listAll();

    int removeById(Long id);
}
