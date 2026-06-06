# 离线同步 — 后端 API 完整参考

> 协议版本: 1.0 | 更新日期: 2026-06-06

## 基础约定

- Base URL: `http://localhost:8080`
- 认证: `Authorization: Bearer <token>`（需要 `mobileEnabled=1`）
- 时间格式: `yyyy-MM-dd HH:mm:ss`
- 设备验证: `/api/sync/**` 需要 `deviceId`（JSON body 或 header `X-Device-Id`）
- 统一响应: `ApiResponse<T>` = `{ code, message, data, timestamp, traceId }`

## 枚举

### SyncStatus

| code | 中文 |
|------|------|
| LOCAL_ONLY | 仅本地 |
| PENDING | 待同步 |
| SYNCING | 同步中 |
| SYNCED | 已同步 |
| FAILED | 同步失败 |
| CONFLICT | 冲突待复核 |
| DELETED | 已删除 |
| IGNORED | 已忽略 |

### SyncModuleType

| code | 对应表 | 方向 |
|------|--------|------|
| WORK_ORDER | work_order | S→M pull |
| WORK_RECORD | work_order_record | M→S push |
| ATTACHMENT_META | work_order_attachment | M→S push |
| SIGNATURE | work_order_signature | M→S push |
| ACCEPTANCE | work_order_acceptance | M→S push |
| PDF | work_order_pdf | M→S push + S→M pull |
| MATERIAL_USAGE | work_order_material_usage | M→S push |
| QUALIFICATION | employee_certificate | S→M pull |
| AI_RESULT | ai_result | M→S push + S→M pull |
| KNOWLEDGE | knowledge_case | S→M pull |
| DEVICE | device_info | M→S register/heartbeat |
| USER_PROFILE | sys_user | S→M pull |

### SyncActionType

| code | 说明 |
|------|------|
| CREATE | 新增 |
| UPDATE | 更新 |
| DELETE | 逻辑删除 |

### ConflictType

| code | 说明 |
|------|------|
| VERSION_CONFLICT | 版本冲突 |
| FIELD_CONFLICT | 字段冲突 |
| UPDATE_AFTER_DELETE | 删除后更新 |
| DELETE_AFTER_UPDATE | 更新后删除 |
| DUPLICATE_CREATE | 重复创建 |
| PERMISSION_CONFLICT | 权限冲突 |
| FILE_META_CONFLICT | 文件元数据冲突 |
| ACCEPTANCE_LOCKED_CONFLICT | 验收锁定冲突 |

### ResolveStrategy

| code | 说明 |
|------|------|
| KEEP_SERVER | 保留服务器版本 |
| KEEP_CLIENT | 保留客户端版本 |
| MANUAL_MERGE | 人工合并 |
| IGNORE_CLIENT | 忽略客户端变更 |

## API 接口

### 1. 设备注册

```
POST /api/sync/device/register
```

请求:
```json
{
  "deviceId": "android-001",
  "deviceName": "Xiaomi 14",
  "platform": "ANDROID",
  "osVersion": "14",
  "appVersion": "1.0.0",
  "manufacturer": "Xiaomi",
  "model": "14 Pro"
}
```

规则: 同一用户同一设备重复注册幂等；设备被禁用后拒绝同步。

### 2. 设备心跳

```
POST /api/sync/device/heartbeat
```

请求: 同 `DeviceRegisterRequest`，更新 `lastHeartbeatTime`。

### 3. 增量拉取 (Pull)

```
POST /api/sync/pull
```

请求:
```json
{
  "deviceId": "android-001",
  "cursor": "2026-06-06 10:00:00",
  "lastSyncTime": "2026-06-06 10:00:00",
  "limit": 200,
  "entityTypes": ["WORK_ORDER", "WORK_RECORD"]
}
```

