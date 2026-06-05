package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderRecord;
import java.util.List;

/**
 * work_order_record 基础Service。
 */
public interface WorkOrderRecordService {
    int create(WorkOrderRecord workOrderRecord);

    int update(WorkOrderRecord workOrderRecord);

    WorkOrderRecord getById(Long id);

    List<WorkOrderRecord> listAll();

    int removeById(Long id);
}
