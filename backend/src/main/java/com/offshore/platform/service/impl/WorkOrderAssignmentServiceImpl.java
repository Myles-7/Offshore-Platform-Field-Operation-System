package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.service.WorkOrderAssignmentService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_assignment 基础Service实现。
 */
@Service
public class WorkOrderAssignmentServiceImpl implements WorkOrderAssignmentService {
    private final WorkOrderAssignmentMapper workOrderAssignmentMapper;

    public WorkOrderAssignmentServiceImpl(WorkOrderAssignmentMapper workOrderAssignmentMapper) {
        this.workOrderAssignmentMapper = workOrderAssignmentMapper;
    }

    @Override
    public int create(WorkOrderAssignment workOrderAssignment) {
        return workOrderAssignmentMapper.insert(workOrderAssignment);
    }

    @Override
    public int update(WorkOrderAssignment workOrderAssignment) {
        return workOrderAssignmentMapper.updateById(workOrderAssignment);
    }

    @Override
    public WorkOrderAssignment getById(Long id) {
        return workOrderAssignmentMapper.selectById(id);
    }

    @Override
    public List<WorkOrderAssignment> listAll() {
        return workOrderAssignmentMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderAssignmentMapper.softDeleteById(id);
    }
}
