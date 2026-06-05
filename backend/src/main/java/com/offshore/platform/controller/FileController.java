package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.common.log.OperationLog;
import com.offshore.platform.dto.file.ChunkInitRequest;
import com.offshore.platform.dto.file.ChunkMergeRequest;
import com.offshore.platform.service.FileService;
import com.offshore.platform.vo.file.ChunkInitVO;
import com.offshore.platform.vo.file.FileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "file", description = "文件附件管理")
@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "上传文件")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @OperationLog(module = "FILE", operation = "UPLOAD_FILE", businessType = "FILE_STORAGE", platform = "MOBILE")
    public ApiResponse<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "workOrderId", required = false) Long workOrderId,
            @RequestParam(value = "recordId", required = false) Long recordId,
            @RequestParam(value = "localId", required = false) String localId,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            HttpServletRequest request) {
        return ApiResponse.success(fileService.upload(file, fileType, workOrderId, recordId, localId, deviceId, request));
    }

    @Operation(summary = "批量上传文件")
    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<FileUploadVO>> batchUpload(@RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "workOrderId", required = false) Long workOrderId,
            @RequestParam(value = "recordId", required = false) Long recordId,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            HttpServletRequest request) {
        return ApiResponse.success(fileService.batchUpload(files, fileType, workOrderId, recordId, deviceId, request));
    }

    @Operation(summary = "初始化分片上传")
    @PostMapping("/chunk/init")
    public ApiResponse<ChunkInitVO> initChunk(@Valid @RequestBody ChunkInitRequest request) {
        return ApiResponse.success(fileService.initChunkUpload(request));
    }

    @Operation(summary = "上传文件分片")
    @PostMapping(value = "/chunk/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ChunkInitVO> uploadChunk(@RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileService.uploadChunk(uploadId, chunkIndex, file));
    }

    @Operation(summary = "合并文件分片")
    @PostMapping("/chunk/merge")
    public ApiResponse<FileUploadVO> mergeChunk(@Valid @RequestBody ChunkMergeRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(fileService.mergeChunks(request, servletRequest));
    }

    @Operation(summary = "预览文件")
    @GetMapping("/{fileId}/preview")
    public ResponseEntity<Resource> preview(@PathVariable String fileId, HttpServletRequest request) {
        return resourceResponse(fileService.loadForPreview(fileId, request), false);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{fileId}/download")
    @OperationLog(module = "FILE", operation = "DOWNLOAD_FILE", businessType = "FILE_STORAGE")
    public ResponseEntity<Resource> download(@PathVariable String fileId, HttpServletRequest request) {
        return resourceResponse(fileService.loadForDownload(fileId, request), true);
    }

    @Operation(summary = "作废文件元数据")
    @DeleteMapping("/{fileId}")
    @OperationLog(module = "FILE", operation = "DELETE_FILE", businessType = "FILE_STORAGE")
    public ApiResponse<Void> delete(@PathVariable String fileId, HttpServletRequest request) {
        fileService.delete(fileId, request);
        return ApiResponse.success();
    }

    private ResponseEntity<Resource> resourceResponse(FileService.FileDownloadResource file, boolean attachment) {
        MediaType mediaType = MediaType.parseMediaType(file.getMimeType());
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=0")
                .contentLength(file.getFileSize() == null ? -1 : file.getFileSize());
        if (attachment) {
            builder.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                    .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                    .build()
                    .toString());
        }
        return builder.body(file.getResource());
    }
}
