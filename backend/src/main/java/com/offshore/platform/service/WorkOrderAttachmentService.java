package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderAttachment;
import java.util.List;

/**
 * work_order_attachment 基础Service。
 */
public interface WorkOrderAttachmentService {
    int create(WorkOrderAttachment workOrderAttachment);

    int update(WorkOrderAttachment workOrderAttachment);

    WorkOrderAttachment getById(Long id);

    List<WorkOrderAttachment> listAll();

    int removeById(Long id);
}
