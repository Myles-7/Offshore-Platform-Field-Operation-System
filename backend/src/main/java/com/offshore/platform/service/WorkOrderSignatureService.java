package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderSignature;
import java.util.List;

/**
 * work_order_signature 基础Service。
 */
public interface WorkOrderSignatureService {
    int create(WorkOrderSignature workOrderSignature);

    int update(WorkOrderSignature workOrderSignature);

    WorkOrderSignature getById(Long id);

    List<WorkOrderSignature> listAll();

    int removeById(Long id);
}
