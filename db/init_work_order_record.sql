-- 海上平台现场作业管理系统 - 移动端现场作业记录表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 project_info、work_order
-- 设计重点：移动端先写 SQLite/Realm，再通过增量同步写入服务端 MySQL。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS work_order_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，关联work_order.id',
  project_id BIGINT NOT NULL COMMENT '项目ID，冗余用于PC后台筛选统计',
  record_no VARCHAR(64) DEFAULT NULL COMMENT '施工记录编号',
  record_type VARCHAR(32) NOT NULL DEFAULT 'CONSTRUCTION' COMMENT '记录类型：CONSTRUCTION/FEEDBACK/EXCEPTION/ACCEPTANCE_PREP',
  construction_time DATETIME NOT NULL COMMENT '施工时间，移动端现场填写时间',
  construction_user_id BIGINT NOT NULL COMMENT '施工人员ID，关联sys_user.id',
  construction_user_name VARCHAR(64) DEFAULT NULL COMMENT '施工人员姓名快照',
  construction_desc TEXT NOT NULL COMMENT '施工描述',
  site_condition TEXT DEFAULT NULL COMMENT '现场情况',
  abnormal_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否异常：0否，1是',
  abnormal_desc VARCHAR(1000) DEFAULT NULL COMMENT '异常说明',
  weather VARCHAR(64) DEFAULT NULL COMMENT '天气',
  temperature DECIMAL(6,2) DEFAULT NULL COMMENT '现场温度',
  humidity DECIMAL(6,2) DEFAULT NULL COMMENT '现场湿度',
  location_name VARCHAR(255) DEFAULT NULL COMMENT '现场位置名称',
  latitude DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  longitude DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  altitude DECIMAL(10,2) DEFAULT NULL COMMENT '海拔/高度',
  attachment_count INT NOT NULL DEFAULT 0 COMMENT '附件数量，照片/视频/语音等统计',
  ai_result_count INT NOT NULL DEFAULT 0 COMMENT 'AI识别结果数量',
  record_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '记录状态：DRAFT/SUBMITTED/REJECTED/CONFIRMED',
  submitted_at DATETIME DEFAULT NULL COMMENT '提交时间',
  confirmed_by BIGINT DEFAULT NULL COMMENT 'PC后台确认人ID',
  confirmed_at DATETIME DEFAULT NULL COMMENT 'PC后台确认时间',
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
  UNIQUE KEY uk_work_order_record_no (record_no),
  UNIQUE KEY uk_work_order_record_local_id (local_id),
  KEY idx_work_order_record_order (work_order_id, construction_time),
  KEY idx_work_order_record_project_time (project_id, construction_time),
  KEY idx_work_order_record_user_time (construction_user_id, construction_time),
  KEY idx_work_order_record_status (record_status, abnormal_flag, deleted_flag),
  KEY idx_work_order_record_sync (sync_status, updated_at),
  KEY idx_work_order_record_ai_bind (work_order_id, id, attachment_count, ai_result_count),
  CONSTRAINT fk_work_order_record_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_record_project
    FOREIGN KEY (project_id) REFERENCES project_info (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_record_user
    FOREIGN KEY (construction_user_id) REFERENCES sys_user (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_record_confirmed_by
    FOREIGN KEY (confirmed_by) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_record_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='施工记录表';

CREATE TABLE IF NOT EXISTS work_order_record_detail (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，关联work_order.id',
  record_id BIGINT NOT NULL COMMENT '施工记录ID，关联work_order_record.id',
  detail_type VARCHAR(32) NOT NULL DEFAULT 'TEXT' COMMENT '明细类型：TEXT/STEP/MATERIAL/MEASURE/VOICE_TEXT/OTHER',
  detail_title VARCHAR(128) DEFAULT NULL COMMENT '明细标题',
  detail_content TEXT DEFAULT NULL COMMENT '明细内容',
  step_no INT DEFAULT NULL COMMENT '步骤序号',
  item_code VARCHAR(64) DEFAULT NULL COMMENT '明细项编码',
  item_name VARCHAR(128) DEFAULT NULL COMMENT '明细项名称',
  item_value VARCHAR(500) DEFAULT NULL COMMENT '明细项值',
  item_unit VARCHAR(32) DEFAULT NULL COMMENT '单位',
  normal_flag TINYINT DEFAULT NULL COMMENT '是否正常：0否，1是，NULL不适用',
  abnormal_desc VARCHAR(1000) DEFAULT NULL COMMENT '异常说明',
  attachment_ref_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否有关联附件：0否，1是',
  ai_ref_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否有关联AI结果：0否，1是',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
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
  UNIQUE KEY uk_work_order_record_detail_local_id (local_id),
  KEY idx_work_order_record_detail_record (record_id, sort_order),
  KEY idx_work_order_record_detail_order (work_order_id, detail_type),
  KEY idx_work_order_record_detail_sync (sync_status, updated_at),
  KEY idx_work_order_record_detail_ai_bind (record_id, attachment_ref_flag, ai_ref_flag),
  CONSTRAINT fk_work_order_record_detail_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_record_detail_record
    FOREIGN KEY (record_id) REFERENCES work_order_record (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_record_detail_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='施工记录明细表';

CREATE TABLE IF NOT EXISTS work_order_check_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，关联work_order.id',
  record_id BIGINT DEFAULT NULL COMMENT '施工记录ID，现场填写时可关联work_order_record.id',
  item_code VARCHAR(64) DEFAULT NULL COMMENT '检查项编码',
  item_name VARCHAR(128) NOT NULL COMMENT '检查项名称',
  item_type VARCHAR(32) NOT NULL DEFAULT 'BOOLEAN' COMMENT '检查项类型：BOOLEAN/TEXT/NUMBER/PHOTO/VIDEO/AUDIO/AI',
  item_desc VARCHAR(500) DEFAULT NULL COMMENT '检查项说明',
  required_flag TINYINT NOT NULL DEFAULT 1 COMMENT '是否必填：0否，1是',
  check_result VARCHAR(32) DEFAULT NULL COMMENT '检查结果：PASS/FAIL/NA/PENDING',
  check_value VARCHAR(500) DEFAULT NULL COMMENT '检查值',
  check_unit VARCHAR(32) DEFAULT NULL COMMENT '检查单位',
  abnormal_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否异常：0否，1是',
  abnormal_desc VARCHAR(1000) DEFAULT NULL COMMENT '异常说明',
  checked_by BIGINT DEFAULT NULL COMMENT '检查人ID',
  checked_at DATETIME DEFAULT NULL COMMENT '检查时间',
  attachment_required_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否要求附件：0否，1是',
  attachment_count INT NOT NULL DEFAULT 0 COMMENT '附件数量',
  ai_required_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要AI识别辅助：0否，1是',
  ai_result_count INT NOT NULL DEFAULT 0 COMMENT 'AI识别结果数量',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
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
  UNIQUE KEY uk_work_order_check_item_local_id (local_id),
  KEY idx_work_order_check_item_order (work_order_id, sort_order),
  KEY idx_work_order_check_item_record (record_id),
  KEY idx_work_order_check_item_result (check_result, abnormal_flag),
  KEY idx_work_order_check_item_sync (sync_status, updated_at),
  KEY idx_work_order_check_item_ai_bind (work_order_id, record_id, ai_required_flag, ai_result_count),
  CONSTRAINT fk_work_order_check_item_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_check_item_record
    FOREIGN KEY (record_id) REFERENCES work_order_record (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_check_item_checked_by
    FOREIGN KEY (checked_by) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_check_item_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='验收检查项表';

SET FOREIGN_KEY_CHECKS = 1;
