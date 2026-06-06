package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order_record 基础Mapper。
 */
public interface WorkOrderRecordMapper {
    int insert(WorkOrderRecord workOrderRecord);

    int updateById(WorkOrderRecord workOrderRecord);

    WorkOrderRecord selectById(Long id);

    List<WorkOrderRecord> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);

    List<WorkOrderRecord> selectAll();

    int softDeleteById(Long id);

    /** Incremental pull: records accessible to this maintainer. */
    List<WorkOrderRecord> selectUpdatedAfterByWorkOrderIds(@Param("cursor") LocalDateTime cursor,
                                                           @Param("workOrderIds") List<Long> workOrderIds,
                                                           @Param("limit") Integer limit);
}
