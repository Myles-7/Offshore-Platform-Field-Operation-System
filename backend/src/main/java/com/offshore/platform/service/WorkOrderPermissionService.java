package com.offshore.platform.service;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.entity.WorkOrder;

public interface WorkOrderPermissionService {
    WorkOrder requireReadable(Long workOrderId, CurrentUser currentUser);

    WorkOrder requirePcReadable(Long workOrderId, CurrentUser currentUser);

    WorkOrder requireMobileReadable(Long workOrderId, CurrentUser currentUser);

    boolean canRead(WorkOrder workOrder, CurrentUser currentUser);

    boolean canReadMobile(WorkOrder workOrder, CurrentUser currentUser);
}
