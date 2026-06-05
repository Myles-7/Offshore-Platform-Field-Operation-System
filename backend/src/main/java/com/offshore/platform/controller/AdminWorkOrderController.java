package com.offshore.platform.controller;

import com.offshore.platform.common.log.OperationLog;
import com.offshore.platform.common.page.PageRequestDTO;
import com.offshore.platform.common.page.PageResult;
import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.dto.admin.ProjectRequest;
import com.offshore.platform.dto.admin.WorkOrderAssignRequest;
import com.offshore.platform.dto.admin.WorkOrderFromTemplateRequest;
import com.offshore.platform.dto.admin.WorkOrderQueryRequest;
import com.offshore.platform.dto.admin.WorkOrderRequest;
import com.offshore.platform.dto.admin.WorkOrderStatusRequest;
import com.offshore.platform.dto.admin.WorkOrderTemplateRequest;
import com.offshore.platform.service.AdminWorkOrderService;
import com.offshore.platform.vo.admin.ProjectVO;
import com.offshore.platform.vo.admin.WorkOrderDetailVO;
import com.offshore.platform.vo.admin.WorkOrderTemplateVO;
import com.offshore.platform.vo.admin.WorkOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "admin-work-order", description = "PC后台项目与工单管理")
@Validated
@RestController
@RequestMapping("/api/admin")
public class AdminWorkOrderController {
    private final AdminWorkOrderService adminWorkOrderService;

    public AdminWorkOrderController(AdminWorkOrderService adminWorkOrderService) {
        this.adminWorkOrderService = adminWorkOrderService;
    }

    @Operation(summary = "项目列表")
    @GetMapping("/projects")
    public ApiResponse<PageResult<ProjectVO>> listProjects(@Valid PageRequestDTO pageRequest) {
        return ApiResponse.success(adminWorkOrderService.listProjects(pageRequest));
    }

    @Operation(summary = "项目详情")
    @GetMapping("/projects/{id}")
    public ApiResponse<ProjectVO> getProject(@PathVariable Long id) {
        return ApiResponse.success(adminWorkOrderService.getProject(id));
    }

    @Operation(summary = "创建项目")
    @PostMapping("/projects")
    public ApiResponse<ProjectVO> createProject(@Valid @RequestBody ProjectRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(adminWorkOrderService.createProject(request, servletRequest));
    }

    @Operation(summary = "更新项目")
    @PutMapping("/projects/{id}")
    public ApiResponse<ProjectVO> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(adminWorkOrderService.updateProject(id, request, servletRequest));
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/projects/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable Long id, HttpServletRequest servletRequest) {
        adminWorkOrderService.deleteProject(id, servletRequest);
        return ApiResponse.success();
    }

    @Operation(summary = "工单列表")
    @GetMapping("/work-orders")
    public ApiResponse<PageResult<WorkOrderVO>> listWorkOrders(@Valid WorkOrderQueryRequest query) {
        return ApiResponse.success(adminWorkOrderService.listWorkOrders(query));
    }

    @Operation(summary = "工单详情")
    @GetMapping("/work-orders/{id}")
    public ApiResponse<WorkOrderDetailVO> getWorkOrder(@PathVariable Long id) {
        return ApiResponse.success(adminWorkOrderService.getWorkOrder(id));
    }

    @Operation(summary = "创建工单")
    @OperationLog(module = "WORK_ORDER", operation = "CREATE_WORK_ORDER", businessType = "WORK_ORDER")
    @PostMapping("/work-orders")
    public ApiResponse<WorkOrderVO> createWorkOrder(@Valid @RequestBody WorkOrderRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(adminWorkOrderService.createWorkOrder(request, servletRequest));
    }

    @Operation(summary = "更新工单")
    @OperationLog(module = "WORK_ORDER", operation = "UPDATE_WORK_ORDER", businessType = "WORK_ORDER")
    @PutMapping("/work-orders/{id}")
    public ApiResponse<WorkOrderVO> updateWorkOrder(@PathVariable Long id, @Valid @RequestBody WorkOrderRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(adminWorkOrderService.updateWorkOrder(id, request, servletRequest));
    }

    @Operation(summary = "删除工单")
    @OperationLog(module = "WORK_ORDER", operation = "DELETE_WORK_ORDER", businessType = "WORK_ORDER")
    @DeleteMapping("/work-orders/{id}")
    public ApiResponse<Void> deleteWorkOrder(@PathVariable Long id, HttpServletRequest servletRequest) {
        adminWorkOrderService.deleteWorkOrder(id, servletRequest);
        return ApiResponse.success();
    }

    @Operation(summary = "工单派工")
    @OperationLog(module = "WORK_ORDER", operation = "ASSIGN_WORK_ORDER", businessType = "WORK_ORDER")
    @PostMapping("/work-orders/{id}/assign")
    public ApiResponse<WorkOrderVO> assignWorkOrder(@PathVariable Long id,
            @Valid @RequestBody WorkOrderAssignRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(adminWorkOrderService.assignWorkOrder(id, request, servletRequest));
    }

    @Operation(summary = "工单状态流转")
    @OperationLog(module = "WORK_ORDER", operation = "CHANGE_WORK_ORDER_STATUS", businessType = "WORK_ORDER")
    @PostMapping("/work-orders/{id}/status")
    public ApiResponse<WorkOrderVO> changeWorkOrderStatus(@PathVariable Long id,
            @Valid @RequestBody WorkOrderStatusRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(adminWorkOrderService.changeWorkOrderStatus(id, request, servletRequest));
    }

    @Operation(summary = "工单状态流")
    @GetMapping("/work-orders/{id}/status-flow")
    public ApiResponse<List<WorkOrderDetailVO.StatusFlowVO>> getStatusFlow(@PathVariable Long id) {
        return ApiResponse.success(adminWorkOrderService.getStatusFlow(id));
    }

    @Operation(summary = "工单模板列表")
    @GetMapping("/work-order-templates")
    public ApiResponse<List<WorkOrderTemplateVO>> listTemplates() {
        return ApiResponse.success(adminWorkOrderService.listTemplates());
    }

    @Operation(summary = "创建工单模板")
    @PostMapping("/work-order-templates")
    public ApiResponse<WorkOrderTemplateVO> createTemplate(@Valid @RequestBody WorkOrderTemplateRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(adminWorkOrderService.createTemplate(request, servletRequest));
    }

    @Operation(summary = "更新工单模板")
    @PutMapping("/work-order-templates/{id}")
    public ApiResponse<WorkOrderTemplateVO> updateTemplate(@PathVariable Long id,
            @Valid @RequestBody WorkOrderTemplateRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(adminWorkOrderService.updateTemplate(id, request, servletRequest));
    }

    @Operation(summary = "删除工单模板")
    @DeleteMapping("/work-order-templates/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id, HttpServletRequest servletRequest) {
        adminWorkOrderService.deleteTemplate(id, servletRequest);
        return ApiResponse.success();
    }

    @Operation(summary = "根据模板创建工单")
    @PostMapping("/work-orders/from-template/{templateId}")
    public ApiResponse<WorkOrderVO> createFromTemplate(@PathVariable Long templateId,
            @Valid @RequestBody WorkOrderFromTemplateRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(adminWorkOrderService.createFromTemplate(templateId, request, servletRequest));
    }
}
