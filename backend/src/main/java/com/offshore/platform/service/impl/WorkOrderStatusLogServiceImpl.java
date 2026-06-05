package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderStatusLog;
import com.offshore.platform.mapper.WorkOrderStatusLogMapper;
import com.offshore.platform.service.WorkOrderStatusLogService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_status_log 基础Service实现。
 */
@Service
public class WorkOrderStatusLogServiceImpl implements WorkOrderStatusLogService {
    private final WorkOrderStatusLogMapper workOrderStatusLogMapper;

    public WorkOrderStatusLogServiceImpl(WorkOrderStatusLogMapper workOrderStatusLogMapper) {
        this.workOrderStatusLogMapper = workOrderStatusLogMapper;
    }

    @Override
    public int create(WorkOrderStatusLog workOrderStatusLog) {
        return workOrderStatusLogMapper.insert(workOrderStatusLog);
    }

    @Override
    public int update(WorkOrderStatusLog workOrderStatusLog) {
        return workOrderStatusLogMapper.updateById(workOrderStatusLog);
    }

    @Override
    public WorkOrderStatusLog getById(Long id) {
        return workOrderStatusLogMapper.selectById(id);
    }

    @Override
    public List<WorkOrderStatusLog> listAll() {
        return workOrderStatusLogMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderStatusLogMapper.softDeleteById(id);
    }
}
