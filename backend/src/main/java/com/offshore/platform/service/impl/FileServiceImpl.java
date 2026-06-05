package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.file.AttachmentBindRequest;
import com.offshore.platform.dto.file.ChunkInitRequest;
import com.offshore.platform.dto.file.ChunkMergeRequest;
import com.offshore.platform.entity.FileStorage;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.mapper.FileStorageMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.FileService;
import com.offshore.platform.vo.file.ChunkInitVO;
import com.offshore.platform.vo.file.FileUploadVO;
import com.offshore.platform.vo.file.WorkOrderAttachmentVO;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024;

    private final FileStorageMapper fileStorageMapper;
    private final WorkOrderAttachmentMapper workOrderAttachmentMapper;
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderAssignmentMapper workOrderAssignmentMapper;
    private final OperationLogMapper operationLogMapper;
    private final DataScopeService dataScopeService;
    private final Path storageRoot;

    public FileServiceImpl(FileStorageMapper fileStorageMapper, WorkOrderAttachmentMapper workOrderAttachmentMapper,
            WorkOrderMapper workOrderMapper, WorkOrderAssignmentMapper workOrderAssignmentMapper,
            OperationLogMapper operationLogMapper, DataScopeService dataScopeService,
            @Value("${app.file.storage-root:uploads}") String storageRoot) {
        this.fileStorageMapper = fileStorageMapper;
        this.workOrderAttachmentMapper = workOrderAttachmentMapper;
        this.workOrderMapper = workOrderMapper;
        this.workOrderAssignmentMapper = workOrderAssignmentMapper;
        this.operationLogMapper = operationLogMapper;
        this.dataScopeService = dataScopeService;
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public FileUploadVO upload(MultipartFile file, String fileType, Long workOrderId, Long recordId,
            String localId, String deviceId, HttpServletRequest request) {
        CurrentUser currentUser = CurrentUserContext.require();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "上传文件不能为空");
        }
        WorkOrder order = null;
        if (workOrderId != null) {
            order = requireAccessibleWorkOrder(workOrderId, currentUser);
        }
        try {
            Files.createDirectories(storageRoot);
            String originalName = sanitizeOriginalName(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + extension(originalName);
            Path relativePath = Paths.get(LocalDateTime.now().format(DAY_FORMATTER), storedName);
            Path target = resolveStoragePath(relativePath.toString());
            Files.createDirectories(target.getParent());
            String hash;
            try (InputStream inputStream = file.getInputStream()) {
                hash = copyWithSha256(inputStream, target);
            }
            FileStorage storage = buildStorage(currentUser, originalName, storedName, normalizeFileType(fileType, file.getContentType(), originalName),
                    normalizeMimeType(file.getContentType()), file.getSize(), relativePath.toString().replace('\\', '/'), hash,
                    workOrderId, recordId, localId, deviceId);
            fileStorageMapper.insert(storage);
            storage.setServerId(storage.getId());
            fileStorageMapper.updateById(storage);
            writeOperationLog(currentUser, request, "UPLOAD_FILE", "FILE", storage.getId(), storage.getFileId(),
                    order == null ? null : order.getProjectId(), "BOTH");
            return toUploadVO(storage);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "文件保存失败");
        }
    }

    @Override
    @Transactional
    public List<FileUploadVO> batchUpload(MultipartFile[] files, String fileType, Long workOrderId, Long recordId,
            String deviceId, HttpServletRequest request) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "批量上传文件不能为空");
        }
        return java.util.Arrays.stream(files)
                .map(file -> upload(file, fileType, workOrderId, recordId, null, deviceId, request))
                .toList();
    }

    @Override
    public ChunkInitVO initChunkUpload(ChunkInitRequest request) {
        ChunkInitVO vo = new ChunkInitVO();
        vo.uploadId = "CHUNK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-"
                + UUID.randomUUID().toString().substring(0, 8);
        vo.uploadUrl = "/api/files/chunk/upload";
        vo.mergeUrl = "/api/files/chunk/merge";
        vo.chunkSize = request.getChunkSize() == null ? DEFAULT_CHUNK_SIZE : request.getChunkSize();
        vo.totalChunks = request.getTotalChunks();
        vo.status = "INIT";
        return vo;
    }

    @Override
    public ChunkInitVO uploadChunk(String uploadId, Integer chunkIndex, MultipartFile file) {
        if (!StringUtils.hasText(uploadId) || chunkIndex == null || file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分片参数不完整");
        }
        try {
            Path chunkDir = resolveStoragePath(Paths.get("chunks", uploadId).toString());
            Files.createDirectories(chunkDir);
            Path chunkPath = chunkDir.resolve(String.valueOf(chunkIndex)).normalize();
            if (!chunkPath.startsWith(chunkDir)) {
                throw new BusinessException(ErrorCode.FILE_ERROR, "非法分片路径");
            }
            file.transferTo(chunkPath);
            ChunkInitVO vo = new ChunkInitVO();
            vo.uploadId = uploadId;
            vo.uploadUrl = "/api/files/chunk/upload";
            vo.mergeUrl = "/api/files/chunk/merge";
            vo.status = "CHUNK_UPLOADED";
            return vo;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "分片保存失败");
        }
    }

    @Override
    @Transactional
    public FileUploadVO mergeChunks(ChunkMergeRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        Path chunkDir = resolveStoragePath(Paths.get("chunks", request.getUploadId()).toString());
        if (!Files.isDirectory(chunkDir)) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "分片上传任务不存在");
        }
        WorkOrder order = null;
        if (request.getWorkOrderId() != null) {
            order = requireAccessibleWorkOrder(request.getWorkOrderId(), currentUser);
        }
        try {
            String originalName = sanitizeOriginalName(request.getOriginalName());
            String storedName = UUID.randomUUID() + extension(originalName);
            Path relativePath = Paths.get(LocalDateTime.now().format(DAY_FORMATTER), storedName);
            Path target = resolveStoragePath(relativePath.toString());
            Files.createDirectories(target.getParent());
            int totalChunks = request.getTotalChunks() == null ? countChunks(chunkDir) : request.getTotalChunks();
            String hash = mergeChunkFiles(chunkDir, target, totalChunks);
            // TODO: 后续补充分片断点续传清单、秒传和客户端声明 hash 的强校验。
            FileStorage storage = buildStorage(currentUser, originalName, storedName,
                    normalizeFileType(request.getFileType(), request.getMimeType(), originalName),
                    normalizeMimeType(request.getMimeType()), Files.size(target), relativePath.toString().replace('\\', '/'),
                    hash, request.getWorkOrderId(), request.getRecordId(), request.getLocalId(), request.getDeviceId());
            fileStorageMapper.insert(storage);
            storage.setServerId(storage.getId());
            fileStorageMapper.updateById(storage);
            deleteDirectoryQuietly(chunkDir);
            writeOperationLog(currentUser, servletRequest, "MERGE_FILE_CHUNKS", "FILE", storage.getId(), storage.getFileId(),
                    order == null ? null : order.getProjectId(), "BOTH");
            return toUploadVO(storage);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "分片合并失败");
        }
    }

    @Override
    public FileDownloadResource loadForPreview(String fileId, HttpServletRequest request) {
        FileStorage storage = requireFile(fileId);
        if (!Integer.valueOf(1).equals(storage.getPreviewEnabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "文件不允许预览");
        }
        checkFileAccess(storage, CurrentUserContext.require());
        writeOperationLog(CurrentUserContext.require(), request, "PREVIEW_FILE", "FILE", storage.getId(), storage.getFileId(),
                resolveProjectId(storage), "BOTH");
        return toDownloadResource(storage);
    }

    @Override
    public FileDownloadResource loadForDownload(String fileId, HttpServletRequest request) {
        FileStorage storage = requireFile(fileId);
        if (!Integer.valueOf(1).equals(storage.getDownloadEnabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "文件不允许下载");
        }
        checkFileAccess(storage, CurrentUserContext.require());
        writeOperationLog(CurrentUserContext.require(), request, "DOWNLOAD_FILE", "FILE", storage.getId(), storage.getFileId(),
                resolveProjectId(storage), "BOTH");
        return toDownloadResource(storage);
    }

    @Override
    @Transactional
    public void delete(String fileId, HttpServletRequest request) {
        CurrentUser currentUser = CurrentUserContext.require();
        FileStorage storage = requireFile(fileId);
        checkFileAccess(storage, currentUser);
        fileStorageMapper.softDeleteById(storage.getId());
        writeOperationLog(currentUser, request, "DELETE_FILE_METADATA", "FILE", storage.getId(), storage.getFileId(),
                resolveProjectId(storage), "BOTH");
    }

    @Override
    @Transactional
    public WorkOrderAttachmentVO bindMobileWorkOrderAttachment(Long workOrderId, AttachmentBindRequest request,
            HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = requireAccessibleWorkOrder(workOrderId, currentUser);
        FileStorage storage = requireFile(request.getFileId());
        if (storage.getWorkOrderId() != null && !workOrderId.equals(storage.getWorkOrderId())) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "文件已绑定其他工单");
        }
        WorkOrderAttachment attachment = new WorkOrderAttachment();
        attachment.setWorkOrderId(workOrderId);
        attachment.setRecordId(request.getRecordId());
        attachment.setFileId(request.getFileId());
        attachment.setAttachmentType(request.getAttachmentType());
        attachment.setAttachmentName(StringUtils.hasText(request.getAttachmentName()) ? request.getAttachmentName() : storage.getOriginalName());
        attachment.setAttachmentDesc(request.getAttachmentDesc());
        attachment.setBusinessScene(request.getBusinessScene());
        attachment.setCaptureTime(request.getCaptureTime() == null ? LocalDateTime.now() : request.getCaptureTime());
        attachment.setCaptureUserId(currentUser.getUserId());
        attachment.setCaptureUserName(currentUser.getRealName());
        attachment.setLatitude(request.getLatitude());
        attachment.setLongitude(request.getLongitude());
        attachment.setLocationName(request.getLocationName());
        attachment.setWatermarkFlag(defaultInt(request.getWatermarkFlag(), isPhoto(request.getAttachmentType()) ? 1 : 0));
        attachment.setWatermarkText(request.getWatermarkText());
        attachment.setWatermarkTime(LocalDateTime.now());
        attachment.setWatermarkWorkOrderNo(order.getWorkOrderNo());
        attachment.setWatermarkUserName(currentUser.getRealName());
        attachment.setWatermarkLatitude(request.getLatitude());
        attachment.setWatermarkLongitude(request.getLongitude());
        attachment.setDurationSeconds(request.getDurationSeconds());
        attachment.setMediaWidth(request.getMediaWidth());
        attachment.setMediaHeight(request.getMediaHeight());
        attachment.setAiBindStatus("NONE");
        attachment.setPreviewStatus("AVAILABLE");
        attachment.setMobileCacheStatus("NOT_CACHED");
        attachment.setUploadStatus("UPLOADED");
        attachment.setRetryCount(0);
        attachment.setLocalId(request.getLocalId());
        attachment.setVersion(1);
        attachment.setSyncStatus("SYNCED");
        attachment.setDeviceId(request.getDeviceId());
        attachment.setOperatorId(currentUser.getUserId());
        attachment.setConflictFlag(0);
        attachment.setCreatedAt(LocalDateTime.now());
        attachment.setUpdatedAt(LocalDateTime.now());
        attachment.setDeletedFlag(0);
        attachment.setCreatedBy(currentUser.getUserId());
        attachment.setUpdatedBy(currentUser.getUserId());
        attachment.setRemark(request.getRemark());
        workOrderAttachmentMapper.insert(attachment);
        attachment.setServerId(attachment.getId());
        workOrderAttachmentMapper.updateById(attachment);

        storage.setWorkOrderId(workOrderId);
        storage.setRecordId(request.getRecordId());
        storage.setUpdatedAt(LocalDateTime.now());
        storage.setUpdatedBy(currentUser.getUserId());
        fileStorageMapper.updateById(storage);

        writeOperationLog(currentUser, servletRequest, "BIND_WORK_ORDER_ATTACHMENT", "WORK_ORDER_ATTACHMENT",
                attachment.getId(), order.getWorkOrderNo(), order.getProjectId(), "MOBILE");
        return toAttachmentVO(attachment);
    }

    @Override
    public List<WorkOrderAttachmentVO> listMobileWorkOrderAttachments(Long workOrderId) {
        requireAccessibleWorkOrder(workOrderId, CurrentUserContext.require());
        return workOrderAttachmentMapper.selectByWorkOrderId(workOrderId).stream()
                .map(this::toAttachmentVO)
                .toList();
    }

    @Override
    public List<WorkOrderAttachmentVO> listAdminWorkOrderAttachments(Long workOrderId) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = requireWorkOrder(workOrderId);
        if (!dataScopeService.canAccessAll(currentUser) && !dataScopeService.canAccessProject(currentUser, order.getProjectId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该工单附件");
        }
        return workOrderAttachmentMapper.selectByWorkOrderId(workOrderId).stream()
                .map(this::toAttachmentVO)
                .toList();
    }

    private FileStorage buildStorage(CurrentUser currentUser, String originalName, String storedName, String fileType,
            String mimeType, Long fileSize, String relativePath, String hash, Long workOrderId, Long recordId,
            String localId, String deviceId) {
        FileStorage storage = new FileStorage();
        storage.setFileId("FILE-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        storage.setOriginalName(originalName);
        storage.setStoredName(storedName);
        storage.setFileType(fileType);
        storage.setMimeType(mimeType);
        storage.setFileSize(fileSize);
        storage.setStorageType("LOCAL");
        storage.setFilePath(relativePath);
        storage.setPreviewPath(relativePath);
        storage.setFileHash(hash);
        storage.setChecksum(hash);
        storage.setUploadUserId(currentUser.getUserId());
        storage.setUploadUserName(currentUser.getRealName());
        storage.setUploadTime(LocalDateTime.now());
        storage.setUploadStatus("UPLOADED");
        storage.setRetryCount(0);
        storage.setWorkOrderId(workOrderId);
        storage.setRecordId(recordId);
        storage.setAccessLevel("PRIVATE");
        storage.setPreviewEnabled(1);
        storage.setDownloadEnabled(1);
        storage.setCacheEnabled(0);
        storage.setLocalId(localId);
        storage.setVersion(1);
        storage.setSyncStatus("SYNCED");
        storage.setDeviceId(deviceId);
        storage.setOperatorId(currentUser.getUserId());
        storage.setConflictFlag(0);
        storage.setCreatedAt(LocalDateTime.now());
        storage.setUpdatedAt(LocalDateTime.now());
        storage.setDeletedFlag(0);
        storage.setCreatedBy(currentUser.getUserId());
        storage.setUpdatedBy(currentUser.getUserId());
        return storage;
    }

    private FileDownloadResource toDownloadResource(FileStorage storage) {
        try {
            Path path = resolveStoragePath(storage.getFilePath());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
            }
            return new FileDownloadResource(resource, storage.getOriginalName(), storage.getMimeType(), storage.getFileSize());
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "文件读取失败");
        }
    }

    private Path resolveStoragePath(String relativePath) {
        Path path = storageRoot.resolve(relativePath).normalize();
        if (!path.startsWith(storageRoot)) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "非法文件路径");
        }
        return path;
    }

    private WorkOrder requireAccessibleWorkOrder(Long workOrderId, CurrentUser currentUser) {
        WorkOrder order = requireWorkOrder(workOrderId);
        if (!canAccessWorkOrder(currentUser, order)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该工单文件");
        }
        return order;
    }

    private WorkOrder requireWorkOrder(Long workOrderId) {
        WorkOrder order = workOrderMapper.selectById(workOrderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在");
        }
        return order;
    }

    private FileStorage requireFile(String fileId) {
        FileStorage storage = fileStorageMapper.selectByFileId(fileId);
        if (storage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return storage;
    }

    private void checkFileAccess(FileStorage storage, CurrentUser currentUser) {
        if (dataScopeService.canAccessAll(currentUser) || currentUser.getUserId().equals(storage.getUploadUserId())) {
            return;
        }
        if (storage.getWorkOrderId() != null && canAccessWorkOrder(currentUser, requireWorkOrder(storage.getWorkOrderId()))) {
            return;
        }
        boolean boundAccessible = workOrderAttachmentMapper.selectByFileId(storage.getFileId()).stream()
                .map(WorkOrderAttachment::getWorkOrderId)
                .distinct()
                .map(workOrderMapper::selectById)
                .anyMatch(order -> order != null && canAccessWorkOrder(currentUser, order));
        if (!boundAccessible) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该文件");
        }
    }

    private boolean canAccessWorkOrder(CurrentUser currentUser, WorkOrder order) {
        if (currentUser == null || order == null) {
            return false;
        }
        if (dataScopeService.canAccessAll(currentUser) || dataScopeService.canAccessProject(currentUser, order.getProjectId())) {
            return true;
        }
        if (currentUser.getUserId().equals(order.getMaintainerId()) || currentUser.getUserId().equals(order.getLeaderId())) {
            return true;
        }
        return workOrderAssignmentMapper.selectByWorkOrderId(order.getId()).stream()
                .map(WorkOrderAssignment::getAssigneeId)
                .anyMatch(currentUser.getUserId()::equals);
    }

    private FileUploadVO toUploadVO(FileStorage storage) {
        FileUploadVO vo = new FileUploadVO();
        vo.id = storage.getId();
        vo.serverId = storage.getServerId();
        vo.localId = storage.getLocalId();
        vo.fileId = storage.getFileId();
        vo.fileType = storage.getFileType();
        vo.fileSize = storage.getFileSize();
        vo.mimeType = storage.getMimeType();
        vo.fileHash = storage.getFileHash();
        vo.previewUrl = "/api/files/" + storage.getFileId() + "/preview";
        vo.downloadUrl = "/api/files/" + storage.getFileId() + "/download";
        vo.originalName = storage.getOriginalName();
        vo.version = storage.getVersion();
        vo.updatedAt = storage.getUpdatedAt();
        vo.syncStatus = storage.getSyncStatus();
        return vo;
    }

    private WorkOrderAttachmentVO toAttachmentVO(WorkOrderAttachment attachment) {
        WorkOrderAttachmentVO vo = new WorkOrderAttachmentVO();
        vo.id = attachment.getId();
        vo.serverId = attachment.getServerId();
        vo.localId = attachment.getLocalId();
        vo.workOrderId = attachment.getWorkOrderId();
        vo.recordId = attachment.getRecordId();
        vo.fileId = attachment.getFileId();
        vo.attachmentType = attachment.getAttachmentType();
        vo.attachmentName = attachment.getAttachmentName();
        vo.attachmentDesc = attachment.getAttachmentDesc();
        vo.businessScene = attachment.getBusinessScene();
        vo.captureTime = attachment.getCaptureTime();
        vo.watermarkText = attachment.getWatermarkText();
        vo.watermarkWorkOrderNo = attachment.getWatermarkWorkOrderNo();
        vo.version = attachment.getVersion();
        vo.updatedAt = attachment.getUpdatedAt();
        vo.syncStatus = attachment.getSyncStatus();
        vo.previewUrl = "/api/files/" + attachment.getFileId() + "/preview";
        vo.downloadUrl = "/api/files/" + attachment.getFileId() + "/download";
        return vo;
    }

    private String copyWithSha256(InputStream inputStream, Path target) throws IOException {
        MessageDigest digest = sha256();
        try (DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);
                OutputStream outputStream = Files.newOutputStream(target)) {
            digestInputStream.transferTo(outputStream);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private String mergeChunkFiles(Path chunkDir, Path target, int totalChunks) throws IOException {
        MessageDigest digest = sha256();
        try (OutputStream outputStream = Files.newOutputStream(target)) {
            for (int i = 0; i < totalChunks; i++) {
                Path chunk = chunkDir.resolve(String.valueOf(i));
                if (!Files.exists(chunk)) {
                    throw new BusinessException(ErrorCode.FILE_ERROR, "分片不完整");
                }
                try (InputStream inputStream = Files.newInputStream(chunk);
                        DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest)) {
                    digestInputStream.transferTo(outputStream);
                }
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "SHA-256不可用");
        }
    }

    private int countChunks(Path chunkDir) throws IOException {
        try (var stream = Files.list(chunkDir)) {
            return (int) stream.filter(Files::isRegularFile).count();
        }
    }

    private void deleteDirectoryQuietly(Path dir) {
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Keeping orphaned chunks is safer than failing a completed merge.
                }
            });
        } catch (IOException ignored) {
            // Keeping orphaned chunks is safer than failing a completed merge.
        }
    }

    private String sanitizeOriginalName(String originalName) {
        String name = StringUtils.hasText(originalName) ? Paths.get(originalName).getFileName().toString() : "upload.bin";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String extension(String originalName) {
        int index = originalName.lastIndexOf('.');
        return index >= 0 ? originalName.substring(index) : "";
    }

    private String normalizeMimeType(String mimeType) {
        return StringUtils.hasText(mimeType) ? mimeType : "application/octet-stream";
    }

    private String normalizeFileType(String fileType, String mimeType, String originalName) {
        if (StringUtils.hasText(fileType)) {
            return fileType.toUpperCase(Locale.ROOT);
        }
        String type = normalizeMimeType(mimeType).toLowerCase(Locale.ROOT);
        String lowerName = originalName == null ? "" : originalName.toLowerCase(Locale.ROOT);
        if (type.startsWith("image/")) {
            return "PHOTO";
        }
        if (type.startsWith("video/")) {
            return "VIDEO";
        }
        if (type.startsWith("audio/")) {
            return "AUDIO";
        }
        if (type.equals("application/pdf") || lowerName.endsWith(".pdf")) {
            return "PDF";
        }
        return "OTHER";
    }

    private boolean isPhoto(String fileType) {
        return "PHOTO".equalsIgnoreCase(fileType) || "IMAGE".equalsIgnoreCase(fileType);
    }

    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Long resolveProjectId(FileStorage storage) {
        if (storage.getWorkOrderId() == null) {
            return null;
        }
        WorkOrder order = workOrderMapper.selectById(storage.getWorkOrderId());
        return order == null ? null : order.getProjectId();
    }

    private void writeOperationLog(CurrentUser currentUser, HttpServletRequest request, String operationType,
            String businessType, Long businessId, String businessNo, Long projectId, String platform) {
        OperationLog log = new OperationLog();
        log.setTraceId(TraceIdUtils.currentTraceId());
        log.setOperatorId(currentUser.getUserId());
        log.setOperatorName(currentUser.getRealName());
        log.setRoleCode(String.join(",", currentUser.getRoleCodes()));
        log.setPlatform(platform);
        log.setModuleName("FILE");
        log.setOperationType(operationType);
        log.setBusinessType(businessType);
        log.setBusinessId(businessId == null ? null : String.valueOf(businessId));
        log.setBusinessNo(businessNo);
        log.setProjectId(projectId);
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(request.getRequestURI());
        log.setRequestIp(clientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setResultStatus("SUCCESS");
        log.setOperationTime(LocalDateTime.now());
        log.setDeletedFlag(0);
        operationLogMapper.insert(log);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
