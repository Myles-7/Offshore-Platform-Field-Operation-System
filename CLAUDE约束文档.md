# 海上平台现场作业管理系统 - Claude Code 开发约束文档

> 本文件供 Claude Code 在本仓库继续开发时优先阅读。  
> 目标是让后续开发严格延续当前已实现的技术栈、目录结构、接口契约、数据库模型、权限模型和业务边界。  
> 如本文件与当前可运行代码不一致，以当前可运行代码为第一优先级；如与原始业务约束不一致，以业务硬约束为准。

---

## 1. 项目当前状态

本项目已经不是空项目，已形成 PC 后台、Java 后端、数据库脚本、接口文档和自动化测试的基础骨架。

### 1.1 已存在的主要目录

```text
.
├─ backend/                         Spring Boot 后端工程
├─ src/                             Vue 3 PC 后台源码
├─ db/                              根目录数据库初始化脚本
├─ docs/                            接口联调文档
├─ database-docs/                   数据库设计与同步契约文档
├─ dist/                            前端构建产物
├─ .claude/                         Claude 本地配置
├─ package.json                     PC 后台 npm 配置
├─ vite.config.ts                   Vite 配置
├─ 海上平台现场作业管理系统_Codex开发约束文档_修订版.md
└─ 海上平台现场作业管理系统_全局统一规范.md
```

### 1.2 当前技术栈

PC 后台：

```text
Vue 3
TypeScript
Vite
Element Plus
Pinia
Vue Router
Axios
ECharts
```

后端：

```text
Java 21
Spring Boot 3.3.13
Spring Web
Spring Validation
Spring AOP
Spring JDBC
MyBatis 3.0.4
Flyway
MySQL 8.0
springdoc-openapi
Apache POI
JUnit 5 + Spring Boot Test
H2 测试库
```

数据库：

```text
生产/开发默认 MySQL
迁移脚本位于 backend/src/main/resources/db/migration
根目录 db/ 中保留分模块初始化 SQL
测试使用 backend/src/test/resources/test-schema.sql
```

---

## 2. 最高优先级业务硬约束

任何开发任务都不得违反以下约束：

1. 系统不是普通 CRUD 后台，必须围绕工单闭环开发。
2. 工单是主线，施工记录、附件、签名、PDF、物料使用、AI 结果、同步冲突、操作日志、统计数据必须能追溯到工单。
3. 移动端是离线优先架构，移动端核心操作必须先写本地库和同步队列，不能依赖实时网络成功。
4. 后端必须支持同步模式，而不是把移动端做成普通实时 REST 客户端。
5. 图片、视频、语音、签名图、PDF、AI 结果图、证书附件等大文件不得直接写入 MySQL。
6. MySQL 只保存文件元数据、业务关联、权限字段和同步状态。
7. AI 只做辅助验收，不能直接改变最终验收结论。
8. AI 失败不能阻塞工单提交、签名、PDF 归档或人工验收。
9. 同步冲突必须保留、记录、可追溯，并由 PC 后台人工复核。
10. 不得让前端传入的 `userId`、`role`、`projectId` 决定数据范围，必须从 token/session 和服务端权限表解析。
11. 关键操作必须写操作日志或版本日志。
12. 不得删除已有同步日志、冲突记录、版本记录、操作日志。
13. 不得随意改变已有 API 返回结构、错误码、路径前缀、数据库命名和包结构。

---

## 3. 开发前必须执行的阅读顺序

每次 Claude Code 开始写代码前，必须按以下顺序阅读相关上下文：

1. 当前用户任务。
2. 本文件 `CLAUDE.md`。
3. 原始全局约束：
   - `海上平台现场作业管理系统_Codex开发约束文档_修订版.md`
   - `海上平台现场作业管理系统_全局统一规范.md`
4. 与任务相关的接口文档：
   - `docs/api.md`
   - `docs/admin_api.md`
   - `docs/mobile_api.md`
   - `docs/sync_api.md`
   - `docs/file_api.md`
   - `docs/ai_api.md`
