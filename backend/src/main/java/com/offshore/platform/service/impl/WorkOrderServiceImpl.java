package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.service.WorkOrderService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order 基础Service实现。
 */
@Service
public class WorkOrderServiceImpl implements WorkOrderService {
    private final WorkOrderMapper workOrderMapper;

    public WorkOrderServiceImpl(WorkOrderMapper workOrderMapper) {
        this.workOrderMapper = workOrderMapper;
    }

    @Override
    public int create(WorkOrder workOrder) {
        return workOrderMapper.insert(workOrder);
    }

    @Override
    public int update(WorkOrder workOrder) {
        return workOrderMapper.updateById(workOrder);
    }

    @Override
    public WorkOrder getById(Long id) {
        return workOrderMapper.selectById(id);
    }

    @Override
    public List<WorkOrder> listAll() {
        return workOrderMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderMapper.softDeleteById(id);
    }
}
