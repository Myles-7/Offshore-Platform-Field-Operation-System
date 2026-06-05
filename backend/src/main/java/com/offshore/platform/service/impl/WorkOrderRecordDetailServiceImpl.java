package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderRecordDetail;
import com.offshore.platform.mapper.WorkOrderRecordDetailMapper;
import com.offshore.platform.service.WorkOrderRecordDetailService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_record_detail 基础Service实现。
 */
@Service
public class WorkOrderRecordDetailServiceImpl implements WorkOrderRecordDetailService {
    private final WorkOrderRecordDetailMapper workOrderRecordDetailMapper;

    public WorkOrderRecordDetailServiceImpl(WorkOrderRecordDetailMapper workOrderRecordDetailMapper) {
        this.workOrderRecordDetailMapper = workOrderRecordDetailMapper;
    }

    @Override
    public int create(WorkOrderRecordDetail workOrderRecordDetail) {
        return workOrderRecordDetailMapper.insert(workOrderRecordDetail);
    }

    @Override
    public int update(WorkOrderRecordDetail workOrderRecordDetail) {
        return workOrderRecordDetailMapper.updateById(workOrderRecordDetail);
    }

    @Override
    public WorkOrderRecordDetail getById(Long id) {
        return workOrderRecordDetailMapper.selectById(id);
    }

    @Override
    public List<WorkOrderRecordDetail> listAll() {
        return workOrderRecordDetailMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderRecordDetailMapper.softDeleteById(id);
    }
}
