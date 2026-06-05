-- 海上平台现场作业管理系统 - 离线同步核心表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 work_order
-- 设计重点：
--   移动端离线操作先写 SQLite/Realm 和本地同步队列；
--   网络恢复后批量上传增量数据；
--   服务端根据 server_id、local_id、version、updated_at 判断新增、更新、删除或冲突；
--   多端冲突采用“最后写入为准 + 人工复核”，冲突数据必须留痕。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS device_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  device_id VARCHAR(128) NOT NULL COMMENT '设备唯一ID，移动端首次生成后长期保存',
  user_id BIGINT NOT NULL COMMENT '绑定用户ID',
  device_name VARCHAR(128) DEFAULT NULL COMMENT '设备名称',
  platform VARCHAR(32) NOT NULL DEFAULT 'ANDROID' COMMENT '平台：ANDROID/IOS/WEB/OTHER',
  os_version VARCHAR(64) DEFAULT NULL COMMENT '系统版本',
  app_version VARCHAR(64) DEFAULT NULL COMMENT 'App版本',
  manufacturer VARCHAR(128) DEFAULT NULL COMMENT '设备厂商',
  model VARCHAR(128) DEFAULT NULL COMMENT '设备型号',
  imei_hash VARCHAR(128) DEFAULT NULL COMMENT '设备标识哈希，避免保存敏感原文',
  push_token VARCHAR(255) DEFAULT NULL COMMENT '推送token',
  register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  last_login_time DATETIME DEFAULT NULL COMMENT '最近登录时间',
  last_heartbeat_time DATETIME DEFAULT NULL COMMENT '最近心跳时间',
  last_sync_time DATETIME DEFAULT NULL COMMENT '最近同步时间',
  last_sync_cursor VARCHAR(255) DEFAULT NULL COMMENT '最近同步游标',
  online_status VARCHAR(32) NOT NULL DEFAULT 'OFFLINE' COMMENT '在线状态：ONLINE/OFFLINE',
  device_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '设备状态：ACTIVE/DISABLED/LOST',
  sync_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许同步：0否，1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_device_info_device_id (device_id),
  KEY idx_device_info_user (user_id, device_status),
  KEY idx_device_info_heartbeat (last_heartbeat_time, online_status),
  KEY idx_device_info_sync (sync_enabled, last_sync_time),
  CONSTRAINT fk_device_info_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备信息表';

