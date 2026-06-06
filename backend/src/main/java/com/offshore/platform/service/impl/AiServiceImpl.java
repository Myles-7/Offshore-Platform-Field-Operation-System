package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.ai.AiDefectBoxRequest;
import com.offshore.platform.dto.ai.AiBatchReviewRequest;
import com.offshore.platform.dto.ai.AiModelRequest;
import com.offshore.platform.dto.ai.AiResultRequest;
import com.offshore.platform.dto.ai.AiResultQueryRequest;
import com.offshore.platform.dto.ai.AiReviewRequest;
import com.offshore.platform.entity.AiDefectBox;
import com.offshore.platform.entity.AiModelInfo;
import com.offshore.platform.entity.AiResult;
import com.offshore.platform.entity.AiReviewRecord;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.entity.WorkOrderRecord;
import com.offshore.platform.mapper.AiDefectBoxMapper;
import com.offshore.platform.mapper.AiModelInfoMapper;
import com.offshore.platform.mapper.AiResultMapper;
import com.offshore.platform.mapper.AiReviewRecordMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import com.offshore.platform.service.AiService;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.vo.ai.AiDefectBoxVO;
import com.offshore.platform.vo.ai.AiModelVO;
import com.offshore.platform.vo.ai.AiResultDetailVO;
import com.offshore.platform.vo.ai.AiResultVO;
import com.offshore.platform.vo.ai.AiReviewRecordVO;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiServiceImpl implements AiService {
    private final AiModelInfoMapper modelMapper;
    private final AiResultMapper resultMapper;
    private final AiDefectBoxMapper boxMapper;
    private final AiReviewRecordMapper reviewMapper;
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderRecordMapper recordMapper;
    private final WorkOrderAttachmentMapper attachmentMapper;
    private final WorkOrderAssignmentMapper assignmentMapper;
    private final OperationLogMapper operationLogMapper;
    private final DataScopeService dataScopeService;

    public AiServiceImpl(AiModelInfoMapper modelMapper, AiResultMapper resultMapper, AiDefectBoxMapper boxMapper,
            AiReviewRecordMapper reviewMapper, WorkOrderMapper workOrderMapper, WorkOrderAttachmentMapper attachmentMapper,
            WorkOrderRecordMapper recordMapper, WorkOrderAssignmentMapper assignmentMapper, OperationLogMapper operationLogMapper,
            DataScopeService dataScopeService) {
        this.modelMapper = modelMapper;
        this.resultMapper = resultMapper;
        this.boxMapper = boxMapper;
        this.reviewMapper = reviewMapper;
        this.workOrderMapper = workOrderMapper;
        this.recordMapper = recordMapper;
        this.attachmentMapper = attachmentMapper;
        this.assignmentMapper = assignmentMapper;
        this.operationLogMapper = operationLogMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public List<AiModelVO> listModels() {
        requireAdmin(CurrentUserContext.require());
        return modelMapper.selectAll().stream().map(this::toModelVO).toList();
    }

    @Override
    @Transactional
    public AiModelVO createModel(AiModelRequest request) {
        CurrentUser user = CurrentUserContext.require();
        requireAdmin(user);
        AiModelInfo model = new AiModelInfo();
        model.setModelCode(request.modelCode);
        model.setModelName(request.modelName);
        model.setModelVersion(request.modelVersion);
        model.setModelType(defaultString(request.modelType, "DETECTION"));
        model.setRuntimeType(defaultString(request.runtimeType, "ONNX"));
        model.setDeploySide(defaultString(request.deploySide, "SERVER"));
        model.setModelFileId(request.modelFileId);
        model.setModelHash(request.modelHash);
        model.setInputSize(request.inputSize);
        model.setDefectTypes(request.defectTypes);
        model.setConfidenceThreshold(request.confidenceThreshold);
        model.setActiveFlag(0);
        model.setModelStatus("ACTIVE");
        model.setReleasedAt(LocalDateTime.now());
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());
        model.setDeletedFlag(0);
        model.setCreatedBy(user.getUserId());
        model.setUpdatedBy(user.getUserId());
        model.setRemark(request.remark);
        modelMapper.insert(model);
        return toModelVO(model);
    }

    @Override
    @Transactional
    public AiModelVO activateModel(Long id) {
        CurrentUser user = CurrentUserContext.require();
        requireAdmin(user);
        AiModelInfo model = requireModel(id);
        for (AiModelInfo item : modelMapper.selectAll()) {
            item.setActiveFlag(item.getId().equals(id) ? 1 : 0);
            item.setUpdatedAt(LocalDateTime.now());
            item.setUpdatedBy(user.getUserId());
            modelMapper.updateById(item);
        }
        model.setActiveFlag(1);
        return toModelVO(model);
    }

    @Override
    @Transactional
    public AiResultVO createResult(AiResultRequest request) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder workOrder = requireMobileOrAdminWorkOrder(request.workOrderId, user);
        WorkOrderAttachment attachment = requireAttachment(request.attachmentId);
        if (!workOrder.getId().equals(attachment.getWorkOrderId())) {
            throw new BusinessException(ErrorCode.AI_ERROR, "AI result attachment must belong to the work order");
        }
        if (!"PHOTO".equals(attachment.getAttachmentType()) && !"AI_IMAGE".equals(attachment.getAttachmentType())) {
            throw new BusinessException(ErrorCode.AI_ERROR, "AI result must bind a construction photo attachment");
        }
        if (request.recordId != null) {
            WorkOrderRecord record = recordMapper.selectById(request.recordId);
            if (record == null || !workOrder.getId().equals(record.getWorkOrderId())) {
                throw new BusinessException(ErrorCode.AI_ERROR, "AI recordId must belong to the work order");
            }
        }
        AiResult existing = findByLocalId(request.localId);
        if (existing != null) {
            return toResultVO(existing);
        }

        AiResult result = new AiResult();
        result.setAiResultNo("AI-" + System.currentTimeMillis());
        result.setWorkOrderId(workOrder.getId());
        result.setWorkOrderNo(workOrder.getWorkOrderNo());
        result.setProjectId(workOrder.getProjectId());
        result.setRecordId(request.recordId == null ? attachment.getRecordId() : request.recordId);
        result.setAttachmentId(attachment.getId());
        result.setFileId(request.fileId == null ? attachment.getFileId() : request.fileId);
        result.setResultImageFileId(request.resultImageFileId);
        result.setModelId(request.modelId);
        result.setModelCode(request.modelCode);
        result.setModelVersion(request.modelVersion);
        result.setInferSide(defaultString(request.inferSide, "SERVER"));
        result.setInferTime(request.inferTime == null ? LocalDateTime.now() : request.inferTime);
        result.setInferCostMs(request.inferenceTimeMs == null ? request.inferCostMs : request.inferenceTimeMs);
        result.setDefectType(defaultString(request.defectType, "UNKNOWN"));
        result.setConfidence(request.confidence);
        result.setSuspectedDefectFlag(request.suspectedDefectFlag == null ? 0 : request.suspectedDefectFlag);
        result.setDefectCount(request.boxes == null ? 0 : request.boxes.size());
        result.setResultSummary(request.resultSummary);
        result.setRawResult(request.rawResult);
        result.setReviewStatus("PENDING_REVIEW");
        result.setReviewedFlag(0);
        fillSyncFields(result, request.localId, request.deviceId, user.getUserId());
        resultMapper.insert(result);
        result.setServerId(result.getId());
        resultMapper.updateById(result);

        if (request.boxes != null) {
            int index = 1;
            for (AiDefectBoxRequest boxRequest : request.boxes) {
                AiDefectBox box = new AiDefectBox();
                box.setAiResultId(result.getId());
                box.setBoxNo("BOX-" + result.getId() + "-" + index);
                box.setDefectType(defaultString(boxRequest.defectType, result.getDefectType()));
                box.setConfidence(boxRequest.confidence);
                box.setX(boxRequest.x);
                box.setY(boxRequest.y);
                box.setWidth(boxRequest.width);
                box.setHeight(boxRequest.height);
                box.setImageWidth(boxRequest.imageWidth);
                box.setImageHeight(boxRequest.imageHeight);
                box.setNormalizedFlag(boxRequest.normalizedFlag == null ? 1 : boxRequest.normalizedFlag);
                box.setBoxLabel(boxRequest.boxLabel);
                box.setBoxColor(boxRequest.boxColor);
                box.setSortOrder(boxRequest.sortOrder == null ? index : boxRequest.sortOrder);
                fillSyncFields(box, boxRequest.localId, request.deviceId, user.getUserId());
                boxMapper.insert(box);
                box.setServerId(box.getId());
                boxMapper.updateById(box);
                index++;
            }
        }

        attachment.setAiResultId(result.getId());
        attachment.setAiBindStatus("BOUND");
        attachment.setUpdatedAt(LocalDateTime.now());
        attachment.setUpdatedBy(user.getUserId());
        attachmentMapper.updateById(attachment);
        return toResultVO(result);
    }

    @Override
    public AiResultVO getResult(Long id) {
        AiResult result = requireResult(id);
        requireReadableWorkOrder(result.getWorkOrderId(), CurrentUserContext.require(), true);
        return toResultVO(result);
    }

    @Override
    public AiResultDetailVO getResultDetail(Long id) {
        AiResult result = requireResult(id);
        requireReadableWorkOrder(result.getWorkOrderId(), CurrentUserContext.require(), false);
        return toDetailVO(result);
    }

    @Override
    public List<AiResultVO> adminResults(AiResultQueryRequest request) {
        CurrentUser user = CurrentUserContext.require();
        requireAdmin(user);
        return resultMapper.selectAll().stream()
                .filter(item -> canReadAiResult(user, item, false))
                .filter(item -> request.projectId == null || request.projectId.equals(item.getProjectId()))
                .filter(item -> request.workOrderId == null || request.workOrderId.equals(item.getWorkOrderId()))
                .filter(item -> request.recordId == null || request.recordId.equals(item.getRecordId()))
                .filter(item -> request.defectType == null || request.defectType.equals(item.getDefectType()))
                .filter(item -> request.reviewStatus == null || request.reviewStatus.equals(item.getReviewStatus()))
                .filter(item -> request.modelVersion == null || request.modelVersion.equals(item.getModelVersion()))
                .filter(item -> request.createdTimeStart == null || (item.getCreatedAt() != null && !item.getCreatedAt().isBefore(request.createdTimeStart)))
                .filter(item -> request.createdTimeEnd == null || (item.getCreatedAt() != null && !item.getCreatedAt().isAfter(request.createdTimeEnd)))
                .map(this::toResultVO)
                .toList();
    }

    @Override
    public List<AiResultVO> adminWorkOrderResults(Long workOrderId) {
        requireReadableWorkOrder(workOrderId, CurrentUserContext.require(), false);
        return resultMapper.selectAll().stream()
                .filter(result -> workOrderId.equals(result.getWorkOrderId()))
                .map(this::toResultVO)
                .toList();
    }

    @Override
    public List<AiResultVO> adminRecordResults(Long recordId) {
        WorkOrderRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Work record not found");
        }
        requireReadableWorkOrder(record.getWorkOrderId(), CurrentUserContext.require(), false);
        return resultMapper.selectAll().stream()
                .filter(result -> recordId.equals(result.getRecordId()))
                .map(this::toResultVO)
                .toList();
    }

    @Override
    public List<AiResultVO> mobileWorkOrderResults(Long workOrderId) {
        requireReadableWorkOrder(workOrderId, CurrentUserContext.require(), true);
        return resultMapper.selectAll().stream()
                .filter(result -> workOrderId.equals(result.getWorkOrderId()))
                .map(this::toResultVO)
                .toList();
    }

    @Override
    @Transactional
    public AiResultVO review(Long id, AiReviewRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        AiResult result = requireResult(id);
        requireReadableWorkOrder(result.getWorkOrderId(), user, false);
        validateReviewStatus(request.reviewStatus);
        result.setReviewStatus(request.reviewStatus);
        result.setReviewedFlag(1);
        result.setReviewerId(user.getUserId());
        result.setReviewTime(LocalDateTime.now());
        result.setUpdatedAt(LocalDateTime.now());
        result.setUpdatedBy(user.getUserId());
        resultMapper.updateById(result);

        AiReviewRecord record = new AiReviewRecord();
        record.setReviewNo("AIRV-" + System.currentTimeMillis());
        record.setAiResultId(result.getId());
        record.setWorkOrderId(result.getWorkOrderId());
        record.setRecordId(result.getRecordId());
        record.setAttachmentId(result.getAttachmentId());
        record.setReviewerId(user.getUserId());
        record.setReviewerName(user.getRealName());
        record.setReviewStatus(request.reviewStatus);
        record.setConfirmedDefectType(request.confirmedDefectType);
        record.setReviewOpinion(defaultString(request.reviewComment, defaultString(request.reviewConclusion, request.reviewOpinion)));
        record.setAcceptanceSuggestion(request.acceptanceSuggestion);
        record.setReviewTime(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        record.setDeletedFlag(0);
        record.setCreatedBy(user.getUserId());
        record.setUpdatedBy(user.getUserId());
        reviewMapper.insert(record);
        writeLog(user, servletRequest, "REVIEW_AI_RESULT", result.getId(), result.getAiResultNo());
        return toResultVO(result);
    }

    @Override
    @Transactional
    public AiDefectBoxVO reviewBox(Long id, Long boxId, AiDefectBoxRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        AiResult result = requireResult(id);
        requireReadableWorkOrder(result.getWorkOrderId(), user, false);
        AiDefectBox box = boxMapper.selectById(boxId);
        if (box == null || !id.equals(box.getAiResultId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "AI defect box not found");
        }
        box.setDefectType(defaultString(request.defectType, box.getDefectType()));
        box.setConfidence(request.confidence == null ? box.getConfidence() : request.confidence);
        box.setX(request.x == null ? box.getX() : request.x);
        box.setY(request.y == null ? box.getY() : request.y);
        box.setWidth(request.width == null ? box.getWidth() : request.width);
        box.setHeight(request.height == null ? box.getHeight() : request.height);
        box.setBoxLabel(defaultString(request.boxLabel, box.getBoxLabel()));
        box.setVersion(defaultInt(box.getVersion(), 1) + 1);
        box.setUpdatedAt(LocalDateTime.now());
        box.setUpdatedBy(user.getUserId());
        boxMapper.updateById(box);
        writeLog(user, servletRequest, "REVIEW_AI_DEFECT_BOX", result.getId(), result.getAiResultNo());
        return toBoxVO(box);
    }

    @Override
    @Transactional
    public List<AiResultVO> batchReview(AiBatchReviewRequest request, HttpServletRequest servletRequest) {
        validateReviewStatus(request.reviewStatus);
        if ("CONFIRMED".equals(request.reviewStatus) && (request.reviewComment == null || request.reviewComment.isBlank())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Batch confirmed review requires comment");
        }
        AiReviewRequest single = new AiReviewRequest();
        single.reviewStatus = request.reviewStatus;
        single.reviewComment = request.reviewComment;
        single.reviewConclusion = request.reviewConclusion;
        return request.resultIds.stream().map(id -> review(id, single, servletRequest)).toList();
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

    private WorkOrder requireMobileOrAdminWorkOrder(Long workOrderId, CurrentUser user) {
        return requireReadableWorkOrder(workOrderId, user, true);
    }

    private WorkOrder requireReadableWorkOrder(Long workOrderId, CurrentUser user, boolean allowMobileSelf) {
        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Work order not found");
        }
        if (dataScopeService.canAccessAll(user) || dataScopeService.canAccessProject(user, workOrder.getProjectId())) {
            return workOrder;
        }
        boolean assigned = allowMobileSelf && (user.getUserId().equals(workOrder.getMaintainerId())
                || assignmentMapper.selectAll().stream().anyMatch(a -> workOrderId.equals(a.getWorkOrderId())
                && user.getUserId().equals(a.getAssigneeId())));
        if (!assigned) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for AI result");
        }
        return workOrder;
    }

    private void requireAdmin(CurrentUser user) {
        if (!dataScopeService.canAccessAll(user) && !user.getRoleCodes().contains("PROJECT_MANAGER")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for AI management");
        }
    }

    private boolean canReadAiResult(CurrentUser user, AiResult result, boolean allowMobileSelf) {
        try {
            requireReadableWorkOrder(result.getWorkOrderId(), user, allowMobileSelf);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    private AiResult findByLocalId(String localId) {
        if (localId == null || localId.isBlank()) {
            return null;
        }
        return resultMapper.selectAll().stream().filter(item -> localId.equals(item.getLocalId())).findFirst().orElse(null);
    }

    private void validateReviewStatus(String status) {
        if (!List.of("CONFIRMED", "FALSE_POSITIVE", "IGNORED").contains(status)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Unsupported AI review status");
        }
    }

    private AiModelInfo requireModel(Long id) {
        AiModelInfo model = modelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "AI model not found");
        }
        return model;
    }

    private AiResult requireResult(Long id) {
        AiResult result = resultMapper.selectById(id);
        if (result == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "AI result not found");
        }
        return result;
    }

    private WorkOrderAttachment requireAttachment(Long id) {
        WorkOrderAttachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Attachment not found");
        }
        return attachment;
    }

    private AiModelVO toModelVO(AiModelInfo model) {
        AiModelVO vo = new AiModelVO();
        vo.id = model.getId();
        vo.modelCode = model.getModelCode();
        vo.modelName = model.getModelName();
        vo.modelVersion = model.getModelVersion();
        vo.modelType = model.getModelType();
        vo.runtimeType = model.getRuntimeType();
        vo.deploySide = model.getDeploySide();
        vo.confidenceThreshold = model.getConfidenceThreshold();
        vo.activeFlag = model.getActiveFlag();
        vo.modelStatus = model.getModelStatus();
        return vo;
    }

    private AiResultVO toResultVO(AiResult result) {
        AiResultVO vo = new AiResultVO();
        vo.id = result.getId();
        vo.aiResultNo = result.getAiResultNo();
        vo.workOrderId = result.getWorkOrderId();
        vo.workOrderNo = result.getWorkOrderNo();
        vo.projectId = result.getProjectId();
        vo.recordId = result.getRecordId();
        vo.attachmentId = result.getAttachmentId();
        vo.fileId = result.getFileId();
        vo.imagePreviewUrl = result.getFileId() == null ? null : "/api/files/" + result.getFileId() + "/preview";
        vo.resultImageFileId = result.getResultImageFileId();
        vo.resultImagePreviewUrl = result.getResultImageFileId() == null ? null : "/api/files/" + result.getResultImageFileId() + "/preview";
        vo.modelId = result.getModelId();
        vo.modelCode = result.getModelCode();
        vo.modelVersion = result.getModelVersion();
        vo.inferCostMs = result.getInferCostMs();
        vo.defectType = result.getDefectType();
        vo.confidence = result.getConfidence();
        vo.suspectedDefectFlag = result.getSuspectedDefectFlag();
        vo.defectCount = result.getDefectCount();
        vo.resultSummary = result.getResultSummary();
        vo.reviewStatus = result.getReviewStatus();
        vo.reviewerId = result.getReviewerId();
        vo.reviewTime = result.getReviewTime();
        vo.reviewedFlag = result.getReviewedFlag();
        vo.version = result.getVersion();
        vo.syncStatus = result.getSyncStatus();
        vo.updatedAt = result.getUpdatedAt();
        vo.boxes = boxMapper.selectAll().stream()
                .filter(box -> result.getId().equals(box.getAiResultId()))
                .map(this::toBoxVO)
                .toList();
        return vo;
    }

    private AiResultDetailVO toDetailVO(AiResult result) {
        AiResultDetailVO vo = new AiResultDetailVO();
        AiResultVO base = toResultVO(result);
        vo.id = base.id; vo.aiResultNo = base.aiResultNo; vo.workOrderId = base.workOrderId; vo.workOrderNo = base.workOrderNo;
        vo.projectId = base.projectId; vo.recordId = base.recordId; vo.attachmentId = base.attachmentId; vo.fileId = base.fileId;
        vo.imagePreviewUrl = base.imagePreviewUrl; vo.resultImageFileId = base.resultImageFileId; vo.resultImagePreviewUrl = base.resultImagePreviewUrl;
        vo.modelId = base.modelId; vo.modelCode = base.modelCode; vo.modelVersion = base.modelVersion; vo.inferCostMs = base.inferCostMs;
        vo.defectType = base.defectType; vo.confidence = base.confidence; vo.suspectedDefectFlag = base.suspectedDefectFlag; vo.defectCount = base.defectCount;
        vo.resultSummary = base.resultSummary; vo.reviewStatus = base.reviewStatus; vo.reviewerId = base.reviewerId; vo.reviewTime = base.reviewTime;
        vo.reviewedFlag = base.reviewedFlag; vo.version = base.version; vo.syncStatus = base.syncStatus; vo.updatedAt = base.updatedAt; vo.boxes = base.boxes;
        vo.workOrder = workOrderMapper.selectById(result.getWorkOrderId());
        vo.workRecord = result.getRecordId() == null ? null : recordMapper.selectById(result.getRecordId());
        vo.attachment = result.getAttachmentId() == null ? null : attachmentMapper.selectById(result.getAttachmentId());
        vo.reviewRecords = reviewMapper.selectAll().stream()
                .filter(item -> result.getId().equals(item.getAiResultId()))
                .map(this::toReviewRecordVO)
                .toList();
        return vo;
    }

    private AiReviewRecordVO toReviewRecordVO(AiReviewRecord record) {
        AiReviewRecordVO vo = new AiReviewRecordVO();
        vo.id = record.getId();
        vo.reviewNo = record.getReviewNo();
        vo.aiResultId = record.getAiResultId();
        vo.reviewerId = record.getReviewerId();
        vo.reviewerName = record.getReviewerName();
        vo.reviewStatus = record.getReviewStatus();
        vo.confirmedDefectType = record.getConfirmedDefectType();
        vo.reviewOpinion = record.getReviewOpinion();
        vo.acceptanceSuggestion = record.getAcceptanceSuggestion();
        vo.reviewTime = record.getReviewTime();
        return vo;
    }

    private AiDefectBoxVO toBoxVO(AiDefectBox box) {
        AiDefectBoxVO vo = new AiDefectBoxVO();
        vo.id = box.getId();
        vo.aiResultId = box.getAiResultId();
        vo.defectType = box.getDefectType();
        vo.confidence = box.getConfidence();
        vo.x = box.getX();
        vo.y = box.getY();
        vo.width = box.getWidth();
        vo.height = box.getHeight();
        vo.boxLabel = box.getBoxLabel();
        return vo;
    }

    private void writeLog(CurrentUser user, HttpServletRequest request, String operationType, Long businessId, String businessNo) {
        OperationLog log = new OperationLog();
        log.setTraceId(TraceIdUtils.currentTraceId());
        log.setOperatorId(user.getUserId());
        log.setOperatorName(user.getRealName());
        log.setRoleCode(String.join(",", user.getRoleCodes()));
        log.setPlatform("PC");
        log.setModuleName("AI");
        log.setOperationType(operationType);
        log.setBusinessType("AI_RESULT");
        log.setBusinessId(String.valueOf(businessId));
        log.setBusinessNo(businessNo);
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(request.getRequestURI());
        log.setRequestIp(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setResultStatus("SUCCESS");
        log.setOperationTime(LocalDateTime.now());
        log.setDeletedFlag(0);
        operationLogMapper.insert(log);
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}
