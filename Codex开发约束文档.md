# 海上平台现场作业管理系统：Codex 开发约束文档（修订版）

> **文件用途**：本文件是给 Codex / AI 编程工具阅读的项目约束文档，用于统一项目背景、业务边界、架构原则、数据库规则、接口规则、离线同步规则、模块联动规则和验收标准。  
> **使用方式**：将本文件放入项目根目录，建议命名为 `CODEX_CONSTRAINTS.md`、`PROJECT_CONTEXT.md` 或 `docs/codex_constraints.md`。Codex 每次执行开发任务前，必须先阅读本文件，再阅读当前任务相关的数据库、后端、PC 后台、移动端、离线同步专项提示词。  
> **文档定位**：本文件不是答辩稿、软著说明、宣传文案，也不是完整需求规格书；它是开发执行时必须遵守的约束文件。

---

## 0. 文档约束等级

### 0.1 约束优先级

当项目源、专项提示词、现有代码和本文件出现不一致时，按以下顺序处理：

```text
1. 已存在且能运行的项目代码结构、包名、工程配置
2. 本文件中的硬性约束、禁止事项、数据一致性规则
3. 原始项目总述中的业务范围与技术路线
4. 数据库 / 后端 / PC后台 / 移动端 / 离线同步专项开发步骤
5. Codex 自行推断或默认脚手架习惯
```

Codex 不得因为某个模块开发方便而违反本文件的硬性规则。

### 0.2 关键术语解释

| 术语 | 含义 |
|---|---|
| 工单闭环 | 工单从创建、分派、接收、施工、记录、验收、归档、统计形成完整链路 |
| 离线优先 | 移动端核心操作先写本地数据库，再由同步服务上传，不依赖实时网络 |
| 同步模式 | 后端提供增量拉取、批量上传、确认、冲突处理接口，而不是每个移动端操作都实时请求服务器 |
| 多媒体留痕 | 拍照、录像、语音备注、签名、PDF 等现场证据与工单绑定 |
| 人工复核 | AI 识别、同步冲突、验收争议等最终由人员确认，不由系统自动替代 |
| 元数据存储 | MySQL 只保存文件信息、路径、类型、大小、关联对象，不保存大文件二进制本体 |

---

## 1. Codex 工作规则

### 1.1 开发前必须做

Codex 每次开始写代码前，必须先完成以下检查：

```text
1. 阅读当前任务说明；
2. 阅读本约束文档；
3. 搜索项目根目录中是否已有 README、需求文档、数据库脚本、后端接口、PC后台、移动端、离线同步相关提示词；
4. 判断当前项目已有技术栈、包结构、目录结构、统一响应格式、异常处理、权限方式、ORM方式；
5. 明确本次任务属于数据库、后端、PC后台、移动端、离线同步、AI、联调中的哪一类；
6. 只修改当前任务必要文件，不做无关重构。
```

如果无法判断项目技术栈或已有结构，Codex 应先输出分析结果和待确认点，不得直接生成一套全新的工程结构覆盖原项目。

### 1.2 开发中必须遵守

1. **必须围绕工单闭环开发。** 施工记录、附件、签名、PDF、物料使用、AI 结果、同步冲突、经营统计都必须能追溯到工单。
2. **必须保留离线优先设计。** 移动端核心操作不能设计成“实时请求服务器成功后才算完成”。
3. **必须复用现有结构。** 如果已有 Controller、Service、Mapper、Repository、DTO、VO、ApiResponse、request 封装、路由、状态管理，应在其基础上补齐。
4. **必须分阶段开发。** 不要一次性生成数据库、后端、PC、移动端全部代码。
5. **必须自检。** 每完成一个阶段，应运行可用的编译、测试、启动或静态检查命令。
6. **必须说明影响范围。** 修改数据库字段时，要说明影响后端接口、PC 页面、移动端本地表、同步字段。
7. **必须明确未完成项。** 若生成占位代码，必须使用 `TODO` 注明后续实现点。

### 1.3 禁止事项

Codex 不得执行以下行为：

```text
1. 不得把项目做成普通后台 CRUD 系统；
2. 不得忽略移动端离线作业和同步队列；
3. 不得把图片、视频、语音、PDF、签名图片、AI结果图等大文件直接存入 MySQL；
4. 不得把 AI 识别结果作为最终验收结论；
5. 不得删除同步日志、冲突记录、版本记录、操作日志；
6. 不得绕过权限校验，让前端传 userId 决定数据范围；
7. 不得在 Controller 中堆复杂业务逻辑、复杂 SQL 或文件处理细节；
8. 不得随意改变已有接口返回格式、异常体系、包名、数据库命名规则；
9. 不得声称“已测试”“已联调”但没有实际执行命令；
10. 不得用无法运行的伪代码冒充实现。
```

### 1.4 Codex 每次输出格式

每次完成开发任务后，必须按以下格式输出：

```text
【本次修改】
- 修改/新增文件：
- 新增/修改表：
- 新增/修改接口：
- 新增/修改页面或移动端模块：

【模块影响】
- 对数据库的影响：
- 对后端接口的影响：
- 对PC后台的影响：
- 对移动端的影响：
- 对离线同步的影响：

【验证情况】
- 已执行命令：
- 结果：
- 未执行原因（如有）：

【风险与待确认】
- 风险：
- TODO：
- 需要人工确认：
```

---

## 2. 项目定位与范围

### 2.1 项目名称

**海上平台现场作业管理系统**

### 2.2 一句话定位

本项目是一套面向海上平台、工业维修、防腐施工、设备检修等弱网现场的作业闭环管理系统，通过 **Android 移动端 + PC Web 后台 + Java 后端服务 + MySQL 中心数据库 + 移动端本地数据库 + 离线同步 + 文件存储 + AI 辅助验收**，实现从工单派发、现场施工、多媒体留痕、电子签名、PDF 验收、数据同步、冲突复核到经营统计的全过程数字化管理。

### 2.3 核心业务问题

本系统要回答以下问题：

