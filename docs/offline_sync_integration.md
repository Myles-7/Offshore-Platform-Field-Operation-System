# 离线同步 — 整体集成说明

> 协议版本: 1.0 | 更新日期: 2026-06-06

## 架构概览

```
移动端 (Room/SQLite)  ←→  sync/pull, sync/push, sync/ack  →  Spring Boot → MySQL
    ↕                              ↕
文件系统 (MinIO/本地)  ←→  multipart upload  →  FileController
    ↕
PC 后台 (Vue3)  →  /api/admin/sync/conflicts →  冲突复核
```

## 核心原则

1. **本地优先 (Local First)**: 用户操作先写本地 Room → 入 `local_sync_queue` → 页面立即反馈成功
2. **增量同步**: pull 基于 `updated_at > cursor`；push 逐条处理支持部分成功
3. **失败重试**: SyncWorker 15min 周期 + 指数退避；最多 5 次重试
4. **冲突复核**: 冲突不丢弃，写入 `sync_conflict`，PC 后台人工复核
5. **变更追溯**: `sync_task` / `sync_log` / `sync_conflict` / `work_order_version_log` / `operation_log` 全链路

## 同步状态机

```
LOCAL_ONLY → PENDING → SYNCING → SYNCED
                ↓          ↓
              FAILED    CONFLICT
                ↓          ↓
           (重试PENDING)  (PC复核→SYNCED)
```

## SyncableEntity 接口

所有参与同步的实体实现 `com.offshore.platform.common.sync.SyncableEntity`：

| 方法 | 说明 |
|------|------|
| getId/setId | 主键 |
| getLocalId/setLocalId | 移动端本地 ID |
| getServerId/setServerId | 服务端 ID 映射 |
| getVersion/setVersion | 乐观锁版本号 |
| getSyncStatus/setSyncStatus | 同步状态 |
| getDeviceId/setDeviceId | 来源设备 |
| getOperatorId/setOperatorId | 操作人 |
| getCreatedAt/setCreatedAt | 创建时间 |
| getUpdatedAt/setUpdatedAt | 更新时间 |
| getDeletedFlag/setDeletedFlag | 逻辑删除 |
| getConflictFlag/setConflictFlag | 冲突标记 (默认 0) |

已实现: WorkOrder, WorkOrderRecord, WorkOrderAttachment, WorkOrderSignature, WorkOrderAcceptance, WorkOrderPdf, WorkOrderMaterialUsage, AiResult, KnowledgeCase

## 文件附件同步

1. 文件本体: `POST /api/files/upload` (multipart, >20MB 走分片)
2. 元数据同步: `POST /api/sync/push` (item.fileId + item.payload 含文件名/类型/大小)
3. 文件不进 MySQL（仅存 file_storage 元数据 + file_path）
4. 删除附件只做逻辑删除（不删真实文件）

## 冲突处理

### 服务端检测
- UPDATE 时 `client.version < server.version` → VERSION_CONFLICT
- DELETE 时服务端已更新 → DELETE_AFTER_UPDATE
- 客户端更新已删除记录 → UPDATE_AFTER_DELETE
- 验收/PDF 锁定 → ACCEPTANCE_LOCKED_CONFLICT

### PC 复核
- KEEP_SERVER: 保留服务端，只更新冲突状态
- KEEP_CLIENT: 用 client_payload 更新业务表 version+1
- MANUAL_MERGE: 用 resolvedPayload 更新业务表
- IGNORE_CLIENT: 忽略冲突，记录原因

### 移动端回传
- PC 复核后业务表 updated_at 更新 + version+1
- 下次 pull 返回最终版本
- 移动端 local_conflict_hint 更新 resolveStatus

## 移动端同步链路

1. `NetworkMonitor` 监听连接状态
2. `SyncViewModel` 在 DISCONNECTED→CONNECTED 时自动触发（30s debounce）
3. 先 `UploadWorker` 上传失败文件
4. 再 `SyncWorker.pushLocalChanges()` → `pullServerChanges()` → `ack()`
5. push 成功后回写 `serverId/version/syncStatus` 到本地 Room
6. pull 后写各业务表 + 更新 `local_sync_checkpoint`

## 已知限制

1. PC 前端项目级权限过滤待进一步细化（当前按角色全局控制）
2. 移动端 AI 离线推理引擎为占位实现（实际需集成 TFLite/ONNX）
3. PDF 生成为元数据同步（实际 PDF 渲染需服务端生成引擎）
4. 分片上传恢复依赖 Mobile ChunkUploadManager（未实现断点续传 session 持久化）
5. 端到端真机联调待验证（自动化测试覆盖 H2 集成测试）

## 后续优化建议

1. WebSocket 推送冲突处理结果到移动端（替代 poll 式 pull）
2. 移动端本地 SQLite FTS 全文搜索
3. 同步压缩（gzip request body）
4. 增量 pull 改为基于 binlog 时间戳的更精确 cursor
5. sync_conflict 增加字段级差异自动合并规则
