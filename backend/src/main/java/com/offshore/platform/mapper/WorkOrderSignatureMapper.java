package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderSignature;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order_signature 基础Mapper。
 */
public interface WorkOrderSignatureMapper {
    int insert(WorkOrderSignature workOrderSignature);

    int updateById(WorkOrderSignature workOrderSignature);

    WorkOrderSignature selectById(Long id);

    List<WorkOrderSignature> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);

    List<WorkOrderSignature> selectAll();

    int softDeleteById(Long id);
}
