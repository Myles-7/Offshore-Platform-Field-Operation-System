# PC 后台接口

所有接口前缀均为 `/api/admin`，需要 `Authorization: Bearer <token>`，账号必须 `pcEnabled=1`。

## 用户、角色、权限

仅系统管理员可访问。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/users` | 用户分页列表，支持 `PageRequestDTO` |
| GET | `/api/admin/users/{id}` | 用户详情 |
| POST | `/api/admin/users` | 新增用户 |
| PUT | `/api/admin/users/{id}` | 更新用户 |
| DELETE | `/api/admin/users/{id}` | 逻辑删除用户 |
| GET | `/api/admin/roles` | 角色列表 |
| GET | `/api/admin/permissions` | 权限列表 |
| PUT | `/api/admin/users/{id}/roles` | 分配用户角色，`roleIds` |
| PUT | `/api/admin/roles/{id}/permissions` | 分配角色权限，`permissionIds` |

`AdminUserCreateRequest`: `username`, `password`, `realName`, `phone`, `email`, `employeeNo`, `accountStatus`, `pcEnabled`, `mobileEnabled`, `primaryProjectId`, `departmentId`, `remark`。

## 项目与工单

项目经理、调度、现场负责人、验收角色可访问；系统管理员可访问全部。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/projects` | 项目列表 |
| GET | `/api/admin/projects/{id}` | 项目详情 |
| POST | `/api/admin/projects` | 新增项目 |
| PUT | `/api/admin/projects/{id}` | 更新项目 |
| DELETE | `/api/admin/projects/{id}` | 逻辑删除项目 |
| GET | `/api/admin/work-orders` | 工单列表 |
| GET | `/api/admin/work-orders/{id}` | 工单聚合详情 |
| POST | `/api/admin/work-orders` | 创建工单 |
| PUT | `/api/admin/work-orders/{id}` | 更新工单 |
| DELETE | `/api/admin/work-orders/{id}` | 逻辑删除工单 |
| POST | `/api/admin/work-orders/{id}/assign` | 派工 |
| POST | `/api/admin/work-orders/{id}/status` | 状态流转 |
| GET | `/api/admin/work-orders/{id}/status-flow` | 状态流 |
| GET | `/api/admin/work-order-templates` | 工单模板列表 |
| POST | `/api/admin/work-order-templates` | 新增模板 |
| POST | `/api/admin/work-orders/from-template/{templateId}` | 模板创建工单 |

工单列表筛选: `projectId`, `workOrderNo`, `status`, `priority`, `maintainerId`, `leaderId`, `plannedStartTimeStart`, `plannedStartTimeEnd`, `syncStatus`, `abnormalFlag`。

## 施工记录

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/work-orders/{workOrderId}/records` | 工单施工记录 |
| GET | `/api/admin/work-records/{recordId}` | 施工记录详情 |
| GET | `/api/admin/work-records/{recordId}/timeline` | 施工记录时间线 |
| POST | `/api/admin/work-records/{recordId}/confirm` | 后台确认 |

## 附件、签名、验收、PDF

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/work-orders/{workOrderId}/attachments` | 工单附件元数据 |
| GET | `/api/admin/work-orders/{workOrderId}/signatures` | 签名列表 |
| GET | `/api/admin/work-orders/{workOrderId}/acceptance` | 验收记录 |
| POST | `/api/admin/work-orders/{workOrderId}/acceptance/review` | 验收复核 |
| POST | `/api/admin/work-orders/{workOrderId}/pdf/generate` | 生成 PDF |
| GET | `/api/admin/work-orders/{workOrderId}/pdf` | PDF 元数据 |
| GET | `/api/admin/work-orders/{workOrderId}/pdf/download` | PDF 下载，走文件鉴权 |

## 物料追溯

物资管理员可访问物料后台接口。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/materials` | 物料列表 |
| GET | `/api/admin/materials/{id}` | 物料详情 |
| POST | `/api/admin/materials` | 新增物料 |
| PUT | `/api/admin/materials/{id}` | 更新物料 |
| DELETE | `/api/admin/materials/{id}` | 逻辑删除物料 |
| GET | `/api/admin/materials/{id}/inventory` | 库存 |
| POST | `/api/admin/materials/inbound` | 入库 |
| POST | `/api/admin/materials/outbound` | 出库 |
| POST | `/api/admin/materials/stocktaking` | 盘点 |
| POST | `/api/admin/materials/{id}/qrcode` | 生成二维码 |
| GET | `/api/admin/materials/qrcode/{code}` | 二维码查询 |
| GET | `/api/admin/work-orders/{workOrderId}/material-usage` | 工单物料使用 |

## 人员资质

资质管理员可访问资质后台接口。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/employees` | 员工列表 |
| GET | `/api/admin/employees/{id}` | 员工详情 |
| POST | `/api/admin/employees` | 新增员工 |
| PUT | `/api/admin/employees/{id}` | 更新员工 |
| DELETE | `/api/admin/employees/{id}` | 删除员工 |
| GET | `/api/admin/employees/{id}/certificates` | 证书列表 |
| POST | `/api/admin/employees/{id}/certificates` | 新增证书 |
| PUT | `/api/admin/certificates/{id}` | 更新证书 |
| DELETE | `/api/admin/certificates/{id}` | 删除证书 |
| GET | `/api/admin/certificates/warnings` | 到期预警 |
| GET | `/api/admin/work-orders/{id}/qualification-candidates` | 派工资质候选人 |

## 看板、报表、日志、同步冲突

经营人员和项目经理可访问看板/报表；操作日志和同步管理默认系统管理员。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/dashboard/overview` | 总览 |
| GET | `/api/admin/dashboard/work-order-statistics` | 工单统计 |
| GET | `/api/admin/dashboard/project-statistics` | 项目统计 |
| GET | `/api/admin/dashboard/person-statistics` | 人员统计 |
| GET | `/api/admin/dashboard/material-statistics` | 物料统计 |
| GET | `/api/admin/dashboard/output-value` | 产值趋势 |
| GET | `/api/admin/reports/reconciliation` | 对账单 |
| GET | `/api/admin/reports/reconciliation/export` | Excel 导出 |
| GET | `/api/admin/operation-logs` | 操作日志列表 |
| GET | `/api/admin/operation-logs/{id}` | 操作日志详情 |
| GET | `/api/admin/work-orders/{workOrderId}/audit-trail` | 工单审计轨迹 |
| GET | `/api/admin/sync/audit-trail` | 同步审计轨迹 |
| GET | `/api/admin/sync/conflicts` | 同步冲突列表 |
| GET | `/api/admin/sync/conflicts/{id}` | 冲突详情 |
| POST | `/api/admin/sync/conflicts/{id}/resolve` | 冲突处理 |
