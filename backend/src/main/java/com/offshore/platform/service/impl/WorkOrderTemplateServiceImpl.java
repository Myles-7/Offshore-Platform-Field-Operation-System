package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderTemplate;
import com.offshore.platform.mapper.WorkOrderTemplateMapper;
import com.offshore.platform.service.WorkOrderTemplateService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_template 基础Service实现。
 */
@Service
public class WorkOrderTemplateServiceImpl implements WorkOrderTemplateService {
    private final WorkOrderTemplateMapper workOrderTemplateMapper;

    public WorkOrderTemplateServiceImpl(WorkOrderTemplateMapper workOrderTemplateMapper) {
        this.workOrderTemplateMapper = workOrderTemplateMapper;
    }

    @Override
    public int create(WorkOrderTemplate workOrderTemplate) {
        return workOrderTemplateMapper.insert(workOrderTemplate);
    }

    @Override
    public int update(WorkOrderTemplate workOrderTemplate) {
        return workOrderTemplateMapper.updateById(workOrderTemplate);
    }

    @Override
    public WorkOrderTemplate getById(Long id) {
        return workOrderTemplateMapper.selectById(id);
    }

    @Override
    public List<WorkOrderTemplate> listAll() {
        return workOrderTemplateMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderTemplateMapper.softDeleteById(id);
    }
}
