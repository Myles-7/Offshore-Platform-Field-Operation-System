package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderRecordDetail;
import java.util.List;

/**
 * work_order_record_detail 基础Mapper。
 */
public interface WorkOrderRecordDetailMapper {
    int insert(WorkOrderRecordDetail workOrderRecordDetail);

    int updateById(WorkOrderRecordDetail workOrderRecordDetail);

    WorkOrderRecordDetail selectById(Long id);

    List<WorkOrderRecordDetail> selectAll();

    int softDeleteById(Long id);
}
