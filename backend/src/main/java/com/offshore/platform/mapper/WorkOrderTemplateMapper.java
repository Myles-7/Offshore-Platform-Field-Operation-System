package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderTemplate;
import java.util.List;

/**
 * work_order_template 基础Mapper。
 */
public interface WorkOrderTemplateMapper {
    int insert(WorkOrderTemplate workOrderTemplate);

    int updateById(WorkOrderTemplate workOrderTemplate);

    WorkOrderTemplate selectById(Long id);

    List<WorkOrderTemplate> selectAll();

    int softDeleteById(Long id);
}
