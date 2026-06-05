-- 海上平台现场作业管理系统 - MySQL 8.0 完整初始化脚本
-- 生成说明：由 db/init_*.sql 分模块脚本按依赖顺序汇总。
-- 统一约束：InnoDB、utf8mb4、软删除 deleted_flag、created_at、updated_at、离线同步字段。
-- 本脚本用于初始化建表和基础角色权限数据；统计查询样例保留在 init_dashboard_report.sql。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- ======================================================================
-- Begin module: init_system_permission.sql
-- ======================================================================
-- 海上平台现场作业管理系统 - 系统权限与操作日志表
-- MySQL 8.0
-- 用途：服务 PC 后台登录、移动端登录、角色权限控制、关键操作审计。


CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(64) NOT NULL COMMENT '登录账号，唯一',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希，不保存明文密码',
  real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
  phone VARCHAR(32) DEFAULT NULL COMMENT '手机号，可用于移动端登录',
  email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  employee_no VARCHAR(64) DEFAULT NULL COMMENT '员工编号',
  avatar_file_id VARCHAR(64) DEFAULT NULL COMMENT '头像文件ID，对应 file_storage.file_id',
  account_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态：ACTIVE/DISABLED/LOCKED/EXPIRED',
  pc_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许登录PC后台：0否，1是',
  mobile_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许登录移动端：0否，1是',
  primary_project_id BIGINT DEFAULT NULL COMMENT '默认所属项目ID，项目表建立后可关联 project.id',
  department_id BIGINT DEFAULT NULL COMMENT '所属部门/班组ID，组织表建立后可关联',
  last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
  last_login_ip VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
  password_updated_at DATETIME DEFAULT NULL COMMENT '密码最后修改时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username),
  UNIQUE KEY uk_sys_user_phone (phone),
  KEY idx_sys_user_status (account_status, deleted_flag),
  KEY idx_sys_user_project (primary_project_id),
  KEY idx_sys_user_department (department_id),
  KEY idx_sys_user_mobile_login (phone, mobile_enabled, account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  role_code VARCHAR(64) NOT NULL COMMENT '角色编码，唯一',
  role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
  role_type VARCHAR(32) NOT NULL DEFAULT 'BUSINESS' COMMENT '角色类型：SYSTEM/BUSINESS',
  data_scope VARCHAR(32) NOT NULL DEFAULT 'SELF' COMMENT '数据范围：ALL/PROJECT/SELF/MATERIAL/QUALIFICATION/DASHBOARD/ACCEPTANCE',
  pc_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否适用于PC后台',
  mobile_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '是否适用于移动端',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (role_code),
  KEY idx_sys_role_status (status, deleted_flag),
  KEY idx_sys_role_data_scope (data_scope)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  parent_id BIGINT DEFAULT NULL COMMENT '父级权限ID',
  permission_code VARCHAR(128) NOT NULL COMMENT '权限编码，唯一',
  permission_name VARCHAR(128) NOT NULL COMMENT '权限名称',
  permission_type VARCHAR(32) NOT NULL COMMENT '权限类型：MENU/BUTTON/API',
  platform VARCHAR(32) NOT NULL DEFAULT 'PC' COMMENT '适用平台：PC/MOBILE/BOTH',
  route_path VARCHAR(255) DEFAULT NULL COMMENT 'PC前端路由',
  api_method VARCHAR(16) DEFAULT NULL COMMENT '接口方法：GET/POST/PUT/DELETE等',
  api_path VARCHAR(255) DEFAULT NULL COMMENT '后端接口路径',
  component_path VARCHAR(255) DEFAULT NULL COMMENT 'PC页面组件路径',
  icon VARCHAR(64) DEFAULT NULL COMMENT '菜单图标',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  visible_flag TINYINT NOT NULL DEFAULT 1 COMMENT '菜单是否可见：0否，1是',
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_permission_code (permission_code),
  KEY idx_sys_permission_parent (parent_id),
  KEY idx_sys_permission_type (permission_type, platform),
  KEY idx_sys_permission_api (api_method, api_path),
  KEY idx_sys_permission_status (status, deleted_flag),
  CONSTRAINT fk_sys_permission_parent
    FOREIGN KEY (parent_id) REFERENCES sys_permission (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_role (user_id, role_id, deleted_flag),
  KEY idx_sys_user_role_user (user_id),
  KEY idx_sys_user_role_role (role_id),
  CONSTRAINT fk_sys_user_role_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_sys_user_role_role
    FOREIGN KEY (role_id) REFERENCES sys_role (id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  permission_id BIGINT NOT NULL COMMENT '权限ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_permission (role_id, permission_id, deleted_flag),
  KEY idx_sys_role_permission_role (role_id),
  KEY idx_sys_role_permission_permission (permission_id),
  CONSTRAINT fk_sys_role_permission_role
    FOREIGN KEY (role_id) REFERENCES sys_role (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_sys_role_permission_permission
    FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  trace_id VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
  operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
  operator_name VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
  role_code VARCHAR(64) DEFAULT NULL COMMENT '操作时角色编码',
  platform VARCHAR(32) NOT NULL DEFAULT 'PC' COMMENT '操作平台：PC/MOBILE/SYNC/API',
  module_name VARCHAR(64) NOT NULL COMMENT '模块名称，如工单、物料、资质、同步、AI',
  operation_type VARCHAR(64) NOT NULL COMMENT '操作类型，如LOGIN/CREATE/UPDATE/DELETE/ASSIGN/EXPORT',
  business_type VARCHAR(64) DEFAULT NULL COMMENT '业务类型，如WORK_ORDER/MATERIAL/CERTIFICATE',
  business_id VARCHAR(64) DEFAULT NULL COMMENT '业务ID，字符串避免前端精度问题',
  business_no VARCHAR(128) DEFAULT NULL COMMENT '业务编号，如工单编号',
  project_id BIGINT DEFAULT NULL COMMENT '项目ID，用于项目经理数据范围和统计',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '移动端设备ID',
  request_method VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  request_path VARCHAR(255) DEFAULT NULL COMMENT '请求路径',
  request_ip VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
  user_agent VARCHAR(500) DEFAULT NULL COMMENT '客户端信息',
  request_body JSON DEFAULT NULL COMMENT '脱敏后的请求体，不保存密码、token和大payload',
  result_status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '结果：SUCCESS/FAILED',
  error_code VARCHAR(64) DEFAULT NULL COMMENT '错误码',
  error_message VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '业务操作时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_operation_log_operator (operator_id, operation_time),
  KEY idx_operation_log_business (business_type, business_id),
  KEY idx_operation_log_business_no (business_no),
  KEY idx_operation_log_project (project_id, operation_time),
  KEY idx_operation_log_module (module_name, operation_type),
  KEY idx_operation_log_platform (platform, device_id),
  KEY idx_operation_log_result (result_status, operation_time),
  CONSTRAINT fk_operation_log_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

INSERT INTO sys_role (
  role_code, role_name, role_type, data_scope, pc_enabled, mobile_enabled, sort_order, remark
) VALUES
  ('SYSTEM_ADMIN', '系统管理员', 'SYSTEM', 'ALL', 1, 0, 10, '拥有系统配置、用户、角色、权限和全部数据权限'),
  ('PROJECT_MANAGER', '项目经理', 'BUSINESS', 'PROJECT', 1, 1, 20, '查看和管理所属项目工单，处理同步冲突和验收复核'),
  ('MAINTAINER', '维修工', 'BUSINESS', 'SELF', 0, 1, 30, '移动端查看、接收、记录和同步自己的工单'),
  ('MATERIAL_ADMIN', '物资管理员', 'BUSINESS', 'MATERIAL', 1, 0, 40, '管理物料、库存、出入库和二维码追溯'),
  ('QUALIFICATION_ADMIN', '资质管理员', 'BUSINESS', 'QUALIFICATION', 1, 0, 50, '管理员工档案、证书和资质预警'),
  ('ACCEPTANCE_USER', '验收人员', 'BUSINESS', 'ACCEPTANCE', 1, 1, 60, '执行现场验收、电子签名、PDF验收单归档'),
  ('BUSINESS_USER', '经营人员', 'BUSINESS', 'DASHBOARD', 1, 0, 70, '查看经营看板和导出对账单')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_type = VALUES(role_type),
  data_scope = VALUES(data_scope),
  pc_enabled = VALUES(pc_enabled),
  mobile_enabled = VALUES(mobile_enabled),
  sort_order = VALUES(sort_order),
  remark = VALUES(remark),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_permission (
  permission_code, permission_name, permission_type, platform, route_path, api_method, api_path, sort_order, remark
) VALUES
  ('AUTH_LOGIN', '登录', 'API', 'BOTH', NULL, 'POST', '/api/auth/login', 10, 'PC后台和移动端登录'),
  ('AUTH_LOGOUT', '退出登录', 'API', 'BOTH', NULL, 'POST', '/api/auth/logout', 20, '退出登录'),
  ('AUTH_CURRENT', '当前用户信息', 'API', 'BOTH', NULL, 'GET', '/api/auth/current', 30, '读取当前登录用户'),
  ('ADMIN_USER_MANAGE', '用户管理', 'MENU', 'PC', '/system/users', NULL, NULL, 100, '系统用户管理'),
  ('ADMIN_ROLE_MANAGE', '角色管理', 'MENU', 'PC', '/system/roles', NULL, NULL, 110, '系统角色管理'),
  ('ADMIN_PERMISSION_MANAGE', '权限管理', 'MENU', 'PC', '/system/permissions', NULL, NULL, 120, '系统权限管理'),
  ('ADMIN_WORK_ORDER_MANAGE', '工单管理', 'MENU', 'PC', '/work-orders', NULL, NULL, 200, 'PC后台工单管理'),
  ('MOBILE_MY_WORK_ORDER', '我的工单', 'API', 'MOBILE', NULL, 'GET', '/api/mobile/work-orders', 300, '维修工只能查看自己的工单'),
  ('MOBILE_WORK_ORDER_SYNC', '工单同步', 'API', 'MOBILE', NULL, 'POST', '/api/sync/push', 310, '移动端离线数据上传'),
  ('ADMIN_MATERIAL_MANAGE', '物料管理', 'MENU', 'PC', '/materials', NULL, NULL, 400, '物料、库存、出入库管理'),
  ('ADMIN_QUALIFICATION_MANAGE', '资质管理', 'MENU', 'PC', '/qualifications', NULL, NULL, 500, '人员证书与资质预警'),
  ('ADMIN_ACCEPTANCE_MANAGE', '验收管理', 'MENU', 'PC', '/acceptance', NULL, NULL, 600, '验收、签名、PDF归档'),
  ('ADMIN_DASHBOARD_VIEW', '经营看板', 'MENU', 'PC', '/dashboard', NULL, NULL, 700, '经营看板查看'),
  ('ADMIN_RECONCILIATION_EXPORT', '导出对账单', 'API', 'PC', NULL, 'GET', '/api/admin/reports/reconciliation/export', 710, '经营对账单导出'),
  ('ADMIN_OPERATION_LOG_VIEW', '操作日志查看', 'MENU', 'PC', '/system/operation-logs', NULL, NULL, 800, '关键操作审计')
ON DUPLICATE KEY UPDATE
  permission_name = VALUES(permission_name),
  permission_type = VALUES(permission_type),
  platform = VALUES(platform),
  route_path = VALUES(route_path),
  api_method = VALUES(api_method),
  api_path = VALUES(api_path),
  sort_order = VALUES(sort_order),
  remark = VALUES(remark),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p
WHERE r.role_code = 'SYSTEM_ADMIN'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP, deleted_flag = 0;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
  'AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT',
  'ADMIN_WORK_ORDER_MANAGE', 'MOBILE_MY_WORK_ORDER', 'MOBILE_WORK_ORDER_SYNC',
  'ADMIN_ACCEPTANCE_MANAGE', 'ADMIN_OPERATION_LOG_VIEW'
)
WHERE r.role_code = 'PROJECT_MANAGER'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP, deleted_flag = 0;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
  'AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT',
  'MOBILE_MY_WORK_ORDER', 'MOBILE_WORK_ORDER_SYNC'
)
WHERE r.role_code = 'MAINTAINER'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP, deleted_flag = 0;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
  'AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT',
  'ADMIN_MATERIAL_MANAGE', 'ADMIN_OPERATION_LOG_VIEW'
)
WHERE r.role_code = 'MATERIAL_ADMIN'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP, deleted_flag = 0;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
  'AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT',
  'ADMIN_QUALIFICATION_MANAGE', 'ADMIN_OPERATION_LOG_VIEW'
)
WHERE r.role_code = 'QUALIFICATION_ADMIN'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP, deleted_flag = 0;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
  'AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT',
  'ADMIN_ACCEPTANCE_MANAGE', 'MOBILE_MY_WORK_ORDER', 'MOBILE_WORK_ORDER_SYNC',
  'ADMIN_OPERATION_LOG_VIEW'
)
WHERE r.role_code = 'ACCEPTANCE_USER'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP, deleted_flag = 0;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
  'AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT',
  'ADMIN_DASHBOARD_VIEW', 'ADMIN_RECONCILIATION_EXPORT'
)
WHERE r.role_code = 'BUSINESS_USER'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP, deleted_flag = 0;


