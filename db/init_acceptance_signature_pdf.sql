-- 海上平台现场作业管理系统 - 电子签名与PDF验收单表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 project_info、work_order
--   3. init_work_order_record.sql 中的 work_order_record
--   4. init_file_attachment.sql 中的 file_storage

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS work_order_acceptance (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  acceptance_no VARCHAR(64) NOT NULL COMMENT '验收编号，唯一',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，关联work_order.id',
  project_id BIGINT NOT NULL COMMENT '项目ID，冗余用于PC后台筛选统计',
  work_order_no VARCHAR(64) NOT NULL COMMENT '工单编号快照',
  project_name VARCHAR(128) DEFAULT NULL COMMENT '项目名称快照',
  construction_user_id BIGINT DEFAULT NULL COMMENT '施工人员ID',
  construction_user_name VARCHAR(64) DEFAULT NULL COMMENT '施工人员姓名快照',
  acceptance_user_id BIGINT NOT NULL COMMENT '验收人员ID',
  acceptance_user_name VARCHAR(64) DEFAULT NULL COMMENT '验收人员姓名快照',
  acceptance_time DATETIME NOT NULL COMMENT '验收时间',
  acceptance_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '验收状态：PENDING/PASSED/REJECTED/LOCKED',
  acceptance_result VARCHAR(32) DEFAULT NULL COMMENT '验收结果：PASS/FAIL/CONDITIONAL_PASS',
  acceptance_opinion VARCHAR(1000) DEFAULT NULL COMMENT '验收意见',
  problem_desc VARCHAR(1000) DEFAULT NULL COMMENT '发现问题说明',
  rectification_required TINYINT NOT NULL DEFAULT 0 COMMENT '是否要求整改：0否，1是',
  record_summary TEXT DEFAULT NULL COMMENT '施工记录摘要，用于PDF验收单',
  attachment_summary JSON DEFAULT NULL COMMENT '附件摘要，保存照片/视频/语音等元数据快照',
  signature_count INT NOT NULL DEFAULT 0 COMMENT '签名数量',
  pdf_generated_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否已生成PDF：0否，1是',
  locked_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否锁定关键验收字段：0否，1是',
  locked_at DATETIME DEFAULT NULL COMMENT '锁定时间',
  locked_by BIGINT DEFAULT NULL COMMENT '锁定人ID',
  lock_reason VARCHAR(500) DEFAULT NULL COMMENT '锁定原因',
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
  UNIQUE KEY uk_work_order_acceptance_no (acceptance_no),
  UNIQUE KEY uk_work_order_acceptance_local_id (local_id),
  KEY idx_work_order_acceptance_order (work_order_id, acceptance_status),
  KEY idx_work_order_acceptance_project_time (project_id, acceptance_time),
  KEY idx_work_order_acceptance_user (acceptance_user_id, acceptance_time),
  KEY idx_work_order_acceptance_lock (locked_flag, pdf_generated_flag),
  KEY idx_work_order_acceptance_sync (sync_status, updated_at),
  CONSTRAINT fk_work_order_acceptance_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_acceptance_project
    FOREIGN KEY (project_id) REFERENCES project_info (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_acceptance_construction_user
    FOREIGN KEY (construction_user_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_acceptance_user
    FOREIGN KEY (acceptance_user_id) REFERENCES sys_user (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_acceptance_locked_by
    FOREIGN KEY (locked_by) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_acceptance_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='验收记录表';

CREATE TABLE IF NOT EXISTS work_order_signature (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  signature_no VARCHAR(64) NOT NULL COMMENT '签名编号，唯一',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，关联work_order.id',
  acceptance_id BIGINT DEFAULT NULL COMMENT '验收记录ID，关联work_order_acceptance.id',
  file_id VARCHAR(64) NOT NULL COMMENT '签名文件ID，关联file_storage.file_id',
  signature_role VARCHAR(32) NOT NULL COMMENT '签名角色：CONSTRUCTION_USER/ACCEPTANCE_USER/PROJECT_MANAGER/OWNER',
  signer_user_id BIGINT DEFAULT NULL COMMENT '签名人用户ID',
  signer_name VARCHAR(64) NOT NULL COMMENT '签名人姓名快照',
  signer_phone VARCHAR(32) DEFAULT NULL COMMENT '签名人手机号快照',
  signed_at DATETIME NOT NULL COMMENT '签名时间',
  sign_location VARCHAR(255) DEFAULT NULL COMMENT '签名地点',
  latitude DECIMAL(10,7) DEFAULT NULL COMMENT '签名纬度',
  longitude DECIMAL(10,7) DEFAULT NULL COMMENT '签名经度',
  signature_hash VARCHAR(128) DEFAULT NULL COMMENT '签名文件哈希，用于防篡改校验',
  signature_status VARCHAR(32) NOT NULL DEFAULT 'SIGNED' COMMENT '签名状态：DRAFT/SIGNED/CANCELLED',
  local_file_path VARCHAR(1000) DEFAULT NULL COMMENT '移动端签名本地文件路径，失败重试时保留',
  upload_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '上传状态：PENDING/UPLOADING/UPLOADED/FAILED',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '同步失败重试次数',
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
  UNIQUE KEY uk_work_order_signature_no (signature_no),
  UNIQUE KEY uk_work_order_signature_local_id (local_id),
  KEY idx_work_order_signature_order (work_order_id, signature_role),
  KEY idx_work_order_signature_acceptance (acceptance_id),
  KEY idx_work_order_signature_file (file_id),
  KEY idx_work_order_signature_signer (signer_user_id, signed_at),
  KEY idx_work_order_signature_sync (sync_status, updated_at),
  KEY idx_work_order_signature_retry (upload_status, retry_count, last_retry_time),
  CONSTRAINT fk_work_order_signature_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_signature_acceptance
    FOREIGN KEY (acceptance_id) REFERENCES work_order_acceptance (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_signature_file
    FOREIGN KEY (file_id) REFERENCES file_storage (file_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_signature_signer
    FOREIGN KEY (signer_user_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_signature_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电子签名表';

CREATE TABLE IF NOT EXISTS work_order_pdf (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  pdf_no VARCHAR(64) NOT NULL COMMENT 'PDF验收单编号，唯一',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，关联work_order.id',
  acceptance_id BIGINT NOT NULL COMMENT '验收记录ID，关联work_order_acceptance.id',
  file_id VARCHAR(64) NOT NULL COMMENT 'PDF文件ID，关联file_storage.file_id',
  work_order_no VARCHAR(64) NOT NULL COMMENT '工单编号快照',
  project_name VARCHAR(128) DEFAULT NULL COMMENT '项目名称快照',
  construction_user_name VARCHAR(256) DEFAULT NULL COMMENT '施工人员快照，多个用逗号分隔',
  acceptance_user_name VARCHAR(64) DEFAULT NULL COMMENT '验收人员姓名快照',
  acceptance_time DATETIME NOT NULL COMMENT '验收时间快照',
  signature_file_ids VARCHAR(1000) DEFAULT NULL COMMENT '签名文件ID列表快照，逗号分隔',
  record_summary TEXT DEFAULT NULL COMMENT '施工记录摘要快照',
  pdf_content_snapshot JSON DEFAULT NULL COMMENT 'PDF内容快照，包含工单、项目、人员、签名、记录摘要等',
  pdf_status VARCHAR(32) NOT NULL DEFAULT 'GENERATED' COMMENT 'PDF状态：GENERATING/GENERATED/FAILED/ARCHIVED',
  generated_by BIGINT DEFAULT NULL COMMENT '生成人ID',
  generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  locked_flag TINYINT NOT NULL DEFAULT 1 COMMENT 'PDF是否不可编辑锁定：0否，1是',
  archive_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '归档状态：PENDING/ARCHIVED/FAILED',
  preview_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许PC后台预览：0否，1是',
  download_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许PC后台下载：0否，1是',
  local_file_path VARCHAR(1000) DEFAULT NULL COMMENT '移动端PDF本地文件路径，失败重试时保留',
  upload_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '上传状态：PENDING/UPLOADING/UPLOADED/FAILED',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '同步失败重试次数',
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
  UNIQUE KEY uk_work_order_pdf_no (pdf_no),
  UNIQUE KEY uk_work_order_pdf_file_id (file_id),
  UNIQUE KEY uk_work_order_pdf_local_id (local_id),
  KEY idx_work_order_pdf_order (work_order_id, pdf_status),
  KEY idx_work_order_pdf_acceptance (acceptance_id),
  KEY idx_work_order_pdf_generated (generated_by, generated_at),
  KEY idx_work_order_pdf_archive (archive_status, locked_flag),
  KEY idx_work_order_pdf_sync (sync_status, updated_at),
  KEY idx_work_order_pdf_retry (upload_status, retry_count, last_retry_time),
  CONSTRAINT fk_work_order_pdf_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_pdf_acceptance
    FOREIGN KEY (acceptance_id) REFERENCES work_order_acceptance (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_pdf_file
    FOREIGN KEY (file_id) REFERENCES file_storage (file_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_pdf_generated_by
    FOREIGN KEY (generated_by) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_pdf_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='PDF验收单表';

SET FOREIGN_KEY_CHECKS = 1;
