# Android 移动端接口

所有 `/api/mobile/**` 接口需要 `Authorization: Bearer <token>`，账号必须 `mobileEnabled=1`。服务端从 token 解析当前用户，不使用前端传入的 `userId`。

## 工单

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/mobile/work-orders` | 本人工单列表，适合离线缓存 |
| GET | `/api/mobile/work-orders/{id}` | 工单详情 |
| POST | `/api/mobile/work-orders/{id}/accept` | 接单 |
| POST | `/api/mobile/work-orders/{id}/start` | 开始施工 |
| POST | `/api/mobile/work-orders/{id}/feedback` | 现场反馈 |
| POST | `/api/mobile/work-orders/{id}/submit-acceptance` | 提交验收 |
| GET | `/api/mobile/work-orders/{id}/materials` | 工单物料 |
| GET | `/api/mobile/work-orders/{id}/qualification-check` | 工单资质校验 |

移动端工单列表返回包含: `id`, `serverId`, `workOrderNo`, `projectName`, `workTitle`, `workLocation`, `workContentSummary`, `status`, `priority`, `plannedStartTime`, `plannedEndTime`, `version`, `updatedAt`, `syncStatus`, `conflictFlag`。

## 施工记录

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/mobile/work-orders/{workOrderId}/records` | 新增施工记录，可带 `localId` |
| PUT | `/api/mobile/work-orders/{workOrderId}/records/{recordId}` | 更新施工记录，比较 `version` |
| GET | `/api/mobile/work-orders/{workOrderId}/records` | 施工记录列表 |
| POST | `/api/mobile/work-records/{recordId}/check-items` | 批量提交检查项 |

`MobileWorkRecordRequest`: `localId`, `version`, `recordType`, `constructionTime`, `constructionDesc`, `siteCondition`, `abnormalFlag`, `abnormalDesc`, `weather`, `temperature`, `humidity`, `locationName`, `latitude`, `longitude`, `altitude`, `deviceId`, `remark`。

## 附件、签名、验收、PDF

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/mobile/work-orders/{workOrderId}/attachments` | 绑定工单附件元数据 |
| GET | `/api/mobile/work-orders/{workOrderId}/attachments` | 工单附件元数据 |
| POST | `/api/mobile/work-orders/{workOrderId}/signatures` | 签名元数据 |
| POST | `/api/mobile/work-orders/{workOrderId}/acceptance` | 验收记录 |
| POST | `/api/mobile/work-orders/{workOrderId}/pdf/metadata` | PDF 元数据 |

附件绑定请求 `AttachmentBindRequest`: `localId`, `recordId`, `fileId`, `attachmentType`, `attachmentName`, `attachmentDesc`, `businessScene`, `captureTime`, `latitude`, `longitude`, `locationName`, `watermarkFlag`, `watermarkText`, `durationSeconds`, `mediaWidth`, `mediaHeight`, `deviceId`, `remark`。

## 物料与资质

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/mobile/work-orders/{workOrderId}/material-requirements` | 工单所需物料 |
| POST | `/api/mobile/work-orders/{workOrderId}/material-usage` | 记录实际使用物料 |
| GET | `/api/mobile/my/qualification-status` | 当前用户资质状态 |

## AI 结果

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/mobile/work-orders/{workOrderId}/ai-results` | 工单 AI 结果 |

## 离线缓存字段

移动端可缓存实体应关注: `id`, `serverId`, `localId`, `version`, `updatedAt`, `syncStatus`。附件和文件只同步元数据及 `fileId`，不通过业务接口返回大文件二进制。
