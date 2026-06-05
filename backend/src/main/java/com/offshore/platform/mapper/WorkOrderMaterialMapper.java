package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderMaterial;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order_material 基础Mapper。
 */
public interface WorkOrderMaterialMapper {
    int insert(WorkOrderMaterial workOrderMaterial);

    int updateById(WorkOrderMaterial workOrderMaterial);

    WorkOrderMaterial selectById(Long id);

    List<WorkOrderMaterial> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);

    List<WorkOrderMaterial> selectAll();

    int softDeleteById(Long id);
}
