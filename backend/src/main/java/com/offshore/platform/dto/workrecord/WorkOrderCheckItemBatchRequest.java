package com.offshore.platform.dto.workrecord;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class WorkOrderCheckItemBatchRequest {
    @Valid
    @NotEmpty(message = "检查项不能为空")
    public List<WorkOrderCheckItemRequest> items = new ArrayList<>();
}
