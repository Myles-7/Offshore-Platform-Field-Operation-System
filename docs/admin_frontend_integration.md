# PC 后台接口联调文档

> 生成日期: 2026-06-05 | 版本: 1.0

---

## 1. PC 后台模块清单

| 序号 | 模块 | 页面路径 | 前端文件 | 权限角色 |
|------|------|----------|----------|----------|
| 1 | 登录 | `/login` | `src/views/login/LoginView.vue` | 公开 |
| 2 | 经营看板 | `/dashboard` | `src/views/dashboard/DashboardView.vue` | SYSTEM_ADMIN, PROJECT_MANAGER, BUSINESS_USER |
| 3 | 项目管理 | `/projects` | `src/views/projects/ProjectsView.vue` | SYSTEM_ADMIN, PROJECT_MANAGER |
| 4 | 工单管理 | `/work-orders` | `src/views/work-orders/WorkOrdersView.vue` | SYSTEM_ADMIN, PROJECT_MANAGER, ACCEPTANCE_USER |
| 5 | 工单模板 | `/work-orders/templates` | `src/views/work-orders/WorkOrderTemplatesView.vue` | SYSTEM_ADMIN, PROJECT_MANAGER, ACCEPTANCE_USER |
| 6 | 工单详情 | `/work-orders/:id` | `src/views/work-orders/WorkOrderDetailView.vue` | SYSTEM_ADMIN, PROJECT_MANAGER, ACCEPTANCE_USER |
| 7 | 施工记录 | `/records` | `src/views/records/RecordsView.vue` | SYSTEM_ADMIN, PROJECT_MANAGER, ACCEPTANCE_USER |
| 8 | 验收归档 | `/acceptance` | `src/views/acceptance/AcceptanceView.vue` | SYSTEM_ADMIN, PROJECT_MANAGER, ACCEPTANCE_USER |
| 9 | 物料追溯 | `/materials` | `src/views/materials/MaterialsView.vue` | SYSTEM_ADMIN, MATERIAL_MANAGER |
| 10 | 人员资质 | `/qualifications` | `src/views/qualifications/QualificationsView.vue` | SYSTEM_ADMIN, QUALIFICATION_MANAGER |
| 11 | 同步冲突 | `/sync/conflicts` | `src/views/sync/SyncConflictsView.vue` | SYSTEM_ADMIN, PROJECT_MANAGER |
| 12 | AI 辅助验收 | `/ai/review` | `src/views/ai/AiReviewView.vue` | SYSTEM_ADMIN, PROJECT_MANAGER, ACCEPTANCE_USER |
| 13 | 知识库 | `/knowledge/cases` | `src/views/knowledge/KnowledgeView.vue` | SYSTEM_ADMIN, PROJECT_MANAGER |
| 14 | 经营报表 | `/reports/reconciliation` | `src/views/reports/ReportsView.vue` | SYSTEM_ADMIN, BUSINESS_USER |
| 15 | 用户管理 | `/system/users` | `src/views/system/SystemUsersView.vue` | SYSTEM_ADMIN |
| 16 | 角色权限 | `/system/roles` | `src/views/system/SystemRolesView.vue` | SYSTEM_ADMIN |
| 17 | 操作日志 | `/logs` | `src/views/logs/LogsView.vue` | SYSTEM_ADMIN |

---

## 2. 接口前缀分类

### 2.1 `/api/admin` 前缀 — PC 后台核心接口（共 45 个）

> 后端 Controller: `AdminWorkOrderController`, `AdminSystemController`, `DashboardController`, `MaterialController`, `QualificationController`, `SyncController`, `AuditController`, `AiController`, `KnowledgeController`, `WorkOrderAttachmentController`, `WorkRecordController`, `AcceptanceController`

