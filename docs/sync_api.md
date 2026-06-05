# 离线同步接口

所有 `/api/sync/**` 接口需要移动端 token。GET 接口通过 `X-Device-Id` 或 `deviceId` 查询参数校验设备；JSON 接口在请求体传 `deviceId`。

## 接口清单

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/sync/device/register` | 设备注册，按 `deviceId` 幂等 |
| POST | `/api/sync/device/heartbeat` | 设备心跳 |
| POST | `/api/sync/pull` | 拉取增量 |
| POST | `/api/sync/push` | 推送本地变更 |
| POST | `/api/sync/ack` | 确认客户端已写入本地 |
| GET | `/api/sync/tasks` | 当前用户同步任务 |
| GET | `/api/sync/logs` | 当前用户同步日志 |

## 请求字段

`SyncPushRequest`: `deviceId`, `batchId`, `clientTime`, `appVersion`, `items`。

`SyncPushItem`: `moduleType`, `entityType`, `actionType`, `localId`, `serverId`, `version`, `updatedAt`, `payload`, `fileId`, `checksum`。

支持 `entityType`: `WORK_ORDER`, `WORK_ORDER_RECORD`, `WORK_ORDER_ATTACHMENT`, `FILE_STORAGE`, `WORK_ORDER_SIGNATURE`, `WORK_ORDER_ACCEPTANCE`, `WORK_ORDER_PDF`, `WORK_ORDER_MATERIAL_USAGE`, `AI_RESULT`, `AI_DEFECT_BOX`。

## sync/pull 示例

```json
{
  "deviceId": "android-001",
  "lastSyncTime": "2026-06-05 10:00:00",
  "entityTypes": ["WORK_ORDER", "WORK_ORDER_RECORD"]
}
```

响应:

```json
{
  "code": 200,
  "data": {
    "serverTime": "2026-06-05 16:00:00",
    "items": [
      {
        "entityType": "WORK_ORDER",
        "serverId": 1,
        "version": 2,
        "updatedAt": "2026-06-05 15:59:00",
        "payload": {}
      }
    ]
  }
}
```

## sync/push 示例

```json
{
  "deviceId": "android-001",
  "batchId": "batch-20260605-001",
  "clientTime": "2026-06-05 16:00:00",
  "appVersion": "1.0.0",
  "items": [
    {
      "moduleType": "WORK_ORDER",
      "entityType": "WORK_ORDER_RECORD",
      "actionType": "CREATE",
      "localId": "local-rec-001",
      "version": 1,
      "payload": {
        "workOrderId": 1,
        "recordType": "DAILY",
        "constructionDesc": "现场施工记录"
      }
    }
  ]
}
```

响应:

```json
{
  "code": 200,
  "data": {
    "batchId": "batch-20260605-001",
    "successCount": 1,
    "failedCount": 0,
    "conflictCount": 0,
    "items": [
      {
        "localId": "local-rec-001",
        "serverId": 10,
        "resultStatus": "SUCCESS"
      }
    ]
  }
}
```

## sync/ack 示例

```json
{
  "deviceId": "android-001",
  "batchId": "pull-batch-001",
  "itemIds": [1, 2, 3]
}
```

响应: `ApiResponse<Void>`。

## 冲突规则

`client.version < server.version`、客户端更新已删除记录、验收锁定后修改关键字段等会写入 `sync_conflict`。后台通过 `/api/admin/sync/conflicts/**` 人工处理，策略包括 `KEEP_SERVER`, `KEEP_CLIENT`, `MANUAL_MERGE`, `IGNORE_CLIENT`。

大文件不得放入 `sync/push`，只同步业务元数据和 `fileId`。