```text
谁去干？            → 人员、角色、派工、资质
去哪干？            → 项目、作业地点、工单详情
干什么？            → 作业内容、检查项、施工记录
用什么物料？        → 物料需求、领用、实际使用、二维码追溯
过程怎么证明？      → 照片、视频、语音、水印、时间线
谁来验收？          → 验收人员、电子签名、PDF验收单
断网怎么办？        → 本地数据库、同步队列、自动重试
数据冲突怎么办？    → 版本比较、冲突记录、人工复核
结果怎么归档？      → PDF、附件、日志、版本记录
经营怎么统计？      → 工单状态、出勤、产值、物料消耗、Excel导出
AI做什么？          → 辅助识别疑似防腐层缺陷，不能替代人工验收
```

### 2.4 项目关键词

```text
工单闭环
移动作业
弱网可用
离线优先
增量同步
同步队列
失败重试
冲突复核
多媒体留痕
照片水印
电子签名
PDF验收
物料追溯
人员资质
证书预警
经营看板
AI辅助识别
数据可追溯
```

---

## 3. 系统必须包含的能力

### 3.1 移动端现场作业子系统

移动端主要服务维修工、施工人员、现场负责人、验收人员。必须支持：

| 能力 | 约束 |
|---|---|
| 工单接收与反馈 | 维修工只能查看和处理自己的工单；工单详情必须包含地点、内容、所需物料、计划时间、注意事项 |
| 现场多媒体记录 | 支持拍照、录像、语音备注；所有附件必须自动关联工单，可选关联施工记录 |
| 照片水印 | 每张现场照片至少记录或生成时间 + 工单号水印；建议包含拍摄人、设备号、地点 |
| 离线操作 | 无网络或弱网时可查看本地工单、填写施工记录、保存附件、签名、AI结果 |
| 电子签名 | 支持手写签名，签名绑定工单和验收记录 |
| PDF 验收单 | 签名后生成或请求生成不可编辑 PDF，并进入归档与同步流程 |
| 同步状态 | 页面必须显示待同步、同步中、已同步、失败、冲突等状态 |

### 3.2 PC 后台管理系统

PC 后台主要服务系统管理员、项目经理、调度员、物资管理员、资质管理员、验收人员、经营人员。必须支持：

| 能力 | 约束 |
|---|---|
| 工单管理中心 | 创建、编辑、分派、关闭、状态流转、模板创建、多条件筛选 |
| 状态流转图 | 展示工单从待派工到已完成/驳回/关闭的状态变化，记录操作人和时间 |
| 物料追溯 | 二维码标签、入库、出库、盘点、工单物料需求、实际使用记录 |
| 人员资质管理 | 员工档案、海上作业证、焊工证等证书、证书附件、到期预警、派工资质校验 |
| 经营看板 | 进行中工单数、今日出勤人数、本周完工产值、工单完成率、物料消耗、Excel 导出 |
| 冲突复核 | 查看同步冲突、版本对比、保留服务端/移动端/手动合并、处理记录 |
| AI 复核 | 查看 AI 识别结果，人工确认属实/误报，填写复核意见 |

### 3.3 后端服务

后端不是简单 CRUD 接口，必须承担以下能力：

```text
1. 登录认证与权限控制；
2. 工单流转和业务规则；
3. 移动端同步服务；
4. 文件上传、下载、预览、鉴权；
5. 电子签名与 PDF 归档；
6. 物料追溯与库存流水；
7. 人员资质与证书预警；
8. AI 结果保存与人工复核；
9. 经营统计与 Excel 导出；
10. 操作日志、同步日志、版本日志、冲突记录。
```

### 3.4 数据同步与离线架构

离线同步是项目技术核心，必须满足：

```text
移动端本地数据库是现场作业的第一写入点；
后端 API 采用同步模式，而不是实时请求模式；
离线数据恢复网络后增量同步；
多端修改同一数据时要做版本判断；
冲突数据不能丢弃，必须进入人工复核；
附件文件与业务数据同步解耦；
同步日志、失败原因、重试状态必须可追溯。
```

### 3.5 AI 图像辅助验收

AI 模块必须定位为“辅助验收”：

| 约束 | 说明 |
|---|---|
| 缺陷识别范围 | 防腐层缺陷初步识别，如起皮、裂纹、锈蚀、破损、鼓泡等 |
| 模型类型 | YOLO 轻量模型、MobileNet 类 CNN、TFLite / NCNN / ONNX Runtime Mobile 等 |
| 模型大小目标 | ≤ 10MB |
| 推理目标 | 普通 Android 手机 CPU 单张图片 ≤ 10 秒 |
| 最终结论 | 必须由人工复核确认，AI 不得直接改变最终验收状态 |
| 失败处理 | AI 失败不能阻塞工单提交、签名或验收流程 |

---

## 4. 推荐总体架构

### 4.1 默认技术路线

如现有项目已确定技术栈，优先沿用现有技术栈；如尚未确定，可按以下默认路线：

| 层级 | 默认技术 | 说明 |
|---|---|---|
| PC 后台 | Vue 3 / React + Element Plus / Ant Design | 工单、物料、人员、看板、冲突复核、AI 复核 |
| 后端 | Java + Spring Boot | 业务接口、同步服务、文件服务、统计服务 |
| 数据库 | MySQL 8.0 | 中心业务数据、同步日志、审计数据 |
| ORM | MyBatis-Plus / MyBatis / JPA | 以项目已有方案为准 |
| 权限 | JWT / Spring Security | PC 后台与移动端统一认证，不同数据范围 |
| 文件存储 | 本地文件系统 / MinIO | 图片、视频、语音、签名、PDF、证书附件 |
| 移动端 | Android 7.0+，Kotlin / Java | 现场作业、离线存储、同步、签名、PDF、AI |
| 移动端本地库 | SQLite / Realm / Room | 工单、施工记录、附件、签名、同步队列 |
| AI 推理 | TFLite / NCNN / ONNX Runtime Mobile | 端侧轻量推理优先，服务端复核可选 |
| 图表 | ECharts | 经营看板 |
| Excel | EasyExcel / Apache POI / SheetJS | 后端导出优先 |

### 4.2 系统架构图