-- ======================================================================
-- Begin module: init_work_order.sql
-- ======================================================================
-- 海上平台现场作业管理系统 - 工单闭环管理表
-- MySQL 8.0
-- 前置依赖：init_system_permission.sql 中的 sys_user 表。


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


-- ======================================================================
-- Begin module: init_work_order_record.sql
-- ======================================================================
-- 海上平台现场作业管理系统 - 移动端现场作业记录表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 project_info、work_order
-- 设计重点：移动端先写 SQLite/Realm，再通过增量同步写入服务端 MySQL。


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


-- ======================================================================
-- Begin module: init_file_attachment.sql
-- ======================================================================
-- 海上平台现场作业管理系统 - 现场多媒体附件与文件元数据表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 work_order
--   3. init_work_order_record.sql 中的 work_order_record
-- 设计重点：
--   MySQL 只保存文件元数据和业务关联，不保存照片、视频、语音、PDF、AI结果图等大文件二进制本体。


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


-- ======================================================================
-- Begin module: init_qualification.sql
-- ======================================================================
-- 海上平台现场作业管理系统 - 人员资质模块表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 work_order
--   3. init_file_attachment.sql 中的 file_storage


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


-- ======================================================================
-- Begin module: init_acceptance_signature_pdf.sql
-- ======================================================================
-- 海上平台现场作业管理系统 - 电子签名与PDF验收单表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 project_info、work_order
--   3. init_work_order_record.sql 中的 work_order_record
--   4. init_file_attachment.sql 中的 file_storage


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


