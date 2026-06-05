package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.dto.file.AttachmentBindRequest;
import com.offshore.platform.service.FileService;
import com.offshore.platform.vo.file.WorkOrderAttachmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "file", description = "工单附件绑定")
@RestController
public class WorkOrderAttachmentController {
    private final FileService fileService;

    public WorkOrderAttachmentController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "移动端绑定工单附件")
    @PostMapping("/api/mobile/work-orders/{workOrderId}/attachments")
    public ApiResponse<WorkOrderAttachmentVO> bindMobileAttachment(@PathVariable Long workOrderId,
            @Valid @RequestBody AttachmentBindRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(fileService.bindMobileWorkOrderAttachment(workOrderId, request, servletRequest));
    }

    @Operation(summary = "移动端查询工单附件")
    @GetMapping("/api/mobile/work-orders/{workOrderId}/attachments")
    public ApiResponse<List<WorkOrderAttachmentVO>> listMobileAttachments(@PathVariable Long workOrderId) {
        return ApiResponse.success(fileService.listMobileWorkOrderAttachments(workOrderId));
    }

    @Operation(summary = "PC后台查询工单附件")
    @GetMapping("/api/admin/work-orders/{workOrderId}/attachments")
    public ApiResponse<List<WorkOrderAttachmentVO>> listAdminAttachments(@PathVariable Long workOrderId) {
        return ApiResponse.success(fileService.listAdminWorkOrderAttachments(workOrderId));
    }
}