5. 与任务相关的数据库文档：
   - `database-docs/01_Database_Design_Document.md`
   - `database-docs/02_Status_Enum_Dictionary.md`
   - `database-docs/04_Sync_File_Contract.md`
6. 当前代码中的同类实现，不得凭空新建一套风格。

---

## 4. 当前已实现模块概览

### 4.1 PC 后台页面

当前路由位于 `src/router/index.ts`，已包含：

```text
/login                  登录
/dashboard              经营看板
/projects               项目管理
/work-orders            工单管理
/records                施工记录
/acceptance             验收归档
/materials              物料追溯
/qualifications         人员资质
/sync/conflicts         同步冲突
/ai/review              AI 辅助验收
/system/users           用户管理
/system/roles           角色权限
/logs                   操作日志
/403                    无权限
/404                    页面不存在
```

页面源码位于：

```text
src/views/dashboard/DashboardView.vue
src/views/projects/ProjectsView.vue
src/views/work-orders/WorkOrdersView.vue
src/views/records/RecordsView.vue
src/views/acceptance/AcceptanceView.vue
src/views/materials/MaterialsView.vue
src/views/qualifications/QualificationsView.vue
src/views/sync/SyncConflictsView.vue
src/views/ai/AiReviewView.vue
src/views/system/SystemUsersView.vue
src/views/system/SystemRolesView.vue
src/views/logs/LogsView.vue
```

### 4.2 PC 后台基础组件

已存在通用组件，不要重复造轮子：

```text
src/components/page/PageShell.vue
src/components/table/DataTable.vue
src/components/form/SearchForm.vue
src/components/dialog/FormDialog.vue
src/components/detail/DetailDescriptions.vue
src/components/status/StatusTag.vue
src/components/state/AppState.vue
```

新增页面应优先复用这些组件。

### 4.3 前端 API 与状态

统一请求封装：

```text
src/api/request.ts
```

已实现 API 文件：

```text
src/api/auth.ts
src/api/admin.ts
```

状态管理：

```text
src/stores/auth.ts
src/stores/app.ts
```

类型定义：

```text
src/types/api.ts
src/types/auth.ts
src/types/menu.ts
src/types/work-order.ts
```

枚举与权限：

```text
src/constants/enums.ts
src/constants/access.ts
src/constants/page-meta.ts
src/constants/pagination.ts
```

### 4.4 后端控制器

当前后端控制器位于 `backend/src/main/java/com/offshore/platform/controller`，已包含：

```text
AcceptanceController.java
AdminSystemController.java
AdminWorkOrderController.java
AiController.java
AuditController.java
AuthController.java
DashboardController.java
FileController.java
HealthController.java
MaterialController.java
MobileWorkOrderController.java
QualificationController.java
SyncController.java
WorkOrderAttachmentController.java
WorkRecordController.java
```

新增接口应优先扩展这些 Controller 或在相同包结构下新增同域 Controller。

### 4.5 后端分层

后端当前包结构：

```text
backend/src/main/java/com/offshore/platform
├─ common      统一响应、错误码、异常、分页、上下文、日志、工具
├─ config      配置、拦截器、安全相关配置
├─ controller  HTTP 接口层
├─ dto         请求 DTO
├─ entity      数据库实体
├─ mapper      MyBatis Mapper 接口
├─ service     业务接口与实现
└─ vo          响应 VO
```

分层规则：

```text
Controller 只做参数接收、鉴权注解/基础校验、调用 Service、返回 ApiResponse
Service 处理业务规则、状态流转、权限判断、日志、同步冲突
Mapper 只做数据访问
DTO 用于请求
VO 用于响应
Entity 对应数据库表
```

禁止把复杂业务逻辑、复杂 SQL、文件处理细节写进 Controller。

---

## 5. API 契约约束

### 5.1 基础约定

本地后端默认：

```text
http://localhost:8080
```

Swagger：

```text
/swagger-ui/index.html
/v3/api-docs
```

时间格式：

```text
yyyy-MM-dd HH:mm:ss
```

认证请求头：

```text
Authorization: Bearer <token>
```

匿名接口：

```text
POST /api/auth/login
GET /api/health
开发环境 Swagger
```