-- ======================================================================
-- Begin module: init_material_trace.sql
-- ======================================================================
-- 海上平台现场作业管理系统 - 物料追溯模块表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 project_info、work_order、work_order_material
-- 设计重点：
--   PC后台支持入库、出库、盘点；移动端可查看所需物料并离线记录实际使用；
--   支持按物料编号、二维码、工单号、项目查询完整追溯链路。


CREATE TABLE IF NOT EXISTS material_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  material_code VARCHAR(64) NOT NULL COMMENT '物料编号，唯一',
  material_name VARCHAR(128) NOT NULL COMMENT '物料名称',
  material_category VARCHAR(64) DEFAULT NULL COMMENT '物料分类',
  material_spec VARCHAR(128) DEFAULT NULL COMMENT '规格型号',
  material_model VARCHAR(128) DEFAULT NULL COMMENT '型号',
  unit VARCHAR(32) NOT NULL DEFAULT '件' COMMENT '计量单位',
  brand VARCHAR(128) DEFAULT NULL COMMENT '品牌',
  manufacturer VARCHAR(128) DEFAULT NULL COMMENT '生产厂家',
  safety_stock_qty DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '安全库存',
  enabled_flag TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0否，1是',
  trace_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用追溯：0否，1是',
  qrcode_required TINYINT NOT NULL DEFAULT 1 COMMENT '是否要求二维码：0否，1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_material_info_code (material_code),
  KEY idx_material_info_name (material_name),
  KEY idx_material_info_category (material_category, enabled_flag),
  KEY idx_material_info_trace (trace_enabled, qrcode_required)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料基础信息表';

