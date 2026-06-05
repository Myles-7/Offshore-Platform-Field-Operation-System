package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderCheckItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order_check_item 基础Mapper。
 */
public interface WorkOrderCheckItemMapper {
    int insert(WorkOrderCheckItem workOrderCheckItem);

    int updateById(WorkOrderCheckItem workOrderCheckItem);

    WorkOrderCheckItem selectById(Long id);

    List<WorkOrderCheckItem> selectByRecordId(@Param("recordId") Long recordId);

    List<WorkOrderCheckItem> selectAll();

    int softDeleteById(Long id);
}
