package com.offshore.platform.vo.sync;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Structured individual pull item — replaces raw Object in SyncPullVO.items.
 *
 * Backend returns this structure to mobile for each changed entity.
 */
public class SyncPullItemVO {
    /** 模块类型 — SyncModuleType.code */
    public String moduleType;
    /** 实体类型 — 对应数据库表名 */
    public String entityType;
    /** 服务端主键 */
    public Long serverId;
    /** 移动端本地 ID（首次创建时返回，方便移动端回写） */
    public String localId;
    /** 服务端当前版本号 */
    public Integer version;
    /** 服务端更新时间 "yyyy-MM-dd HH:mm:ss" */
    public String updatedAt;
    /** 逻辑删除标记: 0 正常, 1 已删除 */
    public Integer deletedFlag;
    /** 操作人 ID */
    public Long operatorId;
    /** 来源设备 ID */
    public String deviceId;
    /** 业务对象 JSON，不含大文件二进制 */
    public JsonNode payload;
}