```text
┌──────────────────────────────┐
│          PC Web 后台           │
│ 工单管理 / 物料追溯 / 资质管理 │
│ 经营看板 / 冲突复核 / AI复核   │
└───────────────┬──────────────┘
                │ HTTPS / REST API
┌───────────────▼──────────────┐
│          Java 后端服务         │
│ 登录权限 / 工单流转 / 同步服务 │
│ 文件管理 / PDF归档 / 报表统计  │
│ 日志审计 / AI结果 / 冲突复核   │
└───────┬──────────────┬───────┘
        │              │
┌───────▼───────┐  ┌──▼─────────────┐
│ MySQL 8.0      │  │ 文件系统/MinIO  │
│ 业务数据/同步日志│  │ 图片/视频/语音/PDF │
└───────────────┘  └────────────────┘

┌──────────────────────────────┐
│          Android 移动端        │
│ 接单 / 施工记录 / 拍照录像语音 │
│ 签名 / PDF / AI识别 / 同步中心 │
└───────────────┬──────────────┘
                │ 本地优先读写
┌───────────────▼──────────────┐
│      SQLite / Realm / Room     │
│ 本地工单 / 附件 / 签名 / PDF    │
│ AI结果 / 物料使用 / 同步队列    │
└──────────────────────────────┘
```

### 4.3 分层职责

| 层级 | 职责 | 硬性约束 |
|---|---|---|
| 展示层 | PC 后台、Android 移动端 | 不直接操作中心数据库；统一调用接口或本地 Repository |
| 业务层 | 工单、验收、物料、资质、AI复核 | 状态变更、审核、派工、复核必须写操作日志 |
| 同步层 | pull、push、ack、冲突处理 | 支持批量、部分成功、失败重试、幂等、冲突留痕 |
| 数据层 | MySQL、本地 SQLite/Realm/Room | 支持软删除、版本号、同步状态、追溯字段 |
| 文件层 | 文件上传、下载、预览、归档 | MySQL 只存元数据；文件访问必须鉴权 |
| AI 层 | 识别结果、检测框、人工复核 | AI 不作最终验收结论 |

---

## 5. 角色与权限约束

### 5.1 角色定义

| 角色 | 主要权限 | 数据范围 |
|---|---|---|
| 系统管理员 | 用户、角色、权限、系统配置、全量数据 | 全部 |
| 项目经理 | 项目工单、派工、验收、统计、冲突复核 | 所属项目 |
| 调度员 | 工单创建、编辑、分派、状态跟踪 | 授权项目/部门 |
| 维修工/施工人员 | 查看我的工单、施工记录、附件、签名、同步 | 仅本人相关工单 |
| 现场负责人 | 现场复核、验收确认、电子签名 | 授权项目/工单 |
| 物资管理员 | 物料入库、出库、盘点、二维码 | 物料模块 |
| 资质管理员 | 员工档案、证书、到期预警 | 人员资质模块 |
| 验收人员 | 查看施工过程、签名、审核验收 | 授权工单 |
| 经营人员 | 经营看板、Excel 对账单 | 经营统计数据 |

### 5.2 权限实现要求

```text
1. 所有受保护接口必须校验登录态；
2. 不得信任前端传入的 userId / role / projectId 作为权限依据；
3. 当前用户、角色、数据范围必须从 token/session + 服务端权限表解析；
4. 维修工只能访问自己的工单、附件、施工记录、签名、同步任务；
5. 项目经理只能访问所属项目数据；
6. 文件下载、PDF下载、证书附件下载必须鉴权；
7. 关键操作写入 operation_log；
8. PC 菜单权限和后端接口权限必须同时存在，不能只做前端隐藏。
```

---

## 6. 核心业务流程

### 6.1 工单闭环主流程

```text
PC后台创建工单
  ↓
填写项目、地点、作业内容、计划时间、所需物料、所需资质
  ↓
派工前校验人员资质
  ↓
分派给维修工
  ↓
维修工移动端同步/接收工单
  ↓
现场施工
  ↓
拍照 / 录像 / 语音备注 / 填写施工记录
  ↓
照片记录时间和工单号水印
  ↓
AI辅助识别疑似防腐层缺陷
  ↓
提交待验收
  ↓
现场负责人或验收人员电子签名
  ↓
生成不可编辑PDF验收单
  ↓
移动端将签名、PDF、记录、附件、AI结果同步到服务器
  ↓
PC后台查看全过程、附件、签名、PDF、物料、AI结果
  ↓
经营看板统计并导出Excel对账单
```

### 6.2 离线作业流程

```text
维修工提前同步工单和基础数据
  ↓
进入海上平台 / 弱网 / 无网现场
  ↓
移动端从本地数据库读取工单
  ↓
本地记录施工数据、附件、签名、PDF、物料使用、AI结果
  ↓
每次操作写入本地同步队列
  ↓
网络恢复后自动或手动同步
  ↓
先上传文件，再同步文件元数据和业务数据
  ↓
服务端按 local_id / server_id / version / updated_at 判断新增、更新、冲突
  ↓
无冲突：写入 MySQL 并返回 server_id/version
  ↓
有冲突：写入 sync_conflict，PC后台人工复核
  ↓
移动端下次 pull 获取最终处理结果
```

### 6.3 物料追溯流程

```text
物料基础信息维护
  ↓
入库并生成/绑定二维码
  ↓
工单创建时选择所需物料
  ↓
维修工领料/出库
  ↓
移动端记录实际使用物料
  ↓
同步到服务端并绑定工单
  ↓
PC后台按物料编号、二维码、工单、项目追溯
  ↓
经营看板统计物料消耗
```

### 6.4 人员资质流程

```text
建立员工档案
  ↓
录入证书编号、类型、发证机构、发证日期、到期日期、附件
  ↓
系统定期生成到期/临期预警
  ↓
派工时校验人员是否具备所需资质
  ↓
移动端工单详情显示资质状态
```

---

## 7. 数据库设计约束

### 7.1 表分组

数据库必须同时支撑 PC 后台、移动端、离线同步、电子签名、PDF、物料、人员资质、经营看板、AI 辅助验收。

