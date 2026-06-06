package com.offshore.platform.dto.sync;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public class SyncPushItem {
    /** 模块类型 — SyncModuleType.code */
    public String moduleType;
    /** 实体类型 — 对应数据库表名 */
    public String entityType;
    /** 动作类型 — SyncActionType.code: CREATE/UPDATE/DELETE */
    public String actionType;
    /** 移动端本地唯一 ID */
    public String localId;
    /** 服务端主键，首次创建时为空 */
    public Long serverId;
    /** 客户端基于的版本号 */
    public Integer version;
    /** 客户端更新时间 */
    public LocalDateTime updatedAt;
    /** 业务对象 JSON，不含大文件二进制 */
    public JsonNode payload;
    /** 文件 ID，文件先通过 /api/files/upload 上传后获得 */
    public String fileId;
    /** 数据校验值，可选 */
    public String checksum;

    // ---- 同步追踪字段 (与移动端 LocalSyncQueueEntity 对齐) ----

    /** 逻辑删除标记: 0 正常, 1 已删除 */
    public Integer deletedFlag;
    /** 当前同步状态 — SyncStatus.code */
    public String syncStatus;
    /** 来源设备 ID */
    public String deviceId;
    /** 操作人 ID */
    public Long operatorId;
    /** 冲突标记: 0 无冲突, 1 存在冲突 */
    public Integer conflictFlag;
}
