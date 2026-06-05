package com.offshore.platform.service;

import com.offshore.platform.dto.file.AttachmentBindRequest;
import com.offshore.platform.dto.file.ChunkInitRequest;
import com.offshore.platform.dto.file.ChunkMergeRequest;
import com.offshore.platform.vo.file.ChunkInitVO;
import com.offshore.platform.vo.file.FileUploadVO;
import com.offshore.platform.vo.file.WorkOrderAttachmentVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadVO upload(MultipartFile file, String fileType, Long workOrderId, Long recordId,
            String localId, String deviceId, HttpServletRequest request);

    List<FileUploadVO> batchUpload(MultipartFile[] files, String fileType, Long workOrderId, Long recordId,
            String deviceId, HttpServletRequest request);

    ChunkInitVO initChunkUpload(ChunkInitRequest request);

    ChunkInitVO uploadChunk(String uploadId, Integer chunkIndex, MultipartFile file);

    FileUploadVO mergeChunks(ChunkMergeRequest request, HttpServletRequest servletRequest);

    FileDownloadResource loadForPreview(String fileId, HttpServletRequest request);

    FileDownloadResource loadForDownload(String fileId, HttpServletRequest request);

    void delete(String fileId, HttpServletRequest request);

    WorkOrderAttachmentVO bindMobileWorkOrderAttachment(Long workOrderId, AttachmentBindRequest request,
            HttpServletRequest servletRequest);

    List<WorkOrderAttachmentVO> listMobileWorkOrderAttachments(Long workOrderId);

    List<WorkOrderAttachmentVO> listAdminWorkOrderAttachments(Long workOrderId);

    class FileDownloadResource {
        private final Resource resource;
        private final String originalName;
        private final String mimeType;
        private final Long fileSize;

        public FileDownloadResource(Resource resource, String originalName, String mimeType, Long fileSize) {
            this.resource = resource;
            this.originalName = originalName;
            this.mimeType = mimeType;
            this.fileSize = fileSize;
        }

        public Resource getResource() { return resource; }
        public String getOriginalName() { return originalName; }
        public String getMimeType() { return mimeType; }
        public Long getFileSize() { return fileSize; }
    }
}
