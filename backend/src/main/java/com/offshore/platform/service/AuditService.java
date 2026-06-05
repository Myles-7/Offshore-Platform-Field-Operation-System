package com.offshore.platform.service;

import com.offshore.platform.entity.OperationLog;
import java.util.List;
import java.util.Map;

public interface AuditService {
    List<OperationLog> operationLogs();

    OperationLog operationLog(Long id);

    List<Map<String, Object>> workOrderAuditTrail(Long workOrderId);

    List<Map<String, Object>> syncAuditTrail();
}
