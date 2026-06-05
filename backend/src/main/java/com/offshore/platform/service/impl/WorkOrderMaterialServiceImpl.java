package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderMaterial;
import com.offshore.platform.mapper.WorkOrderMaterialMapper;
import com.offshore.platform.service.WorkOrderMaterialService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_material 基础Service实现。
 */
@Service
public class WorkOrderMaterialServiceImpl implements WorkOrderMaterialService {
    private final WorkOrderMaterialMapper workOrderMaterialMapper;

    public WorkOrderMaterialServiceImpl(WorkOrderMaterialMapper workOrderMaterialMapper) {
        this.workOrderMaterialMapper = workOrderMaterialMapper;
    }

    @Override
    public int create(WorkOrderMaterial workOrderMaterial) {
        return workOrderMaterialMapper.insert(workOrderMaterial);
    }

    @Override
    public int update(WorkOrderMaterial workOrderMaterial) {
        return workOrderMaterialMapper.updateById(workOrderMaterial);
    }

    @Override
    public WorkOrderMaterial getById(Long id) {
        return workOrderMaterialMapper.selectById(id);
    }

    @Override
    public List<WorkOrderMaterial> listAll() {
        return workOrderMaterialMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderMaterialMapper.softDeleteById(id);
    }
}
