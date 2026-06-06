package com.offshore.platform.controller;

import com.offshore.platform.common.log.OperationLog;
import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.dto.acceptance.AcceptanceLockRequest;
import com.offshore.platform.dto.acceptance.AcceptanceRelockRequest;
import com.offshore.platform.dto.acceptance.AcceptanceRequest;
import com.offshore.platform.dto.acceptance.AcceptanceReviewRequest;
import com.offshore.platform.dto.acceptance.AcceptanceUnlockRequest;
import com.offshore.platform.dto.acceptance.PdfGenerateRequest;
import com.offshore.platform.dto.acceptance.PdfMetadataRequest;
import com.offshore.platform.dto.acceptance.PdfVoidRequest;
import com.offshore.platform.dto.acceptance.SignatureRequest;
import com.offshore.platform.service.AcceptanceService;
import com.offshore.platform.vo.acceptance.AcceptanceLockHistoryVO;
import com.offshore.platform.vo.acceptance.AcceptanceLockStatusVO;
import com.offshore.platform.vo.acceptance.AcceptanceVO;
import com.offshore.platform.vo.acceptance.PdfVO;
import com.offshore.platform.vo.acceptance.SignatureVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "acceptance", description = "电子签名与PDF验收")
@RestController
public class AcceptanceController {
    private final AcceptanceService acceptanceService;

    public AcceptanceController(AcceptanceService acceptanceService) {
        this.acceptanceService = acceptanceService;
    }

