# 海上平台现场作业管理系统：Claude Code 全局统一规范

> 推荐文件名：`CLAUDE.md`、`PROJECT_RULES.md` 或 `docs/CLAUDE_GLOBAL_SPEC.md`。  
> 文件用途：本文件用于 Claude Code 在分模块开发前统一理解项目背景、业务主线、技术边界、接口规范、数据库规范、离线同步规范、文件安全规范和验收标准。  
> 使用对象：Claude Code、后端开发、数据库开发、PC 后台开发、Android 移动端开发、离线同步专项开发、AI 辅助验收开发、联调测试。  
> 使用原则：Claude Code 每次开始新任务前，必须先读取本文档；每次修改代码后，必须根据本文档进行自检。

---

## 0. Claude Code 使用总规则

### 0.1 本文件在项目中的定位

本文件不是答辩稿，不是软著说明书，也不是普通需求文档，而是 **Claude Code 分模块开发时的全局约束文件**。

Claude Code 在执行任何任务时，应把本文档当作全局项目规则，优先用于约束以下行为：

1. 是否应该修改某个模块。
2. 新增表、字段、接口、页面时如何命名。
3. 工单、附件、签名、PDF、AI 结果、物料、资质、同步冲突之间如何关联。
4. 移动端是否符合离线优先原则。
5. 文件上传、下载、预览是否符合安全鉴权要求。
6. API 路径、返回结构、分页结构、错误码是否统一。
7. 修改完成后应该运行哪些检查命令。

### 0.2 Claude Code 阅读顺序

每次开始一个新模块或新任务前，Claude Code 应按以下顺序读取项目上下文：

```text
1. 当前任务说明
2. 本文件：Claude Code 全局统一规范
3. 项目 README / docs / 项目源文件
4. 当前模块已有代码
5. 当前模块相关接口、表结构、类型定义
6. 测试文件、构建脚本、配置文件
7. 再制定修改计划
```

不允许在未阅读现有代码结构的情况下直接生成新目录、新框架或大段孤立代码。

### 0.3 Claude Code 工作循环

每次开发任务必须遵循以下循环：

```text
理解项目上下文
  ↓
定位相关文件
  ↓
提出简短实现计划
  ↓
最小范围修改代码
  ↓
运行编译 / 测试 / 类型检查
  ↓
修复错误
  ↓
输出变更说明和验证结果
```

如果任务较大，必须拆成小步骤执行，不要一次性完成所有模块。

### 0.4 约束优先级

当现有代码、本文档、专项提示词或临时指令之间存在差异时，优先级如下：

```text
1. 当前仓库中已经能运行的代码结构与技术栈
2. 本 Claude Code 全局统一规范
3. 已确认的数据库 / 后端 / PC 后台 / 移动端 / 离线同步专项文档
4. 项目总述和业务需求分析
5. 单次对话中的临时补充要求
```

约束说明：

1. 不允许为了套用文档而破坏已有可运行项目。
2. 不允许因为单模块任务而破坏全局业务主线。
3. 不允许把系统改成普通 CRUD 后台。
4. 不允许跳过编译、测试或构建检查后声称任务完成。
5. 如果无法验证，必须明确说明“未验证原因”。

### 0.5 Claude Code 输出格式

每次完成代码修改后，Claude Code 应输出：

```text
1. 本次任务目标
2. 修改文件清单
3. 新增 / 修改的数据库表或字段
4. 新增 / 修改的接口
5. 新增 / 修改的页面或组件
6. 与工单、同步、文件、权限、日志的关联
7. 已运行的验证命令
8. 验证结果
9. 仍需人工确认的问题
```

如果没有修改某一类内容，应写“无”。

---

# 第一部分：全局业务主线统一

## 1. 项目一句话定位

本项目是一套面向海上平台、工业维修、防腐施工和设备检修场景的现场作业管理系统，通过 **Android 移动端 + PC Web 后台 + Java 后端服务 + MySQL 中心数据库 + 移动端本地数据库 + 离线同步 + 文件存储 + AI 辅助验收**，实现从工单派发、现场施工、多媒体留痕、电子签名、PDF 验收、离线同步到经营统计的全过程数字化闭环。

## 2. 核心业务主线

系统必须围绕以下主线开发：

```text
PC后台创建 / 分派工单
    ↓
移动端接收 / 同步工单
    ↓
现场施工记录
    ↓
照片 / 视频 / 语音多媒体留痕
    ↓
照片水印与附件归档
    ↓
物料实际使用记录
    ↓
人员资质校验
    ↓
AI辅助识别疑似缺陷
    ↓
电子签名验收
    ↓
PDF验收单归档
    ↓
移动端离线数据同步到服务端
    ↓
PC后台复核、冲突处理、经营看板统计
```

所有模块都必须服务这条主线，不得做成互不关联的孤立功能。

## 3. 工单是系统中心

`work_order` 是核心业务对象。开发任何模块时，都必须优先判断是否需要关联 `work_order_id`。

| 模块 | 是否必须关联工单 | 关联字段或方式 |
|---|---|---|
| 项目信息 | 是 | `project_id` 关联工单 |
| 派工记录 | 是 | `work_order_id` |
| 施工记录 | 是 | `work_order_id` |
| 附件文件 | 是 | `work_order_id`，可选 `record_id` |
| 照片水印 | 是 | 水印包含工单号、时间、拍摄人 |
| 视频 / 语音 | 是 | `work_order_id`，可选 `record_id` |
| 电子签名 | 是 | `work_order_id` |
| PDF 验收单 | 是 | `work_order_id`、验收记录、签名记录 |
| 物料使用 | 是 | `work_order_id`、`material_id` |
| 人员资质 | 间接关联 | 派工时通过执行人和所需资质校验 |
| AI 识别结果 | 是 | `work_order_id`、`record_id`、`attachment_id` |
| 同步冲突 | 是或间接关联 | `business_id`、`entity_type`、必要时冗余 `work_order_id` |
| 经营统计 | 是 | `work_order_id`、`project_id`、人员、物料 |

