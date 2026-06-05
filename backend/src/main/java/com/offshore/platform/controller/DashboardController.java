package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.common.log.OperationLog;
import com.offshore.platform.service.DashboardService;
import com.offshore.platform.vo.DashboardVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "dashboard", description = "Dashboard and reports")
@RestController
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/admin/dashboard/overview")
    public ApiResponse<DashboardVO> overview() {
        return ApiResponse.success(dashboardService.overview());
    }

    @GetMapping("/api/admin/dashboard/work-order-statistics")
    public ApiResponse<DashboardVO> workOrderStatistics() {
        return ApiResponse.success(dashboardService.workOrderStatistics());
    }

    @GetMapping("/api/admin/dashboard/project-statistics")
    public ApiResponse<List<Map<String, Object>>> projectStatistics() {
        return ApiResponse.success(dashboardService.projectStatistics());
    }

    @GetMapping("/api/admin/dashboard/person-statistics")
    public ApiResponse<List<Map<String, Object>>> personStatistics() {
        return ApiResponse.success(dashboardService.personStatistics());
    }

    @GetMapping("/api/admin/dashboard/material-statistics")
    public ApiResponse<List<Map<String, Object>>> materialStatistics() {
        return ApiResponse.success(dashboardService.materialStatistics());
    }

    @GetMapping("/api/admin/dashboard/output-value")
    public ApiResponse<List<Map<String, Object>>> outputValue() {
        return ApiResponse.success(dashboardService.outputValue());
    }

    @GetMapping("/api/admin/reports/reconciliation")
    public ApiResponse<List<Map<String, Object>>> reconciliation() {
        return ApiResponse.success(dashboardService.reconciliation());
    }

    @GetMapping("/api/admin/reports/reconciliation/export")
    @OperationLog(module = "DASHBOARD", operation = "EXPORT_RECONCILIATION", businessType = "REPORT")
    public ResponseEntity<byte[]> export(HttpServletRequest request) {
        byte[] body = dashboardService.exportReconciliation(request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("reconciliation.xlsx").build().toString())
                .body(body);
    }
}