    @Operation(summary = "移动端提交签名元数据")
    @OperationLog(module = "ACCEPTANCE", operation = "CREATE_SIGNATURE", businessType = "WORK_ORDER", platform = "MOBILE")
    @PostMapping("/api/mobile/work-orders/{workOrderId}/signatures")
    public ApiResponse<SignatureVO> createSignature(@PathVariable Long workOrderId,
            @Valid @RequestBody SignatureRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.createSignature(workOrderId, request, servletRequest));
    }

    @Operation(summary = "Mobile list signatures")
    @GetMapping("/api/mobile/work-orders/{workOrderId}/signatures")
    public ApiResponse<List<SignatureVO>> listMobileSignatures(@PathVariable Long workOrderId) {
        return ApiResponse.success(acceptanceService.listMobileSignatures(workOrderId));
    }

    @Operation(summary = "移动端提交验收记录")
    @OperationLog(module = "ACCEPTANCE", operation = "SUBMIT_ACCEPTANCE_RECORD", businessType = "WORK_ORDER", platform = "MOBILE")
    @PostMapping("/api/mobile/work-orders/{workOrderId}/acceptance")
    public ApiResponse<AcceptanceVO> submitAcceptance(@PathVariable Long workOrderId,
            @RequestBody AcceptanceRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.submitAcceptance(workOrderId, request, servletRequest));
    }

    @Operation(summary = "Mobile update acceptance")
    @PutMapping("/api/mobile/work-orders/{workOrderId}/acceptance/{acceptanceId}")
    public ApiResponse<AcceptanceVO> updateAcceptance(@PathVariable Long workOrderId, @PathVariable Long acceptanceId,
            @RequestBody AcceptanceRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.updateAcceptance(workOrderId, acceptanceId, request, servletRequest));
    }

    @Operation(summary = "移动端提交PDF元数据")
    @PostMapping("/api/mobile/work-orders/{workOrderId}/pdf/metadata")
    public ApiResponse<PdfVO> savePdfMetadata(@PathVariable Long workOrderId,
            @Valid @RequestBody PdfMetadataRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.savePdfMetadata(workOrderId, request, servletRequest));
    }

    @Operation(summary = "Mobile list PDF metadata")
    @GetMapping("/api/mobile/work-orders/{workOrderId}/pdf")
    public ApiResponse<List<PdfVO>> listMobilePdfs(@PathVariable Long workOrderId) {
        return ApiResponse.success(acceptanceService.listMobilePdfs(workOrderId));
    }

    @Operation(summary = "PC查询工单签名")
    @GetMapping("/api/admin/work-orders/{workOrderId}/signatures")
    public ApiResponse<List<SignatureVO>> listSignatures(@PathVariable Long workOrderId) {
        return ApiResponse.success(acceptanceService.listSignatures(workOrderId));
    }

    @Operation(summary = "PC create signature metadata")
    @PostMapping("/api/admin/work-orders/{workOrderId}/signatures")
    public ApiResponse<SignatureVO> createAdminSignature(@PathVariable Long workOrderId,
            @Valid @RequestBody SignatureRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.createAdminSignature(workOrderId, request, servletRequest));
    }

    @Operation(summary = "PC查询工单验收记录")
    @GetMapping("/api/admin/work-orders/{workOrderId}/acceptance")
    public ApiResponse<List<AcceptanceVO>> listAcceptance(@PathVariable Long workOrderId) {
        return ApiResponse.success(acceptanceService.listAcceptance(workOrderId));
    }

    @Operation(summary = "PC验收复核")
    @OperationLog(module = "ACCEPTANCE", operation = "REVIEW_ACCEPTANCE", businessType = "WORK_ORDER")
    @PostMapping("/api/admin/work-orders/{workOrderId}/acceptance/review")
    public ApiResponse<AcceptanceVO> reviewAcceptance(@PathVariable Long workOrderId,
            @Valid @RequestBody AcceptanceReviewRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.reviewAcceptance(workOrderId, request, servletRequest));
    }

    @Operation(summary = "PC生成PDF验收单")
    @OperationLog(module = "ACCEPTANCE", operation = "GENERATE_PDF", businessType = "WORK_ORDER")
    @PostMapping("/api/admin/work-orders/{workOrderId}/pdf/generate")
    public ApiResponse<PdfVO> generatePdf(@PathVariable Long workOrderId,
            @RequestBody(required = false) PdfGenerateRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.generatePdf(workOrderId,
                request == null ? new PdfGenerateRequest() : request, servletRequest));
    }

    @Operation(summary = "PC查询PDF验收单列表")
    @GetMapping("/api/admin/work-orders/{workOrderId}/pdf")
    public ApiResponse<List<PdfVO>> listPdfs(@PathVariable Long workOrderId) {
        return ApiResponse.success(acceptanceService.listPdfs(workOrderId));
    }

    @Operation(summary = "PC下载最新PDF验收单")
    @OperationLog(module = "ACCEPTANCE", operation = "DOWNLOAD_PDF", businessType = "WORK_ORDER")
    @GetMapping("/api/admin/work-orders/{workOrderId}/pdf/download")
    public ResponseEntity<Void> downloadPdf(@PathVariable Long workOrderId) {
        PdfVO pdf = acceptanceService.latestPdf(workOrderId);
        return ResponseEntity.status(302).location(URI.create("/api/files/" + pdf.fileId + "/download")).build();
    }

    @Operation(summary = "PC archive PDF")
    @PostMapping("/api/admin/work-orders/{workOrderId}/pdf/archive")
    public ApiResponse<PdfVO> archivePdf(@PathVariable Long workOrderId, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.archivePdf(workOrderId, servletRequest));
    }

    @Operation(summary = "PC acceptance lock status")
    @GetMapping("/api/admin/work-orders/{workOrderId}/acceptance/lock-status")
    public ApiResponse<AcceptanceLockStatusVO> lockStatus(@PathVariable Long workOrderId) {
        return ApiResponse.success(acceptanceService.lockStatus(workOrderId));
    }

    @Operation(summary = "PC lock acceptance")
    @PostMapping("/api/admin/work-orders/{workOrderId}/acceptance/lock")
    public ApiResponse<AcceptanceLockStatusVO> lockAcceptance(@PathVariable Long workOrderId,
            @Valid @RequestBody AcceptanceLockRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.lockAcceptance(workOrderId, request, servletRequest));
    }

    @Operation(summary = "PC unlock acceptance")
    @PostMapping("/api/admin/work-orders/{workOrderId}/acceptance/unlock")
    public ApiResponse<AcceptanceLockStatusVO> unlockAcceptance(@PathVariable Long workOrderId,
            @Valid @RequestBody AcceptanceUnlockRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.unlockAcceptance(workOrderId, request, servletRequest));
    }

    @Operation(summary = "PC relock acceptance")
    @PostMapping("/api/admin/work-orders/{workOrderId}/acceptance/relock")
    public ApiResponse<AcceptanceLockStatusVO> relockAcceptance(@PathVariable Long workOrderId,
            @Valid @RequestBody AcceptanceRelockRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.relockAcceptance(workOrderId, request, servletRequest));
    }

    @Operation(summary = "PC void acceptance PDF")
    @PostMapping("/api/admin/work-orders/{workOrderId}/pdf/{pdfId}/void")
    public ApiResponse<PdfVO> voidPdf(@PathVariable Long workOrderId, @PathVariable Long pdfId,
            @Valid @RequestBody PdfVoidRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(acceptanceService.voidPdf(workOrderId, pdfId, request, servletRequest));
    }

    @Operation(summary = "PC acceptance lock history")
    @GetMapping("/api/admin/work-orders/{workOrderId}/acceptance/lock-history")
    public ApiResponse<List<AcceptanceLockHistoryVO>> lockHistory(@PathVariable Long workOrderId) {
        return ApiResponse.success(acceptanceService.lockHistory(workOrderId));
    }
}
