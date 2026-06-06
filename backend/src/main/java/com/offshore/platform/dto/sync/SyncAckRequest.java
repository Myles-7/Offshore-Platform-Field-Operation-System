package com.offshore.platform.dto.sync;

public class SyncAckRequest {
    /** 设备 ID */
    public String deviceId;
    /** 确认的批次 ID */
    public String batchId;
    /** 确认的同步游标，服务端据此更新 device_info.last_sync_cursor */
    public String cursor;
    /** 确认的最后同步时间 */
    public String lastSyncCursor;
}
