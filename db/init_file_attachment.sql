-- 海上平台现场作业管理系统 - 现场多媒体附件与文件元数据表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 work_order
--   3. init_work_order_record.sql 中的 work_order_record
-- 设计重点：
--   MySQL 只保存文件元数据和业务关联，不保存照片、视频、语音、PDF、AI结果图等大文件二进制本体。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS file_storage (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  file_id VARCHAR(64) NOT NULL COMMENT '文件业务ID，唯一，对外暴露使用',
  original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
  file_type VARCHAR(32) NOT NULL COMMENT '文件类型：PHOTO/VIDEO/AUDIO/PDF/AI_IMAGE/SIGNATURE/CERT/QRCODE/OTHER',
  mime_type VARCHAR(128) NOT NULL COMMENT 'MIME类型',
  file_size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
  storage_type VARCHAR(32) NOT NULL DEFAULT 'LOCAL' COMMENT '存储类型：LOCAL/MINIO/OSS',
  bucket_name VARCHAR(128) DEFAULT NULL COMMENT '对象存储桶名称，MinIO/OSS使用',
  file_path VARCHAR(1000) NOT NULL COMMENT '文件相对路径或对象存储key，禁止前端直接访问裸路径',
  preview_path VARCHAR(1000) DEFAULT NULL COMMENT '预览文件路径或缩略图路径',
  thumbnail_path VARCHAR(1000) DEFAULT NULL COMMENT '缩略图路径，照片/视频可用',
  file_hash VARCHAR(128) DEFAULT NULL COMMENT '文件哈希，用于去重、校验和断点续传',
  checksum VARCHAR(128) DEFAULT NULL COMMENT '客户端上传校验值',
  upload_user_id BIGINT DEFAULT NULL COMMENT '上传用户ID，关联sys_user.id',
  upload_user_name VARCHAR(64) DEFAULT NULL COMMENT '上传用户姓名快照',
  upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  upload_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '上传状态：PENDING/UPLOADING/UPLOADED/FAILED',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '上传失败重试次数，视频和语音弱网重试使用',
  last_retry_time DATETIME DEFAULT NULL COMMENT '最近一次重试时间',
  error_code VARCHAR(64) DEFAULT NULL COMMENT '上传失败错误码',
  error_message VARCHAR(500) DEFAULT NULL COMMENT '上传失败错误信息',
  work_order_id BIGINT DEFAULT NULL COMMENT '所属工单ID，关联work_order.id',
  record_id BIGINT DEFAULT NULL COMMENT '所属施工记录ID，关联work_order_record.id',
  access_level VARCHAR(32) NOT NULL DEFAULT 'PRIVATE' COMMENT '访问级别：PRIVATE/PROJECT/PUBLIC',
  preview_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许PC后台预览：0否，1是',
  download_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许下载：0否，1是',
  cache_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许移动端离线缓存：0否，1是',
  cache_key VARCHAR(255) DEFAULT NULL COMMENT '移动端离线缓存key',
  cache_expire_time DATETIME DEFAULT NULL COMMENT '移动端缓存过期时间',
  local_file_path VARCHAR(1000) DEFAULT NULL COMMENT '移动端本地文件路径，仅作同步追踪，不作为服务端访问路径',
  client_created_at DATETIME DEFAULT NULL COMMENT '移动端本地创建时间',
  client_updated_at DATETIME DEFAULT NULL COMMENT '移动端本地更新时间',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID映射，回传移动端使用',
  version INT NOT NULL DEFAULT 0 COMMENT '同步版本号，用于增量同步和冲突判断',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '同步状态：PENDING/SYNCING/SYNCED/FAILED/CONFLICT',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '来源设备ID',
  operator_id BIGINT DEFAULT NULL COMMENT '最后操作人ID',
  conflict_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否存在同步冲突：0否，1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_file_storage_file_id (file_id),
  UNIQUE KEY uk_file_storage_local_id (local_id),
  KEY idx_file_storage_type (file_type, upload_status, deleted_flag),
  KEY idx_file_storage_hash (file_hash),
  KEY idx_file_storage_work_order (work_order_id, record_id),
  KEY idx_file_storage_upload_user (upload_user_id, upload_time),
  KEY idx_file_storage_sync (sync_status, updated_at),
  KEY idx_file_storage_retry (upload_status, retry_count, last_retry_time),
  CONSTRAINT fk_file_storage_upload_user
    FOREIGN KEY (upload_user_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_file_storage_work_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_file_storage_record
    FOREIGN KEY (record_id) REFERENCES work_order_record (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_file_storage_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件存储元数据表';

CREATE TABLE IF NOT EXISTS work_order_attachment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，关联work_order.id',
  record_id BIGINT DEFAULT NULL COMMENT '施工记录ID，关联work_order_record.id',
  file_id VARCHAR(64) NOT NULL COMMENT '文件业务ID，关联file_storage.file_id',
  attachment_type VARCHAR(32) NOT NULL COMMENT '附件类型：PHOTO/VIDEO/AUDIO/PDF/AI_IMAGE/SIGNATURE/OTHER',
  attachment_name VARCHAR(255) DEFAULT NULL COMMENT '附件显示名称',
  attachment_desc VARCHAR(500) DEFAULT NULL COMMENT '附件说明',
  business_scene VARCHAR(64) NOT NULL DEFAULT 'WORK_RECORD' COMMENT '业务场景：WORK_RECORD/ACCEPTANCE/AI_RESULT/PDF/SIGNATURE/MATERIAL/CERT',
  capture_time DATETIME DEFAULT NULL COMMENT '拍摄/录制/生成时间',
  capture_user_id BIGINT DEFAULT NULL COMMENT '拍摄/录制/生成用户ID',
  capture_user_name VARCHAR(64) DEFAULT NULL COMMENT '拍摄/录制/生成用户姓名快照',
  latitude DECIMAL(10,7) DEFAULT NULL COMMENT '拍摄纬度，可选',
  longitude DECIMAL(10,7) DEFAULT NULL COMMENT '拍摄经度，可选',
  location_name VARCHAR(255) DEFAULT NULL COMMENT '拍摄地点名称',
  watermark_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否包含水印：0否，1是',
  watermark_text VARCHAR(1000) DEFAULT NULL COMMENT '水印文本，建议含时间、工单号、拍摄人、经纬度',
  watermark_time DATETIME DEFAULT NULL COMMENT '水印时间',
  watermark_work_order_no VARCHAR(64) DEFAULT NULL COMMENT '水印工单编号快照',
  watermark_user_name VARCHAR(64) DEFAULT NULL COMMENT '水印拍摄人姓名快照',
  watermark_latitude DECIMAL(10,7) DEFAULT NULL COMMENT '水印纬度',
  watermark_longitude DECIMAL(10,7) DEFAULT NULL COMMENT '水印经度',
  duration_seconds INT DEFAULT NULL COMMENT '视频/语音时长，单位秒',
  media_width INT DEFAULT NULL COMMENT '图片/视频宽度',
  media_height INT DEFAULT NULL COMMENT '图片/视频高度',
  ai_result_id BIGINT DEFAULT NULL COMMENT '后续AI识别结果ID，AI表建立后可关联',
  ai_bind_status VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT 'AI绑定状态：NONE/PENDING/BOUND/FAILED',
  preview_status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'PC预览状态：AVAILABLE/PROCESSING/FAILED',
  mobile_cache_status VARCHAR(32) NOT NULL DEFAULT 'NOT_CACHED' COMMENT '移动端缓存状态：NOT_CACHED/CACHED/EXPIRED',
  upload_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '上传状态：PENDING/UPLOADING/UPLOADED/FAILED',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '弱网上传失败重试次数',
  last_retry_time DATETIME DEFAULT NULL COMMENT '最近一次重试时间',
  error_code VARCHAR(64) DEFAULT NULL COMMENT '失败错误码',
  error_message VARCHAR(500) DEFAULT NULL COMMENT '失败错误信息',
  client_created_at DATETIME DEFAULT NULL COMMENT '移动端本地创建时间',
  client_updated_at DATETIME DEFAULT NULL COMMENT '移动端本地更新时间',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID映射，回传移动端使用',
  version INT NOT NULL DEFAULT 0 COMMENT '同步版本号，用于增量同步和冲突判断',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '同步状态：PENDING/SYNCING/SYNCED/FAILED/CONFLICT',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '来源设备ID',
  operator_id BIGINT DEFAULT NULL COMMENT '最后操作人ID',
  conflict_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否存在同步冲突：0否，1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_work_order_attachment_local_id (local_id),
  KEY idx_work_order_attachment_order (work_order_id, attachment_type, deleted_flag),
  KEY idx_work_order_attachment_record (record_id, attachment_type),
  KEY idx_work_order_attachment_file (file_id),
  KEY idx_work_order_attachment_capture_user (capture_user_id, capture_time),
  KEY idx_work_order_attachment_sync (sync_status, updated_at),
  KEY idx_work_order_attachment_retry (upload_status, retry_count, last_retry_time),
  KEY idx_work_order_attachment_ai (ai_result_id, ai_bind_status),
  CONSTRAINT fk_work_order_attachment_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_attachment_record
    FOREIGN KEY (record_id) REFERENCES work_order_record (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_attachment_file
    FOREIGN KEY (file_id) REFERENCES file_storage (file_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_attachment_capture_user
    FOREIGN KEY (capture_user_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_attachment_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单附件表';

SET FOREIGN_KEY_CHECKS = 1;
