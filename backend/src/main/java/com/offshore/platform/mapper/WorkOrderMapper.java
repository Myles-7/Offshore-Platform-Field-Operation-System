package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrder;
import java.util.List;

/**
 * work_order 基础Mapper。
 */
public interface WorkOrderMapper {
    int insert(WorkOrder workOrder);

    int updateById(WorkOrder workOrder);

    WorkOrder selectById(Long id);

    List<WorkOrder> selectAll();

    int softDeleteById(Long id);
}
