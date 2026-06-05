package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.mobile.MobileFeedbackRequest;
import com.offshore.platform.dto.mobile.MobileSubmitAcceptanceRequest;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.entity.WorkOrderMaterial;
import com.offshore.platform.entity.WorkOrderQualificationCheck;
import com.offshore.platform.entity.WorkOrderStatusLog;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderMaterialMapper;
import com.offshore.platform.mapper.WorkOrderQualificationCheckMapper;
import com.offshore.platform.mapper.WorkOrderStatusLogMapper;
import com.offshore.platform.service.MobileWorkOrderService;
import com.offshore.platform.vo.mobile.MobileAttachmentVO;
import com.offshore.platform.vo.mobile.MobileMaterialVO;
import com.offshore.platform.vo.mobile.MobileQualificationCheckVO;
import com.offshore.platform.vo.mobile.MobileStatusFlowVO;
import com.offshore.platform.vo.mobile.MobileWorkOrderDetailVO;
import com.offshore.platform.vo.mobile.MobileWorkOrderListVO;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MobileWorkOrderServiceImpl implements MobileWorkOrderService {
    private static final String ASSIGNED = "ASSIGNED";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String PENDING_ACCEPTANCE = "PENDING_ACCEPTANCE";
    private static final String SYNCED = "SYNCED";

    private final WorkOrderMapper workOrderMapper;
    private final ProjectInfoMapper projectInfoMapper;
    private final WorkOrderAssignmentMapper workOrderAssignmentMapper;
    private final WorkOrderStatusLogMapper workOrderStatusLogMapper;
    private final WorkOrderAttachmentMapper workOrderAttachmentMapper;
    private final WorkOrderMaterialMapper workOrderMaterialMapper;
    private final WorkOrderQualificationCheckMapper workOrderQualificationCheckMapper;
    private final OperationLogMapper operationLogMapper;

    public MobileWorkOrderServiceImpl(WorkOrderMapper workOrderMapper, ProjectInfoMapper projectInfoMapper,
            WorkOrderAssignmentMapper workOrderAssignmentMapper, WorkOrderStatusLogMapper workOrderStatusLogMapper,
            WorkOrderAttachmentMapper workOrderAttachmentMapper, WorkOrderMaterialMapper workOrderMaterialMapper,
            WorkOrderQualificationCheckMapper workOrderQualificationCheckMapper, OperationLogMapper operationLogMapper) {
        this.workOrderMapper = workOrderMapper;
        this.projectInfoMapper = projectInfoMapper;
        this.workOrderAssignmentMapper = workOrderAssignmentMapper;
        this.workOrderStatusLogMapper = workOrderStatusLogMapper;
        this.workOrderAttachmentMapper = workOrderAttachmentMapper;
        this.workOrderMaterialMapper = workOrderMaterialMapper;
        this.workOrderQualificationCheckMapper = workOrderQualificationCheckMapper;
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public List<MobileWorkOrderListVO> listMyWorkOrders() {
        CurrentUser currentUser = CurrentUserContext.require();
        return workOrderMapper.selectAll().stream()
                .filter(order -> canAccess(currentUser, order))
                .sorted(Comparator.comparing(WorkOrder::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toListVO)
                .toList();
    }

    @Override
    public MobileWorkOrderDetailVO getMyWorkOrder(Long id) {
        WorkOrder order = requireMyWorkOrder(id);
        return toDetailVO(order);
    }

    @Override
    @Transactional
    public MobileWorkOrderDetailVO acceptWorkOrder(Long id, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = requireMyWorkOrder(id);
        if (!ASSIGNED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.WORK_ORDER_ERROR, "只有已派工工单可以接单");
        }
        markAssignmentsAccepted(order.getId(), currentUser.getUserId());
        insertStatusLog(order, ASSIGNED, ASSIGNED, "ACCEPT", "移动端接单", currentUser.getUserId());
        touchOrder(order, currentUser.getUserId());
        workOrderMapper.updateById(order);
        writeOperationLog(currentUser, servletRequest, "ACCEPT_WORK_ORDER", order);
        return toDetailVO(order);
    }

    @Override
    @Transactional
    public MobileWorkOrderDetailVO startWorkOrder(Long id, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = requireMyWorkOrder(id);
        if (!ASSIGNED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.WORK_ORDER_ERROR, "只有已派工工单可以开始施工");
        }
        String from = order.getStatus();
        order.setStatus(IN_PROGRESS);
        order.setActualStartTime(LocalDateTime.now());
        touchOrder(order, currentUser.getUserId());
        workOrderMapper.updateById(order);
        insertStatusLog(order, from, IN_PROGRESS, "START", "移动端开始施工", currentUser.getUserId());
        writeOperationLog(currentUser, servletRequest, "START_WORK_ORDER", order);
        return toDetailVO(order);
    }

    @Override
    @Transactional
    public MobileWorkOrderDetailVO feedback(Long id, MobileFeedbackRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = requireMyWorkOrder(id);
        if (!IN_PROGRESS.equals(order.getStatus()) && !ASSIGNED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.WORK_ORDER_ERROR, "当前状态不允许反馈");
        }
        String desc = StringUtils.hasText(request.getFeedback()) ? request.getFeedback() : "移动端反馈";
        insertStatusLog(order, order.getStatus(), order.getStatus(), "FEEDBACK", desc, currentUser.getUserId());
        touchOrder(order, currentUser.getUserId());
        workOrderMapper.updateById(order);
        writeOperationLog(currentUser, servletRequest, "FEEDBACK_WORK_ORDER", order);
        return toDetailVO(order);
    }

    @Override
    @Transactional
    public MobileWorkOrderDetailVO submitAcceptance(Long id, MobileSubmitAcceptanceRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = requireMyWorkOrder(id);
        if (!IN_PROGRESS.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.WORK_ORDER_ERROR, "只有施工中工单可以提交验收");
        }
        String from = order.getStatus();
        order.setStatus(PENDING_ACCEPTANCE);
        touchOrder(order, currentUser.getUserId());
        workOrderMapper.updateById(order);
        String desc = StringUtils.hasText(request.getSubmitDesc()) ? request.getSubmitDesc() : "移动端提交验收";
        insertStatusLog(order, from, PENDING_ACCEPTANCE, "SUBMIT_ACCEPTANCE", desc, currentUser.getUserId());
        writeOperationLog(currentUser, servletRequest, "SUBMIT_ACCEPTANCE", order);
        return toDetailVO(order);
    }

    @Override
    public List<MobileMaterialVO> listMaterials(Long id) {
        WorkOrder order = requireMyWorkOrder(id);
        return workOrderMaterialMapper.selectByWorkOrderId(order.getId()).stream().map(this::toMaterialVO).toList();
    }

    @Override
    public List<MobileQualificationCheckVO> listQualificationChecks(Long id) {
        WorkOrder order = requireMyWorkOrder(id);
        return workOrderQualificationCheckMapper.selectAll().stream()
                .filter(item -> order.getId().equals(item.getWorkOrderId()))
                .map(this::toQualificationVO)
                .toList();
    }

    private WorkOrder requireMyWorkOrder(Long id) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在");
        }
        if (!canAccess(currentUser, order)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该工单");
        }
        return order;
    }

    private boolean canAccess(CurrentUser currentUser, WorkOrder order) {
        return currentUser != null
                && (currentUser.getUserId().equals(order.getMaintainerId())
                || workOrderAssignmentMapper.selectByWorkOrderId(order.getId()).stream()
                        .anyMatch(item -> currentUser.getUserId().equals(item.getAssigneeId())));
    }

    private void markAssignmentsAccepted(Long workOrderId, Long userId) {
        for (WorkOrderAssignment assignment : workOrderAssignmentMapper.selectByWorkOrderId(workOrderId)) {
            if (userId.equals(assignment.getAssigneeId()) && ASSIGNED.equals(assignment.getAssignmentStatus())) {
                assignment.setAssignmentStatus("ACCEPTED");
                assignment.setAcceptedAt(LocalDateTime.now());
                assignment.setUpdatedAt(LocalDateTime.now());
                assignment.setUpdatedBy(userId);
                assignment.setOperatorId(userId);
                assignment.setVersion(assignment.getVersion() == null ? 1 : assignment.getVersion() + 1);
                assignment.setSyncStatus(SYNCED);
                workOrderAssignmentMapper.updateById(assignment);
            }
        }
    }

    private void touchOrder(WorkOrder order, Long userId) {
        order.setVersion(order.getVersion() == null ? 1 : order.getVersion() + 1);
        order.setSyncStatus(SYNCED);
        order.setOperatorId(userId);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(userId);
    }

    private void insertStatusLog(WorkOrder order, String fromStatus, String toStatus, String operationType, String desc, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        WorkOrderStatusLog log = new WorkOrderStatusLog();
        log.setWorkOrderId(order.getId());
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperationType(operationType);
        log.setOperationDesc(desc);
        log.setOperatorId(operatorId);
        log.setOperationTime(now);
        log.setVersion(1);
        log.setSyncStatus(SYNCED);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        log.setDeletedFlag(0);
        log.setCreatedBy(operatorId);
        log.setUpdatedBy(operatorId);
        workOrderStatusLogMapper.insert(log);
    }

    private MobileWorkOrderListVO toListVO(WorkOrder order) {
        MobileWorkOrderListVO vo = new MobileWorkOrderListVO();
        fillBase(vo, order);
        return vo;
    }

    private MobileWorkOrderDetailVO toDetailVO(WorkOrder order) {
        MobileWorkOrderDetailVO vo = new MobileWorkOrderDetailVO();
        fillBase(vo, order);
        vo.projectId = order.getProjectId();
        vo.templateId = order.getTemplateId();
        vo.workType = order.getWorkType();
        vo.workContent = order.getWorkContent();
        vo.requiredMaterialDesc = order.getRequiredMaterialDesc();
        vo.leaderId = order.getLeaderId();
        vo.maintainerId = order.getMaintainerId();
        vo.actualStartTime = order.getActualStartTime();
        vo.actualEndTime = order.getActualEndTime();
        vo.acceptanceRequired = order.getAcceptanceRequired();
        vo.sourceType = order.getSourceType();
        vo.attachments = workOrderAttachmentMapper.selectAll().stream()
                .filter(item -> order.getId().equals(item.getWorkOrderId()))
                .map(this::toAttachmentVO)
                .toList();
        vo.materials = workOrderMaterialMapper.selectByWorkOrderId(order.getId()).stream().map(this::toMaterialVO).toList();
        vo.statusFlow = workOrderStatusLogMapper.selectByWorkOrderId(order.getId()).stream().map(this::toStatusFlowVO).toList();
        return vo;
    }

    private void fillBase(MobileWorkOrderListVO vo, WorkOrder order) {
        ProjectInfo project = projectInfoMapper.selectById(order.getProjectId());
        vo.id = order.getId();
        vo.serverId = order.getServerId();
        vo.localId = order.getLocalId();
        vo.workOrderNo = order.getWorkOrderNo();
        vo.projectName = project == null ? null : project.getProjectName();
        vo.workTitle = order.getWorkTitle();
        vo.workLocation = order.getWorkLocation();
        vo.workContentSummary = summary(order.getWorkContent());
        vo.status = order.getStatus();
        vo.priority = order.getPriority();
        vo.plannedStartTime = order.getPlannedStartTime();
        vo.plannedEndTime = order.getPlannedEndTime();
        vo.version = order.getVersion();
        vo.updatedAt = order.getUpdatedAt();
        vo.syncStatus = order.getSyncStatus();
        vo.conflictFlag = "CONFLICT".equals(order.getSyncStatus()) ? 1 : 0;
    }

    private MobileAttachmentVO toAttachmentVO(WorkOrderAttachment attachment) {
        MobileAttachmentVO vo = new MobileAttachmentVO();
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
        vo.captureUserId = attachment.getCaptureUserId();
        vo.captureUserName = attachment.getCaptureUserName();
        vo.uploadStatus = attachment.getUploadStatus();
        vo.version = attachment.getVersion();
        vo.updatedAt = attachment.getUpdatedAt();
        vo.syncStatus = attachment.getSyncStatus();
        vo.conflictFlag = attachment.getConflictFlag();
        return vo;
    }

    private MobileMaterialVO toMaterialVO(WorkOrderMaterial material) {
        MobileMaterialVO vo = new MobileMaterialVO();
        vo.id = material.getId();
        vo.serverId = material.getServerId();
        vo.localId = material.getLocalId();
        vo.workOrderId = material.getWorkOrderId();
        vo.materialCode = material.getMaterialCode();
        vo.materialName = material.getMaterialName();
        vo.materialSpec = material.getMaterialSpec();
        vo.unit = material.getUnit();
        vo.plannedQty = material.getPlannedQty();
        vo.actualQty = material.getActualQty();
        vo.prepareStatus = material.getPrepareStatus();
        vo.version = material.getVersion();
        vo.updatedAt = material.getUpdatedAt();
        vo.syncStatus = material.getSyncStatus();
        return vo;
    }

    private MobileQualificationCheckVO toQualificationVO(WorkOrderQualificationCheck check) {
        MobileQualificationCheckVO vo = new MobileQualificationCheckVO();
        vo.id = check.getId();
        vo.serverId = check.getServerId();
        vo.localId = check.getLocalId();
        vo.workOrderId = check.getWorkOrderId();
        vo.employeeId = check.getEmployeeId();
        vo.certificateId = check.getCertificateId();
        vo.qualificationTypeId = check.getQualificationTypeId();
        vo.checkResult = check.getCheckResult();
        vo.checkTime = check.getCheckTime();
        vo.checkerId = check.getCheckerId();
        vo.version = check.getVersion();
        vo.updatedAt = check.getUpdatedAt();
        vo.syncStatus = check.getSyncStatus();
        return vo;
    }

    private MobileStatusFlowVO toStatusFlowVO(WorkOrderStatusLog log) {
        MobileStatusFlowVO vo = new MobileStatusFlowVO();
        vo.id = log.getId();
        vo.serverId = log.getServerId();
        vo.localId = log.getLocalId();
        vo.workOrderId = log.getWorkOrderId();
        vo.fromStatus = log.getFromStatus();
        vo.toStatus = log.getToStatus();
        vo.operationType = log.getOperationType();
        vo.operationDesc = log.getOperationDesc();
        vo.operatorId = log.getOperatorId();
        vo.operationTime = log.getOperationTime();
        vo.version = log.getVersion();
        vo.updatedAt = log.getUpdatedAt();
        vo.syncStatus = log.getSyncStatus();
        return vo;
    }

    private void writeOperationLog(CurrentUser currentUser, HttpServletRequest request, String operationType, WorkOrder order) {
        OperationLog log = new OperationLog();
        log.setTraceId(TraceIdUtils.currentTraceId());
        log.setOperatorId(currentUser.getUserId());
        log.setOperatorName(currentUser.getRealName());
        log.setRoleCode(String.join(",", currentUser.getRoleCodes()));
        log.setPlatform("MOBILE");
        log.setModuleName("WORK_ORDER");
        log.setOperationType(operationType);
        log.setBusinessType("WORK_ORDER");
        log.setBusinessId(String.valueOf(order.getId()));
        log.setBusinessNo(order.getWorkOrderNo());
        log.setProjectId(order.getProjectId());
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(request.getRequestURI());
        log.setRequestIp(clientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setResultStatus("SUCCESS");
        log.setOperationTime(LocalDateTime.now());
        log.setDeletedFlag(0);
        log.setCreatedBy(currentUser.getUserId());
        log.setUpdatedBy(currentUser.getUserId());
        operationLogMapper.insert(log);
    }

    private String summary(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.length() <= 80 ? text : text.substring(0, 80);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