CREATE TABLE IF NOT EXISTS sync_task (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  sync_task_no VARCHAR(64) NOT NULL COMMENT '同步任务编号，唯一',
  batch_id VARCHAR(128) NOT NULL COMMENT '移动端同步批次ID，建议同设备唯一',
  device_id VARCHAR(128) NOT NULL COMMENT '设备ID',
  operator_id BIGINT NOT NULL COMMENT '操作人ID',
  sync_direction VARCHAR(32) NOT NULL COMMENT '同步方向：PUSH/PULL/ACK',
  sync_type VARCHAR(32) NOT NULL DEFAULT 'INCREMENTAL' COMMENT '同步类型：FULL/INCREMENTAL/RETRY',
  task_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING/SYNCING/SYNCED/FAILED/CONFLICT',
  total_count INT NOT NULL DEFAULT 0 COMMENT '总条数',
  success_count INT NOT NULL DEFAULT 0 COMMENT '成功条数',
  failed_count INT NOT NULL DEFAULT 0 COMMENT '失败条数',
  conflict_count INT NOT NULL DEFAULT 0 COMMENT '冲突条数',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '任务重试次数',
  max_retry_count INT NOT NULL DEFAULT 5 COMMENT '最大重试次数',
  request_cursor VARCHAR(255) DEFAULT NULL COMMENT '请求游标',
  response_cursor VARCHAR(255) DEFAULT NULL COMMENT '响应游标',
  client_time DATETIME DEFAULT NULL COMMENT '客户端时间',
  server_start_time DATETIME DEFAULT NULL COMMENT '服务端开始处理时间',
  server_end_time DATETIME DEFAULT NULL COMMENT '服务端结束处理时间',
  error_code VARCHAR(64) DEFAULT NULL COMMENT '任务错误码',
  error_message VARCHAR(1000) DEFAULT NULL COMMENT '任务错误信息',
  idempotency_key VARCHAR(128) DEFAULT NULL COMMENT '幂等key，可来自请求头或batch_id',
  request_summary JSON DEFAULT NULL COMMENT '请求摘要，不保存大payload',
  response_summary JSON DEFAULT NULL COMMENT '响应摘要',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sync_task_no (sync_task_no),
  UNIQUE KEY uk_sync_task_device_batch (device_id, batch_id),
  UNIQUE KEY uk_sync_task_idempotency (idempotency_key),
  KEY idx_sync_task_device_status (device_id, task_status, updated_at),
  KEY idx_sync_task_operator (operator_id, created_at),
  KEY idx_sync_task_status (task_status, sync_direction, created_at),
  CONSTRAINT fk_sync_task_device
    FOREIGN KEY (device_id) REFERENCES device_info (device_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_sync_task_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='同步任务表';

CREATE TABLE IF NOT EXISTS sync_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  sync_task_id BIGINT NOT NULL COMMENT '同步任务ID',
  batch_id VARCHAR(128) NOT NULL COMMENT '同步批次ID',
  device_id VARCHAR(128) NOT NULL COMMENT '设备ID',
  operator_id BIGINT NOT NULL COMMENT '操作人ID',
  module_type VARCHAR(64) NOT NULL COMMENT '模块类型：WORK_ORDER/WORK_RECORD/ATTACHMENT_META/SIGNATURE/PDF/MATERIAL_USAGE/AI_RESULT等',
  entity_type VARCHAR(128) NOT NULL COMMENT '实体类型/表名',
  action_type VARCHAR(32) NOT NULL COMMENT '动作类型：CREATE/UPDATE/DELETE/UPLOAD/ACK',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID',
  entity_id BIGINT DEFAULT NULL COMMENT '业务实体ID，通常等同server_id',
  work_order_id BIGINT DEFAULT NULL COMMENT '工单ID，可为空但建议保存',
  business_no VARCHAR(128) DEFAULT NULL COMMENT '业务编号，如工单号/附件编号',
  client_version INT DEFAULT NULL COMMENT '客户端提交版本',
  server_version INT DEFAULT NULL COMMENT '服务端当前版本',
  client_updated_at DATETIME DEFAULT NULL COMMENT '客户端更新时间',
  server_updated_at DATETIME DEFAULT NULL COMMENT '服务端更新时间',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '同步状态：PENDING/SYNCING/SYNCED/FAILED/CONFLICT',
  conflict_id BIGINT DEFAULT NULL COMMENT '冲突ID，关联sync_conflict.id',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '记录级重试次数',
  checksum VARCHAR(128) DEFAULT NULL COMMENT '数据校验值',
  error_code VARCHAR(64) DEFAULT NULL COMMENT '错误码',
  error_message VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
  request_payload JSON DEFAULT NULL COMMENT '单条请求payload，可按需脱敏',
  response_payload JSON DEFAULT NULL COMMENT '单条响应payload',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_sync_log_task (sync_task_id, sync_status),
  KEY idx_sync_log_device_batch (device_id, batch_id),
  KEY idx_sync_log_entity (entity_type, server_id, local_id),
  KEY idx_sync_log_order (work_order_id, created_at),
  KEY idx_sync_log_status (sync_status, module_type, created_at),
  KEY idx_sync_log_retry (sync_status, retry_count, updated_at),
  CONSTRAINT fk_sync_log_task
    FOREIGN KEY (sync_task_id) REFERENCES sync_task (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_sync_log_device
    FOREIGN KEY (device_id) REFERENCES device_info (device_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_sync_log_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_sync_log_work_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='同步日志表';

CREATE TABLE IF NOT EXISTS sync_conflict (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  conflict_no VARCHAR(64) NOT NULL COMMENT '冲突编号，唯一',
  sync_task_id BIGINT DEFAULT NULL COMMENT '同步任务ID',
  sync_log_id BIGINT DEFAULT NULL COMMENT '同步日志ID',
  device_id VARCHAR(128) NOT NULL COMMENT '设备ID',
  operator_id BIGINT NOT NULL COMMENT '移动端操作人ID',
  module_type VARCHAR(64) NOT NULL COMMENT '模块类型',
  entity_type VARCHAR(128) NOT NULL COMMENT '实体类型/表名',
  entity_id BIGINT DEFAULT NULL COMMENT '服务端实体ID',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID',
  work_order_id BIGINT DEFAULT NULL COMMENT '工单ID，便于PC按工单查看冲突',
  business_no VARCHAR(128) DEFAULT NULL COMMENT '业务编号，如工单号',
  base_version INT DEFAULT NULL COMMENT '客户端基于的版本',
  client_version INT DEFAULT NULL COMMENT '客户端提交版本',
  server_version INT DEFAULT NULL COMMENT '服务端当前版本',
  client_updated_at DATETIME DEFAULT NULL COMMENT '客户端更新时间',
  server_updated_at DATETIME DEFAULT NULL COMMENT '服务端更新时间',
  conflict_type VARCHAR(64) NOT NULL COMMENT '冲突类型：VERSION_CONFLICT/FIELD_CONFLICT/UPDATE_AFTER_DELETE/DELETE_AFTER_UPDATE/DUPLICATE_CREATE/PERMISSION_CONFLICT/ACCEPTANCE_LOCKED_CONFLICT',
  conflict_fields JSON DEFAULT NULL COMMENT '冲突字段列表和差异说明',
  old_payload JSON DEFAULT NULL COMMENT '旧版本/基线数据',
  client_payload JSON DEFAULT NULL COMMENT '移动端新版本数据',
  server_payload JSON DEFAULT NULL COMMENT '服务端当前版本数据',
  final_payload JSON DEFAULT NULL COMMENT '最终处理后的数据',
  default_strategy VARCHAR(32) NOT NULL DEFAULT 'LAST_WRITE_WINS' COMMENT '默认策略：LAST_WRITE_WINS',
  resolve_strategy VARCHAR(32) DEFAULT NULL COMMENT '处理策略：KEEP_SERVER/KEEP_CLIENT/MANUAL_MERGE/IGNORE_CLIENT',
  resolve_status VARCHAR(32) NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '处理状态：PENDING_REVIEW/RESOLVED/IGNORED/REOPENED',
  resolver_id BIGINT DEFAULT NULL COMMENT 'PC后台处理人ID',
  resolve_time DATETIME DEFAULT NULL COMMENT '处理时间',
  resolve_comment VARCHAR(1000) DEFAULT NULL COMMENT '处理说明',
  last_write_side VARCHAR(32) DEFAULT NULL COMMENT '最后写入方：CLIENT/SERVER',
  last_write_time DATETIME DEFAULT NULL COMMENT '最后写入时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sync_conflict_no (conflict_no),
  KEY idx_sync_conflict_review (resolve_status, conflict_type, created_at),
  KEY idx_sync_conflict_order (work_order_id, resolve_status),
  KEY idx_sync_conflict_entity (entity_type, server_id, local_id),
  KEY idx_sync_conflict_device (device_id, operator_id),
  KEY idx_sync_conflict_task (sync_task_id, sync_log_id),
  CONSTRAINT fk_sync_conflict_task
    FOREIGN KEY (sync_task_id) REFERENCES sync_task (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_sync_conflict_log
    FOREIGN KEY (sync_log_id) REFERENCES sync_log (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_sync_conflict_device
    FOREIGN KEY (device_id) REFERENCES device_info (device_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_sync_conflict_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_sync_conflict_resolver
    FOREIGN KEY (resolver_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_sync_conflict_work_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='同步冲突表';

CREATE TABLE IF NOT EXISTS work_order_version_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  work_order_id BIGINT NOT NULL COMMENT '工单ID',
  work_order_no VARCHAR(64) NOT NULL COMMENT '工单编号快照',
  version INT NOT NULL COMMENT '工单版本号',
  previous_version INT DEFAULT NULL COMMENT '上一版本号',
  change_source VARCHAR(32) NOT NULL COMMENT '变更来源：PC/MOBILE/SYNC/CONFLICT_RESOLVE',
  change_type VARCHAR(64) NOT NULL COMMENT '变更类型：CREATE/UPDATE/STATUS_CHANGE/ASSIGN/ACCEPTANCE/CONFLICT_RESOLVE/DELETE',
  changed_fields JSON DEFAULT NULL COMMENT '变更字段列表',
  old_payload JSON DEFAULT NULL COMMENT '变更前工单快照',
  new_payload JSON DEFAULT NULL COMMENT '变更后工单快照',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '来源设备ID',
  operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
  sync_task_id BIGINT DEFAULT NULL COMMENT '关联同步任务ID',
  sync_log_id BIGINT DEFAULT NULL COMMENT '关联同步日志ID',
  conflict_id BIGINT DEFAULT NULL COMMENT '关联冲突ID',
  client_updated_at DATETIME DEFAULT NULL COMMENT '客户端更新时间',
  server_updated_at DATETIME DEFAULT NULL COMMENT '服务端更新时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_work_order_version (work_order_id, version),
  KEY idx_work_order_version_no (work_order_no, version),
  KEY idx_work_order_version_source (change_source, change_type, created_at),
  KEY idx_work_order_version_device (device_id, operator_id),
  KEY idx_work_order_version_conflict (conflict_id),
  CONSTRAINT fk_work_order_version_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_version_device
    FOREIGN KEY (device_id) REFERENCES device_info (device_id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_version_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_version_task
    FOREIGN KEY (sync_task_id) REFERENCES sync_task (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_version_log
    FOREIGN KEY (sync_log_id) REFERENCES sync_log (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_version_conflict
    FOREIGN KEY (conflict_id) REFERENCES sync_conflict (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单版本日志表';

SET FOREIGN_KEY_CHECKS = 1;
