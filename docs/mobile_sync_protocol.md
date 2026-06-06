# 移动端同步协议参考

> 面向 Android 移动端 + Room + WorkManager 实现

## 本地数据库

全部位于 `mobile/app/.../data/local/`:
- Room Entity: 17 个 (local_work_order, local_work_order_record, ...)
- Room DAO: 16 个
- AppDatabase: 1 个 (version=2, fallbackToDestructiveMigration)
- 所有同步实体包含: localId / serverId / version / syncStatus / deviceId / operatorId / deletedFlag / conflictFlag

### local_sync_queue

离线核心表 — 每次离线操作先入此队列:
- moduleType / entityType / actionType / localId / serverId / workOrderId
- payloadJson / fileId / checksum
- syncStatus / priority / retryCount / maxRetryCount / nextRetryTime

### local_sync_checkpoint

记录每个模块最近成功的 pull cursor:
- moduleType / lastSyncTime / lastServerCursor / lastSuccessTime

## 同步 DTO (ApiDtos.kt)

| DTO | 后端对应 |
|-----|----------|
| SyncPushItem | dto/sync/SyncPushItem |
| SyncPushRequest | dto/sync/SyncPushRequest |
| SyncPushResponse | vo/sync/SyncPushResultVO |
| SyncItemResult | vo/sync/SyncItemResultVO |
| SyncPullRequest | dto/sync/SyncPullRequest |
| SyncPullItemVO | vo/sync/SyncPullItemVO |
| SyncPullResponse | vo/sync/SyncPullVO |
| SyncAckRequest | dto/sync/SyncAckRequest |

## SyncRepository 流程

```
fullSync():
  1. pushLocalChanges()        → local_sync_queue → /api/sync/push
  2. pullServerChanges()       → /api/sync/pull  → 写入本地 Room
  3. ack()                     → /api/sync/ack   → 更新 checkpoint
  4. 记录 local_sync_log
```

## 网络恢复自动同步

- `NetworkMonitor`: ConnectivityManager.NetworkCallback → StateFlow<NetworkStatus>
- `SyncViewModel.observeNetworkRecovery()`: DISCONNECTED→CONNECTED 时自动 triggerSync()
- Debounce: 30 秒内不重复触发
- 前提: 必须已登录 + deviceId 已生成

## Worker

### SyncWorker (15min 周期)
- 约束: NetworkType.CONNECTED
- 退避: EXPONENTIAL, 30s
- 手动触发: `SyncWorker.enqueueOneTime()`

### UploadWorker (15min 周期)
- 约束: NetworkType.CONNECTED
- 逻辑: `FileUploadRepository.retryFailedUploads()`
- 大文件 (>20MB): ChunkUploadManager 分片上传

## 同步状态展示

### SyncStatusBadge 组件
- 显示各实体 syncStatus
- FAILED: 红色 + 重试按钮
- CONFLICT: 红色 + "等待后台复核"

### SyncCenterScreen
- 网络状态
- 待同步/失败/冲突数量
- 文件待上传数量
- 最近同步时间
- 手动同步按钮
- 最近同步记录列表

## 枚举 (domain/enums/)

全部与后端/PC 前端对齐:
- SyncStatus: LOCAL_ONLY/PENDING/SYNCING/SYNCED/FAILED/CONFLICT/DELETED/IGNORED
- SyncActionType: CREATE/UPDATE/DELETE
- ConflictType: VERSION_CONFLICT/FIELD_CONFLICT/.../ACCEPTANCE_LOCKED_CONFLICT
- NetworkStatus: CONNECTED/DISCONNECTED/METERED/UNKNOWN
- UploadStatus: PENDING/UPLOADING/UPLOADED/FAILED