| 序号 | 方法 | 路径 | 说明 | 前端调用 |
|------|------|------|------|----------|
| 1 | GET | `/api/admin/users` | 用户分页列表 | `system.ts` → `fetchAllUsers()` |
| 2 | GET | `/api/admin/users/{id}` | 用户详情 | SystemUsersView |
| 3 | POST | `/api/admin/users` | 新增用户 | SystemUsersView |
| 4 | PUT | `/api/admin/users/{id}` | 更新用户 | SystemUsersView |
| 5 | DELETE | `/api/admin/users/{id}` | 删除用户 | SystemUsersView |
| 6 | GET | `/api/admin/roles` | 角色列表 | SystemRolesView |
| 7 | GET | `/api/admin/permissions` | 权限列表 | SystemRolesView |
| 8 | PUT | `/api/admin/users/{id}/roles` | 分配用户角色 | (未对接) |
| 9 | PUT | `/api/admin/roles/{id}/permissions` | 分配角色权限 | (未对接) |
| 10 | GET | `/api/admin/projects` | 项目分页列表 | ProjectsView, WorkOrdersView |
| 11 | GET | `/api/admin/projects/{id}` | 项目详情 | (未对接) |
| 12 | POST | `/api/admin/projects` | 新增项目 | ProjectsView |
| 13 | PUT | `/api/admin/projects/{id}` | 更新项目 | ProjectsView |
| 14 | DELETE | `/api/admin/projects/{id}` | 删除项目 | ProjectsView |
| 15 | GET | `/api/admin/work-orders` | 工单列表 | `admin.ts` → `fetchWorkOrders()` |
| 16 | GET | `/api/admin/work-orders/{id}` | 工单详情 | `admin.ts` → `fetchWorkOrderDetail()` |
| 17 | POST | `/api/admin/work-orders` | 创建工单 | `admin.ts` → `createWorkOrder()` |
| 18 | PUT | `/api/admin/work-orders/{id}` | 更新工单 | `admin.ts` → `updateWorkOrder()` |
| 19 | DELETE | `/api/admin/work-orders/{id}` | 删除工单 | `admin.ts` → `deleteWorkOrder()` |
| 20 | POST | `/api/admin/work-orders/{id}/assign` | 工单派工 | `admin.ts` → `assignWorkOrder()` |
| 21 | POST | `/api/admin/work-orders/{id}/status` | 状态流转 | `admin.ts` → `changeWorkOrderStatus()` |
| 22 | GET | `/api/admin/work-orders/{id}/status-flow` | 状态流 | `admin.ts` → `fetchStatusFlow()` |
| 23 | GET | `/api/admin/work-order-templates` | 模板列表 | `admin.ts` → `fetchTemplates()` |
| 24 | POST | `/api/admin/work-order-templates` | 新增模板 | `admin.ts` → `createTemplate()` |
| 25 | PUT | `/api/admin/work-order-templates/{id}` | 更新模板 | `admin.ts` → `updateTemplate()` |
| 26 | DELETE | `/api/admin/work-order-templates/{id}` | 删除模板 | `admin.ts` → `deleteTemplate()` |
| 27 | POST | `/api/admin/work-orders/from-template/{templateId}` | 模板创建工单 | `admin.ts` → `createWorkOrderFromTemplate()` |
| 28 | GET | `/api/admin/work-orders/{workOrderId}/records` | 施工记录列表 | `admin.ts` → `fetchWorkOrderRecords()` |
| 29 | GET | `/api/admin/work-records/{recordId}` | 施工记录详情 | `admin.ts` → `fetchWorkRecord()` |
| 30 | POST | `/api/admin/work-records/{recordId}/confirm` | 确认施工记录 | (未对接) |
| 31 | GET | `/api/admin/work-orders/{workOrderId}/attachments` | 附件列表 | `admin.ts` → `fetchWorkOrderAttachments()` |
| 32 | GET | `/api/admin/work-orders/{workOrderId}/signatures` | 签名列表 | `acceptance.ts` → `fetchSignatures()` |
| 33 | GET | `/api/admin/work-orders/{workOrderId}/acceptance` | 验收记录 | `acceptance.ts` → `fetchAcceptanceRecords()` |
| 34 | POST | `/api/admin/work-orders/{workOrderId}/acceptance/review` | 验收复核 | `acceptance.ts` → `reviewAcceptance()` |
| 35 | POST | `/api/admin/work-orders/{workOrderId}/pdf/generate` | 生成PDF | `acceptance.ts` → `generatePdf()` |
| 36 | GET | `/api/admin/work-orders/{workOrderId}/pdf` | PDF列表 | `acceptance.ts` → `fetchPdfs()` |
| 37 | GET | `/api/admin/work-orders/{workOrderId}/pdf/download` | 下载PDF | `acceptance.ts` → `pdfDownloadUrl()` |
| 38 | GET | `/api/admin/work-orders/{workOrderId}/audit-trail` | 审计轨迹 | `admin.ts` → `fetchAuditTrail()` |
| 39 | GET | `/api/admin/materials` | 物料列表 | `material.ts` → `fetchMaterials()` |
| 40 | POST | `/api/admin/materials` | 新增物料 | `material.ts` → `createMaterial()` |
| 41 | PUT | `/api/admin/materials/{id}` | 更新物料 | `material.ts` → `updateMaterial()` |
| 42 | DELETE | `/api/admin/materials/{id}` | 删除物料 | `material.ts` → `deleteMaterial()` |
| 43 | GET | `/api/admin/materials/{id}/inventory` | 库存 | `material.ts` → `fetchInventory()` |
| 44 | POST | `/api/admin/materials/inbound` | 入库 | `material.ts` → `materialInbound()` |
| 45 | POST | `/api/admin/materials/outbound` | 出库 | `material.ts` → `materialOutbound()` |
| 46 | POST | `/api/admin/materials/stocktaking` | 盘点 | `material.ts` → `materialStocktaking()` |
| 47 | POST | `/api/admin/materials/{id}/qrcode` | 生成二维码 | `material.ts` → `generateQrcode()` |
| 48 | GET | `/api/admin/materials/qrcode/{code}` | 扫码查询 | `material.ts` → `getQrcodeByCode()` |
| 49 | GET | `/api/admin/work-orders/{workOrderId}/material-usage` | 工单物料追溯 | `material.ts` → `fetchWorkOrderMaterialUsage()` |
| 50 | GET | `/api/admin/employees` | 员工列表 | `qualification.ts` → `fetchEmployees()` |
| 51 | GET | `/api/admin/employees/{id}` | 员工详情 | `qualification.ts` → `fetchEmployee()` |
| 52 | POST | `/api/admin/employees` | 新增员工 | QualificationsView |
| 53 | PUT | `/api/admin/employees/{id}` | 更新员工 | QualificationsView |
| 54 | DELETE | `/api/admin/employees/{id}` | 删除员工 | QualificationsView |
| 55 | GET | `/api/admin/employees/{id}/certificates` | 证书列表 | `qualification.ts` → `fetchEmployeeCertificates()` |
| 56 | POST | `/api/admin/employees/{id}/certificates` | 新增证书 | QualificationsView |
| 57 | PUT | `/api/admin/certificates/{id}` | 更新证书 | QualificationsView |
| 58 | DELETE | `/api/admin/certificates/{id}` | 删除证书 | QualificationsView |
| 59 | GET | `/api/admin/certificates/warnings` | 到期预警 | `qualification.ts` → `fetchCertificateWarnings()` |
| 60 | GET | `/api/admin/work-orders/{id}/qualification-candidates` | 派工资质候选人 | `qualification.ts` → `fetchQualificationCandidates()` |
| 61 | GET | `/api/admin/dashboard/overview` | 总览 | `dashboard.ts` → `fetchDashboardOverview()` |
| 62 | GET | `/api/admin/dashboard/work-order-statistics` | 工单统计 | `dashboard.ts` → `fetchWorkOrderStatistics()` |
| 63 | GET | `/api/admin/dashboard/project-statistics` | 项目统计 | `dashboard.ts` → `fetchProjectStatistics()` |
| 64 | GET | `/api/admin/dashboard/person-statistics` | 人员统计 | `dashboard.ts` → `fetchPersonStatistics()` |
| 65 | GET | `/api/admin/dashboard/material-statistics` | 物料统计 | `dashboard.ts` → `fetchMaterialStatistics()` |
| 66 | GET | `/api/admin/dashboard/output-value` | 产值趋势 | `dashboard.ts` → `fetchOutputValue()` |
| 67 | GET | `/api/admin/reports/reconciliation` | 对账单 | ReportsView |
| 68 | GET | `/api/admin/reports/reconciliation/export` | Excel导出 | ReportsView |
| 69 | GET | `/api/admin/operation-logs` | 操作日志 | LogsView |
| 70 | GET | `/api/admin/operation-logs/{id}` | 日志详情 | LogsView |
| 71 | GET | `/api/admin/work-orders/{workOrderId}/audit-trail` | 工单审计轨迹 | LogsView |
| 72 | GET | `/api/admin/sync/audit-trail` | 同步审计轨迹 | LogsView |
| 73 | GET | `/api/admin/sync/conflicts` | 冲突列表 | `sync.ts` → `fetchSyncConflicts()` |
| 74 | GET | `/api/admin/sync/conflicts/{id}` | 冲突详情 | `sync.ts` → `fetchSyncConflict()` |
| 75 | POST | `/api/admin/sync/conflicts/{id}/resolve` | 处理冲突 | `sync.ts` → `resolveSyncConflict()` |
| 76 | GET | `/api/admin/work-orders/{workOrderId}/ai-results` | 工单AI结果 | `ai.ts` → `fetchWorkOrderAiResults()` |
| 77 | POST | `/api/admin/ai/results/{id}/review` | AI复核 | `ai.ts` → `reviewAiResult()` |
| 78 | GET | `/api/admin/ai/models` | AI模型列表 | `ai.ts` → `fetchAiModels()` |
| 79 | POST | `/api/admin/ai/models` | 新增模型 | `ai.ts` → `createAiModel()` |
| 80 | PUT | `/api/admin/ai/models/{id}/activate` | 激活模型 | `ai.ts` → `activateAiModel()` |
| 81 | GET | `/api/admin/knowledge/cases` | 故障案例列表 | `knowledge.ts` → `fetchKnowledgeCases()` |
| 82 | POST | `/api/admin/knowledge/cases` | 新增案例 | `knowledge.ts` → `createKnowledgeCase()` |
| 83 | PUT | `/api/admin/knowledge/cases/{id}` | 更新案例 | `knowledge.ts` → `updateKnowledgeCase()` |
| 84 | DELETE | `/api/admin/knowledge/cases/{id}` | 删除案例 | `knowledge.ts` → `deleteKnowledgeCase()` |
| 85 | GET | `/api/admin/knowledge/processes` | 工艺列表 | `knowledge.ts` → `fetchMaintenanceProcesses()` |
| 86 | POST | `/api/admin/knowledge/processes` | 新增工艺 | `knowledge.ts` → `createMaintenanceProcess()` |
| 87 | PUT | `/api/admin/knowledge/processes/{id}` | 更新工艺 | `knowledge.ts` → `updateMaintenanceProcess()` |