CREATE TABLE IF NOT EXISTS material_inventory (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  material_id BIGINT NOT NULL COMMENT '物料ID，关联material_info.id',
  material_code VARCHAR(64) NOT NULL COMMENT '物料编号快照',
  warehouse_code VARCHAR(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '仓库编号',
  warehouse_name VARCHAR(128) NOT NULL DEFAULT '默认仓库' COMMENT '仓库名称',
  location_code VARCHAR(64) DEFAULT NULL COMMENT '库位编号',
  batch_no VARCHAR(64) DEFAULT NULL COMMENT '批次号',
  qrcode_id BIGINT DEFAULT NULL COMMENT '二维码ID，单件/批次追溯时可关联material_qrcode.id',
  current_qty DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '当前库存数量',
  locked_qty DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '锁定数量',
  available_qty DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '可用数量',
  last_in_time DATETIME DEFAULT NULL COMMENT '最近入库时间',
  last_out_time DATETIME DEFAULT NULL COMMENT '最近出库时间',
  last_check_time DATETIME DEFAULT NULL COMMENT '最近盘点时间',
  inventory_status VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '库存状态：NORMAL/LOW/EMPTY/FROZEN',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_material_inventory_scope (material_id, warehouse_code, location_code, batch_no, qrcode_id, deleted_flag),
  KEY idx_material_inventory_code (material_code),
  KEY idx_material_inventory_qrcode (qrcode_id),
  KEY idx_material_inventory_status (inventory_status, current_qty),
  KEY idx_material_inventory_time (last_in_time, last_out_time, last_check_time),
  CONSTRAINT fk_material_inventory_material
    FOREIGN KEY (material_id) REFERENCES material_info (id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料库存表';

CREATE TABLE IF NOT EXISTS material_qrcode (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  material_id BIGINT NOT NULL COMMENT '物料ID，关联material_info.id',
  material_code VARCHAR(64) NOT NULL COMMENT '物料编号快照',
  qrcode_value VARCHAR(255) NOT NULL COMMENT '二维码值，唯一',
  qrcode_file_id VARCHAR(64) DEFAULT NULL COMMENT '二维码图片文件ID，后续关联file_storage.file_id',
  batch_no VARCHAR(64) DEFAULT NULL COMMENT '批次号',
  serial_no VARCHAR(128) DEFAULT NULL COMMENT '序列号/单件编号',
  generate_user_id BIGINT DEFAULT NULL COMMENT '生成人ID',
  generate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  bind_status VARCHAR(32) NOT NULL DEFAULT 'BOUND' COMMENT '绑定状态：BOUND/UNBOUND/VOID',
  qrcode_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '二维码状态：ACTIVE/USED/VOID/LOST',
  last_scan_time DATETIME DEFAULT NULL COMMENT '最近扫码时间',
  last_scan_user_id BIGINT DEFAULT NULL COMMENT '最近扫码用户ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_material_qrcode_value (qrcode_value),
  KEY idx_material_qrcode_material (material_id, qrcode_status),
  KEY idx_material_qrcode_code (material_code, batch_no),
  KEY idx_material_qrcode_scan (last_scan_time, last_scan_user_id),
  CONSTRAINT fk_material_qrcode_material
    FOREIGN KEY (material_id) REFERENCES material_info (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_material_qrcode_generate_user
    FOREIGN KEY (generate_user_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_material_qrcode_last_scan_user
    FOREIGN KEY (last_scan_user_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料二维码表';

CREATE TABLE IF NOT EXISTS material_inout_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  record_no VARCHAR(64) NOT NULL COMMENT '出入库/盘点记录编号，唯一',
  material_id BIGINT NOT NULL COMMENT '物料ID，关联material_info.id',
  material_code VARCHAR(64) NOT NULL COMMENT '物料编号快照',
  material_name VARCHAR(128) NOT NULL COMMENT '物料名称快照',
  qrcode_id BIGINT DEFAULT NULL COMMENT '二维码ID，关联material_qrcode.id',
  qrcode_value VARCHAR(255) DEFAULT NULL COMMENT '二维码值快照',
  project_id BIGINT DEFAULT NULL COMMENT '项目ID，出库到项目或盘点项目维度',
  work_order_id BIGINT DEFAULT NULL COMMENT '工单ID，出库到工单时关联work_order.id',
  work_order_no VARCHAR(64) DEFAULT NULL COMMENT '工单编号快照',
  inout_type VARCHAR(32) NOT NULL COMMENT '业务类型：IN/OUT/CHECK/ADJUST/RETURN',
  quantity DECIMAL(14,3) NOT NULL COMMENT '本次变动数量',
  before_qty DECIMAL(14,3) DEFAULT NULL COMMENT '变动前库存',
  after_qty DECIMAL(14,3) DEFAULT NULL COMMENT '变动后库存',
  warehouse_code VARCHAR(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '仓库编号',
  warehouse_name VARCHAR(128) NOT NULL DEFAULT '默认仓库' COMMENT '仓库名称',
  location_code VARCHAR(64) DEFAULT NULL COMMENT '库位编号',
  batch_no VARCHAR(64) DEFAULT NULL COMMENT '批次号',
  source_type VARCHAR(32) NOT NULL DEFAULT 'PC' COMMENT '来源：PC/MOBILE/SYNC',
  business_reason VARCHAR(255) DEFAULT NULL COMMENT '业务原因',
  operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
  operator_name VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名快照',
  operate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  approval_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED' COMMENT '审批状态：PENDING/APPROVED/REJECTED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_material_inout_record_no (record_no),
  KEY idx_material_inout_material (material_code, operate_time),
  KEY idx_material_inout_qrcode (qrcode_value),
  KEY idx_material_inout_project (project_id, operate_time),
  KEY idx_material_inout_work_order (work_order_id, operate_time),
  KEY idx_material_inout_type (inout_type, approval_status),
  KEY idx_material_inout_operator (operator_id, operate_time),
  CONSTRAINT fk_material_inout_material
    FOREIGN KEY (material_id) REFERENCES material_info (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_material_inout_qrcode
    FOREIGN KEY (qrcode_id) REFERENCES material_qrcode (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_material_inout_project
    FOREIGN KEY (project_id) REFERENCES project_info (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_material_inout_work_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_material_inout_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料出入库记录表';

CREATE TABLE IF NOT EXISTS work_order_material_usage (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  usage_no VARCHAR(64) DEFAULT NULL COMMENT '使用记录编号',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，关联work_order.id',
  work_order_no VARCHAR(64) NOT NULL COMMENT '工单编号快照',
  project_id BIGINT NOT NULL COMMENT '项目ID，关联project_info.id',
  material_id BIGINT NOT NULL COMMENT '物料ID，关联material_info.id',
  material_code VARCHAR(64) NOT NULL COMMENT '物料编号快照',
  material_name VARCHAR(128) NOT NULL COMMENT '物料名称快照',
  material_spec VARCHAR(128) DEFAULT NULL COMMENT '规格型号快照',
  unit VARCHAR(32) DEFAULT NULL COMMENT '单位快照',
  qrcode_id BIGINT DEFAULT NULL COMMENT '二维码ID，关联material_qrcode.id',
  qrcode_value VARCHAR(255) DEFAULT NULL COMMENT '二维码值快照',
  planned_qty DECIMAL(14,3) DEFAULT NULL COMMENT '计划数量，可来自work_order_material',
  used_qty DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '实际使用数量',
  waste_qty DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '损耗数量',
  return_qty DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '退回数量',
  usage_time DATETIME NOT NULL COMMENT '使用时间',
  usage_user_id BIGINT NOT NULL COMMENT '使用人ID，维修工',
  usage_user_name VARCHAR(64) DEFAULT NULL COMMENT '使用人姓名快照',
  usage_location VARCHAR(255) DEFAULT NULL COMMENT '使用地点',
  usage_desc VARCHAR(500) DEFAULT NULL COMMENT '使用说明',
  cost_price DECIMAL(14,4) DEFAULT NULL COMMENT '成本单价，用于经营统计',
  cost_amount DECIMAL(14,4) DEFAULT NULL COMMENT '成本金额，用于经营统计',
  source_type VARCHAR(32) NOT NULL DEFAULT 'MOBILE' COMMENT '来源：MOBILE/PC/SYNC',
  inout_record_id BIGINT DEFAULT NULL COMMENT '对应出库记录ID，关联material_inout_record.id',
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
  UNIQUE KEY uk_work_order_material_usage_no (usage_no),
  UNIQUE KEY uk_work_order_material_usage_local_id (local_id),
  KEY idx_work_order_material_usage_order (work_order_id, usage_time),
  KEY idx_work_order_material_usage_project (project_id, usage_time),
  KEY idx_work_order_material_usage_material (material_code, usage_time),
  KEY idx_work_order_material_usage_qrcode (qrcode_value),
  KEY idx_work_order_material_usage_user (usage_user_id, usage_time),
  KEY idx_work_order_material_usage_sync (sync_status, updated_at),
  KEY idx_work_order_material_usage_stat (project_id, work_order_id, material_id, cost_amount),
  CONSTRAINT fk_work_order_material_usage_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_material_usage_project
    FOREIGN KEY (project_id) REFERENCES project_info (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_material_usage_material
    FOREIGN KEY (material_id) REFERENCES material_info (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_material_usage_qrcode
    FOREIGN KEY (qrcode_id) REFERENCES material_qrcode (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_material_usage_user
    FOREIGN KEY (usage_user_id) REFERENCES sys_user (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_material_usage_inout
    FOREIGN KEY (inout_record_id) REFERENCES material_inout_record (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_work_order_material_usage_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单物料使用记录表';


-- ======================================================================
-- Begin module: init_offline_sync.sql
-- ======================================================================
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


-- ======================================================================
-- Begin module: init_ai_acceptance.sql
-- ======================================================================
-- 海上平台现场作业管理系统 - AI图像辅助验收表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 project_info、work_order
--   3. init_work_order_record.sql 中的 work_order_record
--   4. init_file_attachment.sql 中的 file_storage、work_order_attachment
-- 设计重点：
--   AI 只作为辅助验收，不能直接替代人工验收结论；
--   AI结果必须绑定工单、施工记录、照片附件，移动端离线识别结果后续同步到服务端。


CREATE TABLE IF NOT EXISTS ai_model_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  model_code VARCHAR(64) NOT NULL COMMENT '模型编码，唯一',
  model_name VARCHAR(128) NOT NULL COMMENT '模型名称',
  model_version VARCHAR(64) NOT NULL COMMENT '模型版本',
  model_type VARCHAR(32) NOT NULL DEFAULT 'DEFECT_DETECTION' COMMENT '模型类型：CLASSIFICATION/DETECTION/SEGMENTATION/DEFECT_DETECTION',
  runtime_type VARCHAR(32) NOT NULL DEFAULT 'TFLITE' COMMENT '运行时：TFLITE/NCNN/ONNX/PYTORCH/SERVICE',
  deploy_side VARCHAR(32) NOT NULL DEFAULT 'MOBILE' COMMENT '部署位置：MOBILE/SERVER/BOTH',
  model_file_id VARCHAR(64) DEFAULT NULL COMMENT '模型文件ID，关联file_storage.file_id',
  model_hash VARCHAR(128) DEFAULT NULL COMMENT '模型文件哈希',
  input_size VARCHAR(64) DEFAULT NULL COMMENT '输入尺寸，如640x640',
  defect_types JSON DEFAULT NULL COMMENT '支持识别的缺陷类型列表',
  confidence_threshold DECIMAL(6,4) NOT NULL DEFAULT 0.5000 COMMENT '默认置信度阈值',
  active_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否当前启用：0否，1是',
  model_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '模型状态：DRAFT/ACTIVE/DISABLED/ARCHIVED',
  released_at DATETIME DEFAULT NULL COMMENT '发布时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_model_info_code_version (model_code, model_version),
  KEY idx_ai_model_info_active (active_flag, model_status),
  KEY idx_ai_model_info_type (model_type, runtime_type, deploy_side),
  CONSTRAINT fk_ai_model_info_file
    FOREIGN KEY (model_file_id) REFERENCES file_storage (file_id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模型信息表';

CREATE TABLE IF NOT EXISTS ai_result (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  ai_result_no VARCHAR(64) NOT NULL COMMENT 'AI识别结果编号，唯一',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，关联work_order.id',
  work_order_no VARCHAR(64) NOT NULL COMMENT '工单编号快照',
  project_id BIGINT NOT NULL COMMENT '项目ID，关联project_info.id',
  record_id BIGINT NOT NULL COMMENT '施工记录ID，关联work_order_record.id',
  attachment_id BIGINT NOT NULL COMMENT '照片附件ID，关联work_order_attachment.id',
  file_id VARCHAR(64) NOT NULL COMMENT '原始照片文件ID，关联file_storage.file_id',
  result_image_file_id VARCHAR(64) DEFAULT NULL COMMENT 'AI标注结果图文件ID，关联file_storage.file_id',
  model_id BIGINT DEFAULT NULL COMMENT '模型ID，关联ai_model_info.id',
  model_code VARCHAR(64) DEFAULT NULL COMMENT '模型编码快照',
  model_version VARCHAR(64) NOT NULL COMMENT '模型版本快照',
  infer_side VARCHAR(32) NOT NULL DEFAULT 'MOBILE' COMMENT '推理位置：MOBILE/SERVER',
  infer_time DATETIME NOT NULL COMMENT '识别时间',
  infer_cost_ms INT DEFAULT NULL COMMENT '推理耗时，毫秒',
  defect_type VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN' COMMENT '主缺陷类型：PEELING/CRACK/RUST/DAMAGE/BUBBLE/UNKNOWN/NORMAL',
  confidence DECIMAL(8,6) DEFAULT NULL COMMENT '主结果置信度',
  suspected_defect_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否疑似缺陷：0否，1是',
  defect_count INT NOT NULL DEFAULT 0 COMMENT '检测到的缺陷框数量',
  result_summary VARCHAR(1000) DEFAULT NULL COMMENT '识别结果摘要',
  raw_result JSON DEFAULT NULL COMMENT '模型原始输出，结构化JSON',
  review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '复核状态：PENDING_REVIEW/CONFIRMED/FALSE_POSITIVE/IGNORED',
  reviewed_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否已人工复核：0否，1是',
  reviewer_id BIGINT DEFAULT NULL COMMENT '最近复核人ID',
  review_time DATETIME DEFAULT NULL COMMENT '最近复核时间',
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
  UNIQUE KEY uk_ai_result_no (ai_result_no),
  UNIQUE KEY uk_ai_result_local_id (local_id),
  KEY idx_ai_result_order (work_order_id, review_status),
  KEY idx_ai_result_project (project_id, infer_time),
  KEY idx_ai_result_record_attachment (record_id, attachment_id),
  KEY idx_ai_result_file (file_id),
  KEY idx_ai_result_defect (defect_type, suspected_defect_flag, confidence),
  KEY idx_ai_result_model (model_code, model_version),
  KEY idx_ai_result_sync (sync_status, updated_at),
  CONSTRAINT fk_ai_result_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ai_result_project
    FOREIGN KEY (project_id) REFERENCES project_info (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_ai_result_record
    FOREIGN KEY (record_id) REFERENCES work_order_record (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ai_result_attachment
    FOREIGN KEY (attachment_id) REFERENCES work_order_attachment (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_ai_result_file
    FOREIGN KEY (file_id) REFERENCES file_storage (file_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_ai_result_image_file
    FOREIGN KEY (result_image_file_id) REFERENCES file_storage (file_id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_ai_result_model
    FOREIGN KEY (model_id) REFERENCES ai_model_info (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_ai_result_reviewer
    FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_ai_result_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI识别结果表';

CREATE TABLE IF NOT EXISTS ai_defect_box (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  ai_result_id BIGINT NOT NULL COMMENT 'AI识别结果ID，关联ai_result.id',
  box_no VARCHAR(64) DEFAULT NULL COMMENT '检测框编号',
  defect_type VARCHAR(32) NOT NULL COMMENT '缺陷类型：PEELING/CRACK/RUST/DAMAGE/BUBBLE/UNKNOWN/NORMAL',
  confidence DECIMAL(8,6) NOT NULL COMMENT '缺陷置信度',
  x DECIMAL(12,4) NOT NULL COMMENT '检测框左上角x坐标',
  y DECIMAL(12,4) NOT NULL COMMENT '检测框左上角y坐标',
  width DECIMAL(12,4) NOT NULL COMMENT '检测框宽度',
  height DECIMAL(12,4) NOT NULL COMMENT '检测框高度',
  image_width INT DEFAULT NULL COMMENT '原图宽度',
  image_height INT DEFAULT NULL COMMENT '原图高度',
  normalized_flag TINYINT NOT NULL DEFAULT 0 COMMENT '坐标是否归一化：0像素坐标，1归一化坐标',
  box_label VARCHAR(128) DEFAULT NULL COMMENT '检测框标签',
  box_color VARCHAR(32) DEFAULT NULL COMMENT '展示颜色',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  local_id VARCHAR(128) DEFAULT NULL COMMENT '移动端本地ID',
  server_id BIGINT DEFAULT NULL COMMENT '服务端ID映射，回传移动端使用',
  version INT NOT NULL DEFAULT 0 COMMENT '同步版本号',
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
  UNIQUE KEY uk_ai_defect_box_local_id (local_id),
  KEY idx_ai_defect_box_result (ai_result_id, sort_order),
  KEY idx_ai_defect_box_defect (defect_type, confidence),
  KEY idx_ai_defect_box_sync (sync_status, updated_at),
  CONSTRAINT fk_ai_defect_box_result
    FOREIGN KEY (ai_result_id) REFERENCES ai_result (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ai_defect_box_operator
    FOREIGN KEY (operator_id) REFERENCES sys_user (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='缺陷检测框表';

CREATE TABLE IF NOT EXISTS ai_review_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  review_no VARCHAR(64) NOT NULL COMMENT '复核编号，唯一',
  ai_result_id BIGINT NOT NULL COMMENT 'AI识别结果ID，关联ai_result.id',
  work_order_id BIGINT NOT NULL COMMENT '工单ID，冗余便于PC后台筛选',
  record_id BIGINT NOT NULL COMMENT '施工记录ID',
  attachment_id BIGINT NOT NULL COMMENT '照片附件ID',
  reviewer_id BIGINT NOT NULL COMMENT '复核人ID',
  reviewer_name VARCHAR(64) DEFAULT NULL COMMENT '复核人姓名快照',
  review_status VARCHAR(32) NOT NULL COMMENT '复核状态：CONFIRMED/FALSE_POSITIVE/IGNORED',
  confirmed_defect_type VARCHAR(32) DEFAULT NULL COMMENT '人工确认缺陷类型',
  review_opinion VARCHAR(1000) DEFAULT NULL COMMENT '复核意见',
  acceptance_suggestion VARCHAR(1000) DEFAULT NULL COMMENT '验收建议',
  review_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '复核时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_review_record_no (review_no),
  KEY idx_ai_review_result (ai_result_id, review_time),
  KEY idx_ai_review_order (work_order_id, review_status),
  KEY idx_ai_review_reviewer (reviewer_id, review_time),
  CONSTRAINT fk_ai_review_result
    FOREIGN KEY (ai_result_id) REFERENCES ai_result (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ai_review_order
    FOREIGN KEY (work_order_id) REFERENCES work_order (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ai_review_record_ref
    FOREIGN KEY (record_id) REFERENCES work_order_record (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ai_review_attachment
    FOREIGN KEY (attachment_id) REFERENCES work_order_attachment (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_ai_review_reviewer
    FOREIGN KEY (reviewer_id) REFERENCES sys_user (id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI人工复核记录表';


-- ======================================================================
-- Begin module: init_dashboard_report.sql (table only)
-- ======================================================================
-- 海上平台现场作业管理系统 - 经营看板统计SQL与可选汇总表
-- MySQL 8.0
-- 说明：
--   1. 工单数量、完成率、项目/人员当前状态适合直接SQL实时统计。
--   2. 本周产值、物料成本、对账单导出适合通过 daily summary 汇总，避免大表反复聚合。
--   3. 本脚本不创建业务数据，只提供可选汇总表和典型统计SQL。


CREATE TABLE IF NOT EXISTS report_daily_summary (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  summary_date DATE NOT NULL COMMENT '统计日期',
  project_id BIGINT DEFAULT NULL COMMENT '项目ID，NULL表示全局汇总',
  project_name VARCHAR(128) DEFAULT NULL COMMENT '项目名称快照',
  work_order_total INT NOT NULL DEFAULT 0 COMMENT '工单总数',
  work_order_in_progress INT NOT NULL DEFAULT 0 COMMENT '进行中工单数',
  work_order_completed INT NOT NULL DEFAULT 0 COMMENT '已完成工单数',
  work_order_pending_acceptance INT NOT NULL DEFAULT 0 COMMENT '待验收工单数',
  work_order_rejected INT NOT NULL DEFAULT 0 COMMENT '已驳回工单数',
  completion_rate DECIMAL(8,4) NOT NULL DEFAULT 0 COMMENT '工单完成率',
  attendance_count INT NOT NULL DEFAULT 0 COMMENT '出勤人数',
  record_count INT NOT NULL DEFAULT 0 COMMENT '施工记录数',
  attachment_count INT NOT NULL DEFAULT 0 COMMENT '附件数量',
  material_used_amount DECIMAL(14,4) NOT NULL DEFAULT 0 COMMENT '物料消耗金额',
  material_used_qty DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '物料消耗数量',
  output_value DECIMAL(14,4) NOT NULL DEFAULT 0 COMMENT '完工产值',
  ai_suspected_count INT NOT NULL DEFAULT 0 COMMENT 'AI疑似缺陷数量',
  conflict_pending_count INT NOT NULL DEFAULT 0 COMMENT '待处理同步冲突数量',
  generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_report_daily_summary_scope (summary_date, project_id, deleted_flag),
  KEY idx_report_daily_summary_project (project_id, summary_date),
  KEY idx_report_daily_summary_date (summary_date, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经营看板每日汇总表';


-- 推荐补充索引，若前序脚本未创建可按需执行：
-- ALTER TABLE work_order ADD INDEX idx_dashboard_status_time (status, actual_end_time, project_id);
-- ALTER TABLE work_order ADD INDEX idx_dashboard_project_status_time (project_id, status, actual_end_time);
-- ALTER TABLE work_order_record ADD INDEX idx_dashboard_record_user_time (construction_user_id, construction_time);
-- ALTER TABLE work_order_material_usage ADD INDEX idx_dashboard_material_time (material_id, usage_time, project_id);
-- ALTER TABLE work_order_acceptance ADD INDEX idx_dashboard_acceptance_time (acceptance_time, acceptance_result);
-- ALTER TABLE sync_conflict ADD INDEX idx_dashboard_conflict_pending (resolve_status, created_at);

SET FOREIGN_KEY_CHECKS = 1;

-- ======================================================================
-- 表创建顺序说明
-- ======================================================================
-- 1. 系统权限基础表：sys_user、sys_role、sys_permission、sys_user_role、sys_role_permission、operation_log。
-- 2. 项目与工单主线：project_info、work_order_template、work_order、work_order_status_log、work_order_assignment、work_order_material。
-- 3. 移动端现场记录：work_order_record、work_order_record_detail、work_order_check_item。
-- 4. 文件与附件元数据：file_storage、work_order_attachment。
-- 5. 人员资质：employee_info、qualification_type、employee_certificate、work_order_qualification_check。
-- 6. 验收签名与PDF归档：work_order_acceptance、work_order_signature、work_order_pdf。
-- 7. 物料追溯：material_info、material_inventory、material_qrcode、material_inout_record、work_order_material_usage。
-- 8. 离线同步核心：device_info、sync_task、sync_log、sync_conflict、work_order_version_log。
-- 9. AI辅助验收：ai_model_info、ai_result、ai_defect_box、ai_review_record。
-- 10. 经营看板汇总：report_daily_summary；经营统计SQL样例见 db/init_dashboard_report.sql。
-- 11. 外键可按部署策略选择保留数据库外键或仅作为逻辑外键使用；弱网同步冲突以 sync_conflict 和 work_order_version_log 留痕。
