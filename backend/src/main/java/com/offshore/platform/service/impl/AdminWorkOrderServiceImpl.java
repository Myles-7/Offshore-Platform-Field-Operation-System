package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.page.PageRequestDTO;
import com.offshore.platform.common.page.PageResult;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.admin.ProjectRequest;
import com.offshore.platform.dto.admin.WorkOrderAssignRequest;
import com.offshore.platform.dto.admin.WorkOrderFromTemplateRequest;
import com.offshore.platform.dto.admin.WorkOrderQueryRequest;
import com.offshore.platform.dto.admin.WorkOrderRequest;
import com.offshore.platform.dto.admin.WorkOrderStatusRequest;
import com.offshore.platform.dto.admin.WorkOrderTemplateRequest;
import com.offshore.platform.entity.*;
import com.offshore.platform.mapper.*;
import com.offshore.platform.service.AdminWorkOrderService;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.vo.admin.ProjectVO;
import com.offshore.platform.vo.admin.WorkOrderDetailVO;
import com.offshore.platform.vo.admin.WorkOrderTemplateVO;
import com.offshore.platform.vo.admin.WorkOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminWorkOrderServiceImpl implements AdminWorkOrderService {
    private static final String PENDING_ASSIGN = "PENDING_ASSIGN";
    private static final String ASSIGNED = "ASSIGNED";
    private static final String SYNCED = "SYNCED";
    private static final DateTimeFormatter NUMBER_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter TEXT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProjectInfoMapper projectInfoMapper;
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderTemplateMapper workOrderTemplateMapper;
    private final WorkOrderAssignmentMapper workOrderAssignmentMapper;
    private final WorkOrderStatusLogMapper workOrderStatusLogMapper;
    private final WorkOrderMaterialMapper workOrderMaterialMapper;
    private final WorkOrderRecordMapper workOrderRecordMapper;
    private final WorkOrderAttachmentMapper workOrderAttachmentMapper;
    private final WorkOrderAcceptanceMapper workOrderAcceptanceMapper;
    private final WorkOrderSignatureMapper workOrderSignatureMapper;
    private final WorkOrderPdfMapper workOrderPdfMapper;
    private final WorkOrderMaterialUsageMapper workOrderMaterialUsageMapper;
    private final AiResultMapper aiResultMapper;
    private final SyncConflictMapper syncConflictMapper;
    private final OperationLogMapper operationLogMapper;
    private final SysUserMapper sysUserMapper;
    private final DataScopeService dataScopeService;

    public AdminWorkOrderServiceImpl(ProjectInfoMapper projectInfoMapper, WorkOrderMapper workOrderMapper,
            WorkOrderTemplateMapper workOrderTemplateMapper, WorkOrderAssignmentMapper workOrderAssignmentMapper,
            WorkOrderStatusLogMapper workOrderStatusLogMapper, WorkOrderMaterialMapper workOrderMaterialMapper,
            WorkOrderRecordMapper workOrderRecordMapper, WorkOrderAttachmentMapper workOrderAttachmentMapper,
            WorkOrderAcceptanceMapper workOrderAcceptanceMapper, WorkOrderSignatureMapper workOrderSignatureMapper,
            WorkOrderPdfMapper workOrderPdfMapper, WorkOrderMaterialUsageMapper workOrderMaterialUsageMapper,
            AiResultMapper aiResultMapper, SyncConflictMapper syncConflictMapper, OperationLogMapper operationLogMapper,
            SysUserMapper sysUserMapper, DataScopeService dataScopeService) {
        this.projectInfoMapper = projectInfoMapper;
        this.workOrderMapper = workOrderMapper;
        this.workOrderTemplateMapper = workOrderTemplateMapper;
        this.workOrderAssignmentMapper = workOrderAssignmentMapper;
        this.workOrderStatusLogMapper = workOrderStatusLogMapper;
        this.workOrderMaterialMapper = workOrderMaterialMapper;
        this.workOrderRecordMapper = workOrderRecordMapper;
        this.workOrderAttachmentMapper = workOrderAttachmentMapper;
        this.workOrderAcceptanceMapper = workOrderAcceptanceMapper;
        this.workOrderSignatureMapper = workOrderSignatureMapper;
        this.workOrderPdfMapper = workOrderPdfMapper;
        this.workOrderMaterialUsageMapper = workOrderMaterialUsageMapper;
        this.aiResultMapper = aiResultMapper;
        this.syncConflictMapper = syncConflictMapper;
        this.operationLogMapper = operationLogMapper;
        this.sysUserMapper = sysUserMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public PageResult<ProjectVO> listProjects(PageRequestDTO pageRequest) {
        CurrentUser user = requirePcManager();
        List<ProjectVO> all = projectInfoMapper.selectAll().stream()
                .filter(project -> canAccessProject(user, project.getId()))
                .filter(project -> !StringUtils.hasText(pageRequest.getKeyword())
                        || contains(project.getProjectCode(), pageRequest.getKeyword())
                        || contains(project.getProjectName(), pageRequest.getKeyword()))
                .sorted(Comparator.comparing(ProjectInfo::getId).reversed())
                .map(this::toProjectVO)
                .toList();
        return page(all, pageRequest.getPageNum(), pageRequest.getPageSize());
    }

    @Override
    public ProjectVO getProject(Long id) {
        CurrentUser user = requirePcManager();
        ProjectInfo project = requireProject(id);
        requireProjectAccess(user, project.getId());
        return toProjectVO(project);
    }

    @Override
    @Transactional
    public ProjectVO createProject(ProjectRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        LocalDateTime now = LocalDateTime.now();
        ProjectInfo project = new ProjectInfo();
        project.setProjectCode(request.projectCode);
        project.setProjectName(request.projectName);
        project.setPlatformName(request.platformName);
        project.setOwnerUnit(request.ownerUnit);
        project.setContractorUnit(request.contractorUnit);
        project.setProjectManagerId(resolveProjectManagerId(user, request.projectManagerId));
        project.setProjectLocation(request.projectLocation);
        project.setStartDate(request.startDate);
        project.setEndDate(request.endDate);
        project.setProjectStatus(defaultText(request.projectStatus, "ACTIVE"));
        project.setSortOrder(defaultInt(request.sortOrder, 0));
        fillSync(project, user.getUserId(), now);
        project.setRemark(request.remark);
        projectInfoMapper.insert(project);
        writeOperationLog(user, servletRequest, "CREATE_PROJECT", "PROJECT", project.getId(), project.getProjectCode());
        return toProjectVO(project);
    }

    @Override
    @Transactional
    public ProjectVO updateProject(Long id, ProjectRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        ProjectInfo project = requireProject(id);
        requireProjectAccess(user, id);
        project.setProjectCode(request.projectCode);
        project.setProjectName(request.projectName);
        project.setPlatformName(request.platformName);
        project.setOwnerUnit(request.ownerUnit);
        project.setContractorUnit(request.contractorUnit);
        project.setProjectManagerId(resolveProjectManagerId(user, request.projectManagerId));
        project.setProjectLocation(request.projectLocation);
        project.setStartDate(request.startDate);
        project.setEndDate(request.endDate);
        project.setProjectStatus(defaultText(request.projectStatus, project.getProjectStatus()));
        project.setSortOrder(defaultInt(request.sortOrder, project.getSortOrder()));
        project.setUpdatedAt(LocalDateTime.now());
        project.setUpdatedBy(user.getUserId());
        project.setOperatorId(user.getUserId());
        project.setRemark(request.remark);
        projectInfoMapper.updateById(project);
        writeOperationLog(user, servletRequest, "UPDATE_PROJECT", "PROJECT", id, project.getProjectCode());
        return toProjectVO(projectInfoMapper.selectById(id));
    }

    @Override
    @Transactional
    public void deleteProject(Long id, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        ProjectInfo project = requireProject(id);
        requireProjectAccess(user, id);
        projectInfoMapper.softDeleteById(id);
        writeOperationLog(user, servletRequest, "DELETE_PROJECT", "PROJECT", id, project.getProjectCode());
    }

    @Override
    public PageResult<WorkOrderVO> listWorkOrders(WorkOrderQueryRequest query) {
        CurrentUser user = requirePcManager();
        List<WorkOrderVO> all = workOrderMapper.selectAll().stream()
                .filter(order -> canAccessProject(user, order.getProjectId()))
                .filter(matchesWorkOrderQuery(query))
                .sorted(Comparator.comparing(WorkOrder::getId).reversed())
                .map(this::toWorkOrderVO)
                .toList();
        return page(all, query.getPageNum(), query.getPageSize());
    }

    @Override
    public WorkOrderDetailVO getWorkOrder(Long id) {
        CurrentUser user = requirePcManager();
        WorkOrder order = requireWorkOrder(id);
        requireProjectAccess(user, order.getProjectId());
        return toDetailVO(order);
    }

    @Override
    @Transactional
    public WorkOrderVO createWorkOrder(WorkOrderRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        requireProjectAccess(user, request.projectId);
        WorkOrder order = new WorkOrder();
        fillWorkOrder(order, request, user, "PC");
        workOrderMapper.insert(order);
        insertStatusLog(order.getId(), null, order.getStatus(), "CREATE", "创建工单", user.getUserId());
        writeOperationLog(user, servletRequest, "CREATE_WORK_ORDER", "WORK_ORDER", order.getId(), order.getWorkOrderNo());
        return toWorkOrderVO(order);
    }

    @Override
    @Transactional
    public WorkOrderVO updateWorkOrder(Long id, WorkOrderRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        WorkOrder order = requireWorkOrder(id);
        requireProjectAccess(user, order.getProjectId());
        if (!Objects.equals(order.getProjectId(), request.projectId)) {
            requireProjectAccess(user, request.projectId);
        }
        fillWorkOrder(order, request, user, order.getSourceType());
        order.setId(id);
        workOrderMapper.updateById(order);
        writeOperationLog(user, servletRequest, "UPDATE_WORK_ORDER", "WORK_ORDER", id, order.getWorkOrderNo());
        return toWorkOrderVO(workOrderMapper.selectById(id));
    }

    @Override
    @Transactional
    public void deleteWorkOrder(Long id, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        WorkOrder order = requireWorkOrder(id);
        requireProjectAccess(user, order.getProjectId());
        workOrderMapper.softDeleteById(id);
        writeOperationLog(user, servletRequest, "DELETE_WORK_ORDER", "WORK_ORDER", id, order.getWorkOrderNo());
    }

    @Override
    @Transactional
    public WorkOrderVO assignWorkOrder(Long id, WorkOrderAssignRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        WorkOrder order = requireWorkOrder(id);
        requireProjectAccess(user, order.getProjectId());
        validateQualificationBeforeAssign(request.maintainerId);
        String fromStatus = order.getStatus();
        order.setLeaderId(request.leaderId);
        order.setMaintainerId(request.maintainerId);
        order.setStatus(ASSIGNED);
        touchOrder(order, user.getUserId());
        workOrderMapper.updateById(order);
        insertAssignment(order.getId(), user.getUserId(), request.maintainerId, defaultText(request.assignmentRole, "MAINTAINER"), request.remark);
        insertStatusLog(order.getId(), fromStatus, ASSIGNED, "ASSIGN", "派工", user.getUserId());
        writeOperationLog(user, servletRequest, "ASSIGN_WORK_ORDER", "WORK_ORDER", id, order.getWorkOrderNo());
        return toWorkOrderVO(order);
    }

    @Override
    @Transactional
    public WorkOrderVO changeWorkOrderStatus(Long id, WorkOrderStatusRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        WorkOrder order = requireWorkOrder(id);
        requireProjectAccess(user, order.getProjectId());
        String target = request.status;
        validateTransition(order.getStatus(), target);
        String fromStatus = order.getStatus();
        order.setStatus(target);
        if ("IN_PROGRESS".equals(target) && order.getActualStartTime() == null) {
            order.setActualStartTime(LocalDateTime.now());
        }
        if ("COMPLETED".equals(target) || "CLOSED".equals(target)) {
            order.setActualEndTime(LocalDateTime.now());
        }
        if ("REJECTED".equals(target)) {
            order.setRejectReason(request.rejectReason);
        }
        if ("CLOSED".equals(target)) {
            order.setCloseReason(request.closeReason);
        }
        touchOrder(order, user.getUserId());
        workOrderMapper.updateById(order);
        insertStatusLog(order.getId(), fromStatus, target, operationType(target), request.operationDesc, user.getUserId());
        writeOperationLog(user, servletRequest, "CHANGE_WORK_ORDER_STATUS", "WORK_ORDER", id, order.getWorkOrderNo());
        return toWorkOrderVO(order);
    }

    @Override
    public List<WorkOrderDetailVO.StatusFlowVO> getStatusFlow(Long id) {
        WorkOrder order = requireWorkOrder(id);
        requireProjectAccess(requirePcManager(), order.getProjectId());
        return workOrderStatusLogMapper.selectByWorkOrderId(id).stream().map(this::toStatusFlowVO).toList();
    }

    @Override
    public List<WorkOrderTemplateVO> listTemplates() {
        requirePcManager();
        return workOrderTemplateMapper.selectAll().stream().map(this::toTemplateVO).toList();
    }

    @Override
    @Transactional
    public WorkOrderTemplateVO createTemplate(WorkOrderTemplateRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        LocalDateTime now = LocalDateTime.now();
        WorkOrderTemplate template = new WorkOrderTemplate();
        template.setTemplateCode(request.templateCode);
        template.setTemplateName(request.templateName);
        template.setWorkType(request.workType);
        template.setDefaultPriority(defaultText(request.defaultPriority, "NORMAL"));
        template.setDefaultWorkContent(request.defaultWorkContent);
        template.setDefaultMaterialDesc(request.defaultMaterialDesc);
        template.setDefaultDurationHours(request.defaultDurationHours);
        template.setEnabledFlag(defaultInt(request.enabledFlag, 1));
        fillSync(template, user.getUserId(), now);
        template.setRemark(request.remark);
        workOrderTemplateMapper.insert(template);
        writeOperationLog(user, servletRequest, "CREATE_WORK_ORDER_TEMPLATE", "WORK_ORDER_TEMPLATE", template.getId(), template.getTemplateCode());
        return toTemplateVO(template);
    }

    @Override
    @Transactional
    public WorkOrderTemplateVO updateTemplate(Long id, WorkOrderTemplateRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        WorkOrderTemplate template = workOrderTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单模板不存在");
        }
        template.setTemplateCode(request.templateCode);
        template.setTemplateName(request.templateName);
        template.setWorkType(request.workType);
        template.setDefaultPriority(defaultText(request.defaultPriority, template.getDefaultPriority()));
        template.setDefaultWorkContent(request.defaultWorkContent);
        template.setDefaultMaterialDesc(request.defaultMaterialDesc);
        template.setDefaultDurationHours(request.defaultDurationHours);
        template.setEnabledFlag(defaultInt(request.enabledFlag, template.getEnabledFlag()));
        template.setUpdatedAt(LocalDateTime.now());
        template.setUpdatedBy(user.getUserId());
        template.setOperatorId(user.getUserId());
        template.setRemark(request.remark);
        workOrderTemplateMapper.updateById(template);
        writeOperationLog(user, servletRequest, "UPDATE_WORK_ORDER_TEMPLATE", "WORK_ORDER_TEMPLATE", id, template.getTemplateCode());
        return toTemplateVO(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        WorkOrderTemplate template = workOrderTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单模板不存在");
        }
        workOrderTemplateMapper.softDeleteById(id);
        writeOperationLog(user, servletRequest, "DELETE_WORK_ORDER_TEMPLATE", "WORK_ORDER_TEMPLATE", id, template.getTemplateCode());
    }

    @Override
    @Transactional
    public WorkOrderVO createFromTemplate(Long templateId, WorkOrderFromTemplateRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = requirePcManager();
        WorkOrderTemplate template = workOrderTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单模板不存在");
        }
        requireProjectAccess(user, request.projectId);
        WorkOrderRequest create = new WorkOrderRequest();
        create.workOrderNo = request.workOrderNo;
        create.projectId = request.projectId;
        create.templateId = templateId;
        create.workTitle = request.workTitle;
        create.workType = template.getWorkType();
        create.workLocation = request.workLocation;
        create.workContent = template.getDefaultWorkContent();
        create.requiredMaterialDesc = template.getDefaultMaterialDesc();
        create.leaderId = request.leaderId;
        create.maintainerId = request.maintainerId;
        create.plannedStartTime = request.plannedStartTime;
        create.plannedEndTime = request.plannedEndTime;
        create.priority = template.getDefaultPriority();
        create.acceptanceRequired = request.acceptanceRequired;
        create.remark = request.remark;
        WorkOrder order = new WorkOrder();
        fillWorkOrder(order, create, user, "TEMPLATE");
        workOrderMapper.insert(order);
        insertStatusLog(order.getId(), null, order.getStatus(), "CREATE", "根据模板创建工单", user.getUserId());
        writeOperationLog(user, servletRequest, "CREATE_WORK_ORDER_FROM_TEMPLATE", "WORK_ORDER", order.getId(), order.getWorkOrderNo());
        return toWorkOrderVO(order);
    }

    private CurrentUser requirePcManager() {
        CurrentUser user = CurrentUserContext.require();
        String scope = dataScopeService.resolveDataScope(user);
        if ("SELF".equals(scope)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "维修工不能访问PC后台全量工单");
        }
        if (!dataScopeService.canAccessAll(user) && !"PROJECT".equals(scope)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return user;
    }

    private void requireProjectAccess(CurrentUser user, Long projectId) {
        if (!canAccessProject(user, projectId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该项目数据");
        }
    }

    private boolean canAccessProject(CurrentUser user, Long projectId) {
        return dataScopeService.canAccessAll(user) || dataScopeService.canAccessProject(user, projectId);
    }

    private Long resolveProjectManagerId(CurrentUser user, Long requestedManagerId) {
        if (dataScopeService.canAccessAll(user)) {
            return requestedManagerId;
        }
        if (requestedManagerId != null && !requestedManagerId.equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "项目经理只能管理自己所属项目");
        }
        return user.getUserId();
    }

    private ProjectInfo requireProject(Long id) {
        ProjectInfo project = projectInfoMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "项目不存在");
        }
        return project;
    }

    private WorkOrder requireWorkOrder(Long id) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在");
        }
        return order;
    }

    private void fillWorkOrder(WorkOrder order, WorkOrderRequest request, CurrentUser user, String sourceType) {
        requireProject(request.projectId);
        LocalDateTime now = LocalDateTime.now();
        if (order.getId() == null) {
            order.setWorkOrderNo(StringUtils.hasText(request.workOrderNo) ? request.workOrderNo : generateNo("WO"));
            order.setStatus(PENDING_ASSIGN);
            order.setCreatedAt(now);
            order.setCreatedBy(user.getUserId());
            order.setDeletedFlag(0);
            order.setVersion(1);
            order.setSyncStatus(SYNCED);
        }
        order.setProjectId(request.projectId);
        order.setTemplateId(request.templateId);
        order.setWorkTitle(request.workTitle);
        order.setWorkType(request.workType);
        order.setWorkLocation(request.workLocation);
        order.setWorkContent(request.workContent);
        order.setRequiredMaterialDesc(request.requiredMaterialDesc);
        order.setLeaderId(request.leaderId);
        order.setMaintainerId(request.maintainerId);
        order.setPlannedStartTime(request.plannedStartTime);
        order.setPlannedEndTime(request.plannedEndTime);
        order.setPriority(defaultText(request.priority, "NORMAL"));
        order.setAcceptanceRequired(defaultInt(request.acceptanceRequired, 1));
        order.setSourceType(defaultText(sourceType, "PC"));
        order.setOperatorId(user.getUserId());
        order.setUpdatedAt(now);
        order.setUpdatedBy(user.getUserId());
        order.setRemark(request.remark);
    }

    private void fillSync(ProjectInfo project, Long userId, LocalDateTime now) {
        project.setVersion(1);
        project.setSyncStatus(SYNCED);
        project.setOperatorId(userId);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        project.setDeletedFlag(0);
        project.setCreatedBy(userId);
        project.setUpdatedBy(userId);
    }

    private void fillSync(WorkOrderTemplate template, Long userId, LocalDateTime now) {
        template.setVersion(1);
        template.setSyncStatus(SYNCED);
        template.setOperatorId(userId);
        template.setCreatedAt(now);
        template.setUpdatedAt(now);
        template.setDeletedFlag(0);
        template.setCreatedBy(userId);
        template.setUpdatedBy(userId);
    }

    private void touchOrder(WorkOrder order, Long userId) {
        order.setVersion(order.getVersion() == null ? 1 : order.getVersion() + 1);
        order.setSyncStatus(SYNCED);
        order.setOperatorId(userId);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(userId);
    }

    private void insertAssignment(Long workOrderId, Long assignerId, Long assigneeId, String role, String remark) {
        LocalDateTime now = LocalDateTime.now();
        WorkOrderAssignment assignment = new WorkOrderAssignment();
        assignment.setWorkOrderId(workOrderId);
        assignment.setAssignerId(assignerId);
        assignment.setAssigneeId(assigneeId);
        assignment.setAssignmentRole(role);
        assignment.setAssignmentStatus(ASSIGNED);
        assignment.setAssignedAt(now);
        assignment.setVersion(1);
        assignment.setSyncStatus(SYNCED);
        assignment.setOperatorId(assignerId);
        assignment.setCreatedAt(now);
        assignment.setUpdatedAt(now);
        assignment.setDeletedFlag(0);
        assignment.setCreatedBy(assignerId);
        assignment.setUpdatedBy(assignerId);
        assignment.setRemark(remark);
        workOrderAssignmentMapper.insert(assignment);
    }

    private void insertStatusLog(Long workOrderId, String fromStatus, String toStatus, String operationType, String desc, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        WorkOrderStatusLog log = new WorkOrderStatusLog();
        log.setWorkOrderId(workOrderId);
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

    private void validateQualificationBeforeAssign(Long maintainerId) {
        SysUser maintainer = sysUserMapper.selectById(maintainerId);
        if (maintainer == null || !"ACTIVE".equals(maintainer.getAccountStatus()) || !Integer.valueOf(1).equals(maintainer.getMobileEnabled())) {
            throw new BusinessException(ErrorCode.QUALIFICATION_ERROR, "派工前资质校验失败：维修工账号不可用");
        }
        // TODO: 人员资质模块完成后，在此校验证书有效期、作业类型资质和项目准入要求。
    }

    private void validateTransition(String from, String to) {
        boolean ok = switch (from) {
            case PENDING_ASSIGN -> ASSIGNED.equals(to) || "CLOSED".equals(to);
            case ASSIGNED -> "IN_PROGRESS".equals(to) || "REJECTED".equals(to) || "CLOSED".equals(to);
            case "IN_PROGRESS" -> "PENDING_ACCEPTANCE".equals(to) || "REJECTED".equals(to) || "CLOSED".equals(to);
            case "PENDING_ACCEPTANCE" -> "COMPLETED".equals(to) || "REJECTED".equals(to);
            case "COMPLETED" -> "CLOSED".equals(to);
            case "REJECTED" -> "IN_PROGRESS".equals(to) || "CLOSED".equals(to);
            default -> false;
        };
        if (!ok) {
            throw new BusinessException(ErrorCode.WORK_ORDER_ERROR, "非法工单状态流转：" + from + " -> " + to);
        }
    }

    private String operationType(String toStatus) {
        return switch (toStatus) {
            case ASSIGNED -> "ASSIGN";
            case "IN_PROGRESS" -> "START";
            case "PENDING_ACCEPTANCE" -> "SUBMIT_ACCEPTANCE";
            case "COMPLETED" -> "COMPLETE";
            case "REJECTED" -> "REJECT";
            case "CLOSED" -> "CLOSE";
            default -> "UPDATE";
        };
    }

    private Predicate<WorkOrder> matchesWorkOrderQuery(WorkOrderQueryRequest query) {
        return order -> (query.getProjectId() == null || query.getProjectId().equals(order.getProjectId()))
                && (!StringUtils.hasText(query.getWorkOrderNo()) || contains(order.getWorkOrderNo(), query.getWorkOrderNo()))
                && (!StringUtils.hasText(query.getWorkType()) || query.getWorkType().equals(order.getWorkType()))
                && (!StringUtils.hasText(query.getWorkLocation()) || contains(order.getWorkLocation(), query.getWorkLocation()))
                && (!StringUtils.hasText(query.getStatus()) || query.getStatus().equals(order.getStatus()))
                && (!StringUtils.hasText(query.getPriority()) || query.getPriority().equals(order.getPriority()))
                && (query.getMaintainerId() == null || query.getMaintainerId().equals(order.getMaintainerId()))
                && (query.getLeaderId() == null || query.getLeaderId().equals(order.getLeaderId()))
                && (!StringUtils.hasText(query.getSyncStatus()) || query.getSyncStatus().equals(order.getSyncStatus()))
                && (query.getPlannedStartTimeStart() == null || (order.getPlannedStartTime() != null && !order.getPlannedStartTime().isBefore(query.getPlannedStartTimeStart())))
                && (query.getPlannedStartTimeEnd() == null || (order.getPlannedStartTime() != null && !order.getPlannedStartTime().isAfter(query.getPlannedStartTimeEnd())))
                && (query.getAbnormalFlag() == null || hasAbnormalRecord(order.getId(), query.getAbnormalFlag()));
    }

    private boolean hasAbnormalRecord(Long workOrderId, Integer abnormalFlag) {
        return workOrderRecordMapper.selectAll().stream()
                .anyMatch(record -> workOrderId.equals(record.getWorkOrderId()) && abnormalFlag.equals(record.getAbnormalFlag()));
    }

    private WorkOrderDetailVO toDetailVO(WorkOrder order) {
        WorkOrderDetailVO vo = new WorkOrderDetailVO();
        vo.workOrder = toWorkOrderVO(order);
        ProjectInfo project = projectInfoMapper.selectById(order.getProjectId());
        vo.project = project == null ? null : toProjectVO(project);
        vo.assignments = workOrderAssignmentMapper.selectByWorkOrderId(order.getId()).stream().map(this::toAssignmentVO).toList();
        vo.statusFlow = workOrderStatusLogMapper.selectByWorkOrderId(order.getId()).stream().map(this::toStatusFlowVO).toList();
        vo.requiredMaterials = workOrderMaterialMapper.selectByWorkOrderId(order.getId()).stream().map(this::toMaterialVO).toList();
        vo.constructionRecordSummary = workOrderRecordMapper.selectAll().stream().filter(item -> order.getId().equals(item.getWorkOrderId()))
                .map(item -> summary(item.getId(), item.getRecordType(), item.getRecordNo(), item.getRecordStatus(), format(item.getConstructionTime()))).toList();
        vo.attachmentSummary = workOrderAttachmentMapper.selectAll().stream().filter(item -> order.getId().equals(item.getWorkOrderId()))
                .map(item -> summary(item.getId(), item.getAttachmentType(), item.getAttachmentName(), item.getUploadStatus(), format(item.getCaptureTime()))).toList();
        vo.acceptanceRecords = workOrderAcceptanceMapper.selectAll().stream().filter(item -> order.getId().equals(item.getWorkOrderId()))
                .map(item -> summary(item.getId(), "ACCEPTANCE", item.getAcceptanceNo(), item.getAcceptanceStatus(), format(item.getAcceptanceTime()))).toList();
        vo.signatureRecords = workOrderSignatureMapper.selectAll().stream().filter(item -> order.getId().equals(item.getWorkOrderId()))
                .map(item -> summary(item.getId(), item.getSignatureRole(), item.getSignatureNo(), item.getSignatureStatus(), format(item.getSignedAt()))).toList();
        vo.pdfRecords = workOrderPdfMapper.selectAll().stream().filter(item -> order.getId().equals(item.getWorkOrderId()))
                .map(item -> summary(item.getId(), "PDF", item.getPdfNo(), item.getPdfStatus(), format(item.getGeneratedAt()))).toList();
        vo.materialUsage = workOrderMaterialUsageMapper.selectAll().stream().filter(item -> order.getId().equals(item.getWorkOrderId()))
                .map(item -> summary(item.getId(), "MATERIAL_USAGE", item.getUsageNo(), item.getSyncStatus(), format(item.getUsageTime()))).toList();
        vo.aiResults = aiResultMapper.selectAll().stream().filter(item -> order.getId().equals(item.getWorkOrderId()))
                .map(item -> summary(item.getId(), "AI_RESULT", item.getAiResultNo(), item.getReviewStatus(), format(item.getInferTime()))).toList();
        int conflictCount = (int) syncConflictMapper.selectAll().stream().filter(item -> order.getId().equals(item.getWorkOrderId())).count();
        vo.syncSummary.syncStatus = order.getSyncStatus();
        vo.syncSummary.conflictCount = conflictCount;
        vo.syncSummary.hasConflict = conflictCount > 0 || "CONFLICT".equals(order.getSyncStatus());
        return vo;
    }

    private ProjectVO toProjectVO(ProjectInfo project) {
        ProjectVO vo = new ProjectVO();
        vo.id = project.getId();
        vo.projectCode = project.getProjectCode();
        vo.projectName = project.getProjectName();
        vo.platformName = project.getPlatformName();
        vo.ownerUnit = project.getOwnerUnit();
        vo.contractorUnit = project.getContractorUnit();
        vo.projectManagerId = project.getProjectManagerId();
        vo.projectLocation = project.getProjectLocation();
        vo.startDate = project.getStartDate();
        vo.endDate = project.getEndDate();
        vo.projectStatus = project.getProjectStatus();
        vo.sortOrder = project.getSortOrder();
        vo.version = project.getVersion();
        vo.syncStatus = project.getSyncStatus();
        vo.createdAt = project.getCreatedAt();
        vo.updatedAt = project.getUpdatedAt();
        vo.remark = project.getRemark();
        return vo;
    }

    private WorkOrderVO toWorkOrderVO(WorkOrder order) {
        WorkOrderVO vo = new WorkOrderVO();
        vo.id = order.getId();
        vo.workOrderNo = order.getWorkOrderNo();
        vo.projectId = order.getProjectId();
        ProjectInfo project = projectInfoMapper.selectById(order.getProjectId());
        vo.projectName = project == null ? null : project.getProjectName();
        vo.templateId = order.getTemplateId();
        vo.workTitle = order.getWorkTitle();
        vo.workType = order.getWorkType();
        vo.workLocation = order.getWorkLocation();
        vo.workContent = order.getWorkContent();
        vo.requiredMaterialDesc = order.getRequiredMaterialDesc();
        vo.leaderId = order.getLeaderId();
        vo.leaderName = order.getLeaderId() == null ? null : getUserRealName(order.getLeaderId());
        vo.maintainerId = order.getMaintainerId();
        vo.maintainerName = order.getMaintainerId() == null ? null : getUserRealName(order.getMaintainerId());
        vo.plannedStartTime = order.getPlannedStartTime();
        vo.plannedEndTime = order.getPlannedEndTime();
        vo.actualStartTime = order.getActualStartTime();
        vo.actualEndTime = order.getActualEndTime();
        vo.status = order.getStatus();
        vo.priority = order.getPriority();
        vo.rejectReason = order.getRejectReason();
        vo.closeReason = order.getCloseReason();
        vo.acceptanceRequired = order.getAcceptanceRequired();
        vo.sourceType = order.getSourceType();
        vo.version = order.getVersion();
        vo.syncStatus = order.getSyncStatus();
        vo.abnormalFlag = workOrderRecordMapper.selectAll().stream()
                .anyMatch(record -> order.getId().equals(record.getWorkOrderId()) && Integer.valueOf(1).equals(record.getAbnormalFlag())) ? 1 : 0;
        vo.createdAt = order.getCreatedAt();
        vo.updatedAt = order.getUpdatedAt();
        vo.remark = order.getRemark();
        return vo;
    }

    private WorkOrderTemplateVO toTemplateVO(WorkOrderTemplate template) {
        WorkOrderTemplateVO vo = new WorkOrderTemplateVO();
        vo.id = template.getId();
        vo.templateCode = template.getTemplateCode();
        vo.templateName = template.getTemplateName();
        vo.workType = template.getWorkType();
        vo.defaultPriority = template.getDefaultPriority();
        vo.defaultWorkContent = template.getDefaultWorkContent();
        vo.defaultMaterialDesc = template.getDefaultMaterialDesc();
        vo.defaultDurationHours = template.getDefaultDurationHours();
        vo.enabledFlag = template.getEnabledFlag();
        vo.version = template.getVersion();
        vo.syncStatus = template.getSyncStatus();
        vo.createdAt = template.getCreatedAt();
        vo.remark = template.getRemark();
        return vo;
    }

    private WorkOrderDetailVO.AssignmentVO toAssignmentVO(WorkOrderAssignment assignment) {
        WorkOrderDetailVO.AssignmentVO vo = new WorkOrderDetailVO.AssignmentVO();
        vo.id = assignment.getId();
        vo.workOrderId = assignment.getWorkOrderId();
        vo.assignerId = assignment.getAssignerId();
        vo.assigneeId = assignment.getAssigneeId();
        vo.assignmentRole = assignment.getAssignmentRole();
        vo.assignmentStatus = assignment.getAssignmentStatus();
        vo.assignedAt = format(assignment.getAssignedAt());
        vo.remark = assignment.getRemark();
        return vo;
    }

    private WorkOrderDetailVO.StatusFlowVO toStatusFlowVO(WorkOrderStatusLog log) {
        WorkOrderDetailVO.StatusFlowVO vo = new WorkOrderDetailVO.StatusFlowVO();
        vo.id = log.getId();
        vo.fromStatus = log.getFromStatus();
        vo.toStatus = log.getToStatus();
        vo.operationType = log.getOperationType();
        vo.operationDesc = log.getOperationDesc();
        vo.operatorId = log.getOperatorId();
        vo.operationTime = format(log.getOperationTime());
        return vo;
    }

    private WorkOrderDetailVO.MaterialRequirementVO toMaterialVO(WorkOrderMaterial material) {
        WorkOrderDetailVO.MaterialRequirementVO vo = new WorkOrderDetailVO.MaterialRequirementVO();
        vo.id = material.getId();
        vo.materialCode = material.getMaterialCode();
        vo.materialName = material.getMaterialName();
        vo.materialSpec = material.getMaterialSpec();
        vo.unit = material.getUnit();
        vo.plannedQty = decimal(material.getPlannedQty());
        vo.actualQty = decimal(material.getActualQty());
        vo.prepareStatus = material.getPrepareStatus();
        return vo;
    }

    private WorkOrderDetailVO.SummaryVO summary(Long id, String type, String title, String status, String time) {
        WorkOrderDetailVO.SummaryVO vo = new WorkOrderDetailVO.SummaryVO();
        vo.id = id;
        vo.type = type;
        vo.title = title;
        vo.status = status;
        vo.time = time;
        return vo;
    }

    private void writeOperationLog(CurrentUser currentUser, HttpServletRequest request, String operationType,
            String businessType, Long businessId, String businessNo) {
        OperationLog log = new OperationLog();
        log.setTraceId(TraceIdUtils.currentTraceId());
        log.setOperatorId(currentUser.getUserId());
        log.setOperatorName(currentUser.getRealName());
        log.setRoleCode(String.join(",", currentUser.getRoleCodes()));
        log.setPlatform("PC");
        log.setModuleName("WORK_ORDER");
        log.setOperationType(operationType);
        log.setBusinessType(businessType);
        log.setBusinessId(String.valueOf(businessId));
        log.setBusinessNo(businessNo);
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

    private <T> PageResult<T> page(List<T> all, Integer pageNumValue, Integer pageSizeValue) {
        int pageNum = pageNumValue == null ? 1 : pageNumValue;
        int pageSize = pageSizeValue == null ? 10 : pageSizeValue;
        int from = Math.min((pageNum - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return new PageResult<>(all.subList(from, to), (long) all.size(), pageNum, pageSize);
    }

    private String getUserRealName(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(userId);
        return user == null ? null : user.getRealName();
    }

    private String generateNo(String prefix) {
        return prefix + "-" + LocalDateTime.now().format(NUMBER_TIME) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private boolean contains(String text, String keyword) {
        return text != null && keyword != null && text.contains(keyword);
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private Integer defaultInt(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    private String decimal(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    private String format(LocalDateTime time) {
        return time == null ? null : time.format(TEXT_TIME);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
