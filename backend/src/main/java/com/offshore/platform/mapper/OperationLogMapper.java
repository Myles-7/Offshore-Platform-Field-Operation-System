package com.offshore.platform.mapper;

import com.offshore.platform.entity.OperationLog;
import java.util.List;

/**
 * operation_log 基础Mapper。
 */
public interface OperationLogMapper {
    int insert(OperationLog operationLog);

    int updateById(OperationLog operationLog);

    OperationLog selectById(Long id);

    List<OperationLog> selectAll();

    int softDeleteById(Long id);
}
