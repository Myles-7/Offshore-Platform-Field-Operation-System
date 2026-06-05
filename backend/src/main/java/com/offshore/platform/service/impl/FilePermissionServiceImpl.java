package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.entity.FileStorage;
import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.mapper.FileStorageMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.FilePermissionService;
import com.offshore.platform.service.WorkOrderPermissionService;
import org.springframework.stereotype.Service;

@Service
public class FilePermissionServiceImpl implements FilePermissionService {
    private final FileStorageMapper fileStorageMapper;
    private final WorkOrderAttachmentMapper attachmentMapper;
    private final WorkOrderMapper workOrderMapper;
    private final DataScopeService dataScopeService;
    private final WorkOrderPermissionService workOrderPermissionService;

    public FilePermissionServiceImpl(FileStorageMapper fileStorageMapper, WorkOrderAttachmentMapper attachmentMapper,
            WorkOrderMapper workOrderMapper, DataScopeService dataScopeService,
            WorkOrderPermissionService workOrderPermissionService) {
        this.fileStorageMapper = fileStorageMapper;
        this.attachmentMapper = attachmentMapper;
        this.workOrderMapper = workOrderMapper;
        this.dataScopeService = dataScopeService;
        this.workOrderPermissionService = workOrderPermissionService;
    }

    @Override
    public void requireAccess(String fileId, CurrentUser currentUser) {
        FileStorage storage = fileStorageMapper.selectByFileId(fileId);
        if (storage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "File not found");
        }
        requireAccess(storage, currentUser);
    }

    @Override
    public void requireAccess(FileStorage storage, CurrentUser currentUser) {
        if (storage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "File not found");
        }
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (dataScopeService.canAccessAll(currentUser) || currentUser.getUserId().equals(storage.getUploadUserId())) {
            return;
        }
        if (storage.getWorkOrderId() != null
                && workOrderPermissionService.canRead(workOrderMapper.selectById(storage.getWorkOrderId()), currentUser)) {
            return;
        }
        boolean boundAccessible = attachmentMapper.selectByFileId(storage.getFileId()).stream()
                .map(WorkOrderAttachment::getWorkOrderId)
                .distinct()
                .map(workOrderMapper::selectById)
                .anyMatch(order -> workOrderPermissionService.canRead(order, currentUser));
        if (!boundAccessible) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for file");
        }
    }
}
