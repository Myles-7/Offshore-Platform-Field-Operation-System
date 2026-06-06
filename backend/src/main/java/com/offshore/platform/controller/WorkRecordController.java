package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.dto.workrecord.WorkOrderCheckItemBatchRequest;
import com.offshore.platform.dto.workrecord.WorkOrderCheckItemRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordCreateRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordDetailRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordQueryRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordRejectRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordUpdateRequest;
import com.offshore.platform.service.WorkRecordService;
import com.offshore.platform.vo.admin.WorkRecordTimelineVO;
import com.offshore.platform.vo.mobile.MobileWorkRecordVO;
import com.offshore.platform.vo.workrecord.WorkOrderCheckItemVO;
import com.offshore.platform.vo.workrecord.WorkOrderRecordDetailVO;
import com.offshore.platform.vo.workrecord.WorkOrderRecordVO;
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

@Tag(name = "mobile-work-order", description = "Mobile and admin work record APIs")
@Validated
@RestController
@RequestMapping
public class WorkRecordController {
    private final WorkRecordService workRecordService;

    public WorkRecordController(WorkRecordService workRecordService) {
        this.workRecordService = workRecordService;
    }

    @Operation(summary = "Create mobile work record")
    @PostMapping("/api/mobile/work-orders/{workOrderId}/records")
    public ApiResponse<WorkOrderRecordVO> createMobileRecord(@PathVariable Long workOrderId,
            @Valid @RequestBody WorkOrderRecordCreateRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.createMobileRecord(workOrderId, request, servletRequest));
    }

    @Operation(summary = "Update mobile work record")
    @PutMapping("/api/mobile/work-orders/{workOrderId}/records/{recordId}")
    public ApiResponse<WorkOrderRecordVO> updateMobileRecord(@PathVariable Long workOrderId,
            @PathVariable Long recordId, @Valid @RequestBody WorkOrderRecordUpdateRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.updateMobileRecord(workOrderId, recordId, request, servletRequest));
    }

    @Operation(summary = "List mobile work records")
    @GetMapping("/api/mobile/work-orders/{workOrderId}/records")
    public ApiResponse<List<MobileWorkRecordVO>> listMobileRecords(@PathVariable Long workOrderId) {
        return ApiResponse.success(workRecordService.listMobileRecords(workOrderId));
    }

    @Operation(summary = "Get mobile work record detail")
    @GetMapping("/api/mobile/work-orders/{workOrderId}/records/{recordId}")
    public ApiResponse<WorkOrderRecordVO> getMobileRecord(@PathVariable Long workOrderId, @PathVariable Long recordId) {
        return ApiResponse.success(workRecordService.getMobileRecord(workOrderId, recordId));
    }

    @Operation(summary = "Delete mobile work record")
    @DeleteMapping("/api/mobile/work-orders/{workOrderId}/records/{recordId}")
    public ApiResponse<Void> deleteMobileRecord(@PathVariable Long workOrderId, @PathVariable Long recordId,
            HttpServletRequest servletRequest) {
        workRecordService.deleteMobileRecord(workOrderId, recordId, servletRequest);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Create work record detail")
    @PostMapping("/api/mobile/work-records/{recordId}/details")
    public ApiResponse<WorkOrderRecordDetailVO> createDetail(@PathVariable Long recordId,
            @Valid @RequestBody WorkOrderRecordDetailRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.createDetail(recordId, request, servletRequest));
    }

    @Operation(summary = "Update work record detail")
    @PutMapping("/api/mobile/work-records/{recordId}/details/{detailId}")
    public ApiResponse<WorkOrderRecordDetailVO> updateDetail(@PathVariable Long recordId, @PathVariable Long detailId,
            @Valid @RequestBody WorkOrderRecordDetailRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.updateDetail(recordId, detailId, request, servletRequest));
    }

    @Operation(summary = "Delete work record detail")
    @DeleteMapping("/api/mobile/work-records/{recordId}/details/{detailId}")
    public ApiResponse<Void> deleteDetail(@PathVariable Long recordId, @PathVariable Long detailId,
            HttpServletRequest servletRequest) {
        workRecordService.deleteDetail(recordId, detailId, servletRequest);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Create or batch save work record check items")
    @PostMapping("/api/mobile/work-records/{recordId}/check-items")
    public ApiResponse<List<WorkOrderCheckItemVO>> createCheckItems(@PathVariable Long recordId,
            @Valid @RequestBody WorkOrderCheckItemBatchRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.saveCheckItems(recordId, request, servletRequest));
    }

    @Operation(summary = "Update work record check item")
    @PutMapping("/api/mobile/work-records/{recordId}/check-items/{itemId}")
    public ApiResponse<WorkOrderCheckItemVO> updateCheckItem(@PathVariable Long recordId, @PathVariable Long itemId,
            @Valid @RequestBody WorkOrderCheckItemRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.updateCheckItem(recordId, itemId, request, servletRequest));
    }

    @Operation(summary = "Delete work record check item")
    @DeleteMapping("/api/mobile/work-records/{recordId}/check-items/{itemId}")
    public ApiResponse<Void> deleteCheckItem(@PathVariable Long recordId, @PathVariable Long itemId,
            HttpServletRequest servletRequest) {
        workRecordService.deleteCheckItem(recordId, itemId, servletRequest);
        return ApiResponse.success(null);
    }

    @Operation(summary = "List admin work records")
    @GetMapping("/api/admin/work-orders/{workOrderId}/records")
    public ApiResponse<List<WorkOrderRecordVO>> listAdminRecords(@PathVariable Long workOrderId,
            @Valid WorkOrderRecordQueryRequest request) {
        return ApiResponse.success(workRecordService.listAdminRecords(workOrderId, request));
    }

    @Operation(summary = "Get admin work record detail")
    @GetMapping("/api/admin/work-records/{recordId}")
    public ApiResponse<WorkOrderRecordVO> getAdminRecord(@PathVariable Long recordId) {
        return ApiResponse.success(workRecordService.getAdminRecordDetail(recordId));
    }

    @Operation(summary = "Get admin work record timeline")
    @GetMapping("/api/admin/work-records/{recordId}/timeline")
    public ApiResponse<List<WorkRecordTimelineVO>> getTimeline(@PathVariable Long recordId) {
        return ApiResponse.success(workRecordService.getRecordTimeline(recordId));
    }

    @Operation(summary = "Confirm admin work record")
    @PostMapping("/api/admin/work-records/{recordId}/confirm")
    public ApiResponse<com.offshore.platform.vo.admin.WorkRecordVO> confirmRecord(@PathVariable Long recordId,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.confirmRecord(recordId, servletRequest));
    }

    @Operation(summary = "Reject admin work record")
    @PostMapping("/api/admin/work-records/{recordId}/reject")
    public ApiResponse<WorkOrderRecordVO> rejectRecord(@PathVariable Long recordId,
            @Valid @RequestBody WorkOrderRecordRejectRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.rejectRecord(recordId, request, servletRequest));
    }
}
