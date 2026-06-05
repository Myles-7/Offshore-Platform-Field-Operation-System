package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.service.AuditService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "log", description = "Operation log and audit trail")
@RestController
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/api/admin/operation-logs")
    public ApiResponse<List<OperationLog>> operationLogs() {
        return ApiResponse.success(auditService.operationLogs());
    }

    @GetMapping("/api/admin/operation-logs/{id}")
    public ApiResponse<OperationLog> operationLog(@PathVariable Long id) {
        return ApiResponse.success(auditService.operationLog(id));
    }

    @GetMapping("/api/admin/work-orders/{workOrderId}/audit-trail")
    public ApiResponse<List<Map<String, Object>>> workOrderAuditTrail(@PathVariable Long workOrderId) {
        return ApiResponse.success(auditService.workOrderAuditTrail(workOrderId));
    }

    @GetMapping("/api/admin/sync/audit-trail")
    public ApiResponse<List<Map<String, Object>>> syncAuditTrail() {
        return ApiResponse.success(auditService.syncAuditTrail());
    }
}
