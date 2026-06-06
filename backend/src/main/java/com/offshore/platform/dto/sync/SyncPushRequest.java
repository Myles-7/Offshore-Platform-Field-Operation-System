package com.offshore.platform.dto.sync;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SyncPushRequest {
    @NotBlank(message = "deviceId不能为空")
    public String deviceId;
    @NotBlank(message = "batchId不能为空")
    public String batchId;
    public LocalDateTime clientTime;
    public String appVersion;
    /** 操作人 ID */
    public Long operatorId;
    public List<SyncPushItem> items = new ArrayList<>();
}