### 3.1 模块关联统一关系

```text
project_info
  └─ work_order
       ├─ work_order_assignment
       ├─ work_order_status_log
       ├─ work_order_record
       │    ├─ work_order_attachment
       │    └─ ai_result
       │         ├─ ai_defect_box
       │         └─ ai_review_record
       ├─ work_order_signature
       ├─ work_order_acceptance
       ├─ work_order_pdf
       ├─ work_order_material
       ├─ work_order_material_usage
       └─ sync_conflict / version_log / operation_log
```

## 4. 统一工单生命周期

### 4.1 工单状态枚举

后端、数据库、PC 后台、移动端必须统一工单状态。建议使用英文枚举存储，中文仅用于展示。

| 枚举值 | 中文含义 | 说明 |
|---|---|---|
| `DRAFT` | 草稿 | 可选，后台暂存未派发工单 |
| `PENDING` | 待派工 | 已创建，等待分派 |
| `ASSIGNED` | 已派工 | 已分派给维修工或施工人员 |
| `ACCEPTED` | 已接收 | 移动端维修工已确认接单，可选 |
| `IN_PROGRESS` | 施工中 | 维修工已开始施工 |
| `PENDING_ACCEPTANCE` | 待验收 | 施工完成，等待验收 |
| `COMPLETED` | 已完成 | 验收通过，流程完成 |
| `REJECTED` | 已驳回 | 验收不通过或退回修改 |
| `CLOSED` | 已关闭 | 工单归档或终止 |

### 4.2 状态流转规则

```text
DRAFT → PENDING → ASSIGNED → ACCEPTED → IN_PROGRESS → PENDING_ACCEPTANCE → COMPLETED → CLOSED
                                      ↘ REJECTED → IN_PROGRESS / PENDING_ACCEPTANCE
```

规则：

1. 状态变更必须写入 `work_order_status_log`。
2. 分派工单必须写入 `work_order_assignment`。
3. 未派工工单不能被移动端接收。
4. 未开始施工的工单不能提交验收。
5. 提交验收前应至少存在施工记录。
6. 已完成或已关闭工单不允许普通用户随意修改验收、签名、PDF 等关键数据。
7. 状态变化应写入 `operation_log`。
8. 离线状态变化同步到服务端时必须进行版本判断。

## 5. 统一用户角色与数据权限

| 角色 | 主要权限 | 数据范围 |
|---|---|---|
| 系统管理员 | 用户、角色、权限、系统配置、全量数据 | 全部 |
| 项目经理 | 项目工单、派工、验收、统计、冲突处理 | 所属项目 |
| 调度员 | 创建、编辑、分派工单 | 授权项目或部门 |
| 维修工 / 施工人员 | 查看我的工单、提交记录、上传附件、同步数据 | 仅本人相关工单 |
| 现场负责人 | 现场复核、签名、验收确认 | 所属项目或授权工单 |
| 物资管理员 | 物料入库、出库、盘点、二维码管理 | 物料模块 |
| 资质管理员 | 员工档案、证书、到期预警 | 人员资质模块 |
| 验收人员 | 查看施工记录、验收确认、签名归档 | 授权工单 |
| 经营人员 | 经营看板、Excel 对账单导出 | 经营统计数据 |

权限规则：

1. 所有受保护接口必须校验登录态。
2. 不允许只在前端隐藏按钮来实现权限控制。
3. 维修工接口必须从 token 中获取当前用户，不信任前端传入的 `userId`。
4. 项目经理不得查看未授权项目数据。
5. 文件下载和预览必须检查用户是否有权限访问对应工单或业务对象。
6. 所有关键操作必须写入 `operation_log`。

---

# 第二部分：全局技术架构统一

## 6. 推荐技术路线

如果项目已有确定技术栈，优先服从现有项目；否则按以下默认方案统一：

| 层级 | 默认技术 | 说明 |
|---|---|---|
| PC 后台 | Vue 3 / React + Element Plus / Ant Design | 工单、物料、资质、看板、冲突复核 |
| 后端服务 | Java + Spring Boot | 业务接口、同步接口、文件接口、统计接口 |
| 数据库 | MySQL 8.0 | 中心业务数据库 |
| ORM | MyBatis / MyBatis-Plus / JPA | 以现有项目为准，不盲目替换 |
| 权限认证 | JWT / Spring Security | PC 后台与移动端统一鉴权 |
| 文件存储 | 本地文件系统 / MinIO | 图片、视频、语音、签名、PDF |
| 移动端 | Android 7.0+，Kotlin / Java | 现场作业、离线存储、同步 |
| 移动端本地库 | SQLite / Realm / Room | 本地工单、记录、附件、签名、同步队列 |
| AI 推理 | TFLite / NCNN / ONNX Runtime Mobile | 端侧轻量推理优先 |
| 图表 | ECharts | PC 后台经营看板 |
| Excel 导出 | EasyExcel / Apache POI / SheetJS | 优先后端导出 |

## 7. 总体架构

