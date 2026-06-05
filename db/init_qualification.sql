-- 海上平台现场作业管理系统 - 人员资质模块表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 work_order
--   3. init_file_attachment.sql 中的 file_storage

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS employee_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT DEFAULT NULL COMMENT '关联系统用户ID',
  employee_no VARCHAR(64) NOT NULL COMMENT '员工编号，唯一',
  real_name VARCHAR(64) NOT NULL COMMENT '姓名',
  phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  id_card_hash VARCHAR(128) DEFAULT NULL COMMENT '身份证号哈希，避免保存敏感明文',
  department_id BIGINT DEFAULT NULL COMMENT '部门/班组ID',
  position_name VARCHAR(128) DEFAULT NULL COMMENT '岗位',
  employee_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '员工状态：ACTIVE/DISABLED/LEFT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_employee_info_no (employee_no),
  KEY idx_employee_info_user (user_id),
  KEY idx_employee_info_name (real_name),
  KEY idx_employee_info_status (employee_status, deleted_flag),
  CONSTRAINT fk_employee_info_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工档案表';

CREATE TABLE IF NOT EXISTS qualification_type (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  qualification_code VARCHAR(64) NOT NULL COMMENT '资质类型编码，唯一',
  qualification_name VARCHAR(128) NOT NULL COMMENT '资质类型名称',
  warning_days INT NOT NULL DEFAULT 30 COMMENT '到期预警天数',
  required_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否关键必备资质：0否，1是',
  enabled_flag TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0否，1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_qualification_type_code (qualification_code),
  KEY idx_qualification_type_enabled (enabled_flag, required_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资质类型表';

CREATE TABLE IF NOT EXISTS employee_certificate (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  employee_id BIGINT NOT NULL COMMENT '员工ID，关联employee_info.id',
  qualification_type_id BIGINT NOT NULL COMMENT '资质类型ID，关联qualification_type.id',
  certificate_no VARCHAR(128) NOT NULL COMMENT '证书编号，唯一',
  certificate_name VARCHAR(128) NOT NULL COMMENT '证书名称',
  issue_org VARCHAR(128) DEFAULT NULL COMMENT '发证机构',
  issue_date DATE DEFAULT NULL COMMENT '发证日期',
  valid_from DATE DEFAULT NULL COMMENT '有效期开始',
  valid_to DATE DEFAULT NULL COMMENT '有效期结束',
  valid_status VARCHAR(32) NOT NULL DEFAULT 'VALID' COMMENT '证书状态：VALID/EXPIRING/EXPIRED/REVOKED',
  warning_level VARCHAR(32) DEFAULT NULL COMMENT '预警级别：NORMAL/WARNING/EXPIRED',
  file_id VARCHAR(64) DEFAULT NULL COMMENT '证书附件文件ID，关联file_storage.file_id',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_employee_certificate_no (certificate_no),
  KEY idx_employee_certificate_employee (employee_id, valid_status),
  KEY idx_employee_certificate_type (qualification_type_id, valid_status),
  KEY idx_employee_certificate_expire (valid_to, valid_status),
  CONSTRAINT fk_employee_certificate_employee
    FOREIGN KEY (employee_id) REFERENCES employee_info (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_employee_certificate_type
    FOREIGN KEY (qualification_type_id) REFERENCES qualification_type (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_employee_certificate_file
    FOREIGN KEY (file_id) REFERENCES file_storage (file_id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工资质证书表';

CREATE TABLE IF NOT EXISTS work_order_qualification_check (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，关联work_order.id',
  employee_id BIGINT NOT NULL COMMENT '员工ID，关联employee_info.id',
  certificate_id BIGINT DEFAULT NULL COMMENT '证书ID，关联employee_certificate.id',
  qualification_type_id BIGINT DEFAULT NULL COMMENT '资质类型ID',
  check_result VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '校验结果：PENDING/PASS/FAIL/EXPIRED/MISSING',
  check_time DATETIME DEFAULT NULL COMMENT '校验时间',
  checker_id BIGINT DEFAULT NULL COMMENT '校验人ID',
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
  UNIQUE KEY uk_work_order_qualification_check_local_id (local_id),
  KEY idx_work_order_qualification_order (work_order_id, check_result),
  KEY idx_work_order_qualification_employee (employee_id, check_result),
  KEY idx_work_order_qualification_sync (sync_status, updated_at),
  CONSTRAINT fk_work_order_qualification_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_qualification_employee
    FOREIGN KEY (employee_id) REFERENCES employee_info (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_qualification_certificate
    FOREIGN KEY (certificate_id) REFERENCES employee_certificate (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_qualification_checker
    FOREIGN KEY (checker_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_qualification_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单人员资质校验表';

SET FOREIGN_KEY_CHECKS = 1;