### 2.2 `/api/files` 前缀 — 文件存储与鉴权接口（共 3 个）

| 方法 | 路径 | 说明 | 前端调用 |
|------|------|------|----------|
| GET | `/api/files/{fileId}/preview` | 文件预览 | `file.ts` → `fetchFilePreviewUrl()` → 拼接URL |
| GET | `/api/files/{fileId}/download` | 文件下载 | `file.ts` → `fetchFileDownloadUrl()` → 拼接URL |
| DELETE | `/api/files/{fileId}` | 作废文件 | `file.ts` → `deleteFile()` |

### 2.3 `/api/sync` 前缀 — 离线同步接口（共 2 个）

| 方法 | 路径 | 说明 | 前端调用 |
|------|------|------|----------|
| GET | `/api/sync/tasks` | 同步任务列表 | `sync.ts` → `fetchSyncTasks()` |
| GET | `/api/sync/logs` | 同步日志 | `sync.ts` → `fetchSyncLogs()` |

### 2.4 `/api/ai` 前缀 — AI 结果详情接口（共 1 个）

| 方法 | 路径 | 说明 | 前端调用 |
|------|------|------|----------|
| GET | `/api/ai/results/{id}` | AI结果详情 | `ai.ts` → `fetchAiResult()` |

### 2.5 `/api/auth` 前缀 — 认证接口（共 3 个）