```text
┌──────────────────────────────┐
│          PC Web 后台          │
│ 工单管理 / 物料追溯 / 资质管理 │
│ 经营看板 / 冲突复核 / PDF查看  │
└───────────────┬──────────────┘
                │ HTTPS / REST API
┌───────────────▼──────────────┐
│          Java 后端服务         │
│ 登录权限 / 工单流转 / 同步服务 │
│ 文件管理 / 报表统计 / 日志审计 │
└───────┬──────────────┬───────┘
        │              │
┌───────▼───────┐  ┌──▼─────────────┐
│ MySQL 8.0      │  │ 文件系统 / MinIO │
│ 业务数据/同步日志│  │ 图片/视频/语音/PDF │
└───────────────┘  └────────────────┘

┌──────────────────────────────┐
│          Android 移动端        │
│ 接单 / 记录 / 拍照 / 录像 / 语音 │
│ 签名 / PDF / AI识别 / 离线同步  │
└───────────────┬──────────────┘
                │ 本地优先读写
┌───────────────▼──────────────┐
│        SQLite / Realm / Room   │
│ 本地工单 / 附件 / 签名 / 同步队列 │
└──────────────────────────────┘
```

## 8. 分层职责

| 层级 | 职责 | 禁止事项 |
|---|---|---|
| 展示层 | PC 后台、移动端页面 | 不直接操作服务端数据库 |
| 业务层 | 工单、物料、资质、验收、AI 复核 | 不把复杂业务写在 Controller 或页面组件中 |
| 同步层 | 增量拉取、批量上传、冲突处理 | 不做全量覆盖，不丢弃冲突数据 |
| 数据层 | MySQL、移动端本地数据库 | 不缺少版本、软删除和审计字段 |
| 文件层 | 上传、下载、预览、分片上传 | 不把大文件直接存 MySQL，不裸露文件路径 |
| AI 层 | 缺陷识别、结果记录、人工复核 | 不自动决定最终验收结果 |

---

# 第三部分：统一 API 设计与返回结构

## 9. API 路径前缀统一

| 接口类别 | 前缀 | 示例 |
|---|---|---|
| 认证与当前用户 | `/api/auth` | `/api/auth/login` |
| PC 后台 | `/api/admin` | `/api/admin/work-orders` |
| 移动端 | `/api/mobile` | `/api/mobile/work-orders` |
| 离线同步 | `/api/sync` | `/api/sync/pull` |
| 文件服务 | `/api/files` | `/api/files/{id}/download` |
| AI 辅助验收 | `/api/ai` | `/api/ai/results` |
| 经营看板 | `/api/admin/dashboard` | `/api/admin/dashboard/overview` |
| 报表导出 | `/api/admin/reports` | `/api/admin/reports/reconciliation/export` |

规则：

1. 所有后端接口必须以 `/api` 开头。
2. PC 后台接口与移动端接口可以复用 Service，但 Controller 层应区分入口和权限。
3. 同步接口统一放在 `/api/sync`。
4. 文件下载、预览不得绕过 `/api/files` 鉴权接口。
5. AI 接口不得直接改变工单最终验收状态。

## 10. 统一返回结构

如果项目已有统一响应结构，沿用现有结构；否则统一为：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-06-05 00:00:00",
  "traceId": "optional-trace-id"
}
```

字段说明：

| 字段 | 说明 |
|---|---|
| `code` | 业务状态码，不等同于 HTTP 状态码 |
| `message` | 用户或开发者可读提示 |
| `data` | 业务数据 |
| `timestamp` | 服务端响应时间 |
| `traceId` | 链路追踪 ID，可选 |

## 11. 统一分页结构

请求参数建议：

```json
{
  "page": 1,
  "size": 10,
  "keyword": "",
  "sortField": "createdAt",
  "sortOrder": "desc"
}
```

返回结构建议：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 0,
    "page": 1,
    "size": 10,
    "pages": 0
  },
  "timestamp": "2026-06-05 00:00:00"
}
```

## 12. 统一错误码

| 错误码 | 含义 |
|---|---|
| `200` | 成功 |
| `400` | 请求参数错误 |
| `401` | 未登录或 token 失效 |
| `403` | 无权限 |
| `404` | 数据不存在 |
| `409` | 数据冲突 |
| `500` | 服务端异常 |
| `1001xx` | 用户与权限错误 |
| `2001xx` | 工单业务错误 |
| `3001xx` | 文件业务错误 |
| `4001xx` | 同步业务错误 |
| `5001xx` | 物料业务错误 |
| `6001xx` | 人员资质错误 |
| `7001xx` | AI 识别业务错误 |
| `8001xx` | 经营统计错误 |

## 13. 统一请求头

所有受保护接口建议携带：

```http
Authorization: Bearer <token>
X-Device-Id: <deviceId>
X-App-Version: <appVersion>
X-Client-Type: PC | ANDROID
X-Request-Id: <uuid>
```

规则：

1. 后端不信任前端传入的 `userId`，应从 token 解析。
2. 移动端同步必须携带 `deviceId`。
3. 文件上传、下载、预览必须携带 token。
4. `X-Request-Id` 可用于排查同步和文件上传问题。

---

# 第四部分：统一数据库设计规范

## 14. 数据库表分组

| 分组 | 表名建议 | 说明 |
|---|---|---|
| 系统权限 | `sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`、`operation_log` | 登录、角色、权限、审计 |
| 项目工单 | `project_info`、`work_order`、`work_order_status_log`、`work_order_assignment`、`work_order_template`、`work_order_material` | 工单闭环主线 |
| 现场记录 | `work_order_record`、`work_order_record_detail`、`work_order_check_item` | 施工过程与检查项 |
| 附件文件 | `file_storage`、`work_order_attachment` | 多媒体与文件元数据 |
| 验收签名 | `work_order_signature`、`work_order_acceptance`、`work_order_pdf` | 签名、验收、PDF 归档 |
| 物料追溯 | `material_info`、`material_inventory`、`material_inout_record`、`material_qrcode`、`work_order_material_usage` | 物料流转 |
| 人员资质 | `employee_info`、`employee_certificate`、`certificate_warning` | 员工与证书 |
| 离线同步 | `device_info`、`sync_task`、`sync_log`、`sync_conflict`、`sync_ack_record`、`work_order_version_log` | 同步、冲突、设备 |
| AI 辅助验收 | `ai_model_info`、`ai_result`、`ai_defect_box`、`ai_review_record` | AI 结果与人工复核 |
| 经营统计 | `report_daily_summary` 或统计视图 | 看板统计 |
| 知识库 | `knowledge_case`、`maintenance_process` | 故障案例与维修工艺，可后期实现 |

