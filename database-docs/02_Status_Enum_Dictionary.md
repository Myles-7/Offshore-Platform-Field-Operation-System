# Status & Enum Dictionary

> String codes below must be shared by backend, PC frontend, Android mobile, offline sync, and AI modules.

## 1. Work Order State Machine `work_order.status`

| Stored Value | Display Text | Meaning | Typical Next States |
|---|---|---|---|
| `PENDING_ASSIGN` | 待派工 | Created but not assigned. | `ASSIGNED`, `CLOSED` |
| `ASSIGNED` | 已派工 | Assigned to maintainer; visible on mobile. | `IN_PROGRESS`, `REJECTED`, `CLOSED` |
| `IN_PROGRESS` | 施工中 | Maintainer has started field work. | `PENDING_ACCEPTANCE`, `REJECTED`, `CLOSED` |
| `PENDING_ACCEPTANCE` | 待验收 | Construction submitted for acceptance. | `COMPLETED`, `REJECTED` |
| `COMPLETED` | 已完成 | Acceptance passed and closed-loop completed. | `CLOSED` |
| `REJECTED` | 已驳回 | Acceptance or review rejected; rework required. | `IN_PROGRESS`, `CLOSED` |
| `CLOSED` | 已关闭 | Manually closed and no longer active. | None |

## 2. Work Order Flow Operation `work_order_status_log.operation_type`

| Code | Display | Meaning |
|---|---|---|
| `CREATE` | 创建 | Create work order. |
| `ASSIGN` | 派工 | Assign maintainer or acceptance user. |
| `START` | 开始施工 | Mobile starts field work. |
| `SUBMIT_ACCEPTANCE` | 提交验收 | Submit construction records for acceptance. |
| `COMPLETE` | 验收完成 | Acceptance passed. |
| `REJECT` | 驳回 | Reject for rework. |
| `CLOSE` | 关闭 | Admin closes work order. |

## 3. Priority

| Code | Display | Meaning |
|---|---|---|
| `LOW` | 低 | Low priority. |
| `NORMAL` | 普通 | Default priority. |
| `HIGH` | 高 | High priority. |
| `URGENT` | 紧急 | Safety/production urgent. |

## 4. Sync Status

| Code | Display | Meaning | Mobile Action |
|---|---|---|---|
| `LOCAL_ONLY` | 仅本地 | Exists only in local DB; not yet queued. | Add to sync queue later. |
| `PENDING` | 待同步 | Waiting for upload or server processing. | Upload when network resumes. |
| `SYNCING` | 同步中 | Push/pull in progress. | Avoid duplicate submission. |
| `SYNCED` | 已同步 | Server confirmed. | Store `server_id/version`. |
| `FAILED` | 同步失败 | Retryable failure. | Keep local data and retry. |
| `CONFLICT` | 存在冲突 | Version/field conflict. | Show read-only conflict status. |

## 5. Sync Direction and Type

| Field | Code | Display | Meaning |
|---|---|---|---|
| `sync_direction` | `PUSH` | 上传 | Mobile uploads incremental data. |
| `sync_direction` | `PULL` | 拉取 | Mobile downloads server increments. |
| `sync_direction` | `ACK` | 确认 | Mobile acknowledges applied changes. |
| `sync_type` | `FULL` | 全量 | Initial full sync or local rebuild. |
| `sync_type` | `INCREMENTAL` | 增量 | Normal weak-network sync. |
| `sync_type` | `RETRY` | 重试 | Retry failed task. |

## 6. File and Attachment Type

| Code | Display | Meaning |
|---|---|---|
| `PHOTO` | 照片 | Construction/acceptance image, can bind AI. |
| `VIDEO` | 视频 | Field video, uploaded through file API. |
| `AUDIO` | 语音 | Voice note. |
| `PDF` | PDF | Locked acceptance PDF. |
| `AI_IMAGE` | AI结果图 | AI annotated result image. |
| `SIGNATURE` | 电子签名 | Handwritten signature image. |
| `CERT` | 资质证书 | Certificate attachment. |
| `QRCODE` | 二维码 | Material QR code image. |
| `OTHER` | 其他 | Fallback file type. |

## 7. Upload Status

| Code | Display | Meaning |
|---|---|---|
| `PENDING` | 待上传 | File metadata exists; body not uploaded. |
| `UPLOADING` | 上传中 | File body is uploading. |
| `UPLOADED` | 已上传 | File body uploaded; `file_id` can be used. |
| `FAILED` | 上传失败 | Keep local file and retry. |

## 8. AI Defect Type

| Code | Display | Meaning |
|---|---|---|
| `PEELING` | 脱落 | Coating peeling. |
| `CRACK` | 裂纹 | Surface or coating crack. |
| `RUST` | 锈蚀 | Rust/corrosion. |
| `DAMAGE` | 破损 | Mechanical damage. |
| `BUBBLE` | 鼓泡 | Coating bubble. |
| `UNKNOWN` | 未知 | Unknown result. |
| `NORMAL` | 正常 | No defect detected. |

## 9. AI Review Status

| Code | Display | Meaning |
|---|---|---|
| `PENDING_REVIEW` | 待复核 | AI result waits for manual review. |
| `CONFIRMED` | 已确认 | Manual reviewer confirmed defect. |
| `FALSE_POSITIVE` | 误报 | Manual reviewer rejected AI result. |
| `IGNORED` | 已忽略 | Result ignored. |

## 10. Acceptance, Material, Qualification

| Field | Code | Display | Meaning |
|---|---|---|---|
| `acceptance_status` | `PENDING` | 待验收 | Not accepted yet. |
| `acceptance_status` | `PASSED` | 验收通过 | Passed. |
| `acceptance_status` | `REJECTED` | 验收驳回 | Rework required. |
| `acceptance_status` | `LOCKED` | 已锁定 | PDF generated; key fields locked. |
| `pdf_status` | `GENERATING` | 生成中 | PDF is being generated. |
| `pdf_status` | `GENERATED` | 已生成 | PDF can be viewed/downloaded. |
| `pdf_status` | `FAILED` | 生成失败 | PDF generation failed. |
| `pdf_status` | `ARCHIVED` | 已归档 | Archived. |
| `inout_type` | `IN` | 入库 | Stock in. |
| `inout_type` | `OUT` | 出库 | Stock out, can bind work order. |
| `inout_type` | `CHECK` | 盘点 | Inventory check. |
| `inout_type` | `ADJUST` | 调整 | Stock adjustment. |
| `inout_type` | `RETURN` | 退库 | Return unused material. |
| `valid_status` | `VALID` | 有效 | Certificate valid. |
| `valid_status` | `EXPIRING` | 即将到期 | Warning period. |
| `valid_status` | `EXPIRED` | 已过期 | Do not dispatch. |
| `valid_status` | `REVOKED` | 已吊销 | Do not dispatch. |
