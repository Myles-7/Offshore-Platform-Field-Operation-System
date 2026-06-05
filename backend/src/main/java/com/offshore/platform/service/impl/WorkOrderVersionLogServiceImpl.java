package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderVersionLog;
import com.offshore.platform.mapper.WorkOrderVersionLogMapper;
import com.offshore.platform.service.WorkOrderVersionLogService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_version_log 基础Service实现。
 */
@Service
public class WorkOrderVersionLogServiceImpl implements WorkOrderVersionLogService {
    private final WorkOrderVersionLogMapper workOrderVersionLogMapper;

    public WorkOrderVersionLogServiceImpl(WorkOrderVersionLogMapper workOrderVersionLogMapper) {
        this.workOrderVersionLogMapper = workOrderVersionLogMapper;
    }

    @Override
    public int create(WorkOrderVersionLog workOrderVersionLog) {
        return workOrderVersionLogMapper.insert(workOrderVersionLog);
    }

    @Override
    public int update(WorkOrderVersionLog workOrderVersionLog) {
        return workOrderVersionLogMapper.updateById(workOrderVersionLog);
    }

    @Override
    public WorkOrderVersionLog getById(Long id) {
        return workOrderVersionLogMapper.selectById(id);
    }

    @Override
    public List<WorkOrderVersionLog> listAll() {
        return workOrderVersionLogMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderVersionLogMapper.softDeleteById(id);
    }
}
