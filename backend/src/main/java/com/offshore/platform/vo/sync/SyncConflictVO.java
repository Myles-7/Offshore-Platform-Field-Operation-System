package com.offshore.platform.vo.sync;

public class SyncConflictVO {
    public Long id;
    /** 冲突编号 */
    public String conflictNo;
    /** 模块类型 — SyncModuleType.code */
    public String moduleType;
    /** 实体类型 — 对应数据库表名 */
    public String entityType;
    /** 移动端本地 ID */
    public String localId;
    /** 服务端 ID */
    public Long serverId;
    /** 关联工单 ID */
    public Long workOrderId;
    /** 冲突类型 — ConflictType.code */
    public String conflictType;
    /** 解决状态: PENDING / RESOLVED / IGNORED */
    public String resolveStatus;
    /** 处理策略 — ResolveStrategy.code */
    public String resolveStrategy;
    /** 处理时间 */
    public String resolveTime;
    /** 处理说明 */
    public String resolveComment;
    /** 移动端提交的数据 (JSON string) */
    public String clientPayload;
    /** 服务端当前数据 (JSON string) */
    public String serverPayload;
    /** 来源设备 ID */
    public String deviceId;
    /** 操作人 ID */
    public Long operatorId;
    /** 客户端版本号 */
    public Integer clientVersion;
    /** 服务端版本号 */
    public Integer serverVersion;
}