## 15. 通用字段

主要业务表建议包含：

```sql
id              BIGINT PRIMARY KEY
created_at      DATETIME
updated_at      DATETIME
deleted_flag    TINYINT DEFAULT 0
created_by      BIGINT NULL
updated_by      BIGINT NULL
remark          VARCHAR(500) NULL
```

需要离线同步的表额外包含：

```sql
local_id        VARCHAR(64)
server_id       VARCHAR(64)
version         INT DEFAULT 1
sync_status     VARCHAR(32)
device_id       VARCHAR(64)
operator_id     BIGINT
conflict_flag   TINYINT DEFAULT 0
```

规则：

1. 所有关键业务数据优先逻辑删除，不物理删除。
2. 需要移动端离线操作的数据必须包含同步字段。
3. 同步字段命名在服务端、移动端、API DTO 中尽量保持一致。
4. 金额字段使用 `DECIMAL`，Java 使用 `BigDecimal`。
5. 时间字段统一使用 `DATETIME`，Java 使用 `LocalDateTime`。

## 16. 唯一编号规范

| 编号 | 字段建议 | 规则 |
|---|---|---|
| 工单编号 | `work_order_no` | 必须唯一 |
| 项目编号 | `project_code` | 建议唯一 |
| 物料编号 | `material_code` | 必须唯一 |
| 二维码值 | `qrcode_value` | 必须唯一 |
| 证书编号 | `certificate_no` | 视业务可与证书类型联合唯一 |
| 用户名 | `username` | 必须唯一 |
| 手机号 | `phone` | 可选唯一 |
| 设备编号 | `device_id` / `device_code` | 必须稳定 |
| 文件编号 | `file_id` | 必须唯一 |

## 17. 索引建议

| 表 | 建议索引 |
|---|---|
| `work_order` | `work_order_no`、`project_id`、`status`、`worker_id`、`plan_start_time`、`updated_at` |
| `work_order_record` | `work_order_id`、`worker_id`、`record_time`、`sync_status` |
| `work_order_attachment` | `work_order_id`、`record_id`、`file_type`、`sync_status` |
| `file_storage` | `file_type`、`storage_key`、`upload_user`、`created_at` |
| `material_inout_record` | `material_id`、`work_order_id`、`inout_type`、`created_at` |
| `employee_certificate` | `employee_id`、`certificate_type`、`expire_date` |
| `sync_task` | `device_id`、`sync_status`、`business_type`、`created_at` |
| `sync_conflict` | `entity_type`、`server_id`、`resolve_status`、`created_at` |
| `ai_result` | `work_order_id`、`attachment_id`、`review_status` |
| `operation_log` | `operator_id`、`module_name`、`created_at` |

---

# 第五部分：统一离线同步与冲突数据规范

## 18. 离线优先原则

移动端不是普通在线 App。核心操作必须先写本地数据库，再进入同步队列。

正确流程：

```text
用户操作
  ↓
写入本地 SQLite / Realm / Room
  ↓
写入 local_sync_queue
  ↓
界面立即反馈“已保存 / 待同步”
  ↓
网络可用时后台同步
  ↓
服务端返回 server_id / version / sync_status
  ↓
移动端更新本地状态
```

禁止流程：

```text
用户操作
  ↓
必须实时请求服务器
  ↓
服务器成功后才允许继续
```

## 19. 同步方向

| 方向 | 内容 |
|---|---|
| 服务端 → 移动端 | 我的工单、工单详情、所需物料、检查项、资质状态、基础字典、AI 复核结果、冲突处理结果 |
| 移动端 → 服务端 | 工单接收、开始施工、反馈、施工记录、附件元数据、签名、验收记录、PDF、物料使用、AI 识别结果 |

## 20. 同步状态枚举

| 枚举值 | 中文含义 | 说明 |
|---|---|---|
| `LOCAL_ONLY` | 仅本地 | 尚未进入同步队列，可选 |
| `PENDING` | 待同步 | 已进入同步队列 |
| `SYNCING` | 同步中 | 正在上传或拉取 |
| `SYNCED` | 已同步 | 服务端已确认 |
| `FAILED` | 同步失败 | 可重试 |
| `CONFLICT` | 冲突待复核 | 等待 PC 后台处理 |
| `DELETED` | 已删除 | 逻辑删除同步 |
| `IGNORED` | 已忽略 | 可选 |

## 21. 同步数据契约

`SyncPushItem` 建议结构：

```json
{
  "moduleType": "WORK_RECORD",
  "entityType": "work_order_record",
  "actionType": "CREATE",
  "localId": "local-001",
  "serverId": null,
  "version": 1,
  "updatedAt": "2026-06-05 10:00:00",
  "deletedFlag": false,
  "deviceId": "device-001",
  "operatorId": 1001,
  "payload": {},
  "checksum": "optional"
}
```

`SyncPushResponse` 建议结构：

