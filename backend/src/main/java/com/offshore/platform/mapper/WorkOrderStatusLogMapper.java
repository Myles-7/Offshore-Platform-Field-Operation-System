package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderStatusLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order_status_log 基础Mapper。
 */
public interface WorkOrderStatusLogMapper {
    int insert(WorkOrderStatusLog workOrderStatusLog);

    int updateById(WorkOrderStatusLog workOrderStatusLog);

    WorkOrderStatusLog selectById(Long id);

    List<WorkOrderStatusLog> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);

    List<WorkOrderStatusLog> selectAll();

    int softDeleteById(Long id);
}
