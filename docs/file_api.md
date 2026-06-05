# 文件附件接口

所有 `/api/files/**` 需要登录。文件二进制保存到本地文件系统或后续对象存储，MySQL 只保存 `file_storage` 元数据。禁止裸路径访问，预览和下载必须走鉴权接口。

## 文件接口

| 方法 | 路径 | Content-Type | 说明 |
| --- | --- | --- | --- |
| POST | `/api/files/upload` | `multipart/form-data` | 普通上传 |
| POST | `/api/files/batch-upload` | `multipart/form-data` | 批量上传 |
| POST | `/api/files/chunk/init` | `application/json` | 分片初始化，TODO: 当前为占位能力 |
| POST | `/api/files/chunk/upload` | `multipart/form-data` | 分片上传，TODO: 当前为占位能力 |
| POST | `/api/files/chunk/merge` | `application/json` | 分片合并，TODO: 当前为占位能力 |
| GET | `/api/files/{fileId}/preview` | - | 文件预览 |
| GET | `/api/files/{fileId}/download` | - | 文件下载 |
| DELETE | `/api/files/{fileId}` | - | 逻辑作废文件元数据 |

普通上传表单字段:

- `file`: 文件本体。
- `fileType`: 如 `PHOTO`, `VIDEO`, `AUDIO`, `PDF`, `SIGNATURE`, `CERTIFICATE`。
- `workOrderId`, `recordId`, `localId`, `deviceId`: 可选业务绑定字段。

返回 `FileUploadVO`: `fileId`, `fileType`, `fileSize`, `mimeType`, `fileHash`, `previewUrl`, `downloadUrl`。不会返回服务器物理路径。

## 工单附件接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/mobile/work-orders/{workOrderId}/attachments` | 移动端绑定附件元数据 |
| GET | `/api/mobile/work-orders/{workOrderId}/attachments` | 移动端附件列表 |
| GET | `/api/admin/work-orders/{workOrderId}/attachments` | PC 后台附件列表 |

`AttachmentBindRequest`: `localId`, `recordId`, `fileId`, `attachmentType`, `attachmentName`, `attachmentDesc`, `businessScene`, `captureTime`, `latitude`, `longitude`, `locationName`, `watermarkFlag`, `watermarkText`, `durationSeconds`, `mediaWidth`, `mediaHeight`, `deviceId`, `remark`。

## 下载鉴权说明

文件访问会检查当前用户是否为上传人、是否具有绑定工单权限、是否为工单派工人员或项目范围内管理人员。PDF、证书附件、签名文件也复用统一文件鉴权。