```json
{
  "code": 200,
  "message": "partial_success",
  "data": {
    "batchId": "batch-001",
    "syncTaskId": "task-001",
    "successCount": 8,
    "failedCount": 1,
    "conflictCount": 1,
    "serverTime": "2026-06-05 10:05:00",
    "items": [
      {
        "localId": "local-001",
        "serverId": "10001",
        "version": 2,
        "status": "SYNCED",
        "message": "ok"
      },
      {
        "localId": "local-002",
        "serverId": "10002",
        "status": "CONFLICT",
        "message": "version conflict"
      }
    ]
  }
}
```

## 22. 同步模块类型

| 枚举值 | 对应业务 |
|---|---|
| `WORK_ORDER` | 工单 |
| `WORK_RECORD` | 施工记录 |
| `ATTACHMENT_META` | 附件元数据 |
| `SIGNATURE` | 电子签名 |
| `ACCEPTANCE` | 验收记录 |
| `PDF` | PDF 验收单 |
| `MATERIAL_USAGE` | 物料实际使用 |
| `QUALIFICATION` | 资质状态 |
| `AI_RESULT` | AI 识别结果 |
| `KNOWLEDGE` | 知识库 |
| `DEVICE` | 设备信息 |
| `USER_PROFILE` | 用户基础信息 |

## 23. 冲突判断规则

服务端处理移动端上传数据时，必须进行冲突判断。

```text
1. server_id 不存在，local_id 未绑定服务端记录：
   视为新增。

2. server_id 存在，客户端 version 与服务端 version 一致：
   视为正常更新，更新成功后 version + 1。

3. server_id 存在，客户端 version 小于服务端 version：
   视为可能冲突。

4. 同一工单或同一记录被多端修改：
   暂按最后写入版本展示，但必须保存旧版本、新版本和冲突字段。

5. 验收记录、PDF、签名已锁定：
   不允许被普通同步覆盖，产生 ACCEPTANCE_LOCKED_CONFLICT。
```

## 24. 冲突类型枚举

| 枚举值 | 含义 |
|---|---|
| `VERSION_CONFLICT` | 版本冲突 |
| `UPDATE_AFTER_DELETE` | 删除后更新 |
| `DELETE_AFTER_UPDATE` | 更新后删除 |
| `FIELD_CONFLICT` | 字段级冲突 |
| `PERMISSION_CONFLICT` | 权限冲突 |
| `DUPLICATE_CREATE` | 重复创建 |
| `FILE_META_CONFLICT` | 文件元数据冲突 |
| `ACCEPTANCE_LOCKED_CONFLICT` | 验收锁定冲突 |

## 25. 冲突记录字段

`sync_conflict` 至少包含：

```sql
conflict_id
entity_type
entity_id
local_id
server_id
work_order_id
business_type
device_id
operator_id
base_version
client_version
server_version
conflict_fields
client_payload
server_payload
resolved_payload
resolve_strategy
resolve_status
resolver_id
resolve_time
resolve_comment
created_at
updated_at
```

## 26. 冲突处理流程

```text
服务端检测冲突
  ↓
写入 sync_conflict
  ↓
接口返回 CONFLICT 给移动端
  ↓
移动端标记本地 conflict_flag
  ↓
PC后台显示冲突列表和冲突详情
  ↓
管理员 / 项目经理人工复核
  ↓
选择保留服务端版本 / 保留移动端版本 / 手动合并
  ↓
写入 resolved_payload
  ↓
更新业务表 version + 1
  ↓
写入 operation_log
  ↓
移动端下次 pull 获取最终结果
```

规则：

1. 冲突数据不能直接丢弃。
2. 冲突处理必须填写处理说明。
3. 已处理冲突不能重复处理，除非管理员重新打开。
4. 项目经理只能处理所属项目冲突。
5. 系统管理员可以处理全部冲突。
6. 移动端不处理复杂冲突，只显示冲突状态。

---

# 第六部分：统一文件存储与安全鉴权规范

## 27. 文件存储原则

系统会产生大量现场文件，禁止把文件二进制本体直接存入 MySQL。

正确方式：

```text
文件真实内容：本地文件系统 / MinIO / 对象存储
MySQL：只保存文件元数据、业务关联、访问权限、同步状态
```

禁止方式：

```text
将图片、视频、语音、PDF、签名图片直接以 BLOB 形式存入核心业务表
```

## 28. 文件类型统一枚举

| 枚举值 | 中文含义 |
|---|---|
| `PHOTO` | 施工照片 |
| `VIDEO` | 施工视频 |
| `AUDIO` | 语音备注 |
| `SIGNATURE` | 签名图片 |
| `PDF` | 验收单 PDF |
| `AI_IMAGE` | AI 识别结果图 |
| `CERT` | 证书附件 |
| `QRCODE` | 二维码图片 |
| `OTHER` | 其他 |

## 29. 文件元数据字段

`file_storage` 建议字段：

```sql
id
file_id
original_name
storage_name
file_type
mime_type
file_size
storage_type
storage_path
storage_key
file_hash
upload_user
upload_time
access_level
sync_status
created_at
updated_at
deleted_flag
```

`work_order_attachment` 建议字段：

```sql
id
work_order_id
record_id
file_id
file_type
watermark_text
capture_time
capture_user
longitude
latitude
local_id
server_id
version
sync_status
device_id
operator_id
created_at
updated_at
deleted_flag
```

## 30. 文件上传流程

### 30.1 普通文件上传

```text
移动端 / PC后台选择文件
  ↓
POST /api/files/upload
  ↓
后端校验 token 和业务权限
  ↓
保存文件到文件系统 / MinIO
  ↓
写入 file_storage
  ↓
按业务写入 work_order_attachment / certificate / pdf 关联表
  ↓
返回 fileId 和可鉴权访问地址
```

### 30.2 大文件分片上传

适用于视频等大文件：

