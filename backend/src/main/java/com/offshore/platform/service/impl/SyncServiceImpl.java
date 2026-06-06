package com.offshore.platform.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ConflictType;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.enums.SyncActionType;
import com.offshore.platform.common.enums.SyncModuleType;
import com.offshore.platform.common.enums.SyncStatus;
import com.offshore.platform.common.sync.SyncableEntity;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.dto.sync.ConflictResolveRequest;
import com.offshore.platform.dto.sync.DeviceRegisterRequest;
import com.offshore.platform.dto.sync.SyncConflictBatchResolveRequest;
import com.offshore.platform.dto.sync.SyncConflictQueryRequest;
import com.offshore.platform.dto.sync.SyncAckRequest;
import com.offshore.platform.dto.sync.SyncPullRequest;
import com.offshore.platform.dto.sync.SyncPushItem;
import com.offshore.platform.dto.sync.SyncPushRequest;
import com.offshore.platform.entity.DeviceInfo;
import com.offshore.platform.entity.AiDefectBox;
import com.offshore.platform.entity.AiResult;
import com.offshore.platform.entity.KnowledgeCase;
import com.offshore.platform.entity.SyncConflict;
import com.offshore.platform.entity.SyncLog;
import com.offshore.platform.entity.SyncTask;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.entity.WorkOrderRecord;
import com.offshore.platform.entity.WorkOrderAcceptance;
import com.offshore.platform.entity.WorkOrderMaterialUsage;
import com.offshore.platform.entity.WorkOrderPdf;
import com.offshore.platform.entity.WorkOrderSignature;
import com.offshore.platform.entity.WorkOrderVersionLog;
import com.offshore.platform.mapper.DeviceInfoMapper;
import com.offshore.platform.mapper.AiDefectBoxMapper;
import com.offshore.platform.mapper.AiResultMapper;
import com.offshore.platform.mapper.SyncConflictMapper;
import com.offshore.platform.mapper.SyncLogMapper;
import com.offshore.platform.mapper.SyncTaskMapper;
import com.offshore.platform.mapper.WorkOrderAcceptanceMapper;
import com.offshore.platform.mapper.WorkOrderAcceptanceMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderMaterialUsageMapper;
import com.offshore.platform.mapper.WorkOrderPdfMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import com.offshore.platform.mapper.WorkOrderSignatureMapper;
import com.offshore.platform.mapper.WorkOrderVersionLogMapper;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.SyncService;
import com.offshore.platform.vo.sync.DeviceVO;
import com.offshore.platform.vo.sync.SyncConflictVO;
import com.offshore.platform.vo.sync.ConflictFieldCompareVO;
import com.offshore.platform.vo.sync.SyncConflictCompareVO;
import com.offshore.platform.vo.sync.SyncConflictDetailVO;
import com.offshore.platform.vo.sync.SyncItemResultVO;
import com.offshore.platform.vo.sync.SyncPullItemVO;
import com.offshore.platform.vo.sync.SyncPullVO;
import com.offshore.platform.vo.sync.SyncPushResultVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
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
    private final WorkOrderSignatureMapper signatureMapper;
    private final WorkOrderAcceptanceMapper acceptanceMapper;
    private final WorkOrderMaterialUsageMapper materialUsageMapper;
    private final WorkOrderPdfMapper workOrderPdfMapper;
    private final WorkOrderVersionLogMapper versionLogMapper;
    private final AiResultMapper aiResultMapper;
    private final AiDefectBoxMapper aiDefectBoxMapper;
    private final WorkOrderAssignmentMapper assignmentMapper;
    private final DataScopeService dataScopeService;
    private final ObjectMapper objectMapper;

    public SyncServiceImpl(DeviceInfoMapper deviceMapper, SyncTaskMapper syncTaskMapper, SyncLogMapper syncLogMapper,
            SyncConflictMapper syncConflictMapper, WorkOrderMapper workOrderMapper, WorkOrderRecordMapper recordMapper,
            WorkOrderAttachmentMapper attachmentMapper, WorkOrderAssignmentMapper assignmentMapper,
            WorkOrderSignatureMapper signatureMapper, WorkOrderAcceptanceMapper acceptanceMapper,
            WorkOrderMaterialUsageMapper materialUsageMapper, WorkOrderPdfMapper workOrderPdfMapper,
            WorkOrderVersionLogMapper versionLogMapper, AiResultMapper aiResultMapper, AiDefectBoxMapper aiDefectBoxMapper,
            DataScopeService dataScopeService, ObjectMapper objectMapper) {
        this.deviceMapper = deviceMapper;
        this.syncTaskMapper = syncTaskMapper;
        this.syncLogMapper = syncLogMapper;
        this.syncConflictMapper = syncConflictMapper;
        this.workOrderMapper = workOrderMapper;
        this.recordMapper = recordMapper;
        this.attachmentMapper = attachmentMapper;
        this.signatureMapper = signatureMapper;
        this.acceptanceMapper = acceptanceMapper;
        this.materialUsageMapper = materialUsageMapper;
        this.workOrderPdfMapper = workOrderPdfMapper;
        this.versionLogMapper = versionLogMapper;
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

        // Determine the cursor: prefer request.cursor, fall back to lastSyncTime, default epoch.
        String cursorStr = request.cursor != null ? request.cursor
                : request.lastSyncTime != null ? request.lastSyncTime : "2000-01-01 00:00:00";
        LocalDateTime cursor = parseCursor(cursorStr);
        int limit = request.limit != null ? request.limit : 200;

        SyncTask task = createTask("PULL", request.deviceId, null, user, 0, cursorStr);
        SyncPullVO vo = new SyncPullVO();
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        vo.serverTime = nowStr;
        vo.ackRequired = true;

        List<String> entityTypes = request.entityTypes;
        boolean pullAll = entityTypes == null || entityTypes.isEmpty();

        LocalDateTime maxUpdatedAt = cursor;
        int totalFetched = 0;

        // ---- Collect accessible work order IDs for scoped queries ----
        List<Long> accessibleWoIds = getAccessibleWorkOrderIds(user);

        // ---- WORK_ORDER (Server → Mobile) ----
        if (pullAll || entityTypes.contains(SyncModuleType.WORK_ORDER.getCode())) {
            List<WorkOrder> workOrders = fetchWorkOrdersIncremental(user, cursor, limit);
            for (WorkOrder wo : workOrders) {
                vo.items.add(toPullItem(SyncModuleType.WORK_ORDER, wo.getId(),
                        wo.getLocalId(), wo.getVersion(), wo.getUpdatedAt(),
                        wo.getDeletedFlag(), wo.getOperatorId(), wo.getDeviceId(), wo));
                if (wo.getUpdatedAt() != null && wo.getUpdatedAt().isAfter(maxUpdatedAt)) {
                    maxUpdatedAt = wo.getUpdatedAt();
                }
                totalFetched++;
            }
        }

        // ---- WORK_ORDER_RECORD (Mobile → Server → Mobile) ----
        if ((pullAll || entityTypes.contains(SyncModuleType.WORK_RECORD.getCode())) && !accessibleWoIds.isEmpty()) {
            List<WorkOrderRecord> records = recordMapper.selectUpdatedAfterByWorkOrderIds(
                    cursor, accessibleWoIds, limit);
            for (WorkOrderRecord rec : records) {
                vo.items.add(toPullItem(SyncModuleType.WORK_RECORD, rec.getId(),
                        rec.getLocalId(), rec.getVersion(), rec.getUpdatedAt(),
                        rec.getDeletedFlag(), rec.getOperatorId(), rec.getDeviceId(), rec));
                if (rec.getUpdatedAt() != null && rec.getUpdatedAt().isAfter(maxUpdatedAt)) {
                    maxUpdatedAt = rec.getUpdatedAt();
                }
                totalFetched++;
            }
        }

        // ---- Generate cursor and hasMore ----
        vo.cursor = nowStr;
        vo.nextCursor = maxUpdatedAt != null && maxUpdatedAt.isAfter(cursor)
                ? maxUpdatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : nowStr;
        vo.hasMore = totalFetched >= limit;

        task.setTaskStatus(SyncStatus.SYNCED.getCode());
        task.setTotalCount(vo.items.size());
        task.setSuccessCount(vo.items.size());
        task.setResponseCursor(vo.nextCursor);
        task.setServerEndTime(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        syncTaskMapper.updateById(task);
        return vo;
    }

    /**
     * Parse a "yyyy-MM-dd HH:mm:ss" cursor string to LocalDateTime.
     */
    private LocalDateTime parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        }
        try {
            return LocalDateTime.parse(cursor, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        }
    }

    /**
     * Get all work order IDs accessible to the current user based on their scope.
     */
    private List<Long> getAccessibleWorkOrderIds(CurrentUser user) {
        if (dataScopeService.canAccessAll(user)) {
            return workOrderMapper.selectAll().stream().map(WorkOrder::getId).toList();
        }
        return workOrderMapper.selectAll().stream()
                .filter(wo -> canMobileAccessWorkOrder(user, wo))
                .map(WorkOrder::getId)
                .toList();
    }

    /**
     * Fetch work orders incrementally, respecting the user's data scope.
     */
    private List<WorkOrder> fetchWorkOrdersIncremental(CurrentUser user, LocalDateTime cursor, int limit) {
        if (dataScopeService.canAccessAll(user)) {
            return workOrderMapper.selectUpdatedAfter(cursor, limit);
        }
        // For maintainer: filter by maintainer_id
        Long userId = user.getUserId();
        return workOrderMapper.selectUpdatedAfterByMaintainer(cursor, userId, limit);
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
        return conflicts(new SyncConflictQueryRequest());
    }

    @Override
    public List<SyncConflictVO> conflicts(SyncConflictQueryRequest request) {
        CurrentUser user = CurrentUserContext.require();
        return syncConflictMapper.selectAll().stream()
                .filter(item -> canAccessConflict(user, item))
                .filter(item -> request.workOrderId == null || request.workOrderId.equals(item.getWorkOrderId()))
                .filter(item -> request.businessNo == null || request.businessNo.equals(item.getBusinessNo()))
                .filter(item -> request.entityType == null || request.entityType.equals(item.getEntityType()))
                .filter(item -> request.conflictStatus == null || request.conflictStatus.equals(item.getResolveStatus()))
                .filter(item -> request.deviceId == null || request.deviceId.equals(item.getDeviceId()))
                .filter(item -> request.operatorId == null || request.operatorId.equals(item.getOperatorId()))
                .filter(item -> request.createdTimeStart == null || (item.getCreatedAt() != null && !item.getCreatedAt().isBefore(request.createdTimeStart)))
                .filter(item -> request.createdTimeEnd == null || (item.getCreatedAt() != null && !item.getCreatedAt().isAfter(request.createdTimeEnd)))
                .map(this::toConflictVO).toList();
    }

    @Override
    public SyncConflictVO conflict(Long id) {
        return conflictDetail(id);
    }

    @Override
    public SyncConflictDetailVO conflictDetail(Long id) {
        CurrentUser user = CurrentUserContext.require();
        SyncConflict conflict = requireConflict(id);
        requireConflictAccess(user, conflict);
        SyncConflictDetailVO vo = new SyncConflictDetailVO();
        fillConflictVO(vo, conflict);
        vo.oldPayload = conflict.getOldPayload();
        vo.finalPayload = conflict.getFinalPayload();
        vo.conflictFields = conflict.getConflictFields();
        vo.availableStrategies = List.of("KEEP_SERVER", "KEEP_CLIENT", "MANUAL_MERGE", "IGNORE_CLIENT");
        return vo;
    }

    @Override
    @Transactional
    public SyncConflictVO resolveConflict(Long id, ConflictResolveRequest request) {
        CurrentUser user = CurrentUserContext.require();
        SyncConflict conflict = requireConflict(id);
        requireConflictAccess(user, conflict);
        if (!"PENDING".equals(conflict.getResolveStatus()) && !"PENDING_REVIEW".equals(conflict.getResolveStatus())) {
            return toConflictVO(conflict);
        }
        if (!List.of("KEEP_SERVER", "KEEP_CLIENT", "MANUAL_MERGE", "IGNORE_CLIENT").contains(request.resolveStrategy)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Unsupported resolve strategy");
        }
        if ("KEEP_CLIENT".equals(request.resolveStrategy)) {
            applyConflictPayload(conflict, conflict.getClientPayload(), user);
        } else if ("MANUAL_MERGE".equals(request.resolveStrategy)) {
            applyConflictPayload(conflict, request.finalPayload, user);
        }
        conflict.setResolveStrategy(request.resolveStrategy);
        conflict.setFinalPayload(request.finalPayload);
        conflict.setResolveComment(request.resolveComment);
        conflict.setResolveStatus("IGNORE_CLIENT".equals(request.resolveStrategy) ? "IGNORED" : "RESOLVED");
        conflict.setResolverId(user.getUserId());
        conflict.setResolveTime(LocalDateTime.now());
        conflict.setUpdatedAt(LocalDateTime.now());
        conflict.setUpdatedBy(user.getUserId());
        syncConflictMapper.updateById(conflict);
        writeVersionLog(conflict, user, request.resolveStrategy, request.finalPayload);
        return toConflictVO(conflict);
    }

    @Override
    @Transactional
    public List<SyncConflictVO> batchResolveConflicts(SyncConflictBatchResolveRequest request) {
        if (!List.of("KEEP_SERVER", "IGNORE_CLIENT").contains(request.resolveStrategy)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Batch resolve only supports KEEP_SERVER or IGNORE_CLIENT");
        }
        List<SyncConflictVO> result = new ArrayList<>();
        for (Long id : request.conflictIds) {
            ConflictResolveRequest single = new ConflictResolveRequest();
            single.resolveStrategy = request.resolveStrategy;
            single.resolveComment = request.resolveComment;
            result.add(resolveConflict(id, single));
        }
        return result;
    }

    @Override
    public SyncConflictCompareVO compareConflict(Long id) {
        CurrentUser user = CurrentUserContext.require();
        SyncConflict conflict = requireConflict(id);
        requireConflictAccess(user, conflict);
        SyncConflictCompareVO vo = new SyncConflictCompareVO();
        vo.conflictId = conflict.getId();
        vo.conflictNo = conflict.getConflictNo();
        vo.entityType = conflict.getEntityType();
        vo.workOrderId = conflict.getWorkOrderId();
        vo.businessNo = conflict.getBusinessNo();
        JsonNode client = readTree(conflict.getClientPayload());
        JsonNode server = readTree(conflict.getServerPayload());
        List<String> names = new ArrayList<>();
        if (client != null) client.fieldNames().forEachRemaining(names::add);
        if (server != null) server.fieldNames().forEachRemaining(name -> { if (!names.contains(name)) names.add(name); });
        for (String name : names) {
            String clientValue = client != null && client.has(name) ? client.get(name).asText() : null;
            String serverValue = server != null && server.has(name) ? server.get(name).asText() : null;
            ConflictFieldCompareVO field = new ConflictFieldCompareVO();
            field.fieldName = name;
            field.fieldLabel = name;
            field.clientValue = clientValue;
            field.serverValue = serverValue;
            field.conflict = !Objects.equals(clientValue, serverValue);
            field.suggestedValue = serverValue;
            vo.fields.add(field);
        }
        return vo;
    }

    @Override
    public List<SyncConflictVO> workOrderConflicts(Long workOrderId) {
        SyncConflictQueryRequest request = new SyncConflictQueryRequest();
        request.workOrderId = workOrderId;
        return conflicts(request);
    }

    private SyncItemResultVO handlePushItem(SyncTask task, SyncPushRequest request, SyncPushItem item, CurrentUser user) {
        SyncLog log = createLog(task, request, item, user);
        try {
            rejectFileBody(item.payload);
            String entityType = item.entityType;
            if (SyncModuleType.WORK_RECORD.getCode().equals(entityType) || "WORK_ORDER_RECORD".equals(entityType)) {
                return handleRecord(task, log, item, user, request.deviceId);
            }
            if (SyncModuleType.ATTACHMENT_META.getCode().equals(entityType) || "WORK_ORDER_ATTACHMENT".equals(entityType)) {
                return handleAttachment(log, item, user, request.deviceId);
            }
            if (SyncModuleType.AI_RESULT.getCode().equals(entityType)) {
                return handleAiResult(log, item, user, request.deviceId);
            }
            if ("AI_DEFECT_BOX".equals(entityType)) {
                return handleAiDefectBox(log, item, user, request.deviceId);
            }
            if (SyncModuleType.SIGNATURE.getCode().equals(entityType)) {
                return handleSignature(log, item, user, request.deviceId);
            }
            if (SyncModuleType.ACCEPTANCE.getCode().equals(entityType)) {
                return handleAcceptance(log, item, user, request.deviceId);
            }
            if (SyncModuleType.MATERIAL_USAGE.getCode().equals(entityType)) {
                return handleMaterialUsage(log, item, user, request.deviceId);
            }
            if (SyncModuleType.WORK_ORDER.getCode().equals(entityType)) {
                return handleWorkOrderSync(log, item, user, request.deviceId);
            }
            return fail(log, item, "UNSUPPORTED_ENTITY", "Unsupported entityType: " + entityType);
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
            fillCreateSyncFields(record, item.localId, deviceId, user.getUserId());
            recordMapper.insert(record);
            record.setServerId(record.getId());
            recordMapper.updateById(record);
            return success(log, item, record.getId(), record.getVersion(), "Created");
        }
        if ("DELETE".equals(item.actionType)) {
            if (existing == null) {
                return success(log, item, null, null, "Already deleted or not found");
            }
            if (!Objects.equals(existing.getVersion(), item.version)) {
                log.setServerVersion(existing.getVersion());
                Long conflictId = createConflict(task, log, item, user, existing.getWorkOrderId(),
                        existing.getId(), item.payload, existing);
                return conflict(log, item, conflictId, existing.getId(), existing.getVersion(),
                        "DELETE_AFTER_UPDATE conflict");
            }
            markDeleted(existing, user.getUserId());
            recordMapper.updateById(existing);
            return success(log, item, existing.getId(), existing.getVersion() + 1, "Deleted");
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
        record.setAbnormalDesc(textField(item.payload, "abnormalDesc", record.getAbnormalDesc()));
        record.setVersion(record.getVersion() + 1);
        record.setUpdatedAt(LocalDateTime.now());
        record.setUpdatedBy(user.getUserId());
        recordMapper.updateById(record);
        return success(log, item, record.getId(), record.getVersion(), "Updated");
    }

    private SyncItemResultVO handleAttachment(SyncLog log, SyncPushItem item, CurrentUser user, String deviceId) {
        WorkOrderAttachment existing = findAttachmentByLocalId(item.localId);
        if (SyncActionType.DELETE.getCode().equals(item.actionType)) {
            if (existing == null) {
                return success(log, item, null, null, "Already deleted or not found");
            }
            markDeleted(existing, user.getUserId());
            attachmentMapper.updateById(existing);
            return success(log, item, existing.getId(), existing.getVersion() + 1, "Deleted");
        }
        if ("CREATE".equals(item.actionType)) {
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
            fillCreateSyncFields(attachment, item.localId, deviceId, user.getUserId());
            attachmentMapper.insert(attachment);
            attachment.setServerId(attachment.getId());
            attachmentMapper.updateById(attachment);
            return success(log, item, attachment.getId(), attachment.getVersion(), "Created");
        }
        return fail(log, item, "UNSUPPORTED_ACTION", "Attachment only supports CREATE or DELETE");
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
        fillCreateSyncFields(result, item.localId, deviceId, user.getUserId());
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
        fillCreateSyncFields(box, item.localId, deviceId, user.getUserId());
        aiDefectBoxMapper.insert(box);
        box.setServerId(box.getId());
        aiDefectBoxMapper.updateById(box);
        return success(log, item, box.getId(), box.getVersion(), "Created");
    }

    // ---- Unified sync field fillers (SyncableEntity interface) ----

    /**
     * Fill sync fields for a newly created entity.
     *
     * @param entity   any entity implementing SyncableEntity
     * @param localId  mobile local ID
     * @param deviceId source device ID
     * @param userId   current operator ID
     */
    private void fillCreateSyncFields(Object entity, String localId, String deviceId, Long userId) {
        if (!(entity instanceof SyncableEntity s)) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "Entity does not implement SyncableEntity");
        }
        LocalDateTime now = LocalDateTime.now();
        s.setLocalId(localId);
        s.setVersion(1);
        s.setSyncStatus(SyncStatus.SYNCED.getCode());
        s.setDeviceId(deviceId);
        s.setOperatorId(userId);
        s.setConflictFlag(0);
        s.setCreatedAt(now);
        s.setUpdatedAt(now);
        s.setDeletedFlag(0);
        if (s instanceof WorkOrderRecord r) {
            r.setCreatedBy(userId);
            r.setUpdatedBy(userId);
            r.setClientCreatedAt(now);
            r.setClientUpdatedAt(now);
        } else if (s instanceof WorkOrderAttachment a) {
            a.setCreatedBy(userId);
            a.setUpdatedBy(userId);
            a.setClientCreatedAt(now);
            a.setClientUpdatedAt(now);
        } else if (s instanceof WorkOrderSignature sig) {
            sig.setCreatedBy(userId);
            sig.setUpdatedBy(userId);
            sig.setClientCreatedAt(now);
            sig.setClientUpdatedAt(now);
        } else if (s instanceof WorkOrderAcceptance acc) {
            acc.setCreatedBy(userId);
            acc.setUpdatedBy(userId);
            acc.setClientCreatedAt(now);
            acc.setClientUpdatedAt(now);
        } else if (s instanceof WorkOrderPdf pdf) {
            pdf.setCreatedBy(userId);
            pdf.setUpdatedBy(userId);
        } else if (s instanceof WorkOrderMaterialUsage mu) {
            mu.setCreatedBy(userId);
            mu.setUpdatedBy(userId);
            mu.setClientCreatedAt(now);
            mu.setClientUpdatedAt(now);
        } else if (s instanceof AiResult ai) {
            ai.setCreatedBy(userId);
            ai.setUpdatedBy(userId);
            ai.setClientCreatedAt(now);
            ai.setClientUpdatedAt(now);
        } else if (s instanceof AiDefectBox box) {
            box.setCreatedBy(userId);
            box.setUpdatedBy(userId);
        } else if (s instanceof KnowledgeCase k) {
            k.setCreatedBy(userId);
            k.setUpdatedBy(userId);
        } else {
            // Generic fallback via setCreatedBy / setUpdatedBy where available
            s.setCreatedAt(now);
            s.setUpdatedAt(now);
        }
    }

    /**
     * Fill sync fields for an updated entity.
     */
    private void fillUpdateSyncFields(Object entity, Long userId) {
        if (!(entity instanceof SyncableEntity s)) return;
        s.setVersion(s.getVersion() + 1);
        s.setUpdatedAt(LocalDateTime.now());
        s.setSyncStatus(SyncStatus.SYNCED.getCode());
        if (s instanceof WorkOrderRecord r) {
            r.setUpdatedBy(userId);
        } else if (s instanceof WorkOrderAttachment a) {
            a.setUpdatedBy(userId);
        } else if (s instanceof WorkOrderSignature sig) {
            sig.setUpdatedBy(userId);
        } else if (s instanceof WorkOrderAcceptance acc) {
            acc.setUpdatedBy(userId);
        } else if (s instanceof WorkOrderPdf pdf) {
            pdf.setUpdatedBy(userId);
        } else if (s instanceof WorkOrderMaterialUsage mu) {
            mu.setUpdatedBy(userId);
        } else if (s instanceof AiResult ai) {
            ai.setUpdatedBy(userId);
        } else if (s instanceof KnowledgeCase k) {
            k.setUpdatedBy(userId);
        }
    }

    /**
     * Mark entity as logically deleted.
     */
    private void markDeleted(Object entity, Long userId) {
        if (!(entity instanceof SyncableEntity s)) return;
        s.setDeletedFlag(1);
        s.setSyncStatus(SyncStatus.DELETED.getCode());
        s.setUpdatedAt(LocalDateTime.now());
        if (s instanceof WorkOrderRecord r) {
            r.setUpdatedBy(userId);
        } else if (s instanceof WorkOrderAttachment a) {
            a.setUpdatedBy(userId);
        } else if (s instanceof WorkOrder w) {
            w.setUpdatedBy(userId);
        }
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

    private boolean canAccessConflict(CurrentUser user, SyncConflict conflict) {
        if (dataScopeService.canAccessAll(user)) {
            return true;
        }
        if (conflict.getWorkOrderId() == null) {
            return false;
        }
        WorkOrder workOrder = workOrderMapper.selectById(conflict.getWorkOrderId());
        return workOrder != null && dataScopeService.canAccessProject(user, workOrder.getProjectId());
    }

    private void requireConflictAccess(CurrentUser user, SyncConflict conflict) {
        if (!canAccessConflict(user, conflict)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for sync conflict");
        }
    }

    private void applyConflictPayload(SyncConflict conflict, String payload, CurrentUser user) {
        if (payload == null || payload.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Payload is required");
        }
        JsonNode node = readTree(payload);
        if (node == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Invalid JSON payload");
        }

        String entityType = conflict.getEntityType();
        Long entityId = conflict.getEntityId();

        // ---- WORK_ORDER_RECORD ----
        if (SyncModuleType.WORK_RECORD.getCode().equals(entityType) || "WORK_ORDER_RECORD".equals(entityType) || "work_order_record".equals(entityType)) {
            WorkOrderRecord record = entityId != null ? recordMapper.selectById(entityId) : null;
            if (record == null) throw new BusinessException(ErrorCode.NOT_FOUND, "Record not found");
            applyRecordFields(record, node, user);
            return;
        }

        // ---- WORK_ORDER_ATTACHMENT ----
        if (SyncModuleType.ATTACHMENT_META.getCode().equals(entityType) || "WORK_ORDER_ATTACHMENT".equals(entityType) || "work_order_attachment".equals(entityType)) {
            WorkOrderAttachment attachment = entityId != null ? attachmentMapper.selectById(entityId) : null;
            if (attachment == null) throw new BusinessException(ErrorCode.NOT_FOUND, "Attachment not found");
            applyAttachmentFields(attachment, node, user);
            return;
        }

        // ---- WORK_ORDER_SIGNATURE ----
        if (SyncModuleType.SIGNATURE.getCode().equals(entityType) || "WORK_ORDER_SIGNATURE".equals(entityType) || "work_order_signature".equals(entityType)) {
            WorkOrderSignature signature = entityId != null ? signatureMapper.selectById(entityId) : null;
            if (signature == null) throw new BusinessException(ErrorCode.NOT_FOUND, "Signature not found");
            applySignatureFields(signature, node, user);
            return;
        }

        // ---- WORK_ORDER_ACCEPTANCE (locked check) ----
        if (SyncModuleType.ACCEPTANCE.getCode().equals(entityType) || "WORK_ORDER_ACCEPTANCE".equals(entityType) || "work_order_acceptance".equals(entityType)) {
            WorkOrderAcceptance acceptance = entityId != null ? acceptanceMapper.selectById(entityId) : null;
            if (acceptance == null) throw new BusinessException(ErrorCode.NOT_FOUND, "Acceptance not found");
            if (Integer.valueOf(1).equals(acceptance.getLockedFlag())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "Acceptance is locked; admin override required");
            }
            applyAcceptanceFields(acceptance, node, user);
            return;
        }

        // ---- WORK_ORDER_PDF (locked check) ----
        if (SyncModuleType.PDF.getCode().equals(entityType) || "WORK_ORDER_PDF".equals(entityType) || "work_order_pdf".equals(entityType)) {
            WorkOrderPdf pdf = entityId != null ? workOrderPdfMapper.selectById(entityId) : null;
            if (pdf == null) throw new BusinessException(ErrorCode.NOT_FOUND, "PDF record not found");
            if (Integer.valueOf(1).equals(pdf.getLockedFlag())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "PDF is locked; admin override required");
            }
            applyPdfFields(pdf, node, user);
            return;
        }

        // ---- WORK_ORDER_MATERIAL_USAGE ----
        if (SyncModuleType.MATERIAL_USAGE.getCode().equals(entityType) || "WORK_ORDER_MATERIAL_USAGE".equals(entityType) || "work_order_material_usage".equals(entityType)) {
            WorkOrderMaterialUsage usage = entityId != null ? materialUsageMapper.selectById(entityId) : null;
            if (usage == null) throw new BusinessException(ErrorCode.NOT_FOUND, "Material usage not found");
            applyMaterialUsageFields(usage, node, user);
            return;
        }

        // ---- AI_RESULT ----
        if (SyncModuleType.AI_RESULT.getCode().equals(entityType) || "AI_RESULT".equals(entityType) || "ai_result".equals(entityType)) {
            AiResult aiResult = entityId != null ? aiResultMapper.selectById(entityId) : null;
            if (aiResult == null) throw new BusinessException(ErrorCode.NOT_FOUND, "AI result not found");
            applyAiResultFields(aiResult, node, user);
            return;
        }

        // ---- WORK_ORDER ----
        if (SyncModuleType.WORK_ORDER.getCode().equals(entityType) || "WORK_ORDER".equals(entityType) || "work_order".equals(entityType)) {
            WorkOrder workOrder = entityId != null ? workOrderMapper.selectById(entityId) : null;
            if (workOrder == null) throw new BusinessException(ErrorCode.NOT_FOUND, "Work order not found");
            applyWorkOrderFields(workOrder, node, user);
            return;
        }
    }

    // ---- Per-entity field application ----

    private void applyRecordFields(WorkOrderRecord record, JsonNode node, CurrentUser user) {
        record.setConstructionDesc(textField(node, "constructionDesc", record.getConstructionDesc()));
        record.setSiteCondition(textField(node, "siteCondition", record.getSiteCondition()));
        record.setAbnormalFlag(intField(node, "abnormalFlag", record.getAbnormalFlag()));
        record.setAbnormalDesc(textField(node, "abnormalDesc", record.getAbnormalDesc()));
        bumpVersion(record, user);
        recordMapper.updateById(record);
    }

    private void applyAttachmentFields(WorkOrderAttachment attachment, JsonNode node, CurrentUser user) {
        attachment.setAttachmentName(textField(node, "attachmentName", attachment.getAttachmentName()));
        attachment.setAttachmentDesc(textField(node, "attachmentDesc", attachment.getAttachmentDesc()));
        bumpVersion(attachment, user);
        attachmentMapper.updateById(attachment);
    }

    private void applySignatureFields(WorkOrderSignature signature, JsonNode node, CurrentUser user) {
        signature.setSignerName(textField(node, "signerName", signature.getSignerName()));
        signature.setSignatureRole(textField(node, "signatureRole", signature.getSignatureRole()));
        bumpVersion(signature, user);
        signatureMapper.updateById(signature);
    }

    private void applyAcceptanceFields(WorkOrderAcceptance acceptance, JsonNode node, CurrentUser user) {
        acceptance.setAcceptanceResult(textField(node, "acceptanceResult", acceptance.getAcceptanceResult()));
        acceptance.setAcceptanceOpinion(textField(node, "acceptanceOpinion", acceptance.getAcceptanceOpinion()));
        bumpVersion(acceptance, user);
        acceptanceMapper.updateById(acceptance);
    }

    private void applyPdfFields(WorkOrderPdf pdf, JsonNode node, CurrentUser user) {
        pdf.setPdfStatus(textField(node, "pdfStatus", pdf.getPdfStatus()));
        bumpVersion(pdf, user);
        workOrderPdfMapper.updateById(pdf);
    }

    private void applyMaterialUsageFields(WorkOrderMaterialUsage usage, JsonNode node, CurrentUser user) {
        if (node.hasNonNull("usedQty")) usage.setUsedQty(new java.math.BigDecimal(node.get("usedQty").asText()));
        usage.setUsageDesc(textField(node, "usageDesc", usage.getUsageDesc()));
        bumpVersion(usage, user);
        materialUsageMapper.updateById(usage);
    }

    private void applyAiResultFields(AiResult aiResult, JsonNode node, CurrentUser user) {
        aiResult.setDefectType(textField(node, "defectType", aiResult.getDefectType()));
        aiResult.setReviewStatus(textField(node, "reviewStatus", aiResult.getReviewStatus()));
        bumpVersion(aiResult, user);
        aiResultMapper.updateById(aiResult);
    }

    private void applyWorkOrderFields(WorkOrder workOrder, JsonNode node, CurrentUser user) {
        if (node.hasNonNull("status")) workOrder.setStatus(node.get("status").asText());
        bumpVersion(workOrder, user);
        workOrderMapper.updateById(workOrder);
    }

    /** Increment version, update timestamp, and clear conflict flag on a SyncableEntity. */
    private void bumpVersion(SyncableEntity entity, CurrentUser user) {
        entity.setVersion(defaultInt(entity.getVersion(), 1) + 1);
        entity.setSyncStatus(SyncStatus.SYNCED.getCode());
        entity.setConflictFlag(0);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(user.getUserId());
    }

    private JsonNode readTree(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private void writeVersionLog(SyncConflict conflict, CurrentUser user, String strategy, String finalPayload) {
        WorkOrderVersionLog log = new WorkOrderVersionLog();
        WorkOrder workOrder = conflict.getWorkOrderId() == null ? null : workOrderMapper.selectById(conflict.getWorkOrderId());
        log.setWorkOrderId(conflict.getWorkOrderId());
        log.setWorkOrderNo(workOrder == null ? conflict.getBusinessNo() : workOrder.getWorkOrderNo());
        log.setPreviousVersion(conflict.getServerVersion());
        log.setVersion(conflict.getServerVersion() == null ? null : conflict.getServerVersion() + 1);
        log.setChangeSource("SYNC_CONFLICT");
        log.setChangeType(strategy);
        log.setChangedFields(conflict.getConflictFields());
        log.setOldPayload(conflict.getServerPayload());
        log.setNewPayload(finalPayload == null ? conflict.getClientPayload() : finalPayload);
        log.setLocalId(conflict.getLocalId());
        log.setServerId(conflict.getServerId());
        log.setDeviceId(conflict.getDeviceId());
        log.setOperatorId(user.getUserId());
        log.setSyncTaskId(conflict.getSyncTaskId());
        log.setSyncLogId(conflict.getSyncLogId());
        log.setConflictId(conflict.getId());
        log.setClientUpdatedAt(conflict.getClientUpdatedAt());
        log.setServerUpdatedAt(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        log.setDeletedFlag(0);
        log.setCreatedBy(user.getUserId());
        log.setUpdatedBy(user.getUserId());
        versionLogMapper.insert(log);
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

    private SyncPullItemVO toPullItem(SyncModuleType moduleType, Long serverId, String localId, Integer version,
            LocalDateTime updatedAt, Integer deletedFlag, Long operatorId, String deviceId, Object payload) {
        SyncPullItemVO item = new SyncPullItemVO();
        item.moduleType = moduleType.getCode();
        item.entityType = moduleType.getCode();
        item.serverId = serverId;
        item.localId = localId;
        item.version = version;
        item.updatedAt = updatedAt != null
                ? updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;
        item.deletedFlag = deletedFlag;
        item.operatorId = operatorId;
        item.deviceId = deviceId;
        item.payload = objectMapper.valueToTree(payload);
        return item;
    }

    // ---- New entityType handlers ----

    private SyncItemResultVO handleSignature(SyncLog log, SyncPushItem item, CurrentUser user, String deviceId) {
        if (!SyncActionType.CREATE.getCode().equals(item.actionType)) {
            return fail(log, item, "UNSUPPORTED_ACTION", "Signature only supports CREATE via sync");
        }
        WorkOrderSignature existing = item.localId != null ? findSignatureByLocalId(item.localId) : null;
        if (existing != null) {
            return success(log, item, existing.getId(), existing.getVersion(), "Idempotent create");
        }
        JsonNode payload = item.payload;
        Long workOrderId = longField(payload, "workOrderId");
        WorkOrder workOrder = requireMobileWorkOrder(workOrderId, user);
        WorkOrderSignature signature = new WorkOrderSignature();
        signature.setWorkOrderId(workOrder.getId());
        signature.setSignatureRole(textField(payload, "signatureRole", "SIGNER"));
        signature.setSignerName(textField(payload, "signerName", user.getRealName()));
        signature.setSignerUserId(user.getUserId());
        signature.setFileId(item.fileId != null ? item.fileId : textField(payload, "fileId", null));
        signature.setSignedAt(dateTimeField(payload, "signedAt", LocalDateTime.now()));
        signature.setSignatureStatus(SyncStatus.SYNCED.getCode());
        fillCreateSyncFields(signature, item.localId, deviceId, user.getUserId());
        signatureMapper.insert(signature);
        signature.setServerId(signature.getId());
        signatureMapper.updateById(signature);
        return success(log, item, signature.getId(), signature.getVersion(), "Created");
    }

    private SyncItemResultVO handleAcceptance(SyncLog log, SyncPushItem item, CurrentUser user, String deviceId) {
        if (!SyncActionType.CREATE.getCode().equals(item.actionType)) {
            return fail(log, item, "UNSUPPORTED_ACTION", "Acceptance only supports CREATE via sync");
        }
        WorkOrderAcceptance existing = item.localId != null ? findAcceptanceByLocalId(item.localId) : null;
        if (existing != null) {
            return success(log, item, existing.getId(), existing.getVersion(), "Idempotent create");
        }
        JsonNode payload = item.payload;
        Long workOrderId = longField(payload, "workOrderId");
        WorkOrder workOrder = requireMobileWorkOrder(workOrderId, user);
        WorkOrderAcceptance acceptance = new WorkOrderAcceptance();
        acceptance.setWorkOrderId(workOrder.getId());
        acceptance.setAcceptanceNo("ACC-" + System.currentTimeMillis());
        acceptance.setAcceptanceResult(textField(payload, "acceptanceResult", "PENDING"));
        acceptance.setAcceptanceOpinion(textField(payload, "acceptanceOpinion", null));
        acceptance.setAcceptanceUserId(user.getUserId());
        acceptance.setAcceptanceTime(dateTimeField(payload, "acceptanceTime", LocalDateTime.now()));
        acceptance.setLockedFlag(intField(payload, "lockedFlag", 0));
        acceptance.setAcceptanceStatus(textField(payload, "acceptanceStatus", SyncStatus.SYNCED.getCode()));
        fillCreateSyncFields(acceptance, item.localId, deviceId, user.getUserId());
        acceptanceMapper.insert(acceptance);
        acceptance.setServerId(acceptance.getId());
        acceptanceMapper.updateById(acceptance);
        return success(log, item, acceptance.getId(), acceptance.getVersion(), "Created");
    }

    private SyncItemResultVO handleMaterialUsage(SyncLog log, SyncPushItem item, CurrentUser user, String deviceId) {
        if (!SyncActionType.CREATE.getCode().equals(item.actionType)) {
            return fail(log, item, "UNSUPPORTED_ACTION", "Material usage only supports CREATE via sync");
        }
        WorkOrderMaterialUsage existing = item.localId != null ? findMaterialUsageByLocalId(item.localId) : null;
        if (existing != null) {
            return success(log, item, existing.getId(), existing.getVersion(), "Idempotent create");
        }
        JsonNode payload = item.payload;
        Long workOrderId = longField(payload, "workOrderId");
        WorkOrder workOrder = requireMobileWorkOrder(workOrderId, user);
        WorkOrderMaterialUsage usage = new WorkOrderMaterialUsage();
        usage.setWorkOrderId(workOrder.getId());
        usage.setMaterialId(longField(payload, "materialId"));
        usage.setUsedQty(new java.math.BigDecimal(textField(payload, "usedQty", "0")));
        usage.setUnit(textField(payload, "unit", null));
        usage.setUsageTime(dateTimeField(payload, "usageTime", LocalDateTime.now()));
        usage.setUsageUserId(user.getUserId());
        usage.setUsageDesc(textField(payload, "usageDesc", null));
        fillCreateSyncFields(usage, item.localId, deviceId, user.getUserId());
        materialUsageMapper.insert(usage);
        usage.setServerId(usage.getId());
        materialUsageMapper.updateById(usage);
        return success(log, item, usage.getId(), usage.getVersion(), "Created");
    }

    private SyncItemResultVO handleWorkOrderSync(SyncLog log, SyncPushItem item, CurrentUser user, String deviceId) {
        JsonNode payload = item.payload;
        Long workOrderId = longField(payload, "workOrderId");
        WorkOrder workOrder = requireMobileWorkOrder(workOrderId, user);

        if (SyncActionType.DELETE.getCode().equals(item.actionType)) {
            if (!Objects.equals(workOrder.getVersion(), item.version)) {
                log.setServerVersion(workOrder.getVersion());
                Long conflictId = createConflict(null, log, item, user, workOrderId,
                        workOrder.getId(), item.payload, workOrder);
                return conflict(log, item, conflictId, workOrder.getId(), workOrder.getVersion(),
                        ConflictType.DELETE_AFTER_UPDATE.getCode());
            }
            workOrder.setDeletedFlag(1);
            workOrder.setVersion(workOrder.getVersion() + 1);
            workOrder.setUpdatedAt(LocalDateTime.now());
            workOrder.setUpdatedBy(user.getUserId());
            workOrderMapper.updateById(workOrder);
            return success(log, item, workOrder.getId(), workOrder.getVersion(),
                    SyncActionType.DELETE.getMessage());
        }

        // UPDATE: status / maintainer fields
        if (!Objects.equals(workOrder.getVersion(), item.version)) {
            log.setServerVersion(workOrder.getVersion());
            Long conflictId = createConflict(null, log, item, user, workOrderId,
                    workOrder.getId(), item.payload, workOrder);
            return conflict(log, item, conflictId, workOrder.getId(), workOrder.getVersion(),
                    ConflictType.VERSION_CONFLICT.getCode());
        }
        if (payload.hasNonNull("status")) {
            workOrder.setStatus(textField(payload, "status", workOrder.getStatus()));
        }
        workOrder.setVersion(workOrder.getVersion() + 1);
        workOrder.setUpdatedAt(LocalDateTime.now());
        workOrder.setUpdatedBy(user.getUserId());
        workOrderMapper.updateById(workOrder);
        return success(log, item, workOrder.getId(), workOrder.getVersion(), "Updated");
    }

    // ---- Sync field fillers now use fillCreateSyncFields / fillUpdateSyncFields / markDeleted above ----
    // The previous genericFillSyncFields(Object, String, String, Long) has been replaced
    // by fillCreateSyncFields which uses the SyncableEntity interface.

    // ---- Lookup helpers for new entity types ----

    private WorkOrderSignature findSignatureByLocalId(String localId) {
        if (localId == null) return null;
        return signatureMapper.selectAll().stream()
                .filter(s -> localId.equals(s.getLocalId())).findFirst().orElse(null);
    }

    private WorkOrderAcceptance findAcceptanceByLocalId(String localId) {
        if (localId == null) return null;
        return acceptanceMapper.selectAll().stream()
                .filter(a -> localId.equals(a.getLocalId())).findFirst().orElse(null);
    }

    private WorkOrderMaterialUsage findMaterialUsageByLocalId(String localId) {
        if (localId == null) return null;
        return materialUsageMapper.selectAll().stream()
                .filter(m -> localId.equals(m.getLocalId())).findFirst().orElse(null);
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
        fillConflictVO(vo, conflict);
        return vo;
    }

    private void fillConflictVO(SyncConflictVO vo, SyncConflict conflict) {
        vo.id = conflict.getId();
        vo.conflictNo = conflict.getConflictNo();
        vo.moduleType = conflict.getModuleType();
        vo.entityType = conflict.getEntityType();
        vo.localId = conflict.getLocalId();
        vo.serverId = conflict.getServerId();
        vo.workOrderId = conflict.getWorkOrderId();
        vo.conflictType = conflict.getConflictType();
        vo.resolveStatus = conflict.getResolveStatus();
        vo.resolveStrategy = conflict.getResolveStrategy();
        vo.resolveTime = conflict.getResolveTime() == null ? null : conflict.getResolveTime().toString();
        vo.resolveComment = conflict.getResolveComment();
        vo.clientPayload = conflict.getClientPayload();
        vo.serverPayload = conflict.getServerPayload();
        vo.deviceId = conflict.getDeviceId();
        vo.operatorId = conflict.getOperatorId();
        vo.clientVersion = conflict.getClientVersion();
        vo.serverVersion = conflict.getServerVersion();
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

    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
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
