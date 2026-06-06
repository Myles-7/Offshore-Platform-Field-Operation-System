package com.offshore.platform.dto.sync;

import java.util.List;

public class SyncPullRequest {
    /** 设备 ID */
    public String deviceId;
    /** 增量拉取游标，通常为上次同步时间 "yyyy-MM-dd HH:mm:ss" */
    public String cursor;
    /** 上次同步时间，增量拉取的起点 */
    public String lastSyncTime;
    /** 本次拉取最大条数 */
    public Integer limit;
    /** 指定拉取的模块类型列表，为空时拉取全部有权限数据 */
    public List<String> entityTypes;
}
