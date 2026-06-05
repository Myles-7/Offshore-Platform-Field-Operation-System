package com.offshore.platform.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.dto.sync.ConflictResolveRequest;
import com.offshore.platform.dto.sync.DeviceRegisterRequest;
import com.offshore.platform.dto.sync.SyncAckRequest;
import com.offshore.platform.dto.sync.SyncPullRequest;
import com.offshore.platform.dto.sync.SyncPushItem;
import com.offshore.platform.dto.sync.SyncPushRequest;
import com.offshore.platform.entity.DeviceInfo;
import com.offshore.platform.entity.AiDefectBox;
import com.offshore.platform.entity.AiResult;
import com.offshore.platform.entity.SyncConflict;
import com.offshore.platform.entity.SyncLog;
import com.offshore.platform.entity.SyncTask;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.entity.WorkOrderRecord;
import com.offshore.platform.mapper.DeviceInfoMapper;
import com.offshore.platform.mapper.AiDefectBoxMapper;
import com.offshore.platform.mapper.AiResultMapper;
import com.offshore.platform.mapper.SyncConflictMapper;
import com.offshore.platform.mapper.SyncLogMapper;
import com.offshore.platform.mapper.SyncTaskMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.SyncService;
import com.offshore.platform.vo.sync.DeviceVO;
import com.offshore.platform.vo.sync.SyncConflictVO;
import com.offshore.platform.vo.sync.SyncItemResultVO;
import com.offshore.platform.vo.sync.SyncPullVO;
import com.offshore.platform.vo.sync.SyncPushResultVO;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncServiceImpl implements SyncService {
    private final DeviceInfoMapper deviceMapper;
    private final SyncTaskMapper syncTaskMapper;
    private final SyncLogMapper syncLogMapper;
    private final SyncConflictMapper syncConflictMapper;
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderRecordMapper recordMapper;
    private final WorkOrderAttachmentMapper attachmentMapper;
    private final AiResultMapper aiResultMapper;
    private final AiDefectBoxMapper aiDefectBoxMapper;
    private final WorkOrderAssignmentMapper assignmentMapper;
    private final DataScopeService dataScopeService;
    private final ObjectMapper objectMapper;

    public SyncServiceImpl(DeviceInfoMapper deviceMapper, SyncTaskMapper syncTaskMapper, SyncLogMapper syncLogMapper,
            SyncConflictMapper syncConflictMapper, WorkOrderMapper workOrderMapper, WorkOrderRecordMapper recordMapper,
            WorkOrderAttachmentMapper attachmentMapper, WorkOrderAssignmentMapper assignmentMapper,
            AiResultMapper aiResultMapper, AiDefectBoxMapper aiDefectBoxMapper,
            DataScopeService dataScopeService, ObjectMapper objectMapper) {
        this.deviceMapper = deviceMapper;
        this.syncTaskMapper = syncTaskMapper;
        this.syncLogMapper = syncLogMapper;
        this.syncConflictMapper = syncConflictMapper;
        this.workOrderMapper = workOrderMapper;
        this.recordMapper = recordMapper;
        this.attachmentMapper = attachmentMapper;
        this.aiResultMapper = aiResultMapper;
        this.aiDefectBoxMapper = aiDefectBoxMapper;
        this.assignmentMapper = assignmentMapper;
        this.dataScopeService = dataScopeService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public DeviceVO registerDevice(DeviceRegisterRequest request) {
        CurrentUser user = CurrentUserContext.require();
        DeviceInfo existingDevice = findDeviceByDeviceId(request.deviceId);
        if (existingDevice != null && !user.getUserId().equals(existingDevice.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Device belongs to another user");
        }
        DeviceInfo device = findDevice(request.deviceId, user.getUserId());
        LocalDateTime now = LocalDateTime.now();
        boolean create = device == null;
        if (create) {
            device = new DeviceInfo();
            device.setDeviceId(request.deviceId);
            device.setUserId(user.getUserId());
            device.setRegisterTime(now);
            device.setCreatedAt(now);
            device.setCreatedBy(user.getUserId());
            device.setDeletedFlag(0);
        }
        fillDevice(device, request);
        device.setLastLoginTime(now);
        device.setLastHeartbeatTime(now);
        device.setOnlineStatus("ONLINE");
        device.setDeviceStatus("ACTIVE");
        device.setSyncEnabled(1);
        device.setUpdatedAt(now);
        device.setUpdatedBy(user.getUserId());
        if (create) {
            deviceMapper.insert(device);
        } else {
            deviceMapper.updateById(device);
        }
        return toDeviceVO(device);
    }

    @Override
    @Transactional
    public DeviceVO heartbeat(DeviceRegisterRequest request) {
        CurrentUser user = CurrentUserContext.require();
        DeviceInfo device = requireDevice(request.deviceId, user.getUserId());
        fillDevice(device, request);
        device.setLastHeartbeatTime(LocalDateTime.now());
        device.setOnlineStatus("ONLINE");
        device.setUpdatedAt(LocalDateTime.now());
        device.setUpdatedBy(user.getUserId());
        deviceMapper.updateById(device);
        return toDeviceVO(device);
    }

    @Override
    @Transactional
    public SyncPullVO pull(SyncPullRequest request) {
        CurrentUser user = CurrentUserContext.require();
        requireDevice(request.deviceId, user.getUserId());
        SyncTask task = createTask("PULL", request.deviceId, null, user, 0, request.cursor);
        SyncPullVO vo = new SyncPullVO();
        vo.cursor = String.valueOf(System.currentTimeMillis());
        for (WorkOrder workOrder : workOrderMapper.selectAll()) {
            if (canMobileAccessWorkOrder(user, workOrder)) {
                vo.items.add(syncItem("WORK_ORDER", workOrder.getId(), workOrder.getLocalId(), workOrder.getVersion(),
                        workOrder.getUpdatedAt(), workOrder));
            }
        }
        task.setTaskStatus("SUCCESS");
        task.setTotalCount(vo.items.size());
        task.setSuccessCount(vo.items.size());
        task.setResponseCursor(vo.cursor);
        task.setServerEndTime(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        syncTaskMapper.updateById(task);
        return vo;
    }

    @Override
    @Transactional
    public SyncPushResultVO push(SyncPushRequest request) {
        CurrentUser user = CurrentUserContext.require();
        requireDevice(request.deviceId, user.getUserId());

        SyncTask existingTask = syncTaskMapper.selectAll().stream()
                .filter(task -> request.batchId.equals(task.getBatchId()) && request.deviceId.equals(task.getDeviceId()))
                .findFirst()
                .orElse(null);
        if (existingTask != null && "SUCCESS".equals(existingTask.getTaskStatus())) {
            return taskResult(existingTask, request.batchId);
        }

        SyncTask task = createTask("PUSH", request.deviceId, request.batchId, user, request.items.size(), null);
        task.setClientTime(request.clientTime);
        SyncPushResultVO result = new SyncPushResultVO();
        result.taskId = task.getId();
        result.batchId = request.batchId;
        result.successCount = 0;
        result.failedCount = 0;
        result.conflictCount = 0;

        for (SyncPushItem item : request.items) {
            SyncItemResultVO itemResult = handlePushItem(task, request, item, user);
            result.items.add(itemResult);
            if ("SUCCESS".equals(itemResult.syncStatus)) {
                result.successCount++;
            } else if ("CONFLICT".equals(itemResult.syncStatus)) {
                result.conflictCount++;
            } else {
                result.failedCount++;
            }
        }

        task.setSuccessCount(result.successCount);
        task.setFailedCount(result.failedCount);
        task.setConflictCount(result.conflictCount);
        task.setTaskStatus(result.failedCount == 0 ? "SUCCESS" : "PARTIAL_SUCCESS");
        task.setServerEndTime(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setResponseSummary(toJson(result));
        syncTaskMapper.updateById(task);
        return result;
    }

    @Override
    @Transactional
    public void ack(SyncAckRequest request) {
        CurrentUser user = CurrentUserContext.require();
        DeviceInfo device = requireDevice(request.deviceId, user.getUserId());
        device.setLastSyncTime(LocalDateTime.now());
        device.setLastSyncCursor(request.cursor);
        device.setUpdatedAt(LocalDateTime.now());
        device.setUpdatedBy(user.getUserId());
        deviceMapper.updateById(device);
    }

    @Override
    public List<Object> myTasks() {
        CurrentUser user = CurrentUserContext.require();
        return syncTaskMapper.selectAll().stream()
                .filter(task -> user.getUserId().equals(task.getOperatorId()))
                .map(task -> (Object) task)
                .toList();
    }

    @Override
    public List<Object> myLogs() {
        CurrentUser user = CurrentUserContext.require();
        return syncLogMapper.selectAll().stream()
                .filter(log -> user.getUserId().equals(log.getOperatorId()))
                .map(log -> (Object) log)
                .toList();
    }

    @Override
    public List<SyncConflictVO> conflicts() {
        requireAdmin(CurrentUserContext.require());
        return syncConflictMapper.selectAll().stream().map(this::toConflictVO).toList();
    }

    @Override
    public SyncConflictVO conflict(Long id) {
        requireAdmin(CurrentUserContext.require());
        return toConflictVO(requireConflict(id));
    }

    @Override
    @Transactional
    public SyncConflictVO resolveConflict(Long id, ConflictResolveRequest request) {
        CurrentUser user = CurrentUserContext.require();
        requireAdmin(user);
        SyncConflict conflict = requireConflict(id);
        conflict.setResolveStrategy(request.resolveStrategy);
        conflict.setFinalPayload(request.finalPayload);
        conflict.setResolveComment(request.resolveComment);
        conflict.setResolveStatus("IGNORE_CLIENT".equals(request.resolveStrategy) ? "IGNORED" : "RESOLVED");
        conflict.setResolverId(user.getUserId());
        conflict.setResolveTime(LocalDateTime.now());
        conflict.setUpdatedAt(LocalDateTime.now());
        conflict.setUpdatedBy(user.getUserId());
        syncConflictMapper.updateById(conflict);
        return toConflictVO(conflict);
    }

    private SyncItemResultVO handlePushItem(SyncTask task, SyncPushRequest request, SyncPushItem item, CurrentUser user) {
        SyncLog log = createLog(task, request, item, user);
        try {
            rejectFileBody(item.payload);
            if ("WORK_ORDER_RECORD".equals(item.entityType)) {
                return handleRecord(task, log, item, user, request.deviceId);
            }
            if ("WORK_ORDER_ATTACHMENT".equals(item.entityType)) {
                return handleAttachment(log, item, user, request.deviceId);
            }
            if ("AI_RESULT".equals(item.entityType)) {
                return handleAiResult(log, item, user, request.deviceId);
            }
            if ("AI_DEFECT_BOX".equals(item.entityType)) {
                return handleAiDefectBox(log, item, user, request.deviceId);
            }
            return fail(log, item, "UNSUPPORTED_ENTITY", "Unsupported entityType: " + item.entityType);
        } catch (BusinessException ex) {
            return fail(log, item, ex.getErrorCode().name(), ex.getMessage());
        } catch (RuntimeException ex) {
            return fail(log, item, "SYNC_ITEM_ERROR", ex.getMessage());
        }
    }

    private SyncItemResultVO handleRecord(SyncTask task, SyncLog log, SyncPushItem item, CurrentUser user, String deviceId) {
        WorkOrderRecord existing = findRecordByLocalId(item.localId);
        if ("CREATE".equals(item.actionType)) {
            if (existing != null) {
                return success(log, item, existing.getId(), existing.getVersion(), "Idempotent create");
            }
            JsonNode payload = item.payload;
            Long workOrderId = longField(payload, "workOrderId");
            WorkOrder workOrder = requireMobileWorkOrder(workOrderId, user);
            WorkOrderRecord record = new WorkOrderRecord();
            record.setWorkOrderId(workOrder.getId());
            record.setProjectId(workOrder.getProjectId());
            record.setRecordNo("REC-" + System.currentTimeMillis());
            record.setRecordType(textField(payload, "recordType", "DAILY"));
            record.setConstructionTime(dateTimeField(payload, "constructionTime", LocalDateTime.now()));
            record.setConstructionUserId(user.getUserId());
            record.setConstructionUserName(user.getRealName());
            record.setConstructionDesc(textField(payload, "constructionDesc", textField(payload, "description", "")));
            record.setSiteCondition(textField(payload, "siteCondition", null));
            record.setAbnormalFlag(intField(payload, "abnormalFlag", 0));
            record.setAbnormalDesc(textField(payload, "abnormalDesc", null));
            record.setWeather(textField(payload, "weather", null));
            record.setLocationName(textField(payload, "locationName", null));
            record.setAttachmentCount(0);
            record.setAiResultCount(0);
            record.setRecordStatus("SUBMITTED");
            record.setSubmittedAt(LocalDateTime.now());
            fillSyncFields(record, item.localId, deviceId, user.getUserId());
            recordMapper.insert(record);
            record.setServerId(record.getId());
            recordMapper.updateById(record);
            return success(log, item, record.getId(), record.getVersion(), "Created");
        }

        WorkOrderRecord record = item.serverId == null ? existing : recordMapper.selectById(item.serverId);
        if (record == null) {
            return fail(log, item, "NOT_FOUND", "Record not found");
        }
        if (!Objects.equals(record.getVersion(), item.version)) {
            log.setServerVersion(record.getVersion());
            Long conflictId = createConflict(task, log, item, user, record.getWorkOrderId(), record.getId(), item.payload, record);
            return conflict(log, item, conflictId, record.getId(), record.getVersion(), "Version conflict");
        }
        requireMobileWorkOrder(record.getWorkOrderId(), user);
        record.setConstructionDesc(textField(item.payload, "constructionDesc", record.getConstructionDesc()));
        record.setSiteCondition(textField(item.payload, "siteCondition", record.getSiteCondition()));
        record.setAbnormalFlag(intField(item.payload, "abnormalFlag", record.getAbnormalFlag()));
        record.setVersion(record.getVersion() + 1);
        record.setUpdatedAt(LocalDateTime.now());
        record.setUpdatedBy(user.getUserId());
        recordMapper.updateById(record);
        return success(log, item, record.getId(), record.getVersion(), "Updated");
    }

    private SyncItemResultVO handleAttachment(SyncLog log, SyncPushItem item, CurrentUser user, String deviceId) {
        WorkOrderAttachment existing = findAttachmentByLocalId(item.localId);
        if (existing != null) {
            return success(log, item, existing.getId(), existing.getVersion(), "Idempotent create");
        }
        JsonNode payload = item.payload;
        Long workOrderId = longField(payload, "workOrderId");
        WorkOrder workOrder = requireMobileWorkOrder(workOrderId, user);
        WorkOrderAttachment attachment = new WorkOrderAttachment();
        attachment.setWorkOrderId(workOrder.getId());
        attachment.setRecordId(optionalLongField(payload, "recordId"));
        attachment.setFileId(item.fileId == null ? textField(payload, "fileId", null) : item.fileId);
        attachment.setAttachmentType(textField(payload, "attachmentType", "PHOTO"));
        attachment.setAttachmentName(textField(payload, "attachmentName", "mobile attachment"));
        attachment.setAttachmentDesc(textField(payload, "attachmentDesc", null));
        attachment.setCaptureTime(dateTimeField(payload, "captureTime", LocalDateTime.now()));
        attachment.setCaptureUserId(user.getUserId());
        attachment.setCaptureUserName(user.getRealName());
        attachment.setUploadStatus("UPLOADED");
        attachment.setPreviewStatus("AVAILABLE");
        attachment.setMobileCacheStatus("NOT_CACHED");
        fillSyncFields(attachment, item.localId, deviceId, user.getUserId());
        attachmentMapper.insert(attachment);
        attachment.setServerId(attachment.getId());
        attachmentMapper.updateById(attachment);
        return success(log, item, attachment.getId(), attachment.getVersion(), "Created");
    }

    private SyncItemResultVO handleAiResult(SyncLog log, SyncPushItem item, CurrentUser user, String deviceId) {
        AiResult existing = findAiResultByLocalId(item.localId);
        if (existing != null) {
            return success(log, item, existing.getId(), existing.getVersion(), "Idempotent create");
        }
        JsonNode payload = item.payload;
        Long workOrderId = longField(payload, "workOrderId");
        WorkOrder workOrder = requireMobileWorkOrder(workOrderId, user);
        Long attachmentId = longField(payload, "attachmentId");
        WorkOrderAttachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null || !workOrderId.equals(attachment.getWorkOrderId())) {
            return fail(log, item, "AI_ATTACHMENT_ERROR", "AI result must bind a work-order photo attachment");
        }
        AiResult result = new AiResult();
        result.setAiResultNo("AI-" + System.currentTimeMillis());
        result.setWorkOrderId(workOrderId);
        result.setWorkOrderNo(workOrder.getWorkOrderNo());
        result.setProjectId(workOrder.getProjectId());
        Long recordId = optionalLongField(payload, "recordId");
        result.setRecordId(recordId == null ? attachment.getRecordId() : recordId);
        result.setAttachmentId(attachmentId);
        result.setFileId(textField(payload, "fileId", attachment.getFileId()));
        result.setResultImageFileId(textField(payload, "resultImageFileId", null));
        result.setModelId(optionalLongField(payload, "modelId"));
        result.setModelCode(textField(payload, "modelCode", null));
        result.setModelVersion(textField(payload, "modelVersion", null));
        result.setInferSide(textField(payload, "inferSide", "MOBILE"));
        result.setInferTime(dateTimeField(payload, "inferTime", LocalDateTime.now()));
        result.setInferCostMs(intField(payload, "inferCostMs", null));
        result.setDefectType(textField(payload, "defectType", "UNKNOWN"));
        result.setConfidence(decimalField(payload, "confidence"));
        result.setSuspectedDefectFlag(intField(payload, "suspectedDefectFlag", 0));
        result.setDefectCount(intField(payload, "defectCount", 0));
        result.setResultSummary(textField(payload, "resultSummary", null));
        result.setRawResult(textField(payload, "rawResult", null));
        result.setReviewStatus("PENDING_REVIEW");
        result.setReviewedFlag(0);
        fillSyncFields(result, item.localId, deviceId, user.getUserId());
        aiResultMapper.insert(result);
        result.setServerId(result.getId());
        aiResultMapper.updateById(result);
        attachment.setAiResultId(result.getId());
        attachment.setAiBindStatus("BOUND");
        attachment.setUpdatedAt(LocalDateTime.now());
        attachment.setUpdatedBy(user.getUserId());
        attachmentMapper.updateById(attachment);
        return success(log, item, result.getId(), result.getVersion(), "Created");
    }

    private SyncItemResultVO handleAiDefectBox(SyncLog log, SyncPushItem item, CurrentUser user, String deviceId) {
        AiDefectBox existing = findAiDefectBoxByLocalId(item.localId);
        if (existing != null) {
            return success(log, item, existing.getId(), existing.getVersion(), "Idempotent create");
        }
        JsonNode payload = item.payload;
        Long aiResultId = longField(payload, "aiResultId");
        AiResult result = aiResultMapper.selectById(aiResultId);
        if (result == null) {
            return fail(log, item, "NOT_FOUND", "AI result not found");
        }
        requireMobileWorkOrder(result.getWorkOrderId(), user);
        AiDefectBox box = new AiDefectBox();
        box.setAiResultId(aiResultId);
        box.setBoxNo("BOX-" + aiResultId + "-" + System.currentTimeMillis());
        box.setDefectType(textField(payload, "defectType", result.getDefectType()));
        box.setConfidence(decimalField(payload, "confidence"));
        box.setX(decimalField(payload, "x"));
        box.setY(decimalField(payload, "y"));
        box.setWidth(decimalField(payload, "width"));
        box.setHeight(decimalField(payload, "height"));
        box.setImageWidth(intField(payload, "imageWidth", null));
        box.setImageHeight(intField(payload, "imageHeight", null));
        box.setNormalizedFlag(intField(payload, "normalizedFlag", 1));
        box.setBoxLabel(textField(payload, "boxLabel", null));
        box.setBoxColor(textField(payload, "boxColor", null));
        box.setSortOrder(intField(payload, "sortOrder", 1));
        fillSyncFields(box, item.localId, deviceId, user.getUserId());
        aiDefectBoxMapper.insert(box);
        box.setServerId(box.getId());
        aiDefectBoxMapper.updateById(box);
        return success(log, item, box.getId(), box.getVersion(), "Created");
    }

    private void fillSyncFields(WorkOrderRecord record, String localId, String deviceId, Long userId) {
        record.setClientCreatedAt(LocalDateTime.now());
        record.setClientUpdatedAt(LocalDateTime.now());
        record.setLocalId(localId);
        record.setVersion(1);
        record.setSyncStatus("SYNCED");
        record.setDeviceId(deviceId);
        record.setOperatorId(userId);
        record.setConflictFlag(0);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        record.setDeletedFlag(0);
        record.setCreatedBy(userId);
        record.setUpdatedBy(userId);
    }

    private void fillSyncFields(WorkOrderAttachment attachment, String localId, String deviceId, Long userId) {
        attachment.setClientCreatedAt(LocalDateTime.now());
        attachment.setClientUpdatedAt(LocalDateTime.now());
        attachment.setLocalId(localId);
        attachment.setVersion(1);
        attachment.setSyncStatus("SYNCED");
        attachment.setDeviceId(deviceId);
        attachment.setOperatorId(userId);
        attachment.setConflictFlag(0);
        attachment.setCreatedAt(LocalDateTime.now());
        attachment.setUpdatedAt(LocalDateTime.now());
        attachment.setDeletedFlag(0);
        attachment.setCreatedBy(userId);
        attachment.setUpdatedBy(userId);
    }

    private void fillSyncFields(AiResult result, String localId, String deviceId, Long userId) {
        result.setClientCreatedAt(LocalDateTime.now());
        result.setClientUpdatedAt(LocalDateTime.now());
        result.setLocalId(localId);
        result.setVersion(1);
        result.setSyncStatus("SYNCED");
        result.setDeviceId(deviceId);
        result.setOperatorId(userId);
        result.setConflictFlag(0);
        result.setCreatedAt(LocalDateTime.now());
        result.setUpdatedAt(LocalDateTime.now());
        result.setDeletedFlag(0);
        result.setCreatedBy(userId);
        result.setUpdatedBy(userId);
    }

    private void fillSyncFields(AiDefectBox box, String localId, String deviceId, Long userId) {
        box.setLocalId(localId);
        box.setVersion(1);
        box.setSyncStatus("SYNCED");
        box.setDeviceId(deviceId);
        box.setOperatorId(userId);
        box.setConflictFlag(0);
        box.setCreatedAt(LocalDateTime.now());
        box.setUpdatedAt(LocalDateTime.now());
        box.setDeletedFlag(0);
        box.setCreatedBy(userId);
        box.setUpdatedBy(userId);
    }

    private SyncTask createTask(String direction, String deviceId, String batchId, CurrentUser user, int totalCount, String cursor) {
        SyncTask task = new SyncTask();
        task.setSyncTaskNo("SYNC-" + System.currentTimeMillis());
        task.setBatchId(batchId == null ? "BATCH-" + direction + "-" + System.currentTimeMillis() : batchId);
        task.setDeviceId(deviceId == null ? "UNKNOWN" : deviceId);
        task.setOperatorId(user.getUserId());
        task.setSyncDirection(direction);
        task.setSyncType("INCREMENTAL");
        task.setTaskStatus("PROCESSING");
        task.setTotalCount(totalCount);
        task.setSuccessCount(0);
        task.setFailedCount(0);
        task.setConflictCount(0);
        task.setRetryCount(0);
        task.setMaxRetryCount(3);
        task.setRequestCursor(cursor);
        task.setServerStartTime(LocalDateTime.now());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDeletedFlag(0);
        task.setCreatedBy(user.getUserId());
        task.setUpdatedBy(user.getUserId());
        syncTaskMapper.insert(task);
        return task;
    }

    private SyncLog createLog(SyncTask task, SyncPushRequest request, SyncPushItem item, CurrentUser user) {
        SyncLog log = new SyncLog();
        log.setSyncTaskId(task.getId());
        log.setBatchId(request.batchId);
        log.setDeviceId(request.deviceId);
        log.setOperatorId(user.getUserId());
        log.setModuleType(item.moduleType);
        log.setEntityType(item.entityType);
        log.setActionType(item.actionType);
        log.setLocalId(item.localId);
        log.setServerId(item.serverId);
        log.setEntityId(item.serverId);
        log.setClientVersion(item.version);
        log.setClientUpdatedAt(item.updatedAt);
        log.setChecksum(item.checksum);
        log.setRequestPayload(toJson(item.payload));
        log.setRetryCount(0);
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        log.setDeletedFlag(0);
        log.setCreatedBy(user.getUserId());
        log.setUpdatedBy(user.getUserId());
        syncLogMapper.insert(log);
        return log;
    }

    private Long createConflict(SyncTask task, SyncLog log, SyncPushItem item, CurrentUser user, Long workOrderId, Long entityId,
            JsonNode clientPayload, Object serverPayload) {
        SyncConflict conflict = new SyncConflict();
        conflict.setConflictNo("CONF-" + System.currentTimeMillis());
        conflict.setSyncTaskId(task.getId());
        conflict.setSyncLogId(log.getId());
        conflict.setDeviceId(log.getDeviceId());
        conflict.setOperatorId(user.getUserId());
        conflict.setModuleType(item.moduleType);
        conflict.setEntityType(item.entityType);
        conflict.setEntityId(entityId);
        conflict.setLocalId(item.localId);
        conflict.setServerId(entityId);
        conflict.setWorkOrderId(workOrderId);
        conflict.setBaseVersion(item.version);
        conflict.setClientVersion(item.version);
        conflict.setServerVersion(log.getServerVersion());
        conflict.setClientUpdatedAt(item.updatedAt);
        conflict.setServerUpdatedAt(LocalDateTime.now());
        conflict.setConflictType("VERSION_CONFLICT");
        conflict.setClientPayload(toJson(clientPayload));
        conflict.setServerPayload(toJson(serverPayload));
        conflict.setDefaultStrategy("KEEP_SERVER");
        conflict.setResolveStatus("PENDING");
        conflict.setCreatedAt(LocalDateTime.now());
        conflict.setUpdatedAt(LocalDateTime.now());
        conflict.setDeletedFlag(0);
        conflict.setCreatedBy(user.getUserId());
        conflict.setUpdatedBy(user.getUserId());
        syncConflictMapper.insert(conflict);
        log.setConflictId(conflict.getId());
        log.setSyncStatus("CONFLICT");
        log.setErrorCode("VERSION_CONFLICT");
        log.setErrorMessage("Client/server version mismatch");
        log.setUpdatedAt(LocalDateTime.now());
        syncLogMapper.updateById(log);
        return conflict.getId();
    }

    private SyncItemResultVO success(SyncLog log, SyncPushItem item, Long serverId, Integer version, String message) {
        log.setServerId(serverId);
        log.setEntityId(serverId);
        log.setServerVersion(version);
        log.setSyncStatus("SUCCESS");
        log.setResponsePayload(message);
        log.setUpdatedAt(LocalDateTime.now());
        syncLogMapper.updateById(log);
        SyncItemResultVO result = baseResult(item, serverId, version);
        result.syncStatus = "SUCCESS";
        result.message = message;
        return result;
    }

    private SyncItemResultVO conflict(SyncLog log, SyncPushItem item, Long conflictId, Long serverId, Integer version, String message) {
        SyncItemResultVO result = baseResult(item, serverId, version);
        result.syncStatus = "CONFLICT";
        result.conflictId = conflictId;
        result.message = message;
        return result;
    }

    private SyncItemResultVO fail(SyncLog log, SyncPushItem item, String code, String message) {
        log.setSyncStatus("FAILED");
        log.setErrorCode(code);
        log.setErrorMessage(message);
        log.setUpdatedAt(LocalDateTime.now());
        syncLogMapper.updateById(log);
        SyncItemResultVO result = baseResult(item, item.serverId, item.version);
        result.syncStatus = "FAILED";
        result.message = message;
        return result;
    }

    private SyncItemResultVO baseResult(SyncPushItem item, Long serverId, Integer version) {
        SyncItemResultVO result = new SyncItemResultVO();
        result.localId = item.localId;
        result.serverId = serverId;
        result.version = version;
        return result;
    }

    private WorkOrder requireMobileWorkOrder(Long workOrderId, CurrentUser user) {
        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Work order not found");
        }
        if (!canMobileAccessWorkOrder(user, workOrder)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for work order");
        }
        return workOrder;
    }

    private boolean canMobileAccessWorkOrder(CurrentUser user, WorkOrder workOrder) {
        if (dataScopeService.canAccessAll(user)) {
            return true;
        }
        if (user.getUserId().equals(workOrder.getMaintainerId())) {
            return true;
        }
        return assignmentMapper.selectAll().stream()
                .anyMatch(assignment -> workOrder.getId().equals(assignment.getWorkOrderId())
                        && user.getUserId().equals(assignment.getAssigneeId()));
    }

    private void requireAdmin(CurrentUser user) {
        if (!dataScopeService.canAccessAll(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Admin sync permission required");
        }
    }

    private DeviceInfo findDevice(String deviceId, Long userId) {
        return deviceMapper.selectAll().stream()
                .filter(device -> deviceId.equals(device.getDeviceId()) && userId.equals(device.getUserId()))
                .findFirst()
                .orElse(null);
    }

    private DeviceInfo findDeviceByDeviceId(String deviceId) {
        return deviceMapper.selectAll().stream()
                .filter(device -> deviceId.equals(device.getDeviceId()))
                .findFirst()
                .orElse(null);
    }

    private DeviceInfo requireDevice(String deviceId, Long userId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "Device not registered");
        }
        DeviceInfo device = findDevice(deviceId, userId);
        if (device == null) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "Device not registered");
        }
        if (!"ACTIVE".equals(device.getDeviceStatus()) || !Integer.valueOf(1).equals(device.getSyncEnabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Device sync disabled");
        }
        return device;
    }

    private SyncConflict requireConflict(Long id) {
        SyncConflict conflict = syncConflictMapper.selectById(id);
        if (conflict == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Conflict not found");
        }
        return conflict;
    }

    private WorkOrderRecord findRecordByLocalId(String localId) {
        if (localId == null) {
            return null;
        }
        return recordMapper.selectAll().stream()
                .filter(record -> localId.equals(record.getLocalId()))
                .findFirst()
                .orElse(null);
    }

    private WorkOrderAttachment findAttachmentByLocalId(String localId) {
        if (localId == null) {
            return null;
        }
        return attachmentMapper.selectAll().stream()
                .filter(attachment -> localId.equals(attachment.getLocalId()))
                .findFirst()
                .orElse(null);
    }

    private AiResult findAiResultByLocalId(String localId) {
        if (localId == null) {
            return null;
        }
        return aiResultMapper.selectAll().stream()
                .filter(result -> localId.equals(result.getLocalId()))
                .findFirst()
                .orElse(null);
    }

    private AiDefectBox findAiDefectBoxByLocalId(String localId) {
        if (localId == null) {
            return null;
        }
        return aiDefectBoxMapper.selectAll().stream()
                .filter(box -> localId.equals(box.getLocalId()))
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> syncItem(String entityType, Long serverId, String localId, Integer version,
            LocalDateTime updatedAt, Object payload) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("entityType", entityType);
        item.put("serverId", serverId);
        item.put("localId", localId);
        item.put("version", version);
        item.put("updatedAt", updatedAt);
        item.put("payload", payload);
        return item;
    }

    private SyncPushResultVO taskResult(SyncTask task, String batchId) {
        SyncPushResultVO result = new SyncPushResultVO();
        result.taskId = task.getId();
        result.batchId = batchId;
        result.successCount = task.getSuccessCount();
        result.failedCount = task.getFailedCount();
        result.conflictCount = task.getConflictCount();
        return result;
    }

    private DeviceVO toDeviceVO(DeviceInfo device) {
        DeviceVO vo = new DeviceVO();
        vo.id = device.getId();
        vo.deviceId = device.getDeviceId();
        vo.userId = device.getUserId();
        vo.onlineStatus = device.getOnlineStatus();
        vo.deviceStatus = device.getDeviceStatus();
        return vo;
    }

    private SyncConflictVO toConflictVO(SyncConflict conflict) {
        SyncConflictVO vo = new SyncConflictVO();
        vo.id = conflict.getId();
        vo.conflictNo = conflict.getConflictNo();
        vo.entityType = conflict.getEntityType();
        vo.localId = conflict.getLocalId();
        vo.serverId = conflict.getServerId();
        vo.workOrderId = conflict.getWorkOrderId();
        vo.conflictType = conflict.getConflictType();
        vo.resolveStatus = conflict.getResolveStatus();
        vo.clientPayload = conflict.getClientPayload();
        vo.serverPayload = conflict.getServerPayload();
        return vo;
    }

    private void fillDevice(DeviceInfo device, DeviceRegisterRequest request) {
        device.setDeviceName(request.deviceName);
        device.setPlatform(request.platform == null ? "ANDROID" : request.platform);
        device.setOsVersion(request.osVersion);
        device.setAppVersion(request.appVersion);
        device.setManufacturer(request.manufacturer);
        device.setModel(request.model);
        device.setImeiHash(request.imeiHash);
        device.setPushToken(request.pushToken);
    }

    private void rejectFileBody(JsonNode payload) {
        if (payload == null) {
            return;
        }
        if (payload.has("fileContent") || payload.has("base64") || payload.has("binary")) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "sync/push only accepts metadata and fileId");
        }
    }

    private Long longField(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, field + " is required");
        }
        return node.get(field).asLong();
    }

    private Long optionalLongField(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) ? node.get(field).asLong() : null;
    }

    private String textField(JsonNode node, String field, String defaultValue) {
        return node != null && node.hasNonNull(field) ? node.get(field).asText() : defaultValue;
    }

    private Integer intField(JsonNode node, String field, Integer defaultValue) {
        return node != null && node.hasNonNull(field) ? node.get(field).asInt() : defaultValue;
    }

    private java.math.BigDecimal decimalField(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) ? node.get(field).decimalValue() : null;
    }

    private LocalDateTime dateTimeField(JsonNode node, String field, LocalDateTime defaultValue) {
        if (node == null || !node.hasNonNull(field)) {
            return defaultValue;
        }
        return LocalDateTime.parse(node.get(field).asText().replace(" ", "T"));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }
}
