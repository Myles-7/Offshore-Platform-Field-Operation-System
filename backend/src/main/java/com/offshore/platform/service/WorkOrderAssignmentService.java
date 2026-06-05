package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderAssignment;
import java.util.List;

/**
 * work_order_assignment 基础Service。
 */
public interface WorkOrderAssignmentService {
    int create(WorkOrderAssignment workOrderAssignment);

    int update(WorkOrderAssignment workOrderAssignment);

    WorkOrderAssignment getById(Long id);

    List<WorkOrderAssignment> listAll();

    int removeById(Long id);
}
