package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.mobile.MobileCheckItemBatchRequest;
import com.offshore.platform.dto.mobile.MobileCheckItemRequest;
import com.offshore.platform.dto.mobile.MobileWorkRecordRequest;
import com.offshore.platform.dto.workrecord.WorkOrderCheckItemBatchRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordCreateRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordDetailRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordQueryRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordRejectRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordUpdateRequest;
import com.offshore.platform.entity.AiResult;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.SyncConflict;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAcceptance;
import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.entity.WorkOrderCheckItem;
import com.offshore.platform.entity.WorkOrderRecord;
import com.offshore.platform.entity.WorkOrderRecordDetail;
import com.offshore.platform.mapper.AiResultMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.SyncConflictMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAcceptanceMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderCheckItemMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderRecordDetailMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.WorkRecordService;
import com.offshore.platform.vo.admin.AiSummaryVO;
import com.offshore.platform.vo.admin.WorkRecordTimelineVO;
import com.offshore.platform.vo.admin.WorkRecordVO;
import com.offshore.platform.vo.mobile.MobileAttachmentVO;
import com.offshore.platform.vo.mobile.MobileCheckItemVO;
import com.offshore.platform.vo.mobile.MobileWorkRecordVO;
import com.offshore.platform.vo.workrecord.WorkOrderCheckItemVO;
import com.offshore.platform.vo.workrecord.WorkOrderRecordDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
    private final WorkOrderRecordDetailMapper workOrderRecordDetailMapper;
    private final WorkOrderCheckItemMapper workOrderCheckItemMapper;
    private final WorkOrderAttachmentMapper workOrderAttachmentMapper;
    private final WorkOrderAcceptanceMapper workOrderAcceptanceMapper;
    private final AiResultMapper aiResultMapper;
    private final SyncConflictMapper syncConflictMapper;
    private final OperationLogMapper operationLogMapper;
    private final DataScopeService dataScopeService;

    public WorkRecordServiceImpl(WorkOrderMapper workOrderMapper, WorkOrderAssignmentMapper workOrderAssignmentMapper,
            WorkOrderRecordMapper workOrderRecordMapper, WorkOrderRecordDetailMapper workOrderRecordDetailMapper,
            WorkOrderCheckItemMapper workOrderCheckItemMapper, WorkOrderAttachmentMapper workOrderAttachmentMapper,
            WorkOrderAcceptanceMapper workOrderAcceptanceMapper, AiResultMapper aiResultMapper,
            SyncConflictMapper syncConflictMapper, OperationLogMapper operationLogMapper, DataScopeService dataScopeService) {
        this.workOrderMapper = workOrderMapper;
        this.workOrderAssignmentMapper = workOrderAssignmentMapper;
        this.workOrderRecordMapper = workOrderRecordMapper;
        this.workOrderRecordDetailMapper = workOrderRecordDetailMapper;
        this.workOrderCheckItemMapper = workOrderCheckItemMapper;
        this.workOrderAttachmentMapper = workOrderAttachmentMapper;
        this.workOrderAcceptanceMapper = workOrderAcceptanceMapper;
        this.aiResultMapper = aiResultMapper;
        this.syncConflictMapper = syncConflictMapper;
        this.operationLogMapper = operationLogMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    @Transactional
    public MobileWorkRecordVO createMobileRecord(Long workOrderId, MobileWorkRecordRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = requireMobileWorkOrder(workOrderId, currentUser);
        ensureRecordCreatable(order);
        validateAbnormal(request.getAbnormalFlag(), request.getAbnormalDesc());
        WorkOrderRecord existing = findRecordByLocalId(workOrderId, request.getLocalId());
        if (existing != null) {
            return toMobileVO(existing);
        }
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
            createRecordConflict(record, currentUser, "VERSION_CONFLICT", request.getVersion(), request.getDeviceId(), "施工记录版本冲突");
            throw new BusinessException(ErrorCode.SYNC_ERROR, "施工记录版本冲突");
        }
        ensureAcceptanceNotLocked(record, currentUser, request.getVersion(), request.getDeviceId(), "UPDATE_LOCKED_RECORD");
        validateAbnormal(request.getAbnormalFlag(), request.getAbnormalDesc());
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
    public com.offshore.platform.vo.workrecord.WorkOrderRecordVO getMobileRecord(Long workOrderId, Long recordId) {
        requireMobileWorkOrder(workOrderId, CurrentUserContext.require());
        WorkOrderRecord record = requireRecord(recordId);
        ensureRecordBelongs(record, workOrderId);
        return toWorkRecordVO(record);
    }

    @Override
    @Transactional
    public com.offshore.platform.vo.workrecord.WorkOrderRecordVO createMobileRecord(Long workOrderId,
            WorkOrderRecordCreateRequest request, HttpServletRequest servletRequest) {
        MobileWorkRecordRequest mobileRequest = toMobileCreateRequest(request);
        return toWorkRecordVO(createMobileRecord(workOrderId, mobileRequest, servletRequest).id);
    }

    @Override
    @Transactional
    public com.offshore.platform.vo.workrecord.WorkOrderRecordVO updateMobileRecord(Long workOrderId, Long recordId,
            WorkOrderRecordUpdateRequest request, HttpServletRequest servletRequest) {
        MobileWorkRecordRequest mobileRequest = toMobileUpdateRequest(request);
        return toWorkRecordVO(updateMobileRecord(workOrderId, recordId, mobileRequest, servletRequest).id);
    }

    @Override
    @Transactional
    public void deleteMobileRecord(Long workOrderId, Long recordId, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrder order = requireMobileWorkOrder(workOrderId, currentUser);
        WorkOrderRecord record = requireRecord(recordId);
        ensureRecordBelongs(record, workOrderId);
        ensureAcceptanceNotLocked(record, currentUser, record.getVersion(), record.getDeviceId(), "DELETE_LOCKED_RECORD");
        record.setDeletedFlag(1);
        record.setSyncStatus("DELETED");
        record.setUpdatedAt(LocalDateTime.now());
        record.setUpdatedBy(currentUser.getUserId());
        workOrderRecordMapper.updateById(record);
        writeOperationLog(currentUser, servletRequest, "DELETE_WORK_RECORD", "MOBILE", order, record.getId(), record.getRecordNo());
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
    @Transactional
    public WorkOrderRecordDetailVO createDetail(Long recordId, WorkOrderRecordDetailRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrderRecord record = requireRecord(recordId);
        WorkOrder order = requireMobileWorkOrder(record.getWorkOrderId(), currentUser);
        ensureAcceptanceNotLocked(record, currentUser, request.version, request.deviceId, "CREATE_DETAIL_LOCKED_RECORD");
        WorkOrderRecordDetail existing = findDetailByLocalId(recordId, request.localId);
        if (existing != null) {
            return toDetailVO(existing);
        }
        LocalDateTime now = LocalDateTime.now();
        WorkOrderRecordDetail detail = new WorkOrderRecordDetail();
        detail.setWorkOrderId(record.getWorkOrderId());
        detail.setRecordId(record.getId());
        fillDetail(detail, request, currentUser, now);
        detail.setVersion(1);
        detail.setSyncStatus("SYNCED");
        detail.setConflictFlag(0);
        detail.setCreatedAt(now);
        detail.setUpdatedAt(now);
        detail.setDeletedFlag(0);
        detail.setCreatedBy(currentUser.getUserId());
        detail.setUpdatedBy(currentUser.getUserId());
        workOrderRecordDetailMapper.insert(detail);
        detail.setServerId(detail.getId());
        workOrderRecordDetailMapper.updateById(detail);
        writeOperationLog(currentUser, servletRequest, "CREATE_WORK_RECORD_DETAIL", "MOBILE", order, record.getId(), record.getRecordNo());
        return toDetailVO(detail);
    }

    @Override
    @Transactional
    public WorkOrderRecordDetailVO updateDetail(Long recordId, Long detailId, WorkOrderRecordDetailRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrderRecord record = requireRecord(recordId);
        WorkOrder order = requireMobileWorkOrder(record.getWorkOrderId(), currentUser);
        ensureAcceptanceNotLocked(record, currentUser, request.version, request.deviceId, "UPDATE_DETAIL_LOCKED_RECORD");
        WorkOrderRecordDetail detail = requireDetail(recordId, detailId);
        if (request.version == null || !request.version.equals(detail.getVersion())) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "施工记录明细版本冲突");
        }
        fillDetail(detail, request, currentUser, LocalDateTime.now());
        detail.setVersion(detail.getVersion() == null ? 1 : detail.getVersion() + 1);
        detail.setSyncStatus("SYNCED");
        detail.setConflictFlag(0);
        workOrderRecordDetailMapper.updateById(detail);
        writeOperationLog(currentUser, servletRequest, "UPDATE_WORK_RECORD_DETAIL", "MOBILE", order, record.getId(), record.getRecordNo());
        return toDetailVO(detail);
    }

    @Override
    @Transactional
    public void deleteDetail(Long recordId, Long detailId, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrderRecord record = requireRecord(recordId);
        WorkOrder order = requireMobileWorkOrder(record.getWorkOrderId(), currentUser);
        ensureAcceptanceNotLocked(record, currentUser, record.getVersion(), record.getDeviceId(), "DELETE_DETAIL_LOCKED_RECORD");
        WorkOrderRecordDetail detail = requireDetail(recordId, detailId);
        detail.setDeletedFlag(1);
        detail.setSyncStatus("DELETED");
        detail.setUpdatedAt(LocalDateTime.now());
        detail.setUpdatedBy(currentUser.getUserId());
        workOrderRecordDetailMapper.updateById(detail);
        writeOperationLog(currentUser, servletRequest, "DELETE_WORK_RECORD_DETAIL", "MOBILE", order, record.getId(), record.getRecordNo());
    }

    @Override
    @Transactional
    public List<WorkOrderCheckItemVO> saveCheckItems(Long recordId, WorkOrderCheckItemBatchRequest request, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrderRecord record = requireRecord(recordId);
        WorkOrder order = requireMobileWorkOrder(record.getWorkOrderId(), currentUser);
        ensureAcceptanceNotLocked(record, currentUser, record.getVersion(), null, "SAVE_CHECK_ITEM_LOCKED_RECORD");
        List<WorkOrderCheckItemVO> result = new ArrayList<>();
        for (com.offshore.platform.dto.workrecord.WorkOrderCheckItemRequest itemRequest : request.items) {
            WorkOrderCheckItem existing = findCheckItemByLocalId(recordId, itemRequest.localId);
            if (existing != null) {
                result.add(toWorkOrderCheckItemVO(existing));
                continue;
            }
            WorkOrderCheckItem item = new WorkOrderCheckItem();
            fillCheckItem(item, record, itemRequest, currentUser, LocalDateTime.now());
            item.setVersion(1);
            item.setSyncStatus("SYNCED");
            item.setConflictFlag(0);
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            item.setDeletedFlag(0);
            item.setCreatedBy(currentUser.getUserId());
            item.setUpdatedBy(currentUser.getUserId());
            workOrderCheckItemMapper.insert(item);
            item.setServerId(item.getId());
            workOrderCheckItemMapper.updateById(item);
            result.add(toWorkOrderCheckItemVO(item));
        }
        updateRecordCounts(record);
        writeOperationLog(currentUser, servletRequest, "CREATE_WORK_RECORD_CHECK_ITEMS", "MOBILE", order, record.getId(), record.getRecordNo());
        return result;
    }

    @Override
    @Transactional
    public WorkOrderCheckItemVO updateCheckItem(Long recordId, Long itemId, com.offshore.platform.dto.workrecord.WorkOrderCheckItemRequest request,
            HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrderRecord record = requireRecord(recordId);
        WorkOrder order = requireMobileWorkOrder(record.getWorkOrderId(), currentUser);
        ensureAcceptanceNotLocked(record, currentUser, request.version, request.deviceId, "UPDATE_CHECK_ITEM_LOCKED_RECORD");
        WorkOrderCheckItem item = requireCheckItem(recordId, itemId);
        if (request.version == null || !request.version.equals(item.getVersion())) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "检查项版本冲突");
        }
        fillCheckItem(item, record, request, currentUser, LocalDateTime.now());
        item.setVersion(item.getVersion() == null ? 1 : item.getVersion() + 1);
        item.setSyncStatus("SYNCED");
        item.setConflictFlag(0);
        workOrderCheckItemMapper.updateById(item);
        updateRecordCounts(record);
        writeOperationLog(currentUser, servletRequest, "UPDATE_WORK_RECORD_CHECK_ITEM", "MOBILE", order, record.getId(), record.getRecordNo());
        return toWorkOrderCheckItemVO(item);
    }

    @Override
    @Transactional
    public void deleteCheckItem(Long recordId, Long itemId, HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrderRecord record = requireRecord(recordId);
        WorkOrder order = requireMobileWorkOrder(record.getWorkOrderId(), currentUser);
        ensureAcceptanceNotLocked(record, currentUser, record.getVersion(), record.getDeviceId(), "DELETE_CHECK_ITEM_LOCKED_RECORD");
        WorkOrderCheckItem item = requireCheckItem(recordId, itemId);
        item.setDeletedFlag(1);
        item.setSyncStatus("DELETED");
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(currentUser.getUserId());
        workOrderCheckItemMapper.updateById(item);
        updateRecordCounts(record);
        writeOperationLog(currentUser, servletRequest, "DELETE_WORK_RECORD_CHECK_ITEM", "MOBILE", order, record.getId(), record.getRecordNo());
    }

    @Override
    public List<WorkRecordVO> listAdminRecords(Long workOrderId) {
        WorkOrder order = requireAdminWorkOrder(workOrderId);
        return workOrderRecordMapper.selectByWorkOrderId(order.getId()).stream().map(this::toAdminVO).toList();
    }

    @Override
    public List<com.offshore.platform.vo.workrecord.WorkOrderRecordVO> listAdminRecords(Long workOrderId, WorkOrderRecordQueryRequest request) {
        WorkOrder order = requireAdminWorkOrder(workOrderId);
        return workOrderRecordMapper.selectByWorkOrderId(order.getId()).stream()
                .filter(item -> request.constructionUserId == null || request.constructionUserId.equals(item.getConstructionUserId()))
                .filter(item -> request.constructionTimeStart == null || (item.getConstructionTime() != null
                        && !item.getConstructionTime().isBefore(request.constructionTimeStart)))
                .filter(item -> request.constructionTimeEnd == null || (item.getConstructionTime() != null
                        && !item.getConstructionTime().isAfter(request.constructionTimeEnd)))
                .filter(item -> request.abnormalFlag == null || request.abnormalFlag.equals(item.getAbnormalFlag()))
                .filter(item -> !StringUtils.hasText(request.recordStatus) || request.recordStatus.equals(item.getRecordStatus()))
                .filter(item -> !StringUtils.hasText(request.syncStatus) || request.syncStatus.equals(item.getSyncStatus()))
                .map(this::toWorkRecordVO)
                .toList();
    }

    @Override
    public WorkRecordVO getAdminRecord(Long recordId) {
        WorkOrderRecord record = requireRecord(recordId);
        requireAdminWorkOrder(record.getWorkOrderId());
        return toAdminVO(record);
    }

    @Override
    public com.offshore.platform.vo.workrecord.WorkOrderRecordVO getAdminRecordDetail(Long recordId) {
        WorkOrderRecord record = requireRecord(recordId);
        requireAdminWorkOrder(record.getWorkOrderId());
        return toWorkRecordVO(record);
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

    @Override
    @Transactional
    public com.offshore.platform.vo.workrecord.WorkOrderRecordVO rejectRecord(Long recordId, WorkOrderRecordRejectRequest request,
            HttpServletRequest servletRequest) {
        CurrentUser currentUser = CurrentUserContext.require();
        WorkOrderRecord record = requireRecord(recordId);
        WorkOrder order = requireAdminWorkOrder(record.getWorkOrderId());
        record.setRecordStatus("REJECTED");
        record.setRemark(request.rejectReason);
        record.setVersion(record.getVersion() == null ? 1 : record.getVersion() + 1);
        record.setSyncStatus("SYNCED");
        record.setUpdatedAt(LocalDateTime.now());
        record.setUpdatedBy(currentUser.getUserId());
        workOrderRecordMapper.updateById(record);
        writeOperationLog(currentUser, servletRequest, "REJECT_WORK_RECORD", "PC", order, record.getId(), record.getRecordNo());
        return toWorkRecordVO(record);
    }

    private MobileWorkRecordRequest toMobileCreateRequest(WorkOrderRecordCreateRequest request) {
        MobileWorkRecordRequest target = new MobileWorkRecordRequest();
        target.setLocalId(request.localId);
        target.setRecordType(request.recordType);
        target.setConstructionTime(request.constructionTime);
        target.setConstructionDesc(request.constructionDesc);
        target.setSiteCondition(request.siteCondition);
        target.setAbnormalFlag(request.abnormalFlag);
        target.setAbnormalDesc(request.abnormalDesc);
        target.setWeather(request.weather);
        target.setTemperature(request.temperature);
        target.setHumidity(request.humidity);
        target.setLocationName(request.locationName);
        target.setLatitude(request.latitude);
        target.setLongitude(request.longitude);
        target.setAltitude(request.altitude);
        target.setDeviceId(request.deviceId);
        target.setRemark(request.remark);
        return target;
    }

    private MobileWorkRecordRequest toMobileUpdateRequest(WorkOrderRecordUpdateRequest request) {
        MobileWorkRecordRequest target = new MobileWorkRecordRequest();
        target.setVersion(request.version);
        target.setRecordType(request.recordType);
        target.setConstructionTime(request.constructionTime);
        target.setConstructionDesc(request.constructionDesc);
        target.setSiteCondition(request.siteCondition);
        target.setAbnormalFlag(request.abnormalFlag);
        target.setAbnormalDesc(request.abnormalDesc);
        target.setWeather(request.weather);
        target.setTemperature(request.temperature);
        target.setHumidity(request.humidity);
        target.setLocationName(request.locationName);
        target.setLatitude(request.latitude);
        target.setLongitude(request.longitude);
        target.setAltitude(request.altitude);
        target.setDeviceId(request.deviceId);
        target.setRemark(request.remark);
        return target;
    }

    private void fillDetail(WorkOrderRecordDetail detail, WorkOrderRecordDetailRequest request, CurrentUser user, LocalDateTime now) {
        detail.setDetailType(request.detailType);
        detail.setDetailTitle(request.detailTitle);
        detail.setDetailContent(request.detailContent);
        detail.setStepNo(request.stepNo);
        detail.setItemCode(request.itemCode);
        detail.setItemName(request.itemName);
        detail.setItemValue(request.itemValue);
        detail.setItemUnit(request.itemUnit);
        detail.setNormalFlag(defaultInt(request.normalFlag, 1));
        detail.setAbnormalDesc(request.abnormalDesc);
        detail.setAttachmentRefFlag(defaultInt(request.attachmentRefFlag, 0));
        detail.setAiRefFlag(defaultInt(request.aiRefFlag, 0));
        detail.setSortOrder(defaultInt(request.sortOrder, 0));
        detail.setClientCreatedAt(request.clientCreatedAt == null ? now : request.clientCreatedAt);
        detail.setClientUpdatedAt(request.clientUpdatedAt == null ? now : request.clientUpdatedAt);
        detail.setLocalId(request.localId);
        detail.setDeviceId(request.deviceId);
        detail.setOperatorId(user.getUserId());
        detail.setUpdatedAt(now);
        detail.setUpdatedBy(user.getUserId());
        detail.setRemark(request.remark);
    }

    private void fillCheckItem(WorkOrderCheckItem item, WorkOrderRecord record,
            com.offshore.platform.dto.workrecord.WorkOrderCheckItemRequest request, CurrentUser user, LocalDateTime now) {
        item.setWorkOrderId(record.getWorkOrderId());
        item.setRecordId(record.getId());
        item.setItemCode(request.itemCode);
        item.setItemName(request.itemName);
        item.setItemType(request.itemType);
        item.setItemDesc(request.itemDesc);
        item.setRequiredFlag(defaultInt(request.requiredFlag, 0));
        item.setCheckResult(request.checkResult);
        item.setCheckValue(request.checkValue);
        item.setCheckUnit(request.checkUnit);
        item.setAbnormalFlag(defaultInt(request.abnormalFlag, 0));
        item.setAbnormalDesc(request.abnormalDesc);
        item.setCheckedBy(user.getUserId());
        item.setCheckedAt(request.checkedAt == null ? now : request.checkedAt);
        item.setAttachmentRequiredFlag(defaultInt(request.attachmentRequiredFlag, 0));
        item.setAttachmentCount(defaultInt(request.attachmentCount, 0));
        item.setAiRequiredFlag(defaultInt(request.aiRequiredFlag, 0));
        item.setAiResultCount(defaultInt(request.aiResultCount, 0));
        item.setSortOrder(defaultInt(request.sortOrder, 0));
        item.setClientCreatedAt(request.clientCreatedAt == null ? now : request.clientCreatedAt);
        item.setClientUpdatedAt(request.clientUpdatedAt == null ? now : request.clientUpdatedAt);
        item.setLocalId(request.localId);
        item.setDeviceId(request.deviceId);
        item.setOperatorId(user.getUserId());
        item.setUpdatedAt(now);
        item.setUpdatedBy(user.getUserId());
        item.setRemark(request.remark);
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

    private void ensureRecordBelongs(WorkOrderRecord record, Long workOrderId) {
        if (!workOrderId.equals(record.getWorkOrderId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "施工记录不属于该工单");
        }
    }

    private WorkOrderRecordDetail requireDetail(Long recordId, Long detailId) {
        WorkOrderRecordDetail detail = workOrderRecordDetailMapper.selectById(detailId);
        if (detail == null || !recordId.equals(detail.getRecordId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "施工记录明细不存在");
        }
        return detail;
    }

    private WorkOrderCheckItem requireCheckItem(Long recordId, Long itemId) {
        WorkOrderCheckItem item = workOrderCheckItemMapper.selectById(itemId);
        if (item == null || !recordId.equals(item.getRecordId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "施工检查项不存在");
        }
        return item;
    }

    private WorkOrderRecord findRecordByLocalId(Long workOrderId, String localId) {
        if (!StringUtils.hasText(localId)) {
            return null;
        }
        return workOrderRecordMapper.selectByWorkOrderId(workOrderId).stream()
                .filter(item -> localId.equals(item.getLocalId()))
                .findFirst()
                .orElse(null);
    }

    private WorkOrderRecordDetail findDetailByLocalId(Long recordId, String localId) {
        if (!StringUtils.hasText(localId)) {
            return null;
        }
        return workOrderRecordDetailMapper.selectAll().stream()
                .filter(item -> recordId.equals(item.getRecordId()) && localId.equals(item.getLocalId()))
                .findFirst()
                .orElse(null);
    }

    private WorkOrderCheckItem findCheckItemByLocalId(Long recordId, String localId) {
        if (!StringUtils.hasText(localId)) {
            return null;
        }
        return workOrderCheckItemMapper.selectByRecordId(recordId).stream()
                .filter(item -> localId.equals(item.getLocalId()))
                .findFirst()
                .orElse(null);
    }

    private void ensureRecordCreatable(WorkOrder order) {
        if (!List.of("IN_PROGRESS", "REJECTED").contains(order.getStatus())) {
            throw new BusinessException(ErrorCode.WORK_ORDER_ERROR, "当前工单状态不允许新增施工记录");
        }
    }

    private void validateAbnormal(Integer abnormalFlag, String abnormalDesc) {
        if (Integer.valueOf(1).equals(abnormalFlag) && !StringUtils.hasText(abnormalDesc)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "异常施工记录必须填写异常说明");
        }
    }

    private void ensureAcceptanceNotLocked(WorkOrderRecord record, CurrentUser currentUser, Integer clientVersion,
            String deviceId, String conflictType) {
        boolean locked = workOrderAcceptanceMapper.selectAll().stream()
                .filter(item -> record.getWorkOrderId().equals(item.getWorkOrderId()))
                .filter(item -> !Integer.valueOf(1).equals(item.getDeletedFlag()))
                .anyMatch(item -> Integer.valueOf(1).equals(item.getLockedFlag()) || Integer.valueOf(1).equals(item.getPdfGeneratedFlag()));
        if (!locked) {
            return;
        }
        record.setSyncStatus("CONFLICT");
        record.setConflictFlag(1);
        record.setUpdatedAt(LocalDateTime.now());
        workOrderRecordMapper.updateById(record);
        createRecordConflict(record, currentUser, conflictType, clientVersion, deviceId, "验收或PDF已锁定，施工记录修改需复核");
        throw new BusinessException(ErrorCode.SYNC_ERROR, "验收或PDF已锁定，施工记录修改需复核");
    }

    private void createRecordConflict(WorkOrderRecord record, CurrentUser currentUser, String conflictType,
            Integer clientVersion, String deviceId, String message) {
        SyncConflict conflict = new SyncConflict();
        conflict.setConflictNo(generateNo("CONF"));
        conflict.setDeviceId(deviceId);
        conflict.setOperatorId(currentUser.getUserId());
        conflict.setModuleType("WORK_RECORD");
        conflict.setEntityType("WORK_ORDER_RECORD");
        conflict.setEntityId(record.getId());
        conflict.setLocalId(record.getLocalId());
        conflict.setServerId(record.getServerId() == null ? record.getId() : record.getServerId());
        conflict.setWorkOrderId(record.getWorkOrderId());
        conflict.setBusinessNo(record.getRecordNo());
        conflict.setBaseVersion(clientVersion);
        conflict.setClientVersion(clientVersion);
        conflict.setServerVersion(record.getVersion());
        conflict.setServerUpdatedAt(record.getUpdatedAt());
        conflict.setConflictType(conflictType);
        conflict.setClientPayload("{\"reason\":\"" + jsonEscape(message) + "\"}");
        conflict.setServerPayload("{\"recordId\":" + record.getId() + ",\"version\":" + record.getVersion() + "}");
        conflict.setDefaultStrategy("KEEP_SERVER");
        conflict.setResolveStatus("PENDING");
        conflict.setCreatedAt(LocalDateTime.now());
        conflict.setUpdatedAt(LocalDateTime.now());
        conflict.setDeletedFlag(0);
        conflict.setCreatedBy(currentUser.getUserId());
        conflict.setUpdatedBy(currentUser.getUserId());
        syncConflictMapper.insert(conflict);
    }

    private String jsonEscape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
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

    private com.offshore.platform.vo.workrecord.WorkOrderRecordVO toWorkRecordVO(Long recordId) {
        return toWorkRecordVO(requireRecord(recordId));
    }

    private com.offshore.platform.vo.workrecord.WorkOrderRecordVO toWorkRecordVO(WorkOrderRecord record) {
        com.offshore.platform.vo.workrecord.WorkOrderRecordVO vo = new com.offshore.platform.vo.workrecord.WorkOrderRecordVO();
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
        vo.attachmentCount = record.getAttachmentCount();
        vo.aiResultCount = record.getAiResultCount();
        vo.recordStatus = record.getRecordStatus();
        vo.submittedAt = record.getSubmittedAt();
        vo.version = record.getVersion();
        vo.syncStatus = record.getSyncStatus();
        vo.deviceId = record.getDeviceId();
        vo.operatorId = record.getOperatorId();
        vo.conflictFlag = record.getConflictFlag();
        vo.createdAt = record.getCreatedAt();
        vo.updatedAt = record.getUpdatedAt();
        vo.details = workOrderRecordDetailMapper.selectAll().stream()
                .filter(item -> record.getId().equals(item.getRecordId()))
                .map(this::toDetailVO)
                .toList();
        vo.checkItems = workOrderCheckItemMapper.selectByRecordId(record.getId()).stream().map(this::toWorkOrderCheckItemVO).toList();
        vo.attachments = workOrderAttachmentMapper.selectAll().stream().filter(item -> record.getId().equals(item.getRecordId())).map(this::toAttachmentVO).toList();
        vo.aiResults = aiResultMapper.selectAll().stream().filter(item -> record.getId().equals(item.getRecordId())).map(this::toAiSummaryVO).toList();
        return vo;
    }

    private WorkOrderRecordDetailVO toDetailVO(WorkOrderRecordDetail detail) {
        WorkOrderRecordDetailVO vo = new WorkOrderRecordDetailVO();
        vo.id = detail.getId();
        vo.serverId = detail.getServerId();
        vo.localId = detail.getLocalId();
        vo.workOrderId = detail.getWorkOrderId();
        vo.recordId = detail.getRecordId();
        vo.detailType = detail.getDetailType();
        vo.detailTitle = detail.getDetailTitle();
        vo.detailContent = detail.getDetailContent();
        vo.stepNo = detail.getStepNo();
        vo.itemCode = detail.getItemCode();
        vo.itemName = detail.getItemName();
        vo.itemValue = detail.getItemValue();
        vo.itemUnit = detail.getItemUnit();
        vo.normalFlag = detail.getNormalFlag();
        vo.abnormalDesc = detail.getAbnormalDesc();
        vo.attachmentRefFlag = detail.getAttachmentRefFlag();
        vo.aiRefFlag = detail.getAiRefFlag();
        vo.sortOrder = detail.getSortOrder();
        vo.version = detail.getVersion();
        vo.updatedAt = detail.getUpdatedAt();
        vo.syncStatus = detail.getSyncStatus();
        vo.conflictFlag = detail.getConflictFlag();
        return vo;
    }

    private WorkOrderCheckItemVO toWorkOrderCheckItemVO(WorkOrderCheckItem item) {
        WorkOrderCheckItemVO vo = new WorkOrderCheckItemVO();
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