| 分组 | 表名建议 | 说明 |
|---|---|---|
| 系统权限 | `sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`、`operation_log` | 登录、角色、权限、审计 |
| 项目工单 | `project_info`、`work_order`、`work_order_status_log`、`work_order_assignment`、`work_order_template`、`work_order_material` | 工单闭环主线 |
| 现场记录 | `work_order_record`、`work_order_record_detail`、`work_order_check_item` | 施工记录、检查项、异常说明 |
| 附件文件 | `file_storage`、`work_order_attachment` | 文件元数据、附件与工单/记录关系 |
| 验收签名 | `work_order_signature`、`work_order_acceptance`、`work_order_pdf` | 签名、验收记录、PDF归档 |
| 物料追溯 | `material_info`、`material_inventory`、`material_inout_record`、`material_qrcode`、`work_order_material_usage` | 物料流转与工单使用 |
| 人员资质 | `employee_info`、`employee_certificate`、`certificate_warning` | 员工档案、证书、预警 |
| 离线同步 | `device_info`、`sync_task`、`sync_log`、`sync_conflict`、`sync_ack_record`、`work_order_version_log` | 设备、任务、日志、冲突、版本 |
| AI 辅助验收 | `ai_model_info`、`ai_result`、`ai_defect_box`、`ai_review_record` | 模型、识别结果、检测框、人工复核 |
| 经营统计 | `report_daily_summary` 或统计视图 | 工单、出勤、产值、物料消耗 |
| 知识库 | `knowledge_case`、`maintenance_process` | 故障案例、维修工艺，后期可实现 |

### 7.2 通用字段

主要业务表建议包含：

```sql
id
created_at
updated_at
deleted_flag
created_by
updated_by
remark
```

需要离线同步的业务表必须额外考虑：

```sql
local_id
server_id
version
sync_status
device_id
operator_id
conflict_flag
```

### 7.3 命名规范

| 类型 | 规范 |
|---|---|
| 数据库表名 | 小写下划线，如 `work_order_record` |
| 数据库字段 | 小写下划线，如 `work_order_id`、`sync_status` |
| Java 类 | 大驼峰，如 `WorkOrderRecord` |
| Java 字段 | 小驼峰，如 `workOrderId` |
| JSON 字段 | 优先小驼峰，如 `workOrderId`；如果项目已有 snake_case，则保持一致 |
| 枚举值 | 大写下划线，如 `IN_PROGRESS`、`PENDING_ACCEPTANCE` |
| 文件类型 | 大写枚举，如 `PHOTO`、`VIDEO`、`PDF` |

### 7.4 唯一性约束

以下字段应设计唯一约束或逻辑唯一校验：

```text
work_order_no       工单编号
project_code        项目编号，可选
material_code       物料编号
qrcode_value        二维码值
certificate_no      证书编号，必要时与证书类型联合唯一
username            用户名
phone               手机号，可选
device_id           移动端设备ID
```

### 7.5 索引建议

| 表 | 索引字段 |
|---|---|
| `work_order` | `work_order_no`、`project_id`、`status`、`worker_id`、`plan_start_time`、`updated_at` |
| `work_order_record` | `work_order_id`、`worker_id`、`record_time`、`sync_status` |
| `work_order_attachment` | `work_order_id`、`record_id`、`file_type`、`sync_status` |
| `file_storage` | `file_type`、`storage_path`、`created_at` |
| `material_inout_record` | `material_id`、`work_order_id`、`inout_type`、`created_at` |
| `employee_certificate` | `employee_id`、`certificate_type`、`expire_date` |
| `sync_task` | `device_id`、`status`、`sync_direction`、`created_at` |
| `sync_log` | `task_id`、`device_id`、`entity_type`、`created_at` |
| `sync_conflict` | `entity_type`、`server_id`、`resolve_status`、`created_at` |
| `ai_result` | `work_order_id`、`attachment_id`、`review_status` |
| `operation_log` | `operator_id`、`module_name`、`created_at` |

### 7.6 逻辑删除规则

```text
1. 关键业务数据默认使用 deleted_flag 逻辑删除；
2. 列表查询默认过滤 deleted_flag=0；
3. 同步接口必须能同步删除状态，避免移动端出现已删除数据；
4. 工单、验收、签名、PDF、同步冲突、操作日志不建议物理删除；
5. 删除已同步附件时，应作废元数据，不直接删除历史关联。
```

---

## 8. 状态枚举约束

### 8.1 工单状态

```text
DRAFT                 草稿，可选
PENDING               待派工
ASSIGNED              已派工
ACCEPTED              已接收，可选，移动端接单后使用
IN_PROGRESS           施工中
PENDING_ACCEPTANCE    待验收
COMPLETED             已完成
REJECTED              已驳回
CLOSED                已关闭
```

状态变化必须写入 `work_order_status_log`。

### 8.2 同步状态

服务端与移动端需保持语义一致：

```text
LOCAL_ONLY    仅本地，移动端可用
PENDING       待同步
SYNCING       同步中
SYNCED        已同步
FAILED        同步失败
CONFLICT      冲突待复核
DELETED       已删除/待删除同步
IGNORED       已忽略，可选
```

### 8.3 文件类型

```text
PHOTO         施工照片
VIDEO         施工视频
AUDIO         语音备注
SIGNATURE     签名图片
PDF           验收单PDF
AI_IMAGE      AI识别结果图
CERT          证书附件
QRCODE        二维码图片
OTHER         其他
```

### 8.4 证书类型

```text
OFFSHORE_WORK      海上作业证
WELDER             焊工证
ELECTRICIAN        电工证
HIGH_ALTITUDE      高处作业证
SAFETY_TRAINING    安全培训证
OTHER              其他
```

### 8.5 AI 缺陷类型

```text
PEELING      起皮
CRACK        裂纹
RUST         锈蚀
DAMAGE       破损
BUBBLE       鼓泡
UNKNOWN      未知/疑似
NORMAL       未发现明显缺陷
```

---

## 9. API 设计约束

### 9.1 通用 API 原则

```text
1. API 路径按模块分组；
2. 返回结构统一；
3. 分页参数统一；
4. 创建、修改、删除、状态变更、审核、复核必须记录日志；
5. PC后台接口和移动端接口可以复用 Service，但必须区分数据权限；
6. 文件下载、PDF下载、证书附件下载必须鉴权；
7. 同步接口必须支持批量、部分成功和幂等；
8. Controller 只做参数接收、校验、调用 Service，不写复杂业务。
```

### 9.2 路径前缀建议

