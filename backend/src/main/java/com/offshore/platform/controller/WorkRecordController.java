package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.dto.mobile.MobileCheckItemBatchRequest;
import com.offshore.platform.dto.mobile.MobileWorkRecordRequest;
import com.offshore.platform.service.WorkRecordService;
import com.offshore.platform.vo.admin.WorkRecordTimelineVO;
import com.offshore.platform.vo.admin.WorkRecordVO;
import com.offshore.platform.vo.mobile.MobileCheckItemVO;
import com.offshore.platform.vo.mobile.MobileWorkRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "mobile-work-order", description = "移动端工单作业")
@Validated
@RestController
@RequestMapping
public class WorkRecordController {
    private final WorkRecordService workRecordService;

    public WorkRecordController(WorkRecordService workRecordService) {
        this.workRecordService = workRecordService;
    }

    @Operation(summary = "移动端新增施工记录")
    @PostMapping("/api/mobile/work-orders/{workOrderId}/records")
    public ApiResponse<MobileWorkRecordVO> createMobileRecord(@PathVariable Long workOrderId,
            @Valid @RequestBody MobileWorkRecordRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.createMobileRecord(workOrderId, request, servletRequest));
    }

    @Operation(summary = "移动端更新施工记录")
    @PutMapping("/api/mobile/work-orders/{workOrderId}/records/{recordId}")
    public ApiResponse<MobileWorkRecordVO> updateMobileRecord(@PathVariable Long workOrderId,
            @PathVariable Long recordId, @Valid @RequestBody MobileWorkRecordRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.updateMobileRecord(workOrderId, recordId, request, servletRequest));
    }

    @Operation(summary = "移动端施工记录列表")
    @GetMapping("/api/mobile/work-orders/{workOrderId}/records")
    public ApiResponse<List<MobileWorkRecordVO>> listMobileRecords(@PathVariable Long workOrderId) {
        return ApiResponse.success(workRecordService.listMobileRecords(workOrderId));
    }

    @Operation(summary = "移动端提交施工检查项")
    @PostMapping("/api/mobile/work-records/{recordId}/check-items")
    public ApiResponse<List<MobileCheckItemVO>> createCheckItems(@PathVariable Long recordId,
            @Valid @RequestBody MobileCheckItemBatchRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.createCheckItems(recordId, request, servletRequest));
    }

    @Operation(summary = "PC后台施工记录列表")
    @GetMapping("/api/admin/work-orders/{workOrderId}/records")
    public ApiResponse<List<WorkRecordVO>> listAdminRecords(@PathVariable Long workOrderId) {
        return ApiResponse.success(workRecordService.listAdminRecords(workOrderId));
    }

    @Operation(summary = "PC后台施工记录详情")
    @GetMapping("/api/admin/work-records/{recordId}")
    public ApiResponse<WorkRecordVO> getAdminRecord(@PathVariable Long recordId) {
        return ApiResponse.success(workRecordService.getAdminRecord(recordId));
    }

    @Operation(summary = "PC后台施工记录时间线")
    @GetMapping("/api/admin/work-records/{recordId}/timeline")
    public ApiResponse<List<WorkRecordTimelineVO>> getTimeline(@PathVariable Long recordId) {
        return ApiResponse.success(workRecordService.getRecordTimeline(recordId));
    }

    @Operation(summary = "PC后台确认施工记录")
    @PostMapping("/api/admin/work-records/{recordId}/confirm")
    public ApiResponse<WorkRecordVO> confirmRecord(@PathVariable Long recordId, HttpServletRequest servletRequest) {
        return ApiResponse.success(workRecordService.confirmRecord(recordId, servletRequest));
    }
}