| 方法 | 路径 | 说明 | 前端调用 |
|------|------|------|----------|
| POST | `/api/auth/login` | 登录 | `auth.ts` → `loginApi()` |
| POST | `/api/auth/logout` | 登出 | `auth.ts` → `logoutApi()` |
| GET | `/api/auth/current` | 当前用户信息 | `auth.ts` → `currentUserApi()` |

---

## 3. 接口请求/响应字段映射

### 3.1 工单列表

**请求**: `GET /api/admin/work-orders?pageNum=1&pageSize=10&workOrderNo=WO-xxx&status=PENDING_ASSIGN&projectId=1&workType=REPAIR&workLocation=C平台&priority=HIGH&syncStatus=SYNCED&abnormalFlag=1&plannedStartTimeStart=2026-01-01&plannedStartTimeEnd=2026-12-31`

**请求Query参数** → 后端 `WorkOrderQueryRequest`:

| 前端字段 | 后端字段 | 类型 | 说明 |
|----------|----------|------|------|
| `query.workOrderNo` | `workOrderNo` | String | 工单编号 |
| `query.projectId` | `projectId` | Long | 项目ID |
| `query.workType` | `workType` | String | 工单类型 |
| `query.workLocation` | `workLocation` | String | 作业地点 |
| `query.status` | `status` | String | 工单状态 |
| `query.priority` | `priority` | String | 优先级 |
| `query.leaderId` | `leaderId` | Long | 负责人ID |
| `query.maintainerId` | `maintainerId` | Long | 维修工ID |
| `query.syncStatus` | `syncStatus` | String | 同步状态 |
| `query.abnormalFlag` | `abnormalFlag` | Integer | 是否异常 |
| `query.dateRange[0]` | `plannedStartTimeStart` | LocalDateTime | 开始时间 |
| `query.dateRange[1]` | `plannedStartTimeEnd` | LocalDateTime | 结束时间 |

