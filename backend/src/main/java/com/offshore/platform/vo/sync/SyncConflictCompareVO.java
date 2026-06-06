package com.offshore.platform.vo.sync;

import java.util.ArrayList;
import java.util.List;

public class SyncConflictCompareVO {
    public Long conflictId;
    public String conflictNo;
    public String entityType;
    public Long workOrderId;
    public String businessNo;
    public List<ConflictFieldCompareVO> fields = new ArrayList<>();
}
