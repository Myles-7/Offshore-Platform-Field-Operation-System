package com.offshore.platform.vo.sync;

import java.util.ArrayList;
import java.util.List;

public class SyncConflictDetailVO extends SyncConflictVO {
    public String oldPayload;
    public String finalPayload;
    public String conflictFields;
    public List<String> availableStrategies = new ArrayList<>();
}
