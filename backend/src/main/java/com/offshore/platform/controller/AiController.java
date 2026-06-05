package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.common.log.OperationLog;
import com.offshore.platform.dto.ai.AiModelRequest;
import com.offshore.platform.dto.ai.AiResultRequest;
import com.offshore.platform.dto.ai.AiReviewRequest;
import com.offshore.platform.service.AiService;
import com.offshore.platform.vo.ai.AiModelVO;
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

    @GetMapping("/api/ai/results/{id}")
    public ApiResponse<AiResultVO> result(@PathVariable Long id) {
        return ApiResponse.success(aiService.getResult(id));
    }

    @GetMapping("/api/admin/work-orders/{workOrderId}/ai-results")
    public ApiResponse<List<AiResultVO>> adminResults(@PathVariable Long workOrderId) {
        return ApiResponse.success(aiService.adminWorkOrderResults(workOrderId));
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
}
