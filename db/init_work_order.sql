-- 海上平台现场作业管理系统 - 工单闭环管理表
-- MySQL 8.0
-- 前置依赖：init_system_permission.sql 中的 sys_user 表。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS project_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  project_code VARCHAR(64) NOT NULL COMMENT '项目编号，建议唯一',
  project_name VARCHAR(128) NOT NULL COMMENT '项目名称',
  platform_name VARCHAR(128) DEFAULT NULL COMMENT '海上平台名称',
  owner_unit VARCHAR(128) DEFAULT NULL COMMENT '业主单位',
  contractor_unit VARCHAR(128) DEFAULT NULL COMMENT '施工单位',
  project_manager_id BIGINT DEFAULT NULL COMMENT '项目经理ID，关联sys_user.id',
  project_location VARCHAR(255) DEFAULT NULL COMMENT '项目地点',
  start_date DATE DEFAULT NULL COMMENT '项目开始日期',
  end_date DATE DEFAULT NULL COMMENT '项目结束日期',
  project_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '项目状态：ACTIVE/SUSPENDED/COMPLETED/CLOSED',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID映射，回传移动端使用',
  version INT NOT NULL DEFAULT 0 COMMENT '同步版本号',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'SYNCED' COMMENT '同步状态：PENDING/SYNCING/SYNCED/FAILED/CONFLICT',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '来源设备ID',
  operator_id BIGINT DEFAULT NULL COMMENT '最后操作人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_project_info_code (project_code),
  UNIQUE KEY uk_project_info_local_id (local_id),
  KEY idx_project_info_manager (project_manager_id),
  KEY idx_project_info_status (project_status, deleted_flag),
  KEY idx_project_info_time (start_date, end_date),
  KEY idx_project_info_sync (sync_status, updated_at),
  CONSTRAINT fk_project_info_manager
    FOREIGN KEY (project_manager_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_project_info_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目信息表';

CREATE TABLE IF NOT EXISTS work_order_template (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  template_code VARCHAR(64) NOT NULL COMMENT '模板编号，唯一',
  template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
  work_type VARCHAR(64) DEFAULT NULL COMMENT '作业类型',
  default_priority VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '默认优先级：LOW/NORMAL/HIGH/URGENT',
  default_work_content TEXT DEFAULT NULL COMMENT '默认作业内容',
  default_material_desc TEXT DEFAULT NULL COMMENT '默认所需物料说明',
  default_duration_hours DECIMAL(10,2) DEFAULT NULL COMMENT '默认计划工时',
  enabled_flag TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0否，1是',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID映射，回传移动端使用',
  version INT NOT NULL DEFAULT 0 COMMENT '同步版本号',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'SYNCED' COMMENT '同步状态：PENDING/SYNCING/SYNCED/FAILED/CONFLICT',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '来源设备ID',
  operator_id BIGINT DEFAULT NULL COMMENT '最后操作人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_work_order_template_code (template_code),
  UNIQUE KEY uk_work_order_template_local_id (local_id),
  KEY idx_work_order_template_type (work_type, enabled_flag),
  KEY idx_work_order_template_sync (sync_status, updated_at),
  CONSTRAINT fk_work_order_template_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单模板表';

CREATE TABLE IF NOT EXISTS work_order (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  work_order_no VARCHAR(64) NOT NULL COMMENT '工单编号，唯一',
  project_id BIGINT NOT NULL COMMENT '项目ID，关联project_info.id',
  template_id BIGINT DEFAULT NULL COMMENT '模板ID，关联work_order_template.id',
  work_title VARCHAR(128) NOT NULL COMMENT '工单标题',
  work_type VARCHAR(64) DEFAULT NULL COMMENT '作业类型',
  work_location VARCHAR(255) NOT NULL COMMENT '作业地点，移动端派工单展示',
  work_content TEXT NOT NULL COMMENT '作业内容，移动端派工单展示',
  required_material_desc TEXT DEFAULT NULL COMMENT '所需物料说明，移动端派工单展示',
  leader_id BIGINT DEFAULT NULL COMMENT '负责人ID，关联sys_user.id',
  maintainer_id BIGINT DEFAULT NULL COMMENT '主维修工ID，关联sys_user.id',
  planned_start_time DATETIME DEFAULT NULL COMMENT '计划开始时间',
  planned_end_time DATETIME DEFAULT NULL COMMENT '计划结束时间',
  actual_start_time DATETIME DEFAULT NULL COMMENT '实际开始时间',
  actual_end_time DATETIME DEFAULT NULL COMMENT '实际结束时间',
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING_ASSIGN' COMMENT '状态：PENDING_ASSIGN/ASSIGNED/IN_PROGRESS/PENDING_ACCEPTANCE/COMPLETED/REJECTED/CLOSED',
  priority VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级：LOW/NORMAL/HIGH/URGENT',
  reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
  close_reason VARCHAR(500) DEFAULT NULL COMMENT '关闭原因',
  acceptance_required TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要验收：0否，1是',
  source_type VARCHAR(32) NOT NULL DEFAULT 'PC' COMMENT '来源：PC/MOBILE/SYNC/TEMPLATE',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID映射，回传移动端使用',
  version INT NOT NULL DEFAULT 0 COMMENT '同步版本号',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'SYNCED' COMMENT '同步状态：PENDING/SYNCING/SYNCED/FAILED/CONFLICT',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '来源设备ID',
  operator_id BIGINT DEFAULT NULL COMMENT '最后操作人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_work_order_no (work_order_no),
  UNIQUE KEY uk_work_order_local_id (local_id),
  KEY idx_work_order_project_status (project_id, status, deleted_flag),
  KEY idx_work_order_time (planned_start_time, planned_end_time, actual_start_time, actual_end_time),
  KEY idx_work_order_people (leader_id, maintainer_id),
  KEY idx_work_order_priority (priority, status),
  KEY idx_work_order_sync (sync_status, updated_at),
  KEY idx_work_order_mobile_list (maintainer_id, status, updated_at),
  CONSTRAINT fk_work_order_project
    FOREIGN KEY (project_id) REFERENCES project_info (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_template
    FOREIGN KEY (template_id) REFERENCES work_order_template (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_leader
    FOREIGN KEY (leader_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_maintainer
    FOREIGN KEY (maintainer_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单主表';

CREATE TABLE IF NOT EXISTS work_order_status_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  work_order_id BIGINT NOT NULL COMMENT '工单ID',
  from_status VARCHAR(32) DEFAULT NULL COMMENT '变更前状态',
  to_status VARCHAR(32) NOT NULL COMMENT '变更后状态',
  operation_type VARCHAR(64) NOT NULL COMMENT '操作类型：CREATE/ASSIGN/START/SUBMIT_ACCEPTANCE/COMPLETE/REJECT/CLOSE',
  operation_desc VARCHAR(500) DEFAULT NULL COMMENT '状态流转说明',
  operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
  operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID映射，回传移动端使用',
  version INT NOT NULL DEFAULT 0 COMMENT '同步版本号',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'SYNCED' COMMENT '同步状态：PENDING/SYNCING/SYNCED/FAILED/CONFLICT',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '来源设备ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_work_order_status_log_local_id (local_id),
  KEY idx_work_order_status_log_order (work_order_id, operation_time),
  KEY idx_work_order_status_log_status (from_status, to_status),
  KEY idx_work_order_status_log_operator (operator_id, operation_time),
  KEY idx_work_order_status_log_sync (sync_status, updated_at),
  CONSTRAINT fk_work_order_status_log_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_status_log_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单状态流转表';

CREATE TABLE IF NOT EXISTS work_order_assignment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  work_order_id BIGINT NOT NULL COMMENT '工单ID',
  assigner_id BIGINT DEFAULT NULL COMMENT '派工人ID',
  assignee_id BIGINT NOT NULL COMMENT '维修工/执行人ID',
  assignment_role VARCHAR(32) NOT NULL DEFAULT 'MAINTAINER' COMMENT '派工角色：LEADER/MAINTAINER/ACCEPTOR',
  assignment_status VARCHAR(32) NOT NULL DEFAULT 'ASSIGNED' COMMENT '派工状态：ASSIGNED/ACCEPTED/REJECTED/CANCELLED/COMPLETED',
  assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '派工时间',
  accepted_at DATETIME DEFAULT NULL COMMENT '接单时间',
  rejected_at DATETIME DEFAULT NULL COMMENT '拒单时间',
  completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
  reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒单原因',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID映射，回传移动端使用',
  version INT NOT NULL DEFAULT 0 COMMENT '同步版本号',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'SYNCED' COMMENT '同步状态：PENDING/SYNCING/SYNCED/FAILED/CONFLICT',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '来源设备ID',
  operator_id BIGINT DEFAULT NULL COMMENT '最后操作人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_work_order_assignment_local_id (local_id),
  KEY idx_work_order_assignment_order (work_order_id, assignment_status),
  KEY idx_work_order_assignment_assignee (assignee_id, assignment_status, updated_at),
  KEY idx_work_order_assignment_assigner (assigner_id, assigned_at),
  KEY idx_work_order_assignment_sync (sync_status, updated_at),
  CONSTRAINT fk_work_order_assignment_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_assignment_assigner
    FOREIGN KEY (assigner_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_assignment_assignee
    FOREIGN KEY (assignee_id) REFERENCES sys_user (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_assignment_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单派工表';

CREATE TABLE IF NOT EXISTS work_order_material (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  work_order_id BIGINT NOT NULL COMMENT '工单ID',
  material_code VARCHAR(64) DEFAULT NULL COMMENT '物料编号，后续可关联material.material_code',
  material_name VARCHAR(128) NOT NULL COMMENT '物料名称',
  material_spec VARCHAR(128) DEFAULT NULL COMMENT '规格型号',
  unit VARCHAR(32) DEFAULT NULL COMMENT '单位',
  planned_qty DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '计划所需数量',
  actual_qty DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '实际使用数量，后续由物料使用记录汇总',
  material_desc VARCHAR(500) DEFAULT NULL COMMENT '物料说明，移动端派工单展示',
  required_flag TINYINT NOT NULL DEFAULT 1 COMMENT '是否必需：0否，1是',
  prepare_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '备料状态：PENDING/PREPARED/PARTIAL/SHORTAGE',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID映射，回传移动端使用',
  version INT NOT NULL DEFAULT 0 COMMENT '同步版本号',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'SYNCED' COMMENT '同步状态：PENDING/SYNCING/SYNCED/FAILED/CONFLICT',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '来源设备ID',
  operator_id BIGINT DEFAULT NULL COMMENT '最后操作人ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_work_order_material_local_id (local_id),
  KEY idx_work_order_material_order (work_order_id, prepare_status),
  KEY idx_work_order_material_code (material_code),
  KEY idx_work_order_material_sync (sync_status, updated_at),
  CONSTRAINT fk_work_order_material_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_material_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单所需物料表';

SET FOREIGN_KEY_CHECKS = 1;
