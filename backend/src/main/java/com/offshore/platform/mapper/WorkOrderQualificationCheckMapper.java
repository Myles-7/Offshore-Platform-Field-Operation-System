package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderQualificationCheck;
import java.util.List;

/**
 * work_order_qualification_check 基础Mapper。
 */
public interface WorkOrderQualificationCheckMapper {
    int insert(WorkOrderQualificationCheck workOrderQualificationCheck);

    int updateById(WorkOrderQualificationCheck workOrderQualificationCheck);

    WorkOrderQualificationCheck selectById(Long id);

    List<WorkOrderQualificationCheck> selectAll();

    int softDeleteById(Long id);
}
