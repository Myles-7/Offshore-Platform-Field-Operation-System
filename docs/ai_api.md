# AI 辅助验收接口

AI 结果仅作为辅助验收，不直接改变工单最终状态。所有 AI 结果必须绑定 `workOrderId`，并绑定施工照片附件 `attachmentId`。

## 接口清单

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/ai/models` | AI 模型列表 |
| POST | `/api/admin/ai/models` | 新增 AI 模型 |
| PUT | `/api/admin/ai/models/{id}/activate` | 激活模型 |
| POST | `/api/ai/results` | 提交 AI 结果 |
| GET | `/api/ai/results/{id}` | AI 结果详情 |
| GET | `/api/admin/work-orders/{workOrderId}/ai-results` | PC 查看工单 AI 结果 |
| GET | `/api/mobile/work-orders/{workOrderId}/ai-results` | 移动端查看工单 AI 结果 |
| POST | `/api/admin/ai/results/{id}/review` | 人工复核 |

## 请求字段

`AiModelRequest`: 模型基础信息，具体以 Swagger 为准。

`AiResultRequest`: `workOrderId`, `attachmentId`, `recordId`, `defectType`, `confidence`, `modelVersion`, `inferenceTimeMs`, `reviewStatus`, `boxes`。

`AiDefectBoxRequest`: 检测框坐标和缺陷标签字段。

`AiReviewRequest`: 人工复核状态和复核说明。复核状态包括 `PENDING_REVIEW`, `CONFIRMED`, `FALSE_POSITIVE`, `IGNORED`。

## 鉴权说明

- `/api/admin/ai/**`: 需要 PC 后台权限，项目经理或验收角色可访问，系统管理员可访问全部。
- `/api/ai/results`: 需要当前用户对绑定工单有权限。
- `/api/mobile/work-orders/{workOrderId}/ai-results`: 维修工只能查看本人负责或派工记录中的工单 AI 结果。

AI 人工复核写入 `operation_log`，AI 结果可进入 PDF 摘要但必须标注为辅助识别。
