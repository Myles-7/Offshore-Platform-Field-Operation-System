package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.mobile.MobileCheckItemBatchRequest;
import com.offshore.platform.dto.mobile.MobileCheckItemRequest;
import com.offshore.platform.dto.mobile.MobileWorkRecordRequest;
import com.offshore.platform.entity.AiResult;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.entity.WorkOrderCheckItem;
import com.offshore.platform.entity.WorkOrderRecord;
import com.offshore.platform.mapper.AiResultMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderCheckItemMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.WorkRecordService;
import com.offshore.platform.vo.admin.AiSummaryVO;
import com.offshore.platform.vo.admin.WorkRecordTimelineVO;
import com.offshore.platform.vo.admin.WorkRecordVO;
import com.offshore.platform.vo.mobile.MobileAttachmentVO;
import com.offshore.platform.vo.mobile.MobileCheckItemVO;
import com.offshore.platform.vo.mobile.MobileWorkRecordVO;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class WorkRecordServiceImpl implements WorkRecordService {
    private static final DateTimeFormatter NUMBER_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderAssignmentMapper workOrderAssignmentMapper;
    private final WorkOrderRecordMapper workOrderRecordMapper;
    private final WorkOrderCheckItemMapper workOrderCheckItemMapper;
    private final WorkOrderAttachmentMapper workOrderAttachmentMapper;
    private final AiResultMapper aiResultMapper;
    private final OperationLogMapper operationLogMapper;
    private final DataScopeService dataScopeService;

    public WorkRecordServiceImpl(WorkOrderMapper workOrderMapper, WorkOrderAssignmentMapper workOrderAssignmentMapper,
            WorkOrderRecordMapper workOrderRecordMapper, WorkOrderCheckItemMapper workOrderCheckItemMapper,
            WorkOrderAttachmentMapper workOrderAttachmentMapper, AiResultMapper aiResultMapper,
            OperationLogMapper operationLogMapper, DataScopeService dataScopeService) {
        this.workOrderMapper = workOrderMapper;
        this.workOrderAssignmentMapper = workOrderAssignmentMapper;
        this.workOrderRecordMapper = workOrderRecordMapper;
        this.workOrderCheckItemMapper = workOrderCheckItemMapper;
        this.workOrderAttachmentMapper = workOrderAttachmentMapper;
        this.aiResultMapper = aiResultMapper;
        this.operationLogMapper = operationLogMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    @Transactional
    public MobileWorkRecordVO createMobileRecord(Long workOrderId, MobileWorkRecordRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = requireMobileWorkOrder(workOrderId, currentUser);
        LocalDateTime now = LocalDateTime.now();
        WorkOrderRecord record = new WorkOrderRecord();
        record.setWorkOrderId(order.getId());
        record.setProjectId(order.getProjectId());
        record.setRecordNo(generateNo("REC"));
        fillRecord(record, request, currentUser, now);
        record.setAttachmentCount(0);
        record.setAiResultCount(0);
        record.setRecordStatus("SUBMITTED");
        record.setSubmittedAt(now);
        record.setClientCreatedAt(request.getConstructionTime());
        record.setClientUpdatedAt(now);
        record.setVersion(1);
        record.setSyncStatus("SYNCED");
        record.setConflictFlag(0);
        record.setServerId(null);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setDeletedFlag(0);
        record.setCreatedBy(currentUser.getUserId());
        record.setUpdatedBy(currentUser.getUserId());
        workOrderRecordMapper.insert(record);
        record.setServerId(record.getId());
        workOrderRecordMapper.updateById(record);
        writeOperationLog(currentUser, servletRequest, "CREATE_WORK_RECORD", "MOBILE", order, record.getId(), record.getRecordNo());
        return toMobileVO(record);
    }

    @Override
    @Transactional
    public MobileWorkRecordVO updateMobileRecord(Long workOrderId, Long recordId, MobileWorkRecordRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = requireMobileWorkOrder(workOrderId, currentUser);
        WorkOrderRecord record = requireRecord(recordId);
        if (!workOrderId.equals(record.getWorkOrderId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "记录不属于该工单");
        }
        if (request.getVersion() == null || !request.getVersion().equals(record.getVersion())) {
            record.setSyncStatus("CONFLICT");
            record.setConflictFlag(1);
            record.setUpdatedAt(LocalDateTime.now());
            workOrderRecordMapper.updateById(record);
            throw new BusinessException(ErrorCode.SYNC_ERROR, "施工记录版本冲突");
        }
        fillRecord(record, request, currentUser, LocalDateTime.now());
        record.setVersion(record.getVersion() + 1);
        record.setSyncStatus("SYNCED");
        record.setConflictFlag(0);
        record.setClientUpdatedAt(LocalDateTime.now());
        workOrderRecordMapper.updateById(record);
        writeOperationLog(currentUser, servletRequest, "UPDATE_WORK_RECORD", "MOBILE", order, record.getId(), record.getRecordNo());
        return toMobileVO(record);
    }

    @Override
    public List<MobileWorkRecordVO> listMobileRecords(Long workOrderId) {
        requireMobileWorkOrder(workOrderId, CurrentUserContext.require());
        return workOrderRecordMapper.selectByWorkOrderId(workOrderId).stream().map(this::toMobileVO).toList();
    }

    @Override
    @Transactional
    public List<MobileCheckItemVO> createCheckItems(Long recordId, MobileCheckItemBatchRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrderRecord record = requireRecord(recordId);
        WorkOrder order = requireMobileWorkOrder(record.getWorkOrderId(), currentUser);
        List<MobileCheckItemVO> result = new ArrayList<>();
        for (MobileCheckItemRequest itemRequest : request.getItems()) {
            WorkOrderCheckItem item = new WorkOrderCheckItem();
            LocalDateTime now = LocalDateTime.now();
            item.setWorkOrderId(record.getWorkOrderId());
            item.setRecordId(record.getId());
            item.setItemCode(itemRequest.getItemCode());
            item.setItemName(itemRequest.getItemName());
            item.setItemType(itemRequest.getItemType());
            item.setItemDesc(itemRequest.getItemDesc());
            item.setRequiredFlag(defaultInt(itemRequest.getRequiredFlag(), 0));
            item.setCheckResult(itemRequest.getCheckResult());
            item.setCheckValue(itemRequest.getCheckValue());
            item.setCheckUnit(itemRequest.getCheckUnit());
            item.setAbnormalFlag(defaultInt(itemRequest.getAbnormalFlag(), 0));
            item.setAbnormalDesc(itemRequest.getAbnormalDesc());
            item.setCheckedBy(currentUser.getUserId());
            item.setCheckedAt(itemRequest.getCheckedAt() == null ? now : itemRequest.getCheckedAt());
            item.setAttachmentRequiredFlag(defaultInt(itemRequest.getAttachmentRequiredFlag(), 0));
            item.setAttachmentCount(defaultInt(itemRequest.getAttachmentCount(), 0));
            item.setAiRequiredFlag(defaultInt(itemRequest.getAiRequiredFlag(), 0));
            item.setAiResultCount(defaultInt(itemRequest.getAiResultCount(), 0));
            item.setSortOrder(defaultInt(itemRequest.getSortOrder(), 0));
            item.setClientCreatedAt(now);
            item.setClientUpdatedAt(now);
            item.setLocalId(itemRequest.getLocalId());
            item.setVersion(1);
            item.setSyncStatus("SYNCED");
            item.setDeviceId(itemRequest.getDeviceId());
            item.setOperatorId(currentUser.getUserId());
            item.setConflictFlag(0);
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            item.setDeletedFlag(0);
            item.setCreatedBy(currentUser.getUserId());
            item.setUpdatedBy(currentUser.getUserId());
            item.setRemark(itemRequest.getRemark());
            workOrderCheckItemMapper.insert(item);
            item.setServerId(item.getId());
            workOrderCheckItemMapper.updateById(item);
            result.add(toCheckItemVO(item));
        }
        updateRecordCounts(record);
        writeOperationLog(currentUser, servletRequest, "CREATE_WORK_RECORD_CHECK_ITEMS", "MOBILE", order, record.getId(), record.getRecordNo());
        return result;
    }

    @Override
    public List<WorkRecordVO> listAdminRecords(Long workOrderId) {
        WorkOrder order = requireAdminWorkOrder(workOrderId);
        return workOrderRecordMapper.selectByWorkOrderId(order.getId()).stream().map(this::toAdminVO).toList();
    }

    @Override
    public WorkRecordVO getAdminRecord(Long recordId) {
        WorkOrderRecord record = requireRecord(recordId);
        requireAdminWorkOrder(record.getWorkOrderId());
        return toAdminVO(record);
    }

    @Override
    public List<WorkRecordTimelineVO> getRecordTimeline(Long recordId) {
        WorkOrderRecord record = requireRecord(recordId);
        requireAdminWorkOrder(record.getWorkOrderId());
        List<WorkRecordTimelineVO> timeline = new ArrayList<>();
        timeline.add(timeline("RECORD", record.getId(), record.getRecordNo(), record.getConstructionDesc(), record.getRecordStatus(), record.getConstructionTime()));
        workOrderCheckItemMapper.selectByRecordId(record.getId()).forEach(item ->
                timeline.add(timeline("CHECK_ITEM", item.getId(), item.getItemName(), item.getCheckValue(), item.getCheckResult(), item.getCheckedAt())));
        workOrderAttachmentMapper.selectAll().stream().filter(item -> record.getId().equals(item.getRecordId())).forEach(item ->
                timeline.add(timeline("ATTACHMENT", item.getId(), item.getAttachmentName(), item.getAttachmentDesc(), item.getUploadStatus(), item.getCaptureTime())));
        aiResultMapper.selectAll().stream().filter(item -> record.getId().equals(item.getRecordId())).forEach(item ->
                timeline.add(timeline("AI_RESULT", item.getId(), item.getAiResultNo(), item.getResultSummary(), item.getReviewStatus(), item.getInferTime())));
        return timeline.stream().sorted(Comparator.comparing(item -> item.eventTime, Comparator.nullsLast(Comparator.naturalOrder()))).toList();
    }

    @Override
    @Transactional
    public WorkRecordVO confirmRecord(Long recordId, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrderRecord record = requireRecord(recordId);
        WorkOrder order = requireAdminWorkOrder(record.getWorkOrderId());
        record.setRecordStatus("CONFIRMED");
        record.setConfirmedBy(currentUser.getUserId());
        record.setConfirmedAt(LocalDateTime.now());
        record.setVersion(record.getVersion() == null ? 1 : record.getVersion() + 1);
        record.setSyncStatus("SYNCED");
        record.setUpdatedAt(LocalDateTime.now());
        record.setUpdatedBy(currentUser.getUserId());
        workOrderRecordMapper.updateById(record);
        writeOperationLog(currentUser, servletRequest, "CONFIRM_WORK_RECORD", "PC", order, record.getId(), record.getRecordNo());
        return toAdminVO(record);
    }

    private void fillRecord(WorkOrderRecord record, MobileWorkRecordRequest request, CurrentUser user, LocalDateTime now) {
        record.setRecordType(request.getRecordType());
        record.setConstructionTime(request.getConstructionTime() == null ? now : request.getConstructionTime());
        record.setConstructionUserId(user.getUserId());
        record.setConstructionUserName(user.getRealName());
        record.setConstructionDesc(request.getConstructionDesc());
        record.setSiteCondition(request.getSiteCondition());
        record.setAbnormalFlag(defaultInt(request.getAbnormalFlag(), 0));
        record.setAbnormalDesc(request.getAbnormalDesc());
        record.setWeather(request.getWeather());
        record.setTemperature(request.getTemperature());
        record.setHumidity(request.getHumidity());
        record.setLocationName(request.getLocationName());
        record.setLatitude(request.getLatitude());
        record.setLongitude(request.getLongitude());
        record.setAltitude(request.getAltitude());
        record.setLocalId(request.getLocalId());
        record.setDeviceId(request.getDeviceId());
        record.setOperatorId(user.getUserId());
        record.setUpdatedAt(now);
        record.setUpdatedBy(user.getUserId());
        record.setRemark(request.getRemark());
    }

    private WorkOrder requireMobileWorkOrder(Long workOrderId, CurrentUser currentUser) {
        WorkOrder order = workOrderMapper.selectById(workOrderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在");
        }
        boolean assigned = currentUser.getUserId().equals(order.getMaintainerId())
                || workOrderAssignmentMapper.selectByWorkOrderId(order.getId()).stream()
                .anyMatch(item -> currentUser.getUserId().equals(item.getAssigneeId()));
        if (!assigned) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该工单记录");
        }
        return order;
    }

    private WorkOrder requireAdminWorkOrder(Long workOrderId) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = workOrderMapper.selectById(workOrderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在");
        }
        if (!dataScopeService.canAccessAll(currentUser) && !dataScopeService.canAccessProject(currentUser, order.getProjectId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该项目记录");
        }
        return order;
    }

    private WorkOrderRecord requireRecord(Long recordId) {
        WorkOrderRecord record = workOrderRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "施工记录不存在");
        }
        return record;
    }

    private void updateRecordCounts(WorkOrderRecord record) {
        record.setAttachmentCount((int) workOrderAttachmentMapper.selectAll().stream().filter(item -> record.getId().equals(item.getRecordId())).count());
        record.setAiResultCount((int) aiResultMapper.selectAll().stream().filter(item -> record.getId().equals(item.getRecordId())).count());
        record.setUpdatedAt(LocalDateTime.now());
        workOrderRecordMapper.updateById(record);
    }

    private MobileWorkRecordVO toMobileVO(WorkOrderRecord record) {
        MobileWorkRecordVO vo = new MobileWorkRecordVO();
        fillRecordVO(vo, record);
        vo.checkItems = workOrderCheckItemMapper.selectByRecordId(record.getId()).stream().map(this::toCheckItemVO).toList();
        vo.attachments = workOrderAttachmentMapper.selectAll().stream().filter(item -> record.getId().equals(item.getRecordId())).map(this::toAttachmentVO).toList();
        return vo;
    }

    private WorkRecordVO toAdminVO(WorkOrderRecord record) {
        WorkRecordVO vo = new WorkRecordVO();
        fillRecordVO(vo, record);
        vo.checkItems = workOrderCheckItemMapper.selectByRecordId(record.getId()).stream().map(this::toCheckItemVO).toList();
        vo.attachments = workOrderAttachmentMapper.selectAll().stream().filter(item -> record.getId().equals(item.getRecordId())).map(this::toAttachmentVO).toList();
        vo.aiResults = aiResultMapper.selectAll().stream().filter(item -> record.getId().equals(item.getRecordId())).map(this::toAiSummaryVO).toList();
        return vo;
    }

    private void fillRecordVO(MobileWorkRecordVO vo, WorkOrderRecord record) {
        vo.id = record.getId();
        vo.serverId = record.getServerId();
        vo.localId = record.getLocalId();
        vo.workOrderId = record.getWorkOrderId();
        vo.projectId = record.getProjectId();
        vo.recordNo = record.getRecordNo();
        vo.recordType = record.getRecordType();
        vo.constructionTime = record.getConstructionTime();
        vo.constructionUserId = record.getConstructionUserId();
        vo.constructionUserName = record.getConstructionUserName();
        vo.constructionDesc = record.getConstructionDesc();
        vo.siteCondition = record.getSiteCondition();
        vo.abnormalFlag = record.getAbnormalFlag();
        vo.abnormalDesc = record.getAbnormalDesc();
        vo.weather = record.getWeather();
        vo.temperature = record.getTemperature();
        vo.humidity = record.getHumidity();
        vo.locationName = record.getLocationName();
        vo.latitude = record.getLatitude();
        vo.longitude = record.getLongitude();
        vo.altitude = record.getAltitude();
        vo.attachmentCount = record.getAttachmentCount();
        vo.aiResultCount = record.getAiResultCount();
        vo.recordStatus = record.getRecordStatus();
        vo.submittedAt = record.getSubmittedAt();
        vo.confirmedBy = record.getConfirmedBy();
        vo.confirmedAt = record.getConfirmedAt();
        vo.version = record.getVersion();
        vo.updatedAt = record.getUpdatedAt();
        vo.syncStatus = record.getSyncStatus();
        vo.conflictFlag = record.getConflictFlag();
    }

    private void fillRecordVO(WorkRecordVO vo, WorkOrderRecord record) {
        vo.id = record.getId();
        vo.serverId = record.getServerId();
        vo.localId = record.getLocalId();
        vo.workOrderId = record.getWorkOrderId();
        vo.projectId = record.getProjectId();
        vo.recordNo = record.getRecordNo();
        vo.recordType = record.getRecordType();
        vo.constructionTime = record.getConstructionTime();
        vo.constructionUserId = record.getConstructionUserId();
        vo.constructionUserName = record.getConstructionUserName();
        vo.constructionDesc = record.getConstructionDesc();
        vo.siteCondition = record.getSiteCondition();
        vo.abnormalFlag = record.getAbnormalFlag();
        vo.abnormalDesc = record.getAbnormalDesc();
        vo.weather = record.getWeather();
        vo.temperature = record.getTemperature();
        vo.humidity = record.getHumidity();
        vo.locationName = record.getLocationName();
        vo.attachmentCount = record.getAttachmentCount();
        vo.aiResultCount = record.getAiResultCount();
        vo.recordStatus = record.getRecordStatus();
        vo.submittedAt = record.getSubmittedAt();
        vo.confirmedBy = record.getConfirmedBy();
        vo.confirmedAt = record.getConfirmedAt();
        vo.version = record.getVersion();
        vo.updatedAt = record.getUpdatedAt();
        vo.syncStatus = record.getSyncStatus();
        vo.conflictFlag = record.getConflictFlag();
    }

    private MobileCheckItemVO toCheckItemVO(WorkOrderCheckItem item) {
        MobileCheckItemVO vo = new MobileCheckItemVO();
        vo.id = item.getId();
        vo.serverId = item.getServerId();
        vo.localId = item.getLocalId();
        vo.workOrderId = item.getWorkOrderId();
        vo.recordId = item.getRecordId();
        vo.itemCode = item.getItemCode();
        vo.itemName = item.getItemName();
        vo.itemType = item.getItemType();
        vo.itemDesc = item.getItemDesc();
        vo.requiredFlag = item.getRequiredFlag();
        vo.checkResult = item.getCheckResult();
        vo.checkValue = item.getCheckValue();
        vo.checkUnit = item.getCheckUnit();
        vo.abnormalFlag = item.getAbnormalFlag();
        vo.abnormalDesc = item.getAbnormalDesc();
        vo.checkedBy = item.getCheckedBy();
        vo.checkedAt = item.getCheckedAt();
        vo.attachmentRequiredFlag = item.getAttachmentRequiredFlag();
        vo.attachmentCount = item.getAttachmentCount();
        vo.aiRequiredFlag = item.getAiRequiredFlag();
        vo.aiResultCount = item.getAiResultCount();
        vo.sortOrder = item.getSortOrder();
        vo.version = item.getVersion();
        vo.updatedAt = item.getUpdatedAt();
        vo.syncStatus = item.getSyncStatus();
        vo.conflictFlag = item.getConflictFlag();
        return vo;
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

    private AiSummaryVO toAiSummaryVO(AiResult result) {
        AiSummaryVO vo = new AiSummaryVO();
        vo.id = result.getId();
        vo.aiResultNo = result.getAiResultNo();
        vo.defectType = result.getDefectType();
        vo.suspectedDefectFlag = result.getSuspectedDefectFlag();
        vo.defectCount = result.getDefectCount();
        vo.reviewStatus = result.getReviewStatus();
        vo.inferTime = result.getInferTime();
        return vo;
    }

    private WorkRecordTimelineVO timeline(String type, Long id, String title, String desc, String status, LocalDateTime time) {
        WorkRecordTimelineVO vo = new WorkRecordTimelineVO();
        vo.eventType = type;
        vo.eventId = id;
        vo.title = title;
        vo.description = desc;
        vo.status = status;
        vo.eventTime = time;
        return vo;
    }

    private void writeOperationLog(CurrentUser currentUser, HttpServletRequest request, String operationType,
            String platform, WorkOrder order, Long recordId, String recordNo) {
        OperationLog log = new OperationLog();
        log.setTraceId(TraceIdUtils.currentTraceId());
        log.setOperatorId(currentUser.getUserId());
        log.setOperatorName(currentUser.getRealName());
        log.setRoleCode(String.join(",", currentUser.getRoleCodes()));
        log.setPlatform(platform);
        log.setModuleName("WORK_RECORD");
        log.setOperationType(operationType);
        log.setBusinessType("WORK_RECORD");
        log.setBusinessId(String.valueOf(recordId));
        log.setBusinessNo(recordNo);
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

    private String generateNo(String prefix) {
        return prefix + "-" + LocalDateTime.now().format(NUMBER_TIME) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private Integer defaultInt(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