**响应** `PageResult<WorkOrderVO>`:

| 后端字段 | 前端字段 | 类型 | 说明 |
|----------|----------|------|------|
| `id` | `id` | number | 工单ID |
| `workOrderNo` | `workOrderNo` | string | 工单编号 |
| `projectId` | `projectId` | number | 项目ID |
| `projectName` | `projectName` | string | 项目名称 |
| `templateId` | `templateId` | number | 模板ID |
| `workTitle` | `workTitle` | string | 作业标题 |
| `workType` | `workType` | string | 工单类型 |
| `workLocation` | `workLocation` | string | 作业地点 |
| `workContent` | `workContent` | string | 作业内容 |
| `requiredMaterialDesc` | `requiredMaterialDesc` | string | 所需物料说明 |
| `leaderId` | `leaderId` | number | 负责人ID |
| `leaderName` | `leaderName` | string | 负责人姓名 |
| `maintainerId` | `maintainerId` | number | 维修工ID |
| `maintainerName` | `maintainerName` | string | 维修工姓名 |
| `plannedStartTime` | `plannedStartTime` | string | 计划开始 |
| `plannedEndTime` | `plannedEndTime` | string | 计划结束 |
| `actualStartTime` | `actualStartTime` | string | 实际开始 |
| `actualEndTime` | `actualEndTime` | string | 实际结束 |
| `status` | `status` | string | 工单状态 |
| `priority` | `priority` | string | 优先级 |
| `rejectReason` | `rejectReason` | string | 驳回原因 |
| `closeReason` | `closeReason` | string | 关闭原因 |
| `acceptanceRequired` | `acceptanceRequired` | number | 是否需验收 |
| `sourceType` | `sourceType` | string | 来源 |
| `version` | `version` | number | 版本号 |
| `syncStatus` | `syncStatus` | string | 同步状态 |
| `abnormalFlag` | `abnormalFlag` | number | 异常标志 |
| `createdAt` | `createdAt` | string | 创建时间 |
| `updatedAt` | `updatedAt` | string | 更新时间 |
| `remark` | `remark` | string | 备注 |

### 3.2 工单详情

**请求**: `GET /api/admin/work-orders/{id}`

**响应** `WorkOrderDetailVO`:

```typescript
{
  workOrder: WorkOrderListItem,           // 工单基本信息
  project: Record<string, unknown>,      // 项目信息
  assignments: AssignmentVO[],            // 派工记录
  statusFlow: StatusFlowVO[],             // 状态流转
  requiredMaterials: MaterialRequirementVO[], // 所需物料
  constructionRecordSummary: SummaryVO[],  // 施工记录摘要
  attachmentSummary: SummaryVO[],         // 附件摘要
  acceptanceRecords: SummaryVO[],         // 验收记录
  signatureRecords: SummaryVO[],          // 签名记录
  pdfRecords: SummaryVO[],                // PDF记录
  materialUsage: SummaryVO[],             // 物料使用
  aiResults: SummaryVO[],                 // AI结果
  syncSummary: { syncStatus, conflictCount, hasConflict }
}
```

### 3.3 新增/编辑工单

**请求**: `POST|PUT /api/admin/work-orders[/{id}]`

**请求体** → 后端 `WorkOrderRequest`:

| 前端表单字段 | 后端字段 | 必填 | 说明 |
|-------------|----------|------|------|
| `projectId` | `projectId` | 是 | 项目ID |
| `workTitle` | `workTitle` | 是 | 作业标题 |
| `workLocation` | `workLocation` | 是 | 作业地点 |
| `workContent` | `workContent` | 是 | 作业内容 |
| `workType` | `workType` | 否 | 工单类型 |
| `leaderId` | `leaderId` | 否 | 负责人ID |
| `maintainerId` | `maintainerId` | 否 | 维修工ID |
| `plannedStartTime` | `plannedStartTime` | 是 | 计划开始(前端校验) |
| `plannedEndTime` | `plannedEndTime` | 是 | 计划结束(前端校验) |
| `priority` | `priority` | 否 | 优先级，默认NORMAL |
| `requiredMaterialDesc` | `requiredMaterialDesc` | 否 | 物料说明 |
| `acceptanceRequired` | `acceptanceRequired` | 否 | 需验收，默认1 |
| `remark` | `remark` | 否 | 备注 |