响应:
```json
{
  "code": 200,
  "data": {
    "cursor": "2026-06-06 11:00:00",
    "serverTime": "2026-06-06 11:00:00",
    "nextCursor": "2026-06-06 10:59:00",
    "hasMore": false,
    "ackRequired": true,
    "items": [
      {
        "moduleType": "WORK_ORDER",
        "entityType": "work_order",
        "serverId": 1,
        "localId": "local-wo-001",
        "version": 2,
        "updatedAt": "2026-06-06 10:59:00",
        "deletedFlag": 0,
        "operatorId": 1,
        "deviceId": "android-001",
        "payload": { "workOrderNo": "WO-001", "workTitle": "..." }
      }
    ]
  }
}
```

规则:
- cursor 优先 → 回退到 lastSyncTime → 默认 epoch
- entityTypes 为空则拉取全部有权限的核心模块
- 按 updated_at ASC, id ASC 排序，支持 limit 分页
- 维修工只能拉自己的工单；项目经理按项目范围
- 每个 item 包含 moduleType/entityType/serverId/localId/version/updatedAt/deletedFlag/operatorId/deviceId/payload

### 4. 增量上传 (Push)

```
POST /api/sync/push
```

请求:
```json
{
  "deviceId": "android-001",
  "batchId": "batch-20260606-001",
  "clientTime": "2026-06-06 10:00:00",
  "appVersion": "1.0.0",
  "operatorId": 1,
  "items": [
    {
      "moduleType": "WORK_ORDER",
      "entityType": "WORK_ORDER_RECORD",
      "actionType": "CREATE",
      "localId": "local-rec-001",
      "version": 0,
      "payload": { "workOrderId": 1, "constructionDesc": "..." },
      "fileId": null,
      "checksum": null,
      "deletedFlag": 0,
      "syncStatus": "PENDING",
      "deviceId": "android-001",
      "operatorId": 1,
      "conflictFlag": 0
    }
  ]
}
```

响应:
```json
{
  "code": 200,
  "data": {
    "taskId": 1,
    "batchId": "batch-20260606-001",
    "successCount": 1,
    "failedCount": 0,
    "conflictCount": 0,
    "items": [
      {
        "localId": "local-rec-001",
        "serverId": 10,
        "version": 1,
        "syncStatus": "SYNCED",
        "entityType": "WORK_ORDER_RECORD",
        "moduleType": "WORK_ORDER",
        "actionType": "CREATE",
        "message": "Created"
      }
    ]
  }
}
```

规则:
- 同一 batchId+deviceId 重复提交幂等
- 每条独立处理，部分成功
- 大文件不进 payload，只传 fileId
- version 冲突写入 sync_conflict
- 验收/PDF 锁定后拒绝普通修改

### 5. 同步确认 (Ack)

```
POST /api/sync/ack
```

请求:
```json
{
  "deviceId": "android-001",
  "batchId": "pull-ack-xxx",
  "cursor": "2026-06-06 10:59:00",
  "lastSyncCursor": "2026-06-06 10:59:00"
}
```

### 6. PC 冲突复核

```
GET  /api/admin/sync/conflicts
GET  /api/admin/sync/conflicts/{id}
POST /api/admin/sync/conflicts/{id}/resolve
```

处理请求:
```json
{
  "resolveStrategy": "KEEP_CLIENT",
  "finalPayload": "{...}",
  "resolveComment": "采用移动端版本"
}
```

### 7. 同步任务/日志

```
GET /api/sync/tasks
GET /api/sync/logs
```

## 冲突检测规则

| 条件 | 冲突类型 |
|------|----------|
| client.version < server.version | VERSION_CONFLICT |
| 客户端更新已删除记录 | UPDATE_AFTER_DELETE |
| 客户端删除但服务端已更新 | DELETE_AFTER_UPDATE |
| 验收锁定后修改 | ACCEPTANCE_LOCKED_CONFLICT |
| 权限不满足 | PERMISSION_CONFLICT |

## 文件同步顺序

```
1. 文件本体 → POST /api/files/upload (multipart, chunked for >20MB)
2. 文件元数据 → POST /api/sync/push (fileId ONLY, no binary)
3. 服务端 → 验证 fileId 存在 → 写 file_storage + work_order_attachment
```

## 权限规则

- 维修工只能操作自己的工单
- 验收人员只能操作待验收工单
- 项目经理只能操作所属项目
- 系统管理员全部权限
- 文件下载/预览/删除需校验业务归属