```text
POST /api/files/chunk/init
  ↓
POST /api/files/chunk/upload
  ↓
POST /api/files/chunk/merge
  ↓
写入 file_storage
  ↓
返回 fileId
```

规则：

1. 图片、语音、签名、PDF 可普通上传。
2. 视频优先支持分片上传或失败重试。
3. 文件上传接口必须幂等，避免重复上传。
4. 上传成功后再同步附件元数据。
5. 弱网失败时保留本地文件和同步任务。

## 31. 文件安全鉴权

文件访问必须通过后端接口鉴权。

统一接口：

```text
GET /api/files/{fileId}/download
GET /api/files/{fileId}/preview
```

规则：

1. 不允许前端直接拼接服务器物理路径访问文件。
2. 不允许公开暴露 MinIO bucket 原始地址给无权限用户。
3. 下载前必须判断用户是否有对应业务对象权限。
4. 维修工只能访问自己工单的附件。
5. 项目经理只能访问所属项目附件。
6. 证书附件只能由授权人员查看。
7. PDF 验收单下载必须记录操作日志。
8. 删除文件优先逻辑删除或作废，不直接删除物理文件。

## 32. 照片水印规范

现场照片必须保存水印信息，至少包含：

```text
工单号
拍摄时间
拍摄人
```

可选包含：

```text
作业地点
设备 ID
经纬度
项目名称
```

规则：

1. 移动端拍照后应生成水印图作为默认上传文件。
2. 水印信息同时写入附件元数据。
3. 水印文字应清晰，但不遮挡主体。
4. 原图是否保留由配置决定。
5. 未同步照片不能被本地缓存清理。

## 33. PDF 验收单规范

PDF 验收单不是普通附件，必须能追溯到：

```text
work_order
work_order_record
work_order_signature
work_order_acceptance
work_order_pdf
file_storage
```

PDF 内容至少包含：

1. 工单编号。
2. 项目名称。
3. 作业地点。
4. 作业内容。
5. 施工人员。
6. 施工记录摘要。
7. 附件摘要。
8. AI 识别结果摘要，可选。
9. 验收人员。
10. 验收时间。
11. 验收意见。
12. 手写签名。
13. 生成时间。

规则：

1. 签名后生成不可编辑 PDF 验收单。
2. PDF 生成后锁定关键验收记录。
3. PDF 文件保存到文件系统 / MinIO。
4. PDF 元数据写入 `work_order_pdf` 和 `file_storage`。
5. PDF 下载必须鉴权并写操作日志。

---

# 第七部分：AI 辅助验收统一规范

## 34. AI 模块定位

AI 模块只能作为 **辅助验收**，不能作为最终验收结论。

统一表述：

```text
AI 图像辅助验收模块用于对施工照片中的防腐层缺陷进行初步识别，输出疑似缺陷类型、置信度和检测区域，最终验收结果仍由人工复核确认。
```

禁止表述：

```text
AI 自动验收
AI 自动判定合格 / 不合格
AI 直接完成验收流程
```

## 35. AI 结果字段

`ai_result` 至少包含：

```sql
id
work_order_id
record_id
attachment_id
model_id
model_version
defect_type
confidence
suspected_flag
inference_time_ms
review_status
review_result
reviewer_id
review_time
created_at
updated_at
deleted_flag
local_id
server_id
version
sync_status
device_id
operator_id
```

目标检测框表 `ai_defect_box` 至少包含：

```sql
id
ai_result_id
x
y
width
height
confidence
defect_type
```

## 36. AI 缺陷类型枚举

| 枚举值 | 中文含义 |
|---|---|
| `PEELING` | 起皮 |
| `CRACK` | 裂纹 |
| `RUST` | 锈蚀 |
| `DAMAGE` | 破损 |
| `BUBBLE` | 鼓泡 |
| `UNKNOWN` | 未知 / 疑似 |
| `NORMAL` | 未发现明显缺陷 |

## 37. AI 复核规则

1. AI 结果必须绑定工单、施工记录和照片附件。
2. AI 结果可以进入 PDF 验收单摘要，但不能直接决定验收结论。
3. PC 后台必须支持人工复核 AI 结果。
4. 人工复核结果写入 `ai_review_record`。
5. AI 识别失败不能阻塞工单正常验收流程。
6. 端侧 AI 结果必须支持离线保存和后续同步。
7. 模型文件目标大小 ≤ 10MB。
8. 普通 Android 手机 CPU 推理单张图片目标时间 ≤ 10 秒。

---

# 第八部分：PC 后台统一规范

## 38. PC 后台必须覆盖的模块

PC 后台不是孤立管理页面，必须和移动端、同步、文件、AI、经营统计形成闭环。

必须覆盖：

1. 登录、权限、菜单、路由。
2. 首页经营看板。
3. 工单列表、多维筛选。
4. 工单创建、编辑、模板创建。
5. 工单分派与人员资质校验。
6. 工单详情、状态流转图、施工记录时间线。
7. 附件预览：照片、视频、语音、水印信息。
8. 电子签名与 PDF 验收单查看下载。
9. 物料追溯：入库、出库、盘点、二维码。
10. 人员档案与资质证书到期预警。
11. 离线同步冲突复核后台。
12. AI 辅助验收结果查看与人工复核。
13. Excel 对账单与经营报表导出。
14. 操作日志与审计轨迹。

## 39. PC 后台页面联动要求

1. 首页统计卡片点击后应跳转到对应工单列表筛选结果。
2. 工单详情必须展示施工记录、附件、签名、PDF、物料、AI 结果和同步状态。
3. 分派工单必须展示资质校验结果。
4. 附件预览必须走鉴权接口。
5. PDF 下载必须写操作日志。
6. 冲突复核页面必须能对比服务端版本和移动端版本。
7. AI 复核页面必须提示“AI 仅作辅助，最终以人工验收为准”。

