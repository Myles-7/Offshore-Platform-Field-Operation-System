package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderCheckItem;
import com.offshore.platform.mapper.WorkOrderCheckItemMapper;
import com.offshore.platform.service.WorkOrderCheckItemService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_check_item 基础Service实现。
 */
@Service
public class WorkOrderCheckItemServiceImpl implements WorkOrderCheckItemService {
    private final WorkOrderCheckItemMapper workOrderCheckItemMapper;

    public WorkOrderCheckItemServiceImpl(WorkOrderCheckItemMapper workOrderCheckItemMapper) {
        this.workOrderCheckItemMapper = workOrderCheckItemMapper;
    }

    @Override
    public int create(WorkOrderCheckItem workOrderCheckItem) {
        return workOrderCheckItemMapper.insert(workOrderCheckItem);
    }

    @Override
    public int update(WorkOrderCheckItem workOrderCheckItem) {
        return workOrderCheckItemMapper.updateById(workOrderCheckItem);
    }

    @Override
    public WorkOrderCheckItem getById(Long id) {
        return workOrderCheckItemMapper.selectById(id);
    }

    @Override
    public List<WorkOrderCheckItem> listAll() {
        return workOrderCheckItemMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderCheckItemMapper.softDeleteById(id);
    }
}
