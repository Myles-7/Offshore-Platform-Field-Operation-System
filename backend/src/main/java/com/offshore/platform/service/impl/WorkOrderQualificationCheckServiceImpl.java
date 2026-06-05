package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderQualificationCheck;
import com.offshore.platform.mapper.WorkOrderQualificationCheckMapper;
import com.offshore.platform.service.WorkOrderQualificationCheckService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_qualification_check 基础Service实现。
 */
@Service
public class WorkOrderQualificationCheckServiceImpl implements WorkOrderQualificationCheckService {
    private final WorkOrderQualificationCheckMapper workOrderQualificationCheckMapper;

    public WorkOrderQualificationCheckServiceImpl(WorkOrderQualificationCheckMapper workOrderQualificationCheckMapper) {
        this.workOrderQualificationCheckMapper = workOrderQualificationCheckMapper;
    }

    @Override
    public int create(WorkOrderQualificationCheck workOrderQualificationCheck) {
        return workOrderQualificationCheckMapper.insert(workOrderQualificationCheck);
    }

    @Override
    public int update(WorkOrderQualificationCheck workOrderQualificationCheck) {
        return workOrderQualificationCheckMapper.updateById(workOrderQualificationCheck);
    }

    @Override
    public WorkOrderQualificationCheck getById(Long id) {
        return workOrderQualificationCheckMapper.selectById(id);
    }

    @Override
    public List<WorkOrderQualificationCheck> listAll() {
        return workOrderQualificationCheckMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderQualificationCheckMapper.softDeleteById(id);
    }
}
