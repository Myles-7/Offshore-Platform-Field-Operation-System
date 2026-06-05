package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderAssignment;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order_assignment 基础Mapper。
 */
public interface WorkOrderAssignmentMapper {
    int insert(WorkOrderAssignment workOrderAssignment);

    int updateById(WorkOrderAssignment workOrderAssignment);

    WorkOrderAssignment selectById(Long id);

    List<WorkOrderAssignment> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);

    List<WorkOrderAssignment> selectAll();

    int softDeleteById(Long id);
}
