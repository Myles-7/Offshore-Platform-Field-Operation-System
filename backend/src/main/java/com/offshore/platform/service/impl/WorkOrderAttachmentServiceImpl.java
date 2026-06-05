package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.service.WorkOrderAttachmentService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_attachment 基础Service实现。
 */
@Service
public class WorkOrderAttachmentServiceImpl implements WorkOrderAttachmentService {
    private final WorkOrderAttachmentMapper workOrderAttachmentMapper;

    public WorkOrderAttachmentServiceImpl(WorkOrderAttachmentMapper workOrderAttachmentMapper) {
        this.workOrderAttachmentMapper = workOrderAttachmentMapper;
    }

    @Override
    public int create(WorkOrderAttachment workOrderAttachment) {
        return workOrderAttachmentMapper.insert(workOrderAttachment);
    }

    @Override
    public int update(WorkOrderAttachment workOrderAttachment) {
        return workOrderAttachmentMapper.updateById(workOrderAttachment);
    }

    @Override
    public WorkOrderAttachment getById(Long id) {
        return workOrderAttachmentMapper.selectById(id);
    }

    @Override
    public List<WorkOrderAttachment> listAll() {
        return workOrderAttachmentMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderAttachmentMapper.softDeleteById(id);
    }
}
