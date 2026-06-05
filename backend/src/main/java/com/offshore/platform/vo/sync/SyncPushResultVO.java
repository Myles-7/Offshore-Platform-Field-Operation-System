package com.offshore.platform.vo.sync;

import java.util.ArrayList;
import java.util.List;

public class SyncPushResultVO {
    public Long taskId;
    public String batchId;
    public Integer successCount;
    public Integer failedCount;
    public Integer conflictCount;
    public List<SyncItemResultVO> items = new ArrayList<>();
}
