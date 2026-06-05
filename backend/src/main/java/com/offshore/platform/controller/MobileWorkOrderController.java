package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.dto.mobile.MobileFeedbackRequest;
import com.offshore.platform.dto.mobile.MobileSubmitAcceptanceRequest;
import com.offshore.platform.service.MobileWorkOrderService;
import com.offshore.platform.vo.mobile.MobileMaterialVO;
import com.offshore.platform.vo.mobile.MobileQualificationCheckVO;
import com.offshore.platform.vo.mobile.MobileWorkOrderDetailVO;
import com.offshore.platform.vo.mobile.MobileWorkOrderListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "mobile-work-order", description = "移动端工单作业")
@Validated
@RestController
@RequestMapping("/api/mobile/work-orders")
public class MobileWorkOrderController {
    private final MobileWorkOrderService mobileWorkOrderService;

    public MobileWorkOrderController(MobileWorkOrderService mobileWorkOrderService) {
        this.mobileWorkOrderService = mobileWorkOrderService;
    }

    @Operation(summary = "移动端我的工单列表")
    @GetMapping
    public ApiResponse<List<MobileWorkOrderListVO>> listMyWorkOrders() {
        return ApiResponse.success(mobileWorkOrderService.listMyWorkOrders());
    }

    @Operation(summary = "移动端工单详情")
    @GetMapping("/{id}")
    public ApiResponse<MobileWorkOrderDetailVO> getMyWorkOrder(@PathVariable Long id) {
        return ApiResponse.success(mobileWorkOrderService.getMyWorkOrder(id));
    }

    @Operation(summary = "移动端接单")
    @PostMapping("/{id}/accept")
    public ApiResponse<MobileWorkOrderDetailVO> acceptWorkOrder(@PathVariable Long id, HttpServletRequest servletRequest) {
        return ApiResponse.success(mobileWorkOrderService.acceptWorkOrder(id, servletRequest));
    }

    @Operation(summary = "移动端开始施工")
    @PostMapping("/{id}/start")
    public ApiResponse<MobileWorkOrderDetailVO> startWorkOrder(@PathVariable Long id, HttpServletRequest servletRequest) {
        return ApiResponse.success(mobileWorkOrderService.startWorkOrder(id, servletRequest));
    }

    @Operation(summary = "移动端工单反馈")
    @PostMapping("/{id}/feedback")
    public ApiResponse<MobileWorkOrderDetailVO> feedback(@PathVariable Long id,
            @Valid @RequestBody MobileFeedbackRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(mobileWorkOrderService.feedback(id, request, servletRequest));
    }

    @Operation(summary = "移动端提交验收")
    @PostMapping("/{id}/submit-acceptance")
    public ApiResponse<MobileWorkOrderDetailVO> submitAcceptance(@PathVariable Long id,
            @Valid @RequestBody MobileSubmitAcceptanceRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(mobileWorkOrderService.submitAcceptance(id, request, servletRequest));
    }

    @Operation(summary = "移动端工单所需物料")
    @GetMapping("/{id}/materials")
    public ApiResponse<List<MobileMaterialVO>> listMaterials(@PathVariable Long id) {
        return ApiResponse.success(mobileWorkOrderService.listMaterials(id));
    }

    @Operation(summary = "移动端工单资质检查")
    @GetMapping("/{id}/qualification-check")
    public ApiResponse<List<MobileQualificationCheckVO>> listQualificationChecks(@PathVariable Long id) {
        return ApiResponse.success(mobileWorkOrderService.listQualificationChecks(id));
    }
}
