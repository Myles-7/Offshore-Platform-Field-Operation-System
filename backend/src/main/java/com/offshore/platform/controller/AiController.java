package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.common.log.OperationLog;
import com.offshore.platform.dto.ai.AiBatchReviewRequest;
import com.offshore.platform.dto.ai.AiDefectBoxRequest;
import com.offshore.platform.dto.ai.AiModelRequest;
import com.offshore.platform.dto.ai.AiResultRequest;
import com.offshore.platform.dto.ai.AiResultQueryRequest;
import com.offshore.platform.dto.ai.AiReviewRequest;
import com.offshore.platform.service.AiService;
import com.offshore.platform.vo.ai.AiModelVO;
import com.offshore.platform.vo.ai.AiDefectBoxVO;
import com.offshore.platform.vo.ai.AiResultDetailVO;
import com.offshore.platform.vo.ai.AiResultVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ai", description = "AI assisted acceptance")
@RestController
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/api/admin/ai/models")
    public ApiResponse<List<AiModelVO>> models() {
        return ApiResponse.success(aiService.listModels());
    }

    @PostMapping("/api/admin/ai/models")
    public ApiResponse<AiModelVO> createModel(@Valid @RequestBody AiModelRequest request) {
        return ApiResponse.success(aiService.createModel(request));
    }

    @PutMapping("/api/admin/ai/models/{id}/activate")
    public ApiResponse<AiModelVO> activateModel(@PathVariable Long id) {
        return ApiResponse.success(aiService.activateModel(id));
    }

    @PostMapping("/api/ai/results")
    public ApiResponse<AiResultVO> createResult(@Valid @RequestBody AiResultRequest request) {
        return ApiResponse.success(aiService.createResult(request));
    }

    @GetMapping("/api/admin/ai/results")
    public ApiResponse<List<AiResultVO>> adminResults(AiResultQueryRequest request) {
        return ApiResponse.success(aiService.adminResults(request));
    }

    @GetMapping("/api/ai/results/{id}")
    public ApiResponse<AiResultVO> result(@PathVariable Long id) {
        return ApiResponse.success(aiService.getResult(id));
    }

    @GetMapping("/api/admin/ai/results/{id}")
    public ApiResponse<AiResultDetailVO> adminResult(@PathVariable Long id) {
        return ApiResponse.success(aiService.getResultDetail(id));
    }

    @GetMapping("/api/admin/work-orders/{workOrderId}/ai-results")
    public ApiResponse<List<AiResultVO>> adminResults(@PathVariable Long workOrderId) {
        return ApiResponse.success(aiService.adminWorkOrderResults(workOrderId));
    }

    @GetMapping("/api/admin/work-records/{recordId}/ai-results")
    public ApiResponse<List<AiResultVO>> adminRecordResults(@PathVariable Long recordId) {
        return ApiResponse.success(aiService.adminRecordResults(recordId));
    }

    @GetMapping("/api/mobile/work-orders/{workOrderId}/ai-results")
    public ApiResponse<List<AiResultVO>> mobileResults(@PathVariable Long workOrderId) {
        return ApiResponse.success(aiService.mobileWorkOrderResults(workOrderId));
    }

    @PostMapping("/api/admin/ai/results/{id}/review")
    @OperationLog(module = "AI", operation = "REVIEW_AI_RESULT", businessType = "AI_RESULT")
    public ApiResponse<AiResultVO> review(@PathVariable Long id, @Valid @RequestBody AiReviewRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(aiService.review(id, request, servletRequest));
    }

    @PostMapping("/api/admin/ai/results/{id}/boxes/{boxId}/review")
    @OperationLog(module = "AI", operation = "REVIEW_AI_BOX", businessType = "AI_RESULT")
    public ApiResponse<AiDefectBoxVO> reviewBox(@PathVariable Long id, @PathVariable Long boxId,
            @Valid @RequestBody AiDefectBoxRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(aiService.reviewBox(id, boxId, request, servletRequest));
    }

    @PostMapping("/api/admin/ai/results/batch-review")
    @OperationLog(module = "AI", operation = "BATCH_REVIEW_AI_RESULT", businessType = "AI_RESULT")
    public ApiResponse<List<AiResultVO>> batchReview(@Valid @RequestBody AiBatchReviewRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(aiService.batchReview(request, servletRequest));
    }
}