### 3.4 工单派工

**请求**: `POST /api/admin/work-orders/{id}/assign`

**请求体** → 后端 `WorkOrderAssignRequest`:

| 前端字段 | 后端字段 | 必填 | 说明 |
|----------|----------|------|------|
| `leaderId` | `leaderId` | 否 | 负责人ID |
| `maintainerId` | `maintainerId` | 是 | 维修工ID |
| `assignmentRole` | `assignmentRole` | 否 | 角色，默认MAINTAINER |
| `remark` | `remark` | 否 | 备注（含预估工时、强制分派原因） |

### 3.5 验收复核

**请求**: `POST /api/admin/work-orders/{workOrderId}/acceptance/review`

**请求体** → 后端 `AcceptanceReviewRequest`:

| 前端字段 | 后端字段 | 必填 | 说明 |
|----------|----------|------|------|
| `acceptanceStatus` | `acceptanceStatus` | 是 | PASSED/REJECTED |
| `acceptanceResult` | `acceptanceResult` | 否 | 验收结果 |
| `acceptanceOpinion` | `acceptanceOpinion` | 否 | 验收意见 |
| `rejectReason` | `rejectReason` | 否 | 驳回原因 |

### 3.6 冲突处理

**请求**: `POST /api/admin/sync/conflicts/{id}/resolve`

**请求体** → 后端 `ConflictResolveRequest`:

| 前端字段 | 后端字段 | 必填 | 说明 |
|----------|----------|------|------|
| `resolveStrategy` | `resolveStrategy` | 是 | KEEP_SERVER/KEEP_LOCAL/MERGE |
| `finalPayload` | `finalPayload` | 否 | MERGE时的合并数据 |
| `resolveComment` | `resolveComment` | 是 | 处理说明 |

---

## 4. 状态枚举说明

### 4.1 工单状态 (`work_order.status`)

| 枚举值 | 前端标签 | Tag 颜色 |
|--------|----------|----------|
| `PENDING_ASSIGN` | 待派工 | warning |
| `ASSIGNED` | 已派工 | primary |
| `ACCEPTED` | 已接收 | primary |
| `IN_PROGRESS` | 施工中 | warning |
| `PENDING_ACCEPTANCE` | 待验收 | warning |
| `COMPLETED` | 已完成 | success |
| `REJECTED` | 已驳回 | danger |
| `CLOSED` | 已关闭 | info |

### 4.2 优先级 (`work_order.priority`)

| 枚举值 | 前端标签 | Tag 颜色 |
|--------|----------|----------|
| `LOW` | 低 | info |
| `NORMAL` | 普通 | info |
| `HIGH` | 高 | warning |
| `URGENT` | 紧急 | danger |

### 4.3 同步状态 (`sync_status`)

| 枚举值 | 前端标签 | Tag 颜色 |
|--------|----------|----------|
| `PENDING` | 待同步 | warning |
| `SYNCING` | 同步中 | primary |
| `SYNCED` | 已同步 | success |
| `FAILED` | 同步失败 | danger |
| `CONFLICT` | 冲突待处理 | danger |

### 4.4 AI 复核状态 (`review_status`)

| 枚举值 | 前端标签 | Tag 颜色 |
|--------|----------|----------|
| `PENDING_REVIEW` | 待复核 | warning |
| `CONFIRMED` | 已确认 | success |
| `FALSE_POSITIVE` | 误报 | info |
| `IGNORED` | 已忽略 | info |

### 4.5 证书状态 (`valid_status`)

| 枚举值 | 前端标签 | Tag 颜色 |
|--------|----------|----------|
| `VALID` | 有效 | success |
| `EXPIRING` | 即将到期 | warning |
| `EXPIRED` | 已过期 | danger |
| `REVOKED` | 已吊销 | danger |

### 4.6 工单类型 (`work_type`)

