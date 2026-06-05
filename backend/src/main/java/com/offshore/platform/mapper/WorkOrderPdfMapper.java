package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderPdf;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order_pdf 基础Mapper。
 */
public interface WorkOrderPdfMapper {
    int insert(WorkOrderPdf workOrderPdf);

    int updateById(WorkOrderPdf workOrderPdf);

    WorkOrderPdf selectById(Long id);

    List<WorkOrderPdf> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);

    List<WorkOrderPdf> selectAll();

    int softDeleteById(Long id);
}
