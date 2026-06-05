# 海上平台现场作业管理系统后端接口联调总览

## 基础约定

- Base URL: 以部署环境为准，本地开发默认 `http://localhost:8080`。
- Swagger UI: `/swagger-ui/index.html`。
- OpenAPI JSON: `/v3/api-docs`。
- 时间格式: `yyyy-MM-dd HH:mm:ss`。
- 请求体: 默认 `application/json`，文件上传为 `multipart/form-data`。
- Token: 登录后将 `LoginResponse.token` 放入请求头 `Authorization: Bearer <token>`。
- 匿名接口: `POST /api/auth/login`、`GET /api/health`、开发环境 Swagger。

## 统一返回结构

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-06-05 16:00:00",
  "traceId": "trace-id"
}
```

## 统一错误码

| code | 含义 |
| --- | --- |
| 200 | SUCCESS |
| 400 | PARAM_ERROR |
| 401 | UNAUTHORIZED |
| 403 | FORBIDDEN |
| 404 | NOT_FOUND |
| 10001 | USER_ERROR |
| 20001 | WORK_ORDER_ERROR |
| 30001 | FILE_ERROR |
| 40001 | SYNC_ERROR |
| 50001 | AI_ERROR |
| 50002 | PDF_ERROR |
| 60001 | MATERIAL_ERROR |
| 70001 | QUALIFICATION_ERROR |
| 80001 | DASHBOARD_ERROR |
| 500 | SYSTEM_ERROR |

## 分页参数

`PageRequestDTO`: `pageNum` 默认 1，`pageSize` 默认 10 且最大 200，`keyword`，`sortField`，`sortOrder` 可为 `asc` 或 `desc`。

## 鉴权与数据范围

- `/api/admin/**`: 需要登录、账号 `pcEnabled=1`，并按后台模块角色校验。
- `/api/mobile/**`: 需要登录、账号 `mobileEnabled=1`，用户身份从 token 解析，不信任前端传入 `userId`。
- `/api/sync/**`: 需要登录、移动端权限，并校验 `deviceId`；GET 可传 `X-Device-Id`，JSON 接口在请求体传 `deviceId`。
- `/api/files/**`: 需要登录，文件下载/预览/删除会校验上传人、文件绑定工单、施工记录或工单附件权限。
- `/api/ai/**`: 需要登录，AI 结果必须绑定有权限访问的工单或后台 AI 权限。
- 系统管理员可访问全部；维修工仅能访问本人负责或派工记录中的工单；项目经理仅能访问所属项目；物资管理员仅物料模块；资质管理员仅资质模块；经营人员仅看板和报表。

## 认证接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录，匿名 |
| POST | `/api/auth/logout` | 退出 |
| GET | `/api/auth/current` | 当前用户 |

`LoginRequest`: `loginName`, `password`, `platform`(`PC`/`MOBILE`)。

`LoginResponse`: `token`, `userId`, `username`, `realName`, `roleCodes`, `permissionCodes`, `dataScope`, `primaryProjectId`。

## 文档索引

- PC 后台: [admin_api.md](admin_api.md)
- Android 移动端: [mobile_api.md](mobile_api.md)
- 离线同步: [sync_api.md](sync_api.md)
- 文件附件: [file_api.md](file_api.md)
- AI 辅助验收: [ai_api.md](ai_api.md)

## 常见联调错误

- `401`: 缺少 `Authorization`，token 过期，或 token 无法解析当前用户。
- `403`: 账号未开通 PC/Mobile 端，角色不匹配，或工单/文件/项目不在当前数据范围内。
- `40001`: 同步请求缺少 `deviceId`、设备未注册、设备不属于当前用户或设备禁用。
- `30001`: 文件不存在、文件已逻辑删除、文件元数据未绑定可访问业务对象。
- `20001`: 工单状态流转非法、越权访问工单、版本冲突或验收锁定。
