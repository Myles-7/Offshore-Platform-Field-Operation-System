package com.offshore.platform.vo.sync;

public class SyncItemResultVO {
    /** 移动端本地 ID */
    public String localId;
    /** 服务端主键 */
    public Long serverId;
    /** 服务端当前版本号 */
    public Integer version;
    /** 同步状态 — SyncStatus.code */
    public String syncStatus;
    /** 冲突 ID，仅冲突时非空 */
    public Long conflictId;
    /** 结果消息 */
    public String message;
    /** 实体类型 — 与请求的 entityType 对应 */
    public String entityType;
    /** 模块类型 — SyncModuleType.code */
    public String moduleType;
    /** 动作类型 — SyncActionType.code */
    public String actionType;
}
