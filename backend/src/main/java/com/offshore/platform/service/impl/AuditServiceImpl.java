package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.SyncConflict;
import com.offshore.platform.entity.SyncLog;
import com.offshore.platform.entity.SyncTask;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderStatusLog;
import com.offshore.platform.entity.WorkOrderVersionLog;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.SyncConflictMapper;
import com.offshore.platform.mapper.SyncLogMapper;
import com.offshore.platform.mapper.SyncTaskMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderStatusLogMapper;
import com.offshore.platform.mapper.WorkOrderVersionLogMapper;
import com.offshore.platform.service.AuditService;
import com.offshore.platform.service.DataScopeService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {
    private final OperationLogMapper operationLogMapper;
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderStatusLogMapper statusLogMapper;
    private final WorkOrderVersionLogMapper versionLogMapper;
    private final SyncTaskMapper syncTaskMapper;
    private final SyncLogMapper syncLogMapper;
    private final SyncConflictMapper syncConflictMapper;
    private final DataScopeService dataScopeService;

    public AuditServiceImpl(OperationLogMapper operationLogMapper, WorkOrderMapper workOrderMapper,
            WorkOrderStatusLogMapper statusLogMapper, WorkOrderVersionLogMapper versionLogMapper,
            SyncTaskMapper syncTaskMapper, SyncLogMapper syncLogMapper, SyncConflictMapper syncConflictMapper,
            DataScopeService dataScopeService) {
        this.operationLogMapper = operationLogMapper;
        this.workOrderMapper = workOrderMapper;
        this.statusLogMapper = statusLogMapper;
        this.versionLogMapper = versionLogMapper;
        this.syncTaskMapper = syncTaskMapper;
        this.syncLogMapper = syncLogMapper;
        this.syncConflictMapper = syncConflictMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public List<OperationLog> operationLogs() {
        requireAdmin(CurrentUserContext.require());
        return operationLogMapper.selectAll().stream()
                .sorted(Comparator.comparing(OperationLog::getOperationTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public OperationLog operationLog(Long id) {
        requireAdmin(CurrentUserContext.require());
        OperationLog log = operationLogMapper.selectById(id);
        if (log == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Operation log not found");
        }
        return log;
    }

    @Override
    public List<Map<String, Object>> workOrderAuditTrail(Long workOrderId) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Work order not found");
        }
        if (!dataScopeService.canAccessAll(user) && !dataScopeService.canAccessProject(user, workOrder.getProjectId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for work-order audit trail");
        }
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (OperationLog log : operationLogMapper.selectAll()) {
            boolean businessMatch = String.valueOf(workOrderId).equals(log.getBusinessId());
            boolean pathMatch = log.getRequestPath() != null && log.getRequestPath().contains("/work-orders/" + workOrderId);
            if (businessMatch || pathMatch) {
                timeline.add(item("OPERATION_LOG", log.getOperationTime(), log.getOperationType(), log));
            }
        }
        for (WorkOrderStatusLog log : statusLogMapper.selectByWorkOrderId(workOrderId)) {
            timeline.add(item("WORK_ORDER_STATUS_LOG", log.getOperationTime(), log.getOperationType(), log));
        }
        for (WorkOrderVersionLog log : versionLogMapper.selectAll()) {
            if (workOrderId.equals(log.getWorkOrderId())) {
                timeline.add(item("WORK_ORDER_VERSION_LOG", log.getCreatedAt(), log.getChangeType(), log));
            }
        }
        for (SyncConflict conflict : syncConflictMapper.selectAll()) {
            if (workOrderId.equals(conflict.getWorkOrderId())) {
                timeline.add(item("SYNC_CONFLICT", conflict.getCreatedAt(), conflict.getConflictType(), conflict));
            }
        }
        return timeline.stream()
                .sorted(Comparator.comparing(entry -> (LocalDateTime) entry.get("eventTime"), Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    public List<Map<String, Object>> syncAuditTrail() {
        requireAdmin(CurrentUserContext.require());
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (SyncTask task : syncTaskMapper.selectAll()) {
            timeline.add(item("SYNC_TASK", task.getCreatedAt(), task.getSyncDirection(), task));
        }
        for (SyncLog log : syncLogMapper.selectAll()) {
            timeline.add(item("SYNC_LOG", log.getCreatedAt(), log.getSyncStatus(), log));
        }
        for (SyncConflict conflict : syncConflictMapper.selectAll()) {
            timeline.add(item("SYNC_CONFLICT", conflict.getCreatedAt(), conflict.getResolveStatus(), conflict));
        }
        for (OperationLog log : operationLogMapper.selectAll()) {
            if ("SYNC".equals(log.getModuleName()) || (log.getRequestPath() != null && log.getRequestPath().contains("/sync/"))) {
                timeline.add(item("OPERATION_LOG", log.getOperationTime(), log.getOperationType(), log));
            }
        }
        return timeline.stream()
                .sorted(Comparator.comparing(entry -> (LocalDateTime) entry.get("eventTime"), Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private void requireAdmin(CurrentUser user) {
        if (!dataScopeService.canAccessAll(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Audit permission required");
        }
    }

    private Map<String, Object> item(String source, LocalDateTime time, String action, Object payload) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("source", source);
        item.put("eventTime", time);
        item.put("action", action);
        item.put("payload", payload);
        return item;
    }
}
