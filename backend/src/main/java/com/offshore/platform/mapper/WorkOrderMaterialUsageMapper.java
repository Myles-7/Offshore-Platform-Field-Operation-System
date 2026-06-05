package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderMaterialUsage;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order_material_usage 基础Mapper。
 */
public interface WorkOrderMaterialUsageMapper {
    int insert(WorkOrderMaterialUsage workOrderMaterialUsage);

    int updateById(WorkOrderMaterialUsage workOrderMaterialUsage);

    WorkOrderMaterialUsage selectById(Long id);

    List<WorkOrderMaterialUsage> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);

    List<WorkOrderMaterialUsage> selectAll();

    int softDeleteById(Long id);
}