| 前缀 | 用途 |
|---|---|
| `/api/auth` | 登录、退出、当前用户 |
| `/api/admin` | PC后台管理接口 |
| `/api/mobile` | 移动端业务接口 |
| `/api/sync` | 离线同步接口 |
| `/api/files` | 文件上传、下载、预览 |
| `/api/ai` | AI模型、识别结果、复核 |
| `/api/dashboard` 或 `/api/admin/dashboard` | 经营看板 |

### 9.3 推荐接口清单

```text
POST   /api/auth/login
POST   /api/auth/logout
GET    /api/auth/current

GET    /api/admin/projects
POST   /api/admin/projects
PUT    /api/admin/projects/{id}
DELETE /api/admin/projects/{id}

GET    /api/admin/work-orders
POST   /api/admin/work-orders
GET    /api/admin/work-orders/{id}
PUT    /api/admin/work-orders/{id}
POST   /api/admin/work-orders/{id}/assign
POST   /api/admin/work-orders/{id}/status
GET    /api/admin/work-orders/{id}/status-flow
GET    /api/admin/work-orders/{id}/records
GET    /api/admin/work-orders/{id}/attachments
GET    /api/admin/work-orders/{id}/pdf
GET    /api/admin/work-orders/{id}/ai-results

GET    /api/mobile/work-orders
GET    /api/mobile/work-orders/{id}
POST   /api/mobile/work-orders/{id}/accept
POST   /api/mobile/work-orders/{id}/start
POST   /api/mobile/work-orders/{id}/feedback
POST   /api/mobile/work-orders/{id}/submit-acceptance
POST   /api/mobile/work-orders/{id}/records
POST   /api/mobile/work-orders/{id}/attachments
POST   /api/mobile/work-orders/{id}/signatures
POST   /api/mobile/work-orders/{id}/acceptance
POST   /api/mobile/work-orders/{id}/material-usage

POST   /api/files/upload
POST   /api/files/batch-upload
GET    /api/files/{id}/download
GET    /api/files/{id}/preview
POST   /api/files/chunk/init
POST   /api/files/chunk/upload
POST   /api/files/chunk/merge

POST   /api/sync/device/register
POST   /api/sync/device/heartbeat
POST   /api/sync/pull
POST   /api/sync/push
POST   /api/sync/ack
GET    /api/sync/tasks
GET    /api/sync/logs
GET    /api/admin/sync/conflicts
GET    /api/admin/sync/conflicts/{id}
POST   /api/admin/sync/conflicts/{id}/resolve

GET    /api/admin/materials
POST   /api/admin/materials
POST   /api/admin/materials/inbound
POST   /api/admin/materials/outbound
POST   /api/admin/materials/stocktaking
GET    /api/admin/materials/trace

GET    /api/admin/employees
POST   /api/admin/employees
GET    /api/admin/employees/{id}/certificates
POST   /api/admin/employees/{id}/certificates
GET    /api/admin/certificates/warnings

POST   /api/ai/results
GET    /api/ai/results/{id}
POST   /api/admin/ai/results/{id}/review
GET    /api/admin/ai/models
POST   /api/admin/ai/models

GET    /api/admin/dashboard/overview
GET    /api/admin/dashboard/work-order-statistics
GET    /api/admin/dashboard/material-statistics
GET    /api/admin/reports/reconciliation/export
```

### 9.4 统一返回结构建议

如项目已有统一格式，以现有格式为准；没有时建议：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-06-04 00:00:00",
  "traceId": "trace-id"
}
```

分页返回建议：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 0,
    "page": 1,
    "size": 10
  }
}
```

