package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderSignature;
import com.offshore.platform.mapper.WorkOrderSignatureMapper;
import com.offshore.platform.service.WorkOrderSignatureService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_signature 基础Service实现。
 */
@Service
public class WorkOrderSignatureServiceImpl implements WorkOrderSignatureService {
    private final WorkOrderSignatureMapper workOrderSignatureMapper;

    public WorkOrderSignatureServiceImpl(WorkOrderSignatureMapper workOrderSignatureMapper) {
        this.workOrderSignatureMapper = workOrderSignatureMapper;
    }

    @Override
    public int create(WorkOrderSignature workOrderSignature) {
        return workOrderSignatureMapper.insert(workOrderSignature);
    }

    @Override
    public int update(WorkOrderSignature workOrderSignature) {
        return workOrderSignatureMapper.updateById(workOrderSignature);
    }

    @Override
    public WorkOrderSignature getById(Long id) {
        return workOrderSignatureMapper.selectById(id);
    }

    @Override
    public List<WorkOrderSignature> listAll() {
        return workOrderSignatureMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderSignatureMapper.softDeleteById(id);
    }
}
