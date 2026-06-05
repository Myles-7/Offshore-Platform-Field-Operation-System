package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderMaterialUsage;
import com.offshore.platform.mapper.WorkOrderMaterialUsageMapper;
import com.offshore.platform.service.WorkOrderMaterialUsageService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_material_usage 基础Service实现。
 */
@Service
public class WorkOrderMaterialUsageServiceImpl implements WorkOrderMaterialUsageService {
    private final WorkOrderMaterialUsageMapper workOrderMaterialUsageMapper;

    public WorkOrderMaterialUsageServiceImpl(WorkOrderMaterialUsageMapper workOrderMaterialUsageMapper) {
        this.workOrderMaterialUsageMapper = workOrderMaterialUsageMapper;
    }

    @Override
    public int create(WorkOrderMaterialUsage workOrderMaterialUsage) {
        return workOrderMaterialUsageMapper.insert(workOrderMaterialUsage);
    }

    @Override
    public int update(WorkOrderMaterialUsage workOrderMaterialUsage) {
        return workOrderMaterialUsageMapper.updateById(workOrderMaterialUsage);
    }

    @Override
    public WorkOrderMaterialUsage getById(Long id) {
        return workOrderMaterialUsageMapper.selectById(id);
    }

    @Override
    public List<WorkOrderMaterialUsage> listAll() {
        return workOrderMaterialUsageMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderMaterialUsageMapper.softDeleteById(id);
    }
}