| 枚举值 | 前端标签 |
|--------|----------|
| `ANTICORROSION` | 防腐作业 |
| `REPAIR` | 维修作业 |
| `INSPECTION` | 巡检作业 |
| `MAINTENANCE` | 保养作业 |

---

## 5. 错误码处理说明

### 全局拦截（`src/api/request.ts`）

```typescript
// HTTP 层面拦截
status === 401 → 清除token，跳转 /login
status === 403 → 跳转 /403

// 业务层面拦截
body.code === 401 → 清除token，跳转 /login
body.code === 403 → 跳转 /403
其他 → ElMessage.error(message)
```

### 后端错误码枚举

| code | 含义 | 前端处理 |
|------|------|----------|
| 200 | 成功 | 正常返回data |
| 400 | 参数错误 | 显示message |
| 401 | 未登录 | 跳转/login |
| 403 | 无权限 | 跳转/403 |
| 404 | 资源不存在 | 显示message |
| 500 | 服务器错误 | 显示message |
| 10001 | 用户错误 | 显示message |
| 20001 | 工单错误 | 显示message |
| 30001 | 文件错误 | 显示message |
| 40001 | 同步错误 | 显示message |
| 50001 | AI错误 | 显示message |
| 50002 | PDF错误 | 显示message |
| 60001 | 物料错误 | 显示message |
| 70001 | 资质错误 | 显示message |
| 80001 | 看板错误 | 显示message |

---

## 6. 文件预览和下载说明

### 安全规则
- 所有文件访问 **必须** 走 `/api/files/{fileId}/preview|download` 鉴权接口
- **禁止** 前端直接拼接服务器物理路径
- 文件下载/预览自动携带 `Authorization: Bearer <token>`

### 文件类型与前端处理

| 文件类型 | 前端组件 | 预览方式 |
|----------|----------|----------|
| PHOTO | `FilePreviewDialog` | `<el-image>` |
| AI_IMAGE | `FilePreviewDialog` | `<el-image>` |
| VIDEO | `FilePreviewDialog` | `<video>` |
| AUDIO | `FilePreviewDialog` | `<audio>` |
| PDF | `FilePreviewDialog` | `<iframe>` |
| SIGNATURE | `FilePreviewDialog` | `<el-image>` + 签名信息卡 |
| CERT / QRCODE / OTHER | `FilePreviewDialog` | `<el-image>` + 文件类型图标 |

### PDF 验收单下载
- 使用 `GET /api/admin/work-orders/{workOrderId}/pdf/download` (302重定向)
- PDF 生成后关键验收记录锁定，不可删除

---

## 7. Excel 导出说明

