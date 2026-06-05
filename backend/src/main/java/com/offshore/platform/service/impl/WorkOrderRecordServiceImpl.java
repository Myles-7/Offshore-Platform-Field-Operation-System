package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderRecord;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import com.offshore.platform.service.WorkOrderRecordService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_record 基础Service实现。
 */
@Service
public class WorkOrderRecordServiceImpl implements WorkOrderRecordService {
    private final WorkOrderRecordMapper workOrderRecordMapper;

    public WorkOrderRecordServiceImpl(WorkOrderRecordMapper workOrderRecordMapper) {
        this.workOrderRecordMapper = workOrderRecordMapper;
    }

    @Override
    public int create(WorkOrderRecord workOrderRecord) {
        return workOrderRecordMapper.insert(workOrderRecord);
    }

    @Override
    public int update(WorkOrderRecord workOrderRecord) {
        return workOrderRecordMapper.updateById(workOrderRecord);
    }

    @Override
    public WorkOrderRecord getById(Long id) {
        return workOrderRecordMapper.selectById(id);
    }

    @Override
    public List<WorkOrderRecord> listAll() {
        return workOrderRecordMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderRecordMapper.softDeleteById(id);
    }
}
