package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.WorkOrderPermissionService;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderPermissionServiceImpl implements WorkOrderPermissionService {
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderAssignmentMapper assignmentMapper;
    private final DataScopeService dataScopeService;

    public WorkOrderPermissionServiceImpl(WorkOrderMapper workOrderMapper, WorkOrderAssignmentMapper assignmentMapper,
            DataScopeService dataScopeService) {
        this.workOrderMapper = workOrderMapper;
        this.assignmentMapper = assignmentMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public WorkOrder requireReadable(Long workOrderId, CurrentUser currentUser) {
        WorkOrder workOrder = requireWorkOrder(workOrderId);
        if (!canRead(workOrder, currentUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for work order");
        }
        return workOrder;
    }

    @Override
    public WorkOrder requirePcReadable(Long workOrderId, CurrentUser currentUser) {
        WorkOrder workOrder = requireWorkOrder(workOrderId);
        if (!dataScopeService.canAccessAll(currentUser)
                && !dataScopeService.canAccessProject(currentUser, workOrder.getProjectId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for project work order");
        }
        return workOrder;
    }

    @Override
    public WorkOrder requireMobileReadable(Long workOrderId, CurrentUser currentUser) {
        WorkOrder workOrder = requireWorkOrder(workOrderId);
        if (!canReadMobile(workOrder, currentUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for mobile work order");
        }
        return workOrder;
    }

    @Override
    public boolean canRead(WorkOrder workOrder, CurrentUser currentUser) {
        if (workOrder == null || currentUser == null) {
            return false;
        }
        return dataScopeService.canAccessAll(currentUser)
                || dataScopeService.canAccessProject(currentUser, workOrder.getProjectId())
                || canReadMobile(workOrder, currentUser);
    }

    @Override
    public boolean canReadMobile(WorkOrder workOrder, CurrentUser currentUser) {
        if (workOrder == null || currentUser == null) {
            return false;
        }
        if (currentUser.getUserId().equals(workOrder.getMaintainerId())
                || currentUser.getUserId().equals(workOrder.getLeaderId())) {
            return true;
        }
        return assignmentMapper.selectByWorkOrderId(workOrder.getId()).stream()
                .map(WorkOrderAssignment::getAssigneeId)
                .anyMatch(currentUser.getUserId()::equals);
    }

    private WorkOrder requireWorkOrder(Long workOrderId) {
        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Work order not found");
        }
        return workOrder;
    }
}