- 路径: `GET /api/admin/reports/reconciliation/export`
- 后端返回 `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- 前端: `window.open('/api/admin/reports/reconciliation/export', '_blank')`
- 导出操作写入 `operation_log`（后端 `@OperationLog`）
- Content-Disposition: `attachment;filename="reconciliation.xlsx"`

---

## 8. 与移动端联动的接口

| 场景 | 移动端接口 | PC后台查看接口 |
|------|-----------|---------------|
| 施工记录 | `POST /api/mobile/work-orders/{id}/records` | `GET /api/admin/work-orders/{id}/records` |
| 附件上传 | `POST /api/mobile/work-orders/{id}/attachments` | `GET /api/admin/work-orders/{id}/attachments` |
| 签名提交 | `POST /api/mobile/work-orders/{id}/signatures` | `GET /api/admin/work-orders/{id}/signatures` |
| 验收记录 | `POST /api/mobile/work-orders/{id}/acceptance` | `GET /api/admin/work-orders/{id}/acceptance` |
| PDF元数据 | `POST /api/mobile/work-orders/{id}/pdf/metadata` | `GET /api/admin/work-orders/{id}/pdf` |
| 物料使用 | `POST /api/mobile/work-orders/{id}/material-usage` | `GET /api/admin/work-orders/{id}/material-usage` |
| AI识别 | (移动端push AI结果) | `GET /api/admin/work-orders/{id}/ai-results` |

---

## 9. 与离线同步联动的接口

| 场景 | 接口路径 | 前端页面 |
|------|----------|----------|
| 冲突列表 | `GET /api/admin/sync/conflicts` | `/sync/conflicts` |
| 冲突详情 | `GET /api/admin/sync/conflicts/{id}` | 冲突详情弹窗 |
| 冲突处理 | `POST /api/admin/sync/conflicts/{id}/resolve` | 冲突处理弹窗 |
| 同步日志 | `GET /api/sync/logs` | `/sync/conflicts` - 同步日志Tab |
| 同步任务 | `GET /api/sync/tasks` | `/sync/conflicts` - 同步任务Tab |
| 同步审计 | `GET /api/admin/sync/audit-trail` | `/logs` - 同步审计Tab |

联动规则：
- 工单列表显示冲突图标 → `syncStatus === 'CONFLICT'`
- 工单详情显示冲突Alert → `syncSummary.hasConflict === true`
- 冲突处理写入操作日志 → 后端 `@OperationLog`

---

## 10. 与 AI 辅助验收联动的接口

| 场景 | 接口路径 | 前端页面 |
|------|----------|----------|
| 工单AI结果 | `GET /api/admin/work-orders/{id}/ai-results` | `/ai/review` |
| AI结果详情 | `GET /api/ai/results/{id}` | 详情弹窗 |
| AI复核 | `POST /api/admin/ai/results/{id}/review` | `/ai/review` - 复核弹窗 |
| AI模型管理 | `GET/POST /api/admin/ai/models` | `/ai/review` - 模型Tab |

联动规则：
- AI 仅辅助，不改变最终验收状态
- 低置信度 (<0.7) 显示红色警告
- 复核写入操作日志
- AI 结果可进入 PDF 验收单摘要

---

## 11. 后端待补充接口

| 序号 | 路径 | 说明 | 影响页面 |
|------|------|------|----------|
| 1 | `PUT /api/admin/knowledge/processes/{id}` | 维修工艺更新 | 知识库 | **已补充：后端已创建 KnowledgeController** |
| 2 | `DELETE /api/admin/knowledge/processes/{id}` | 维修工艺删除 | 知识库 | **可补充：后端需添加 @DeleteMapping** |
| 3 | `GET /api/admin/reports/work-orders` | 工单产值明细 | `/reports` | **待补充** |
| 4 | `GET /api/admin/reports/materials` | 物料消耗明细 | `/reports` | **可复用 dashboard/material-statistics** |
| 5 | 按工单导出Excel | 单工单Excel导出 | 工单详情 | **待补充** |

### 数据库表待创建

| 表名 | 说明 | SQL文件 | 状态 |
|------|------|----------|------|
| `knowledge_case` | 故障案例 | `db/init_knowledge.sql` | **已生成 SQL** |
| `maintenance_process` | 维修工艺 | `db/init_knowledge.sql` | **已生成 SQL** |

> **注意**: 知识库两张表需在数据库中执行 `db/init_knowledge.sql` 创建。

---

## 12. 统一响应结构

```typescript
interface ApiResponse<T> {
  code: number;      // 200 成功
  message: string;   // 提示信息
  data: T;           // 业务数据
  timestamp: string; // 服务器时间
  traceId?: string;  // 链路ID
}

interface PageResult<T> {
  records: T[];      // 数据列表
  total: number;     // 总条数
  pageNum: number;   // 当前页
  pageSize: number;  // 每页大小
}
```

---

## 13. 前端 API 模块索引

| 文件 | 对应后端 Controller | 接口前缀 |
|------|---------------------|----------|
| `src/api/auth.ts` | `AuthController` | `/api/auth` |
| `src/api/admin.ts` | `AdminWorkOrderController` + `WorkRecordController` + `WorkOrderAttachmentController` + `AuditController` | `/api/admin` |
| `src/api/dashboard.ts` | `DashboardController` | `/api/admin/dashboard` |
| `src/api/system.ts` | `AdminSystemController` | `/api/admin` |
| `src/api/material.ts` | `MaterialController` | `/api/admin/materials` |
| `src/api/qualification.ts` | `QualificationController` | `/api/admin` |
| `src/api/file.ts` | `FileController` | `/api/files` |
| `src/api/acceptance.ts` | `AcceptanceController` | `/api/admin/work-orders/{id}` |
| `src/api/sync.ts` | `SyncController` | `/api/admin/sync`, `/api/sync` |
| `src/api/ai.ts` | `AiController` | `/api/admin/ai`, `/api/ai` |
| `src/api/knowledge.ts` | `KnowledgeController` | `/api/admin/knowledge` |
| `src/api/employee.ts` | `QualificationController` | `/api/admin/employees` |

---

## 14. 验证检查

```bash
npx vue-tsc --noEmit    # TypeScript 类型检查
npx vite build          # 生产构建
```

---

> **文档维护**: 接口变更时请同步更新本文档。
