package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderAcceptance;
import com.offshore.platform.mapper.WorkOrderAcceptanceMapper;
import com.offshore.platform.service.WorkOrderAcceptanceService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_acceptance 基础Service实现。
 */
@Service
public class WorkOrderAcceptanceServiceImpl implements WorkOrderAcceptanceService {
    private final WorkOrderAcceptanceMapper workOrderAcceptanceMapper;

    public WorkOrderAcceptanceServiceImpl(WorkOrderAcceptanceMapper workOrderAcceptanceMapper) {
        this.workOrderAcceptanceMapper = workOrderAcceptanceMapper;
    }

    @Override
    public int create(WorkOrderAcceptance workOrderAcceptance) {
        return workOrderAcceptanceMapper.insert(workOrderAcceptance);
    }

    @Override
    public int update(WorkOrderAcceptance workOrderAcceptance) {
        return workOrderAcceptanceMapper.updateById(workOrderAcceptance);
    }

    @Override
    public WorkOrderAcceptance getById(Long id) {
        return workOrderAcceptanceMapper.selectById(id);
    }

    @Override
    public List<WorkOrderAcceptance> listAll() {
        return workOrderAcceptanceMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderAcceptanceMapper.softDeleteById(id);
    }
}