---

# 第九部分：移动端统一规范

## 40. 移动端必须覆盖的模块

移动端是现场作业入口，不是普通在线 App。

必须覆盖：

1. 登录、用户会话、设备注册。
2. 本地数据库与离线数据模型。
3. 我的工单列表和详情。
4. 接收工单、开始施工、反馈、提交待验收。
5. 现场施工记录。
6. 拍照、录像、语音备注。
7. 图片水印和本地缓存。
8. 文件上传、分片上传、失败重试。
9. 电子签名和验收记录。
10. PDF 验收单生成与本地归档。
11. 离线同步：pull、push、ack、同步队列。
12. 同步状态、失败重试、冲突提示。
13. 工单所需物料查看与实际使用记录。
14. 人员资质状态展示。
15. AI 端侧辅助识别与结果同步。

## 41. 移动端本地数据表

移动端本地数据库建议包含：

```text
local_work_order
local_work_order_record
local_work_order_attachment
local_signature
local_acceptance
local_pdf
local_material_requirement
local_material_usage
local_qualification_status
local_ai_result
local_knowledge_case
local_sync_queue
local_sync_log
local_conflict_hint
local_device_info
local_sync_checkpoint
```

所有需要同步的本地表必须包含：

```text
local_id
server_id
version
updated_at
sync_status
device_id
operator_id
deleted_flag
conflict_flag
```

## 42. 移动端同步队列规则

以下操作必须写入 `local_sync_queue`：

1. 接收工单。
2. 开始施工。
3. 工单反馈。
4. 提交待验收。
5. 新增施工记录。
6. 修改施工记录。
7. 拍照附件元数据。
8. 视频附件元数据。
9. 语音附件元数据。
10. 上传签名。
11. 提交验收记录。
12. 生成 PDF 元数据。
13. 记录物料实际使用。
14. 保存 AI 识别结果。
15. 本地删除或作废附件。

---

# 第十部分：经营看板与报表统一规范

## 43. 经营看板指标

必须支持：

1. 进行中工单数。
2. 今日出勤人数。
3. 本周完工产值。
4. 待验收工单数。
5. 异常工单数。
6. 工单完成率。
7. 项目维度统计。
8. 人员维度统计。
9. 物料消耗统计。
10. Excel 对账单导出。

## 44. 统计来源

| 指标 | 来源 |
|---|---|
| 进行中工单数 | `work_order.status` |
| 今日出勤人数 | 施工记录、接单、开始施工或考勤数据 |
| 本周完工产值 | 已完成工单金额或结算字段 |
| 待验收工单数 | `PENDING_ACCEPTANCE` 状态 |
| 异常工单数 | 施工记录异常标记或工单异常字段 |
| 物料消耗 | `work_order_material_usage` |
| 完成率 | 已完成工单 / 总工单 |

如果当前数据库尚未有金额或考勤字段，Claude Code 应明确标注并补充合理字段或 TODO，不得伪造统计来源。

---

# 第十一部分：日志、审计与安全统一规范

## 45. 操作日志必须记录的行为

1. 登录、退出。
2. 新增、编辑、删除用户。
3. 工单创建、编辑、分派、状态变更。
4. 施工记录新增、修改、删除。
5. 附件上传、删除、下载。
6. 签名提交。
7. PDF 生成、下载。
8. 物料入库、出库、盘点。
9. 证书新增、修改、删除。
10. 同步冲突处理。
11. AI 结果人工复核。
12. Excel 对账单导出。

`operation_log` 建议字段：

```sql
id
operator_id
operator_name
module_name
action_type
business_type
business_id
work_order_id
request_method
request_path
request_ip
request_params
result_status
error_message
created_at
```

## 46. 安全要求

1. 密码必须加密存储。
2. token 过期必须重新登录。
3. 移动端本地 token 建议安全存储。
4. 所有文件接口必须鉴权。
5. 所有列表查询必须做数据范围过滤。
6. 所有输入必须做参数校验。
7. 文件上传必须校验类型、大小和后缀。
8. 文件路径不能直接使用用户输入拼接。
9. SQL 查询不得拼接用户输入。
10. 异常信息不得泄露服务器物理路径、密钥或数据库敏感信息。

---

# 第十二部分：Claude Code 分模块开发流程

## 47. 推荐开发顺序

```text
1. 检查现有项目结构与技术栈
2. 建立统一响应、异常处理、权限认证、ORM 风格
3. 设计数据库初始化脚本和迁移方案
4. 实现用户、角色、权限、操作日志
5. 实现项目与工单核心表
6. 实现工单创建、分派、状态流转接口
7. 实现移动端“我的工单”接口
8. 实现施工记录表与接口
9. 实现文件元数据表与上传下载接口
10. 实现电子签名、验收记录、PDF 记录
11. 实现物料基础、库存、出入库、工单物料使用
12. 实现员工档案、证书、到期预警
13. 实现离线同步任务、日志、冲突表
14. 实现同步 pull、push、ack 接口
15. 实现 AI 结果表和人工复核接口
16. 实现经营看板统计接口
17. 生成初始化数据
18. PC 后台页面联调
19. Android 移动端联调
20. 全流程测试与验收
```

## 48. MVP 版本范围

先做最小可运行版本时，优先实现：

```text
1. 登录与权限
2. 工单创建与分派
3. 移动端我的工单
4. 施工记录填写
5. 图片上传与水印字段保存
6. 电子签名记录
7. PDF 验收单记录
8. 离线同步基础字段
9. PC 后台工单列表和详情
10. 经营看板基础统计
```

