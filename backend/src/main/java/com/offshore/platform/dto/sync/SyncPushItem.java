package com.offshore.platform.dto.sync;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public class SyncPushItem {
    public String moduleType;
    public String entityType;
    public String actionType;
    public String localId;
    public Long serverId;
    public Integer version;
    public LocalDateTime updatedAt;
    public JsonNode payload;
    public String fileId;
    public String checksum;
}
