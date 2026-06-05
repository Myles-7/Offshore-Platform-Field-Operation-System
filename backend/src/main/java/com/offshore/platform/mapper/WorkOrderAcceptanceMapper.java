package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderAcceptance;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order_acceptance 基础Mapper。
 */
public interface WorkOrderAcceptanceMapper {
    int insert(WorkOrderAcceptance workOrderAcceptance);

    int updateById(WorkOrderAcceptance workOrderAcceptance);

    WorkOrderAcceptance selectById(Long id);

    List<WorkOrderAcceptance> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);

    List<WorkOrderAcceptance> selectAll();

    int softDeleteById(Long id);
}
