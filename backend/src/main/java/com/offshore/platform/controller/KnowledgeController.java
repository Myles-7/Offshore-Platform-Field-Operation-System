package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.common.log.OperationLog;
import com.offshore.platform.entity.KnowledgeCase;
import com.offshore.platform.entity.MaintenanceProcess;
import com.offshore.platform.service.KnowledgeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "knowledge", description = "Knowledge base")
@RestController
public class KnowledgeController {
    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    /* ========== 故障案例 ========== */
    @GetMapping("/api/admin/knowledge/cases")
    public ApiResponse<List<KnowledgeCase>> listCases() {
        return ApiResponse.success(knowledgeService.listCases());
    }

    @GetMapping("/api/admin/knowledge/cases/{id}")
    public ApiResponse<KnowledgeCase> getCase(@PathVariable Long id) {
        return ApiResponse.success(knowledgeService.getCase(id));
    }

    @PostMapping("/api/admin/knowledge/cases")
    @OperationLog(module = "KNOWLEDGE", operation = "CREATE_KNOWLEDGE_CASE", businessType = "KNOWLEDGE_CASE")
    public ApiResponse<KnowledgeCase> createCase(@Valid @RequestBody KnowledgeCase entity, HttpServletRequest request) {
        return ApiResponse.success(knowledgeService.createCase(entity, request));
    }

    @PutMapping("/api/admin/knowledge/cases/{id}")
    @OperationLog(module = "KNOWLEDGE", operation = "UPDATE_KNOWLEDGE_CASE", businessType = "KNOWLEDGE_CASE")
    public ApiResponse<KnowledgeCase> updateCase(@PathVariable Long id, @Valid @RequestBody KnowledgeCase entity,
            HttpServletRequest request) {
        return ApiResponse.success(knowledgeService.updateCase(id, entity, request));
    }

    @DeleteMapping("/api/admin/knowledge/cases/{id}")
    @OperationLog(module = "KNOWLEDGE", operation = "DELETE_KNOWLEDGE_CASE", businessType = "KNOWLEDGE_CASE")
    public ApiResponse<Void> deleteCase(@PathVariable Long id, HttpServletRequest request) {
        knowledgeService.deleteCase(id, request);
        return ApiResponse.success();
    }

    /* ========== 维修工艺 ========== */
    @GetMapping("/api/admin/knowledge/processes")
    public ApiResponse<List<MaintenanceProcess>> listProcesses() {
        return ApiResponse.success(knowledgeService.listProcesses());
    }

    @GetMapping("/api/admin/knowledge/processes/{id}")
    public ApiResponse<MaintenanceProcess> getProcess(@PathVariable Long id) {
        return ApiResponse.success(knowledgeService.getProcess(id));
    }

    @PostMapping("/api/admin/knowledge/processes")
    @OperationLog(module = "KNOWLEDGE", operation = "CREATE_MAINTENANCE_PROCESS", businessType = "MAINTENANCE_PROCESS")
    public ApiResponse<MaintenanceProcess> createProcess(@Valid @RequestBody MaintenanceProcess entity,
            HttpServletRequest request) {
        return ApiResponse.success(knowledgeService.createProcess(entity, request));
    }

    @PutMapping("/api/admin/knowledge/processes/{id}")
    @OperationLog(module = "KNOWLEDGE", operation = "UPDATE_MAINTENANCE_PROCESS", businessType = "MAINTENANCE_PROCESS")
    public ApiResponse<MaintenanceProcess> updateProcess(@PathVariable Long id, @Valid @RequestBody MaintenanceProcess entity,
            HttpServletRequest request) {
        return ApiResponse.success(knowledgeService.updateProcess(id, entity, request));
    }

    @DeleteMapping("/api/admin/knowledge/processes/{id}")
    @OperationLog(module = "KNOWLEDGE", operation = "DELETE_MAINTENANCE_PROCESS", businessType = "MAINTENANCE_PROCESS")
    public ApiResponse<Void> deleteProcess(@PathVariable Long id, HttpServletRequest request) {
        knowledgeService.deleteProcess(id, request);
        return ApiResponse.success();
    }
}
