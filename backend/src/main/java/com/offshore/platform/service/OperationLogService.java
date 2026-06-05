package com.offshore.platform.service;

import com.offshore.platform.entity.OperationLog;
import java.util.List;

/**
 * operation_log 基础Service。
 */
public interface OperationLogService {
    int create(OperationLog operationLog);

    int update(OperationLog operationLog);

    OperationLog getById(Long id);

    List<OperationLog> listAll();

    int removeById(Long id);
}
