package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.ai.AiDefectBoxRequest;
import com.offshore.platform.dto.ai.AiModelRequest;
import com.offshore.platform.dto.ai.AiResultRequest;
import com.offshore.platform.dto.ai.AiReviewRequest;
import com.offshore.platform.entity.AiDefectBox;
import com.offshore.platform.entity.AiModelInfo;
import com.offshore.platform.entity.AiResult;
import com.offshore.platform.entity.AiReviewRecord;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.mapper.AiDefectBoxMapper;
import com.offshore.platform.mapper.AiModelInfoMapper;
import com.offshore.platform.mapper.AiResultMapper;
import com.offshore.platform.mapper.AiReviewRecordMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.service.AiService;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.vo.ai.AiDefectBoxVO;
import com.offshore.platform.vo.ai.AiModelVO;
import com.offshore.platform.vo.ai.AiResultVO;
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
    private final WorkOrderAttachmentMapper attachmentMapper;
    private final WorkOrderAssignmentMapper assignmentMapper;
    private final OperationLogMapper operationLogMapper;
    private final DataScopeService dataScopeService;

    public AiServiceImpl(AiModelInfoMapper modelMapper, AiResultMapper resultMapper, AiDefectBoxMapper boxMapper,
            AiReviewRecordMapper reviewMapper, WorkOrderMapper workOrderMapper, WorkOrderAttachmentMapper attachmentMapper,
            WorkOrderAssignmentMapper assignmentMapper, OperationLogMapper operationLogMapper,
            DataScopeService dataScopeService) {
        this.modelMapper = modelMapper;
        this.resultMapper = resultMapper;
        this.boxMapper = boxMapper;
        this.reviewMapper = reviewMapper;
        this.workOrderMapper = workOrderMapper;
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
        result.setInferCostMs(request.inferCostMs);
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
    public List<AiResultVO> adminWorkOrderResults(Long workOrderId) {
        requireReadableWorkOrder(workOrderId, CurrentUserContext.require(), false);
        return resultMapper.selectAll().stream()
                .filter(result -> workOrderId.equals(result.getWorkOrderId()))
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
        record.setReviewOpinion(request.reviewOpinion);
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
}
