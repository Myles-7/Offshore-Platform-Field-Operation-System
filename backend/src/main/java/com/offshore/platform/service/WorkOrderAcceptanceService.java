package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderAcceptance;
import java.util.List;

/**
 * work_order_acceptance 基础Service。
 */
public interface WorkOrderAcceptanceService {
    int create(WorkOrderAcceptance workOrderAcceptance);

    int update(WorkOrderAcceptance workOrderAcceptance);

    WorkOrderAcceptance getById(Long id);

    List<WorkOrderAcceptance> listAll();

    int removeById(Long id);
}