### 5.2 统一返回结构

后端统一使用：

```java
ApiResponse<T>
```

返回 JSON 结构必须保持：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-06-05 16:00:00",
  "traceId": "trace-id"
}
```

前端 `src/api/request.ts` 只把 `code === 200` 视为成功。

不得改成：

```json
{ "success": true, "result": {} }
```

不得直接返回裸对象绕过 `ApiResponse`。

### 5.3 错误码

沿用 `backend/src/main/java/com/offshore/platform/common/enums/ErrorCode.java`。

当前文档中的错误码语义：

```text
200    SUCCESS
400    PARAM_ERROR
401    UNAUTHORIZED
403    FORBIDDEN
404    NOT_FOUND
10001  USER_ERROR
20001  WORK_ORDER_ERROR
30001  FILE_ERROR
40001  SYNC_ERROR
50001  AI_ERROR
50002  PDF_ERROR
60001  MATERIAL_ERROR
70001  QUALIFICATION_ERROR
80001  DASHBOARD_ERROR
500    SYSTEM_ERROR
```

新增业务错误优先扩展 `ErrorCode`，不要散落魔法数字。

### 5.4 路径前缀

```text
/api/auth/**       登录、退出、当前用户
/api/admin/**      PC 后台接口
/api/mobile/**     Android 移动端接口
/api/sync/**       离线同步接口
/api/files/**      文件上传、下载、预览、绑定
/api/ai/**         AI 辅助验收接口
/api/health        健康检查
```

不要新增与上述前缀并列但语义重复的路径，例如不要新增 `/workOrder/list`、`/adminApi/*`、`/mobileApi/*`。

---

## 6. 权限与数据范围约束

### 6.1 后端权限

必须遵守：

```text
/api/admin/** 需要登录，账号 pcEnabled=1，并按后台角色/权限校验
/api/mobile/** 需要登录，账号 mobileEnabled=1，用户身份从 token 解析
/api/sync/** 需要移动端权限，并校验 deviceId
/api/files/** 需要登录，下载/预览/删除必须校验文件业务归属
/api/ai/** 需要登录，并校验工单访问权或后台 AI 权限
```

系统管理员可访问全部。维修工仅能访问本人负责或派工记录中的工单。项目经理仅能访问所属项目。物资管理员访问物料模块。资质管理员访问资质模块。经营人员访问看板和报表。

### 6.2 当前用户

后端已有：

```text
common/context/CurrentUser.java
common/context/CurrentUserContext.java
common/util/JwtTokenUtil.java
```

新增业务必须从当前上下文取用户，不得信任请求体中的 `userId`、`roleCode`、`projectId` 作为权限依据。

### 6.3 前端权限

前端角色权限集中在：

```text
src/constants/access.ts
```

当前角色分组：

```text
SYSTEM_ADMIN / SYS_ADMIN
PROJECT_MANAGER
MAINTAINER / WORKER
MATERIAL_ADMIN / MATERIAL_MANAGER
QUALIFICATION_ADMIN / QUALIFICATION_MANAGER
ACCEPTANCE_USER / ACCEPTOR
BUSINESS_USER
```

新增路由必须配置 `meta.roles` 或 `meta.permissions`，并接入 `routeAccess`。

前端隐藏菜单不是安全边界，后端接口仍必须校验。

---

## 7. 数据库约束

### 7.1 迁移脚本

后端 Flyway 脚本位置：

```text
backend/src/main/resources/db/migration/V1__init_schema.sql
backend/src/main/resources/db/migration/V2__init_data.sql
```

根目录分模块 SQL：

```text
db/init_system_permission.sql
db/init_work_order.sql
db/init_work_order_record.sql
db/init_file_attachment.sql
db/init_acceptance_signature_pdf.sql
db/init_material_trace.sql
db/init_qualification.sql
db/init_offline_sync.sql
db/init_ai_acceptance.sql
db/init_dashboard_report.sql
db/init_data.sql
db/init_schema.sql
```

开发时：

1. 若改变生产 schema，必须新增 Flyway 迁移，不得直接改已发布迁移脚本，除非项目明确仍处于可重建阶段并经用户确认。
2. 若测试依赖表结构，必须同步更新 `backend/src/test/resources/test-schema.sql`。
3. 若根目录 `db/` 仍作为交付脚本来源，需同步更新对应模块脚本和 `db/init_schema.sql`。
4. 新增字段必须考虑后端 Entity、Mapper XML、DTO/VO、前端类型、接口文档、测试数据。

### 7.2 表设计硬约束

主要业务表应包含：

```text
id
created_at
updated_at
deleted_flag
created_by
updated_by
remark
```

需要离线同步的表必须考虑：

```text
local_id
server_id
version
sync_status
device_id
operator_id
conflict_flag
updated_at
deleted_flag
```

文件表只保存元数据，不保存二进制。

### 7.3 当前核心表分组

系统权限：

```text
sys_user
sys_role
sys_permission
sys_user_role
sys_role_permission
operation_log
```

项目与工单：

```text
project_info
work_order
work_order_assignment
work_order_status_log
work_order_template
work_order_version_log
work_order_qualification_check
```

施工记录：

```text
work_order_record
work_order_record_detail
work_order_check_item
```

附件文件：

```text
file_storage
work_order_attachment
```

验收签名 PDF：

```text
work_order_signature
work_order_acceptance
work_order_pdf
```

物料追溯：

```text
material_info
material_inventory
material_inout_record
material_qrcode
work_order_material
work_order_material_usage
```

人员资质：

```text
employee_info
employee_certificate
qualification_type
```

离线同步：

```text
device_info
sync_task
sync_log
sync_conflict
```

AI 辅助验收：

```text
ai_model_info
ai_result
ai_defect_box
ai_review_record
```

经营统计：

```text
report_daily_summary
```

---

## 8. 工单闭环约束

### 8.1 工单状态

前端枚举位于 `src/constants/enums.ts`，当前工单状态：

```text
DRAFT               草稿
PENDING             待派工
ASSIGNED            已派工
ACCEPTED            已接收
IN_PROGRESS         施工中
PENDING_ACCEPTANCE  待验收
COMPLETED           已完成
REJECTED            已驳回
CLOSED              已关闭
```

新增状态必须同步更新：

```text
数据库枚举/字典
后端业务校验
前端 src/constants/enums.ts
接口文档
测试
```

### 8.2 状态流转

工单状态变更必须：

1. 校验当前用户权限。
2. 校验当前状态是否允许流转。
3. 写入 `work_order_status_log`。
4. 必要时写入 `work_order_version_log`。
5. 更新 `version` 与 `updated_at`。
6. 关键节点写 `operation_log`。

不得只更新 `work_order.status`。

### 8.3 工单详情聚合

PC 后台工单详情必须能聚合：

```text
基础信息
项目
派工信息
状态流
施工记录
附件
签名
验收记录
PDF
物料需求
物料使用
AI 结果
同步状态
操作日志或审计轨迹
```

新增模块如果和工单有关，必须在工单详情链路中体现。

---

## 9. 离线同步约束

### 9.1 当前同步接口

已约定接口：

```text
POST /api/sync/device/register
POST /api/sync/device/heartbeat
POST /api/sync/pull
POST /api/sync/push
POST /api/sync/ack
GET  /api/sync/tasks
GET  /api/sync/logs
```

文档位于：

```text
docs/sync_api.md
database-docs/04_Sync_File_Contract.md
```

### 9.2 同步请求实体

`SyncPushRequest` 字段：

```text
deviceId
batchId
clientTime
appVersion
items
```

`SyncPushItem` 字段：

```text
moduleType
entityType
actionType
localId
serverId
version
updatedAt
payload
fileId
checksum
```

当前支持的 `entityType`：

```text
WORK_ORDER
WORK_ORDER_RECORD
WORK_ORDER_ATTACHMENT
FILE_STORAGE
WORK_ORDER_SIGNATURE
WORK_ORDER_ACCEPTANCE
WORK_ORDER_PDF
WORK_ORDER_MATERIAL_USAGE
AI_RESULT
AI_DEFECT_BOX
```

新增可同步实体必须同步更新：

```text
SyncServiceImpl
SyncController
DTO/VO
sync_conflict 处理逻辑
docs/sync_api.md
移动端本地表建议
测试
```

### 9.3 sync/push 必须支持

```text
批量上传
部分成功
单条返回结果
重复 batch 幂等
CREATE 返回 server_id/version
UPDATE 比较 version/updated_at
DELETE 使用逻辑删除
失败写 sync_log
冲突写 sync_conflict
大文件不进 payload
```

### 9.4 冲突规则

以下情况必须进入冲突或明确拒绝：

```text
client.version < server.version
客户端更新已删除记录
验收锁定后修改关键字段
同一工单多端修改关键业务字段
server_id 与 local_id 绑定关系异常
```

冲突处理策略：

```text
KEEP_SERVER
KEEP_CLIENT
MANUAL_MERGE
IGNORE_CLIENT
```

冲突处理必须保留：

```text
client_payload
server_payload
conflict_fields
resolve_strategy
resolve_user
resolve_time
resolve_remark
```

---

## 10. 文件与附件约束

### 10.1 文件接口

文件文档：

```text
docs/file_api.md
```

后端相关：

```text
FileController.java
FileServiceImpl.java
FileStorageServiceImpl.java
FilePermissionServiceImpl.java
WorkOrderAttachmentController.java
```

数据库相关：

```text
file_storage
work_order_attachment
```

### 10.2 文件存储规则

必须遵守：

1. 文件本体存本地文件系统或 MinIO，不得进 MySQL。
2. `file_storage` 只存元数据，如 `file_id`、`file_type`、`file_name`、`file_path`、`file_size`、`mime_type`、`upload_user`、`upload_time`、`sync_status`。
3. 业务附件通过 `work_order_attachment` 关联工单/施工记录。
4. 下载、预览、删除必须走鉴权接口。
5. 不得在前端拼接本地路径下载。
6. PDF 归档文件不得被普通编辑接口覆盖。
7. 未同步移动端文件不得被缓存清理。
8. 大文件上传应支持失败重试；视频建议分片。

### 10.3 附件同步顺序

移动端正确顺序：

```text
采集文件并保存本地路径
写 local_attachment 和 local_sync_queue
有网时先上传文件本体
服务端返回 fileId/remoteUrl
sync/push 上传附件元数据
服务端写 file_storage 和 work_order_attachment
PC 后台通过鉴权接口预览/下载
```

---

## 11. AI 辅助验收约束

### 11.1 AI 定位

AI 只能辅助识别防腐层缺陷，不能替代人工验收。

支持缺陷包括但不限于：

```text
起皮
裂纹
锈蚀
破损
鼓泡
```

### 11.2 当前 AI 相关模块

后端：

```text
AiController.java
AiServiceImpl.java
AiModelInfo
AiResult
AiDefectBox
AiReviewRecord
```

前端：

```text
src/views/ai/AiReviewView.vue
```

文档：

```text
docs/ai_api.md
```

### 11.3 AI 数据约束

AI 结果必须绑定：

```text
work_order_id
record_id 可选
attachment_id
model_id/model_version
defect_type
confidence
inference_time_ms
review_status
```

检测框必须保存：

```text
x
y
width
height
confidence
defect_type
```

人工复核必须保存：

```text
review_status
review_result
review_user
review_time
review_remark
```

AI 结果不得直接把工单改为 `COMPLETED` 或 `REJECTED`。

---

## 12. PC 后台开发约束

### 12.1 页面设计原则

PC 后台是工业作业管理系统，应保持：

```text
信息密度适中
表格筛选清晰
状态明确
操作可追溯
少装饰
重业务闭环
```

不要做营销式首页、过度卡片化装饰、无业务价值的大面积插画。

### 12.2 页面实现规则

新增页面应：

1. 复用 `AdminLayout`。
2. 使用 `PageShell` 作为页面外壳。
3. 列表优先使用 `DataTable`。
4. 搜索区优先使用 `SearchForm`。
5. 状态显示优先使用 `StatusTag` 和 `src/constants/enums.ts`。
6. 弹窗表单优先使用 `FormDialog`。
7. 详情展示优先使用 `DetailDescriptions`。
8. 所有请求通过 `src/api/request.ts`。
9. API 类型放入 `src/types` 或就近 API 文件。
10. 新增路由必须配置权限元信息。

### 12.3 前端请求规则

统一请求函数会：

```text
自动附加 Authorization
自动附加 X-Request-Id
只接受 ApiResponse.code === 200
401 清 session 并跳登录
403 跳 /403
默认弹出错误信息
```

不要在页面里直接使用裸 `axios`。

### 12.4 枚举同步

这些枚举已存在：

```text
workOrderStatusOptions
qualificationStatusOptions
syncStatusOptions
aiReviewStatusOptions
```

新增后端状态值必须同步到前端枚举，否则页面会显示原始值或状态样式错误。

---

## 13. 后端开发约束

### 13.1 Controller 规则

Controller 必须：

```text
使用 /api/* 规范路径
使用 DTO 接收请求
使用 @Valid 做参数校验
返回 ApiResponse<T>
从 CurrentUserContext 或服务层获取当前用户
调用 Service 完成业务
不写复杂 SQL
不直接处理复杂文件逻辑
不吞异常
```

### 13.2 Service 规则

Service 必须负责：

```text
权限判断
数据范围过滤
状态流转校验
版本比较
同步冲突判断
操作日志
业务聚合
事务边界
文件元数据绑定
验收锁定规则
```

### 13.3 Mapper 规则

Mapper XML 位于：

```text
backend/src/main/resources/mapper
```

MyBatis 配置开启：

```text
map-underscore-to-camel-case: true
```

新增字段时必须同步：

```text
Entity 字段
Mapper XML resultMap/SQL
DTO/VO
测试 schema
Flyway SQL
接口文档
```

### 13.4 日志与审计

已有：

```text
common/log/OperationLog.java
common/log/OperationLogAspect.java
OperationLogServiceImpl.java
AuditController.java
```

关键操作必须写审计：

```text
登录/退出
创建/更新/删除工单
派工
状态流转
验收复核
PDF 生成/归档
文件删除/敏感下载
物料入库/出库/盘点
证书新增/更新/删除
同步冲突处理
AI 人工复核
用户/角色/权限变更
```

日志不得保存明文密码、token、大 payload、文件二进制。

---

## 14. 移动端相关约束

当前仓库主要已有 PC 后台和后端，尚未看到独立 Android 工程。任何移动端相关开发或接口扩展都必须遵守以下规则：

1. Android 兼容 7.0+。
2. 页面默认读取本地数据库。
3. 用户操作先写本地表，再写本地同步队列。
4. 无网时提示“已离线保存，待同步”，不得提示“操作失败”。
5. App 重启后同步队列不能丢失。
6. 同步失败不能删除本地数据。
7. 文件本体与业务数据同步解耦。
8. 未同步附件不能被缓存清理。
9. 冲突由 PC 后台复核，移动端只展示冲突状态。
10. 移动端接口不得要求前端传 `userId` 获取“我的工单”。

---

## 15. 当前接口清单摘要

详细接口以 `docs/*.md` 和 Swagger 为准。

### 15.1 认证

```text
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/current
```

### 15.2 PC 后台

```text
GET    /api/admin/users
GET    /api/admin/users/{id}
POST   /api/admin/users
PUT    /api/admin/users/{id}
DELETE /api/admin/users/{id}
GET    /api/admin/roles
GET    /api/admin/permissions
PUT    /api/admin/users/{id}/roles
PUT    /api/admin/roles/{id}/permissions

GET    /api/admin/projects
GET    /api/admin/projects/{id}
POST   /api/admin/projects
PUT    /api/admin/projects/{id}
DELETE /api/admin/projects/{id}

GET    /api/admin/work-orders
GET    /api/admin/work-orders/{id}
POST   /api/admin/work-orders
PUT    /api/admin/work-orders/{id}
DELETE /api/admin/work-orders/{id}
POST   /api/admin/work-orders/{id}/assign
POST   /api/admin/work-orders/{id}/status
GET    /api/admin/work-orders/{id}/status-flow

GET    /api/admin/work-orders/{workOrderId}/records
GET    /api/admin/work-records/{recordId}
GET    /api/admin/work-records/{recordId}/timeline
POST   /api/admin/work-records/{recordId}/confirm

GET    /api/admin/work-orders/{workOrderId}/attachments
GET    /api/admin/work-orders/{workOrderId}/signatures
GET    /api/admin/work-orders/{workOrderId}/acceptance
POST   /api/admin/work-orders/{workOrderId}/acceptance/review
POST   /api/admin/work-orders/{workOrderId}/pdf/generate
GET    /api/admin/work-orders/{workOrderId}/pdf
GET    /api/admin/work-orders/{workOrderId}/pdf/download

GET    /api/admin/materials
POST   /api/admin/materials
PUT    /api/admin/materials/{id}
DELETE /api/admin/materials/{id}
POST   /api/admin/materials/inbound
POST   /api/admin/materials/outbound
POST   /api/admin/materials/stocktaking

GET    /api/admin/employees
POST   /api/admin/employees
PUT    /api/admin/employees/{id}
DELETE /api/admin/employees/{id}
GET    /api/admin/certificates/warnings

GET    /api/admin/dashboard/overview
GET    /api/admin/reports/reconciliation
GET    /api/admin/reports/reconciliation/export

GET    /api/admin/operation-logs
GET    /api/admin/work-orders/{workOrderId}/audit-trail
GET    /api/admin/sync/conflicts
POST   /api/admin/sync/conflicts/{id}/resolve
```

### 15.3 移动端

```text
GET  /api/mobile/work-orders
GET  /api/mobile/work-orders/{id}
POST /api/mobile/work-orders/{id}/accept
POST /api/mobile/work-orders/{id}/start
POST /api/mobile/work-orders/{id}/feedback
POST /api/mobile/work-orders/{id}/submit-acceptance
GET  /api/mobile/work-orders/{id}/materials
GET  /api/mobile/work-orders/{id}/qualification-check

POST /api/mobile/work-orders/{workOrderId}/records
PUT  /api/mobile/work-orders/{workOrderId}/records/{recordId}
GET  /api/mobile/work-orders/{workOrderId}/records
POST /api/mobile/work-records/{recordId}/check-items

POST /api/mobile/work-orders/{workOrderId}/attachments
GET  /api/mobile/work-orders/{workOrderId}/attachments
POST /api/mobile/work-orders/{workOrderId}/signatures
POST /api/mobile/work-orders/{workOrderId}/acceptance
POST /api/mobile/work-orders/{workOrderId}/pdf/metadata

GET  /api/mobile/work-orders/{workOrderId}/material-requirements
POST /api/mobile/work-orders/{workOrderId}/material-usage
GET  /api/mobile/my/qualification-status
GET  /api/mobile/work-orders/{workOrderId}/ai-results
```

### 15.4 同步

```text
POST /api/sync/device/register
POST /api/sync/device/heartbeat
POST /api/sync/pull
POST /api/sync/push
POST /api/sync/ack
GET  /api/sync/tasks
GET  /api/sync/logs
```

---

## 16. 测试与验证命令

### 16.1 前端

安装依赖：

```bash
npm install
```

开发启动：

```bash
npm run dev
```

构建验证：

```bash
npm run build
```

### 16.2 后端

进入后端目录：

```bash
cd backend
```

运行全部测试：

```bash
mvn test
```

启动后端：

```bash
mvn spring-boot:run
```

如需连接本地 MySQL，默认配置：

```text
DB_URL=jdbc:mysql://localhost:3308/offshore_platform?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=
SERVER_PORT=8080
```

### 16.3 已存在测试

当前测试类：

```text
AcceptanceMaterialControllerTest
AdminSystemControllerTest
AdminWorkOrderControllerTest
AiDashboardControllerTest
AuthControllerTest
BackendEndToEndIntegrationTest
DatabaseCoreRelationTest
FileControllerTest
HealthControllerTest
MapperDataAccessTest
MobileWorkOrderControllerTest
PermissionSecurityControllerTest
QualificationSyncControllerTest
WorkRecordControllerTest
```

修改后端业务时必须运行相关测试；修改跨模块流程时优先运行 `mvn test`。

---

## 17. 文档同步要求

修改接口、字段、状态、权限、同步协议时，必须同步更新文档。

接口文档：

```text
docs/api.md
docs/admin_api.md
docs/mobile_api.md
docs/sync_api.md
docs/file_api.md
docs/ai_api.md
```

数据库文档：

```text
database-docs/01_Database_Design_Document.md
database-docs/02_Status_Enum_Dictionary.md
database-docs/04_Sync_File_Contract.md
```

SQL：

```text
backend/src/main/resources/db/migration
backend/src/test/resources/test-schema.sql
db/
```

---

## 18. Git 与文件编辑约束

1. 不得删除用户已有改动。
2. 不得执行 `git reset --hard`、`git checkout -- .` 等破坏性命令，除非用户明确要求。
3. 不得提交 `node_modules/`、`dist/`、`backend/target/` 中的构建产物，除非用户明确要求。
4. 不得把测试上传文件、临时文件、日志文件加入提交。
5. 修改前先查看相关文件当前内容。
6. 保持改动范围与任务直接相关，避免无关重构。
7. 不要为了新任务改变已有包名、路径、统一响应结构和接口前缀。

---

## 19. Claude Code 每次任务输出格式

完成开发任务后，请按以下格式输出：

```text
【本次修改】
- 修改/新增文件：
- 新增/修改表：
- 新增/修改接口：
- 新增/修改页面或模块：

【模块影响】
- 对数据库的影响：
- 对后端接口的影响：
- 对 PC 后台的影响：
- 对移动端/离线同步的影响：
- 对文件/AI/验收/物料/资质的影响：

【验证情况】
- 已执行命令：
- 结果：
- 未执行原因：

【风险与待确认】
- 风险：
- TODO：
- 需要人工确认：
```

如果没有修改某类内容，明确写“无”。

---

## 20. 常见禁止行为清单

Claude Code 不得：

1. 新建一套与现有 Vue/Spring Boot 结构无关的工程。
2. 绕开 `src/api/request.ts` 直接在页面里裸调 `axios`。
3. 绕开 `ApiResponse` 返回裸 JSON。
4. 在 Controller 中写复杂业务。
5. 让前端传 `userId` 决定“我的工单”。
6. 把文件二进制写入数据库。
7. 让 AI 自动通过或驳回验收。
8. 删除或覆盖同步冲突与操作日志。
9. 修改已有接口路径但不兼容前端和文档。
10. 新增数据库字段但不更新 Entity/Mapper/DTO/VO/测试。
11. 修改工单状态但不写状态日志。
12. 更新验收归档后的关键字段而不做锁定校验。
13. 同步接口单条失败导致整批失败。
14. 把大文件塞进 `sync/push.payload`。
15. 声称已测试但没有实际执行命令。

---

## 21. 推荐后续开发优先级

在没有用户指定任务时，推荐按以下方向推进：

1. 补齐 PC 后台各页面的真实接口联调和异常状态。
2. 强化工单详情聚合页，完整展示记录、附件、签名、PDF、物料、AI、审计。
3. 完善同步冲突复核页面和后端冲突处理细节。
4. 完善文件预览、下载鉴权和分片上传流程。
5. 补齐验收 PDF 生成/归档的真实文件生成逻辑。
6. 强化物料二维码追溯链路。
7. 强化人员资质派工校验和到期预警。
8. 完善 AI 复核页面与检测框展示。
9. 增加更多端到端测试和权限边界测试。
10. 如开始 Android 工程，优先实现本地数据库、同步队列、我的工单、施工记录、附件元数据。

---

## 22. 最终判断标准

一个改动只有同时满足以下条件，才算合格：

```text
不破坏当前可运行结构
遵守工单闭环
遵守离线优先
遵守统一响应和权限模型
数据库、后端、前端、文档同步
文件只存元数据
AI 只做辅助
冲突可追溯
相关测试或构建已执行
风险和未完成项已说明
```

