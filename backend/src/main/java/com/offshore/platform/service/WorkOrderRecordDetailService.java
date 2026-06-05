package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderRecordDetail;
import java.util.List;

/**
 * work_order_record_detail 基础Service。
 */
public interface WorkOrderRecordDetailService {
    int create(WorkOrderRecordDetail workOrderRecordDetail);

    int update(WorkOrderRecordDetail workOrderRecordDetail);

    WorkOrderRecordDetail getById(Long id);

    List<WorkOrderRecordDetail> listAll();

    int removeById(Long id);
}