后续增强：

```text
1. 视频和语音上传
2. 分片上传和断点续传
3. 同步冲突人工复核页面
4. 物料二维码扫描
5. 人员资质自动预警
6. AI 端侧模型推理
7. AI 识别结果人工复核
8. Excel 对账单导出
9. MinIO 文件存储
10. Docker 容器化部署
```

## 49. Claude Code 单任务提示词模板

每次给 Claude Code 的任务建议使用以下结构：

```text
请先阅读 CLAUDE.md / PROJECT_RULES.md 和当前模块代码，不要立即大规模重构。

本次任务：{填写具体任务}

必须遵守：
1. 工单 work_order 是核心主线；相关数据必须能关联 work_order_id。
2. 涉及移动端离线数据时，必须包含 local_id、server_id、version、sync_status、device_id、operator_id、updated_at。
3. 文件只存元数据，不直接存 MySQL；下载和预览必须鉴权。
4. API 路径、返回结构、错误码必须符合全局规范。
5. AI 只能作为辅助验收，不得直接改变最终验收状态。
6. 修改后必须运行编译 / 测试 / 构建命令。

请输出：
1. 修改计划；
2. 修改文件清单；
3. 关键代码说明；
4. 验证命令和结果；
5. 仍需人工确认的问题。
```

---

# 第十三部分：验证命令与完成标准

## 50. Claude Code 验证规则

Claude Code 修改代码后，应优先根据项目实际技术栈运行命令。不能确定时，先查看 `README`、`package.json`、`pom.xml`、`build.gradle`、`docker-compose.yml`。

### 50.1 后端常见验证命令

```bash
mvn clean compile
mvn test
mvn spring-boot:run
```

如果是 Gradle：

```bash
./gradlew build
./gradlew test
```

### 50.2 PC 后台常见验证命令

```bash
npm install
npm run lint
npm run build
npm run dev
```

或：

```bash
pnpm install
pnpm lint
pnpm build
pnpm dev
```

### 50.3 Android 移动端常见验证命令

```bash
./gradlew assembleDebug
./gradlew test
./gradlew connectedAndroidTest
```

### 50.4 数据库验证

```bash
# 按项目实际方式执行 SQL / Flyway / Liquibase
mvn test
mvn spring-boot:run
```

必须检查：

1. SQL 能否执行。
2. 初始化数据能否插入。
3. 实体字段是否与数据库一致。
4. 索引是否覆盖高频查询。
5. 同步字段是否齐全。

## 51. 完成标准

一个模块完成至少满足：

1. 编译通过。
2. 新增或修改接口有 DTO / VO / Service 分层。
3. 表字段与实体字段一致。
4. 权限校验不缺失。
5. 操作日志或同步日志按要求记录。
6. 文件访问不裸露路径。
7. 移动端离线数据不丢失。
8. PC 后台能展示移动端产生的数据。
9. 同步冲突不被直接覆盖或丢弃。
10. Claude Code 输出了验证命令和结果。

---

# 第十四部分：禁止事项清单

Claude Code 开发过程中禁止：

1. 把项目做成普通后台 CRUD。
2. 脱离工单主线开发孤立模块。
3. 移动端核心操作强依赖实时网络。
4. 大文件直接存入 MySQL。
5. 文件下载绕过后端鉴权。
6. AI 直接决定最终验收结果。
7. 删除同步日志、冲突记录或操作审计。
8. 随意重构已有可运行项目结构。
9. 一次性生成大量未验证代码。
10. 跳过编译和测试却声称完成。
11. 前端自行伪造权限判断，不以后端权限为准。
12. 同步时全量覆盖移动端或服务端数据。
13. 冲突数据直接丢弃。
14. PDF 生成后仍允许随意修改关键验收记录。
15. 物料库存变化不写出入库流水。
16. 派工时完全忽略人员资质校验。
17. 接口路径、返回结构、枚举值各模块不一致。

---

# 第十五部分：最终自检清单

Claude Code 每次提交前必须回答以下问题：

```text
1. 这次修改是否服务于工单闭环？
2. 是否影响 PC 后台、移动端、后端、数据库、离线同步中的其他模块？
3. 是否需要关联 work_order_id？如果需要，是否已关联？
4. 是否涉及移动端离线数据？如果涉及，同步字段是否齐全？
5. 是否涉及文件？如果涉及，是否只存元数据并通过鉴权接口访问？
6. 是否涉及权限？如果涉及，是否后端校验数据范围？
7. 是否涉及状态枚举？如果涉及，PC、移动端、后端是否一致？
8. 是否涉及 AI？如果涉及，是否仍保持“辅助验收”定位？
9. 是否涉及 PDF 或签名？如果涉及，是否锁定关键验收记录？
10. 是否写入操作日志、同步日志或冲突记录？
11. 是否运行编译、测试或构建命令？
12. 是否存在未说明的 TODO 或未验证内容？
```

如果以上任意问题不能确认，应在输出中明确说明，并给出下一步修复建议。

---

# 结论

Claude Code 在本项目中应始终围绕以下核心判断开发：

```text
是否服务于工单闭环？
是否适配海上平台弱网和离线作业？
是否保证现场数据可追溯？
是否把多媒体、签名、PDF、物料、AI结果正确绑定到工单？
是否保留同步日志、冲突记录和操作审计？
是否避免把 AI 当成最终验收？
是否避免把大文件直接存入 MySQL？
是否与 PC后台、移动端、数据库、后端接口保持一致？
是否完成了编译、测试或构建验证？
```

如果某项实现只适合普通后台系统，而没有体现移动端离线、工单闭环、现场留痕和验收追溯，则必须重新调整设计。