同步接口返回建议：

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
    "items": [
      {
        "localId": "local-001",
        "serverId": "10001",
        "status": "SYNCED",
        "version": 2,
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

---

## 10. 离线同步专项约束

### 10.1 同步方向

| 方向 | 内容 |
|---|---|
| 服务端 → 移动端 | 我的工单、工单详情、工单所需物料、检查项、人员资质状态、基础字典、知识库摘要、冲突处理结果 |
| 移动端 → 服务端 | 工单接收、开始施工、工单反馈、施工记录、附件元数据、签名、PDF、物料使用、AI结果、状态变更 |

### 10.2 所有可同步数据必须包含

```text
local_id
server_id
module_type
entity_type
version
updated_at
deleted_flag
sync_status
device_id
operator_id
payload 或业务字段
```

### 10.3 sync/pull 规则

```text
1. 用于移动端拉取服务端增量数据；
2. 参数至少包含 deviceId、lastSyncTime、moduleList、cursor；
3. userId 必须从 token 解析，不信任前端；
4. 首次同步可返回较多数据，但必须受权限范围控制；
5. 后续同步按 updated_at、version、cursor 增量返回；
6. 大文件不返回二进制，只返回元数据和下载/预览标识；
7. 返回数据必须便于移动端写入本地数据库；
8. 移动端写入成功后应调用 ack。
```

### 10.4 sync/push 规则

```text
1. 用于移动端批量上传本地变更；
2. 支持部分成功，不因单条失败导致整批全部失败；
3. 每条数据必须返回处理结果；
4. CREATE 成功后返回 server_id 和 version；
5. UPDATE 必须比较 version / updated_at；
6. DELETE 使用逻辑删除；
7. 重复提交同一 batch 必须幂等；
8. 附件大文件不直接放在 payload 中，只传 fileId 或附件元数据；
9. 签名、PDF、AI结果、物料使用都必须支持同步；
10. 同步失败写 sync_log。
```

### 10.5 sync/ack 规则

```text
1. 移动端 pull 数据写入本地成功后再 ack；
2. ack 失败不能导致本地数据丢失；
3. 服务端记录 sync_ack_record；
4. ack 必须幂等；
5. ack 成功后移动端更新 local_sync_checkpoint。
```

### 10.6 冲突判断规则

```text
1. server_id 不存在且 local_id 未绑定服务端记录：视为新增；
2. server_id 存在且客户端 version 与服务端 version 一致：正常更新，version + 1；
3. server_id 存在但客户端 version 小于服务端 version：视为可能冲突；
4. 同一工单或同一记录被多端修改：生成冲突记录；
5. 验收已锁定、PDF已归档的数据不得被普通同步覆盖；
6. 冲突不能丢弃，必须保存 client_payload、server_payload、conflict_fields；
7. 默认展示可采用最后写入版本，但必须保留旧版本并进入人工复核；
8. PC后台处理冲突后，移动端下次 pull 获取最终版本。
```

### 10.7 附件同步顺序

```text
1. 移动端采集文件并保存本地路径；
2. 写入 local_attachment 和 local_sync_queue；
3. 有网时先上传文件本体；
4. 上传成功后获得 fileId / remoteUrl；
5. 再通过 sync/push 上传附件元数据；
6. 服务端写入 file_storage 和 work_order_attachment；
7. PC后台通过鉴权接口预览或下载。
```

---

## 11. 文件存储约束

### 11.1 文件类型

系统会产生以下文件：

```text
施工照片
施工视频
语音备注
电子签名图片
PDF验收单
AI识别结果图
证书附件
物料二维码图片
```

### 11.2 存储规则

```text
1. MySQL 不保存文件二进制；
2. MySQL 保存文件元数据：file_id、file_type、file_name、file_path、file_size、mime_type、upload_user、upload_time、work_order_id、record_id、sync_status；
3. 文件本体保存在本地文件系统或 MinIO；
4. 文件下载和预览必须经过权限校验；
5. 移动端未同步文件不得被缓存清理；
6. 已同步文件可按策略清理本地缓存，但必须保留元数据；
7. 视频建议支持压缩、分片上传、失败重试；
8. PDF 归档文件不允许随意删除或覆盖。
```

---

## 12. 模块开发约束

### 12.1 数据库模块

数据库开发时必须按以下顺序优先实现：

```text
1. 系统权限与操作日志
2. 项目与工单核心表
3. 施工记录与检查项
4. 文件元数据与工单附件
5. 电子签名、验收记录、PDF归档
6. 物料追溯
7. 人员资质与证书预警
8. 离线同步核心表
9. AI识别结果与人工复核
10. 经营统计视图或汇总表
11. 初始化数据
12. 数据库迁移与测试
```

硬性规则：

```text
1. 所有表使用 utf8mb4；
2. 关键业务表使用 InnoDB；
3. 每张业务表有主键 id；
4. 高频查询字段加索引；
5. 不强制数据库外键，可使用逻辑外键；
6. 时间字段统一；
7. 金额字段使用 decimal / BigDecimal；
8. 需要离线同步的表必须有同步字段。
```

### 12.2 后端模块

后端开发顺序建议：

```text
1. 统一响应、分页、异常、参数校验、错误码
2. 接口文档能力
3. 登录认证、用户、角色、权限
4. 项目与工单管理
5. 移动端工单接口
6. 施工记录接口
7. 文件附件接口
8. 电子签名与PDF接口
9. 物料追溯接口
10. 人员资质接口
11. 离线同步接口
12. AI辅助验收接口
13. 经营看板与Excel导出
14. 操作日志与审计
15. 单元测试与联调测试
```

后端硬性规则：

```text
1. Controller 不写复杂业务；
2. Service 处理业务规则；
3. Mapper/Repository 处理数据访问；
4. DTO/VO/Entity 分层清楚；
5. 接口必须鉴权；
6. 移动端接口不信任前端 userId；
7. 重要操作写 operation_log；
8. 同步处理写 sync_log；
9. 冲突写 sync_conflict；
10. PDF生成、文件处理放 Service 或专门组件，不写在 Controller。
```

### 12.3 PC 后台模块

PC 后台开发顺序建议：

```text
1. 工程规范、request封装、路由、权限、菜单
2. 登录与主布局
3. 首页经营看板
4. 工单列表与多维筛选
5. 工单创建、编辑、模板
6. 工单分派与资质校验
7. 工单详情、状态流转、施工记录时间线
8. 附件预览：图片、视频、语音、PDF、水印信息
9. 电子签名与PDF验收单查看下载
10. 物料追溯
11. 人员档案与证书预警
12. 离线同步冲突复核
13. AI识别结果与人工复核
14. Excel对账单与报表导出
15. 操作日志与审计轨迹
```

PC 后台硬性规则：

```text
1. 不直接拼接文件路径下载，必须调用鉴权接口；
2. 工单详情必须聚合施工记录、附件、签名、PDF、物料、AI结果；
3. 同步冲突必须有复核入口；
4. AI结果页面必须提示“仅作辅助，最终以人工验收为准”；
5. 工单、物料、证书、同步、AI状态必须使用统一枚举映射；
6. 项目经理、经营人员、资质管理员等菜单按权限显示。
```

### 12.4 移动端模块

移动端开发顺序建议：

```text
1. 工程规范、网络封装、本地数据库、Repository、ViewModel
2. 登录、token、设备注册、心跳
3. 本地数据库与离线数据模型
4. 工单列表与详情
5. 工单接收、开始施工、反馈、提交待验收
6. 施工记录
7. 拍照、录像、语音
8. 照片水印、图片压缩、附件缓存
9. 文件上传、分片上传、失败重试
10. 电子签名
11. PDF验收单生成和本地归档
12. SyncManager / SyncWorker / 同步队列
13. 同步状态与冲突提示
14. 物料查看与实际使用
15. 人员资质状态展示
16. AI端侧识别与结果同步
17. 总联调
```

移动端硬性规则：

```text
1. 页面默认读取本地数据库；
2. 用户操作先写本地表，再写同步队列；
3. 无网时不提示“操作失败”，应提示“已离线保存，待同步”；
4. 有网时后台自动同步；
5. 同步失败不能删除本地数据；
6. 文件本体与业务数据同步解耦；
7. App 重启后同步队列不能丢失；
8. 未同步附件不能被缓存清理；
9. 冲突由 PC 后台复核，移动端只提示冲突状态；
10. 兼容 Android 7.0+。
```

### 12.5 AI 模块

AI 模块开发规则：

```text
1. 识别结果必须绑定 work_order_id、record_id、attachment_id；
2. 保存缺陷类型、置信度、模型版本、推理耗时；
3. 目标检测保存 x、y、width、height；
4. 支持人工复核状态、复核结论、复核人、复核时间；
5. 移动端离线识别结果可以后续同步；
6. 服务端可只预留模型管理和结果保存，不强制实现训练和推理；
7. AI结果不能直接改变工单最终验收状态；
8. AI失败不能阻塞工单提交。
```

---

## 13. 模块间关联规则

### 13.1 工单是主线

以下对象都应直接或间接关联工单：

```text
施工记录 → work_order_id
附件 → work_order_id，可选 record_id
签名 → work_order_id
验收记录 → work_order_id
PDF → work_order_id
物料需求 → work_order_id
物料使用 → work_order_id
AI结果 → work_order_id + attachment_id
状态日志 → work_order_id
同步冲突 → entity_id / work_order_id / business_id
经营统计 → work_order_id / project_id
```

### 13.2 附件关系

```text
file_storage
  ↑
work_order_attachment
  ↑
work_order / work_order_record / signature / pdf / ai_result / certificate
```

业务表不要散落保存文件路径，优先通过 `file_storage` 管理文件元数据。

### 13.3 AI 结果关系

```text
work_order
  └─ work_order_record
       └─ work_order_attachment(PHOTO)
            └─ ai_result
                 ├─ ai_defect_box
                 └─ ai_review_record
```

### 13.4 验收关系

```text
work_order
  ├─ work_order_record
  ├─ work_order_signature
  ├─ work_order_acceptance
  └─ work_order_pdf
```

PDF 归档后，应锁定关键验收字段，不允许普通编辑接口随意覆盖。

---

## 14. 推荐开发顺序

### 14.1 总体从 0 到联调顺序

```text
0. 读取本约束文档和项目源
1. 分析现有项目结构
2. 设计数据库总体方案
3. 实现权限与用户表
4. 实现项目与工单核心表
5. 实现施工记录、附件、签名、PDF表
6. 实现物料、资质、AI、同步表
7. 生成初始化脚本和迁移脚本
8. 生成后端 Entity / Mapper / Service
9. 建立后端统一响应、异常、权限
10. 开发工单核心后端接口
11. 开发文件、签名、PDF、物料、资质接口
12. 开发离线同步 pull / push / ack / conflict
13. 开发AI结果和人工复核接口
14. 开发经营看板接口
15. 开发PC后台基础工程、登录、菜单
16. 开发PC后台工单、附件、签名、PDF、物料、资质、冲突、AI、看板页面
17. 开发移动端本地数据库、登录、工单、施工记录、附件、签名、PDF、同步队列
18. 开发移动端离线同步和状态展示
19. PC后台、移动端、后端、数据库联调
20. 完整离线场景测试与验收
```

### 14.2 MVP 最小可运行版本

先实现可演示闭环：

```text
1. 用户登录与角色权限
2. PC后台创建和分派工单
3. 移动端查看我的工单
4. 移动端填写施工记录
5. 移动端拍照并保存水印元数据
6. 移动端本地保存 + 同步队列
7. sync/push 上传施工记录和附件元数据
8. PC后台查看工单详情、施工记录和附件
9. 电子签名记录
10. PDF验收单记录或生成
11. 简单经营看板统计
```

### 14.3 增强版本

```text
1. 视频和语音采集上传
2. 分片上传和断点续传
3. 同步冲突人工复核页面
4. 物料二维码扫描和追溯链路
5. 证书到期定时预警
6. AI端侧推理和结果图展示
7. AI人工复核
8. Excel对账单导出
9. MinIO 文件存储
10. Docker 容器化部署
```

---

## 15. 验收清单

### 15.1 业务验收

| 检查项 | 是否必须 |
|---|---|
| PC后台能创建、编辑、分派、筛选工单 | 必须 |
| 移动端只能看到自己的工单 | 必须 |
| 移动端无网可查看本地工单 | 必须 |
| 移动端无网可填写施工记录 | 必须 |
| 移动端无网可保存照片、视频、语音、签名 | 必须 |
| 网络恢复后能增量同步 | 必须 |
| 同步失败能重试 | 必须 |
| 同步冲突能进入 PC 后台复核 | 必须 |
| 工单详情能展示施工记录、附件、签名、PDF、物料、AI结果 | 必须 |
| PDF 验收单能归档和下载 | 必须 |
| 物料能入库、出库、盘点、追溯到工单 | 必须 |
| 人员证书能到期预警，派工能校验资质 | 必须 |
| AI结果能人工复核，且不替代最终验收 | 必须 |
| 经营看板能统计核心指标并支持导出 | 建议 |

### 15.2 技术验收

| 检查项 | 是否必须 |
|---|---|
| 后端能编译/启动 | 必须 |
| 前端能构建/启动 | 必须 |
| 移动端能编译 | 必须 |
| 数据库脚本可执行 | 必须 |
| 关键表有索引 | 必须 |
| 同步字段齐全 | 必须 |
| 权限校验不依赖前端 userId | 必须 |
| 文件不直接存 MySQL | 必须 |
| 大文件上传失败不丢数据 | 必须 |
| 日志不保存密码、token、大 payload | 必须 |
| Controller/Service/Mapper 分层清晰 | 必须 |
| 有基本测试或联调说明 | 必须 |

### 15.3 离线场景测试

必须至少覆盖以下场景：

```text
1. 无网登录后进入离线模式（已有本地登录缓存）；
2. 无网查看已同步工单；
3. 无网新增施工记录；
4. 无网拍照并保存水印图；
5. 无网签名并生成PDF；
6. 网络恢复后自动同步；
7. 同步一半失败后重试；
8. 同一工单多端修改产生冲突；
9. PC后台处理冲突；
10. 移动端 pull 后更新最终版本。
```

---

## 16. Codex 常用任务模板

### 16.1 项目结构分析模板

```text
请先阅读当前项目根目录，不要立即写代码。

请根据《海上平台现场作业管理系统：Codex 开发约束文档》分析当前项目：
1. 当前技术栈；
2. 后端、PC后台、移动端、数据库目录结构；
3. 已有接口、表、页面、移动端模块；
4. 当前任务相关文件；
5. 本次开发应遵守的约束；
6. 推荐修改步骤。

暂时不要修改代码，只输出分析结果。
```

### 16.2 数据库开发模板

```text
请基于当前项目结构和《Codex 开发约束文档》开发数据库模块。

要求：
1. 不要只做普通后台表；
2. 必须体现工单闭环、离线同步、附件元数据、电子签名、PDF、物料追溯、人员资质、AI辅助验收；
3. 需要同步的表必须包含 local_id、server_id、version、sync_status、device_id、operator_id、updated_at；
4. 关键表要有索引；
5. 如果项目使用 Flyway/Liquibase，生成迁移文件；否则生成 db/init_schema.sql；
6. 完成后执行数据库脚本或测试。
```

### 16.3 后端接口开发模板

```text
请基于当前项目结构和《Codex 开发约束文档》开发后端接口。

要求：
1. 优先复用已有统一响应、异常、权限、ORM结构；
2. Controller / Service / Mapper 分层清晰；
3. 移动端接口必须按当前用户权限返回数据，不信任前端 userId；
4. 状态变更写日志；
5. 同步接口支持批量、部分成功、幂等、冲突记录；
6. 文件接口只保存元数据，文件访问鉴权；
7. 完成后运行编译和测试。
```

### 16.4 PC 后台开发模板

```text
请基于当前项目结构和《Codex 开发约束文档》开发 PC 后台模块。

要求：
1. 优先复用现有 request、router、store、layout、components；
2. 页面围绕工单闭环，不做孤立 CRUD；
3. 工单详情必须展示施工记录、附件、签名、PDF、物料、AI结果；
4. 文件预览必须走鉴权接口；
5. 同步冲突必须有复核入口；
6. AI结果必须显示“仅作辅助”；
7. 完成后运行构建或启动命令。
```

### 16.5 移动端开发模板

```text
请基于当前项目结构和《Codex 开发约束文档》开发移动端模块。

要求：
1. 移动端核心功能必须本地优先；
2. 页面默认读取本地数据库；
3. 用户操作先写本地表，再写 local_sync_queue；
4. 无网时提示已离线保存，不提示操作失败；
5. 文件本体与业务数据同步解耦；
6. 同步失败不能删除本地数据；
7. 兼容 Android 7.0+；
8. 完成后运行 Gradle 编译或测试。
```

### 16.6 离线同步开发模板

```text
请基于当前项目结构和《Codex 开发约束文档》开发离线同步模块。

要求：
1. 先梳理 sync/pull、sync/push、sync/ack 的数据契约；
2. 补齐 device_info、sync_task、sync_log、sync_conflict、sync_ack_record；
3. 移动端补齐 local_sync_queue、local_sync_log、local_sync_checkpoint；
4. 实现版本比较和冲突检测；
5. 冲突不能丢弃，必须由 PC 后台复核；
6. 附件大文件不进入 sync/push payload；
7. 写离线新增施工记录、冲突、重试测试。
```

### 16.7 联调检查模板

```text
请对当前模块进行联调检查。

检查：
1. 数据库脚本能否执行；
2. 后端能否启动；
3. 前端能否构建；
4. 移动端能否编译；
5. 工单创建、分派、移动端接收是否闭环；
6. 施工记录、附件、签名、PDF是否能关联工单；
7. 离线保存、联网同步、冲突复核是否可用；
8. 权限是否正确；
9. 日志是否完整；
10. 是否违反《Codex 开发约束文档》。

输出已通过项、发现问题、已修复项、待确认项。
```

---

## 17. 原始项目源摘要

### 17.1 移动端现场作业

```text
1. 工单接收与反馈：维修工可通过移动端查看派工单，包括作业地点、内容、所需物料等。
2. 现场多媒体记录：支持拍照、录像、语音备注，自动关联至对应工单，每张照片自动添加时间和工单号水印。
3. 离线操作：无网络或弱网环境下，可正常浏览已有工单、记录施工过程数据，本地存储，恢复网络后自动同步至服务器。
4. 电子签名：支持手写签名验收，签名数据与工单绑定，签名后生成不可编辑 PDF 验收单。
```

### 17.2 PC 后台管理

```text
1. 工单管理中心：创建、编辑、分派工单；查看状态流转图；支持工单模板；按项目、时间、人员、状态筛选。
2. 物料追溯模块：通过二维码标签管理，实现物料入库、出库、盘点。
3. 人员资质管理：员工档案、特种作业证书、海上作业证、焊工证、到期预警。
4. 经营看板：进行中工单数、今日出勤人数、本周完工产值、Excel 对账单导出。
```

### 17.3 数据同步与离线架构

```text
1. 离线优先架构：移动端采用本地 SQLite/Realm 数据库作为主数据源；后端 API 采用同步模式而非实时请求；断网时移动端核心功能仍可用。
2. 冲突处理策略：多端同时修改同一工单时，采用“最后写入为准 + 人工复核”，确保数据不丢失并可追溯变更历史。
3. 增量同步：仅同步变更数据，不做全量覆盖，降低带宽消耗。
```

### 17.4 轻量 AI 图像辅助验收

```text
1. 防腐层缺陷识别：基于 YOLO 或轻量级 CNN 模型，对施工照片进行初步分类。
2. 模型轻量化：模型文件 ≤10MB，可在普通安卓手机 CPU 推理运行，单张图片处理时间 ≤10 秒。
```

### 17.5 技术方法和路线

```text
1. 实地完成现场需求调研，梳理整合现有设备信息系统功能，清理冗余流程和功能点。
2. 结合行业经验、标准、工况和决策需求构建数据模型、数据清洗和质量控制规则，搭建“故障案例 + 维修工艺”知识库。
3. 采用敏捷开发模式分模块迭代，使用 HTML、JavaScript、Java 等主流语言，结合 MySQL 8.0，搭建轻量化、高可用系统；PC 后台采用 B/S 模式，移动端基于 Android 7.0 及以上版本。
```

---

## 18. 最终约束总结

Codex 在本项目中的核心目标不是“快速生成代码”，而是**在不破坏项目结构的前提下，逐步实现一个以工单为主线、移动端离线优先、PC 后台可管理、后端可同步、数据可追溯、AI 仅作辅助的现场作业管理系统**。

任何实现只要违反以下五条，都应视为不合格：

```text
1. 脱离工单闭环；
2. 忽略移动端离线优先；
3. 文件本体直接入 MySQL；
4. AI 替代人工验收；
5. 同步冲突不保留、不复核、不可追溯。
```
