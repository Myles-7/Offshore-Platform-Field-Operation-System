package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrderAttachment;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order_attachment 基础Mapper。
 */
public interface WorkOrderAttachmentMapper {
    int insert(WorkOrderAttachment workOrderAttachment);

    int updateById(WorkOrderAttachment workOrderAttachment);

    WorkOrderAttachment selectById(Long id);

    List<WorkOrderAttachment> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);

    List<WorkOrderAttachment> selectByFileId(@Param("fileId") String fileId);

    List<WorkOrderAttachment> selectAll();

    int softDeleteById(Long id);
}
