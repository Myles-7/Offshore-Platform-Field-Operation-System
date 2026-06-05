-- 海上平台现场作业管理系统 - 系统权限与操作日志表
-- MySQL 8.0
-- 用途：服务 PC 后台登录、移动端登录、角色权限控制、关键操作审计。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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

SET FOREIGN_KEY_CHECKS = 1;
