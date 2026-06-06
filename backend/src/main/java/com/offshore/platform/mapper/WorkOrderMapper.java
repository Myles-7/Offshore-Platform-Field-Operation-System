package com.offshore.platform.mapper;

import com.offshore.platform.entity.WorkOrder;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * work_order 基础Mapper。
 */
public interface WorkOrderMapper {
    int insert(WorkOrder workOrder);

    int updateById(WorkOrder workOrder);

    WorkOrder selectById(Long id);

    List<WorkOrder> selectAll();

    int softDeleteById(Long id);

    /**
     * Incremental pull: select work orders updated after the given cursor,
     * filtered by maintainer ID for worker-scope access.
     */
    List<WorkOrder> selectUpdatedAfterByMaintainer(@Param("cursor") LocalDateTime cursor,
                                                   @Param("maintainerId") Long maintainerId,
                                                   @Param("limit") Integer limit);

    /**
     * Incremental pull: select work orders updated after the given cursor,
     * filtered by project IDs for project-manager scope.
     */
    List<WorkOrder> selectUpdatedAfterByProjectScope(@Param("cursor") LocalDateTime cursor,
                                                     @Param("projectIds") List<Long> projectIds,
                                                     @Param("limit") Integer limit);

    /**
     * Incremental pull: select all work orders updated after cursor (admin scope).
     */
    List<WorkOrder> selectUpdatedAfter(@Param("cursor") LocalDateTime cursor,
                                       @Param("limit") Integer limit);
}
