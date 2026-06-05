-- 海上平台现场作业管理系统 - 初始化与演示数据
-- MySQL 8.0
-- 前置依赖：请先执行 db/init_schema.sql
-- 密码说明：
--   当前项目尚未接入明确的 Spring Security/BCrypt 加密实现。
--   password_hash 使用明显占位值，正式联调前请替换为项目 PasswordEncoder 生成的哈希。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 默认角色
INSERT INTO sys_role (
  role_code, role_name, role_type, data_scope, pc_enabled, mobile_enabled, sort_order, status, remark
) VALUES
  ('SYSTEM_ADMIN', '系统管理员', 'SYSTEM', 'ALL', 1, 0, 10, 'ACTIVE', '拥有系统配置、用户、角色、权限和全部数据权限'),
  ('PROJECT_MANAGER', '项目经理', 'BUSINESS', 'PROJECT', 1, 1, 20, 'ACTIVE', '查看和管理所属项目工单，处理同步冲突和验收复核'),
  ('MAINTAINER', '维修工', 'BUSINESS', 'SELF', 0, 1, 30, 'ACTIVE', '移动端查看、接收、记录和同步自己的工单'),
  ('MATERIAL_ADMIN', '物资管理员', 'BUSINESS', 'MATERIAL', 1, 0, 40, 'ACTIVE', '管理物料、库存、出入库和二维码追溯'),
  ('QUALIFICATION_ADMIN', '资质管理员', 'BUSINESS', 'QUALIFICATION', 1, 0, 50, 'ACTIVE', '管理员工档案、证书和资质预警'),
  ('ACCEPTANCE_USER', '验收人员', 'BUSINESS', 'ACCEPTANCE', 1, 1, 60, 'ACTIVE', '执行现场验收、电子签名、PDF验收单归档'),
  ('BUSINESS_USER', '经营人员', 'BUSINESS', 'DASHBOARD', 1, 0, 70, 'ACTIVE', '查看经营看板和导出对账单')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_type = VALUES(role_type),
  data_scope = VALUES(data_scope),
  pc_enabled = VALUES(pc_enabled),
  mobile_enabled = VALUES(mobile_enabled),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_at = CURRENT_TIMESTAMP;

-- 2. 默认权限菜单/API
INSERT INTO sys_permission (
  permission_code, permission_name, permission_type, platform, route_path, api_method, api_path, component_path, icon, sort_order, visible_flag, status, remark
) VALUES
  ('AUTH_LOGIN', '登录', 'API', 'BOTH', NULL, 'POST', '/api/auth/login', NULL, NULL, 10, 0, 'ACTIVE', 'PC后台和移动端登录'),
  ('AUTH_LOGOUT', '退出登录', 'API', 'BOTH', NULL, 'POST', '/api/auth/logout', NULL, NULL, 20, 0, 'ACTIVE', '退出登录'),
  ('AUTH_CURRENT', '当前用户信息', 'API', 'BOTH', NULL, 'GET', '/api/auth/current', NULL, NULL, 30, 0, 'ACTIVE', '读取当前登录用户'),
  ('MENU_DASHBOARD', '经营看板', 'MENU', 'PC', '/dashboard', NULL, NULL, 'views/dashboard/index', 'dashboard', 100, 1, 'ACTIVE', '经营看板入口'),
  ('MENU_WORK_ORDER', '工单管理', 'MENU', 'PC', '/work-orders', NULL, NULL, 'views/work-order/index', 'clipboard-list', 200, 1, 'ACTIVE', 'PC后台工单管理'),
  ('MENU_MATERIAL', '物料追溯', 'MENU', 'PC', '/materials', NULL, NULL, 'views/material/index', 'package', 300, 1, 'ACTIVE', '物料、库存、出入库管理'),
  ('MENU_QUALIFICATION', '人员资质', 'MENU', 'PC', '/qualifications', NULL, NULL, 'views/qualification/index', 'badge-check', 400, 1, 'ACTIVE', '人员证书与资质预警'),
  ('MENU_ACCEPTANCE', '验收签名', 'MENU', 'PC', '/acceptance', NULL, NULL, 'views/acceptance/index', 'file-check', 500, 1, 'ACTIVE', '验收、签名、PDF归档'),
  ('MENU_SYNC_CONFLICT', '同步冲突', 'MENU', 'PC', '/sync/conflicts', NULL, NULL, 'views/sync/conflicts', 'git-compare', 600, 1, 'ACTIVE', '离线同步冲突复核'),
  ('MENU_AI_REVIEW', 'AI复核', 'MENU', 'PC', '/ai/review', NULL, NULL, 'views/ai/review', 'scan-search', 700, 1, 'ACTIVE', 'AI辅助验收复核'),
  ('MENU_SYSTEM', '系统管理', 'MENU', 'PC', '/system', NULL, NULL, 'views/system/index', 'settings', 800, 1, 'ACTIVE', '用户、角色、权限与日志'),
  ('MOBILE_WORK_ORDER_LIST', '移动端我的工单', 'API', 'MOBILE', NULL, 'GET', '/api/mobile/work-orders', NULL, NULL, 900, 0, 'ACTIVE', '维修工查看自己的派工单'),
  ('MOBILE_SYNC_PUSH', '移动端增量上传', 'API', 'MOBILE', NULL, 'POST', '/api/sync/push', NULL, NULL, 910, 0, 'ACTIVE', '移动端离线数据上传'),
  ('MOBILE_SYNC_PULL', '移动端增量拉取', 'API', 'MOBILE', NULL, 'POST', '/api/sync/pull', NULL, NULL, 920, 0, 'ACTIVE', '移动端拉取服务端变更'),
  ('REPORT_EXPORT', '导出对账单', 'API', 'PC', NULL, 'GET', '/api/admin/reports/reconciliation/export', NULL, NULL, 1000, 0, 'ACTIVE', '经营对账单导出')
ON DUPLICATE KEY UPDATE
  permission_name = VALUES(permission_name),
  permission_type = VALUES(permission_type),
  platform = VALUES(platform),
  route_path = VALUES(route_path),
  api_method = VALUES(api_method),
  api_path = VALUES(api_path),
  component_path = VALUES(component_path),
  icon = VALUES(icon),
  sort_order = VALUES(sort_order),
  visible_flag = VALUES(visible_flag),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_at = CURRENT_TIMESTAMP;

-- 3. 角色权限关联
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p
WHERE r.role_code = 'SYSTEM_ADMIN'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
  'AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT', 'MENU_DASHBOARD', 'MENU_WORK_ORDER',
  'MENU_ACCEPTANCE', 'MENU_SYNC_CONFLICT', 'MENU_AI_REVIEW',
  'MOBILE_WORK_ORDER_LIST', 'MOBILE_SYNC_PUSH', 'MOBILE_SYNC_PULL'
)
WHERE r.role_code = 'PROJECT_MANAGER'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
  'AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT', 'MOBILE_WORK_ORDER_LIST', 'MOBILE_SYNC_PUSH', 'MOBILE_SYNC_PULL'
)
WHERE r.role_code = 'MAINTAINER'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT', 'MENU_MATERIAL')
WHERE r.role_code = 'MATERIAL_ADMIN'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT', 'MENU_QUALIFICATION')
WHERE r.role_code = 'QUALIFICATION_ADMIN'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
  'AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT', 'MENU_ACCEPTANCE',
  'MOBILE_WORK_ORDER_LIST', 'MOBILE_SYNC_PUSH', 'MOBILE_SYNC_PULL'
)
WHERE r.role_code = 'ACCEPTANCE_USER'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('AUTH_LOGIN', 'AUTH_LOGOUT', 'AUTH_CURRENT', 'MENU_DASHBOARD', 'REPORT_EXPORT')
WHERE r.role_code = 'BUSINESS_USER'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;

-- 4. 默认用户：管理员、项目经理、维修工、物资、资质、验收、经营
INSERT INTO sys_user (
  username, password_hash, real_name, phone, email, employee_no, account_status,
  pc_enabled, mobile_enabled, primary_project_id, department_id, remark
) VALUES
  ('admin', 'PLACEHOLDER_PASSWORD_HASH_USE_PROJECT_ENCODER_ADMIN123456', '系统管理员', '13800000000', 'admin@example.com', 'EMP-ADMIN-001', 'ACTIVE', 1, 0, NULL, NULL, '默认管理员账号，正式环境必须替换密码哈希'),
  ('pm_zhang', 'PLACEHOLDER_PASSWORD_HASH_USE_PROJECT_ENCODER_123456', '张项目', '13800000001', 'pm@example.com', 'EMP-PM-001', 'ACTIVE', 1, 1, NULL, NULL, '示例项目经理'),
  ('worker_li', 'PLACEHOLDER_PASSWORD_HASH_USE_PROJECT_ENCODER_123456', '李维修', '13800000002', 'worker@example.com', 'EMP-WORKER-001', 'ACTIVE', 0, 1, NULL, NULL, '示例维修工，移动端接单测试'),
  ('material_wang', 'PLACEHOLDER_PASSWORD_HASH_USE_PROJECT_ENCODER_123456', '王物资', '13800000003', 'material@example.com', 'EMP-MAT-001', 'ACTIVE', 1, 0, NULL, NULL, '示例物资管理员'),
  ('qualification_chen', 'PLACEHOLDER_PASSWORD_HASH_USE_PROJECT_ENCODER_123456', '陈资质', '13800000004', 'qualification@example.com', 'EMP-QUAL-001', 'ACTIVE', 1, 0, NULL, NULL, '示例资质管理员'),
  ('acceptor_zhao', 'PLACEHOLDER_PASSWORD_HASH_USE_PROJECT_ENCODER_123456', '赵验收', '13800000005', 'acceptor@example.com', 'EMP-ACC-001', 'ACTIVE', 1, 1, NULL, NULL, '示例验收人员'),
  ('business_sun', 'PLACEHOLDER_PASSWORD_HASH_USE_PROJECT_ENCODER_123456', '孙经营', '13800000006', 'business@example.com', 'EMP-BIZ-001', 'ACTIVE', 1, 0, NULL, NULL, '示例经营人员')
ON DUPLICATE KEY UPDATE
  real_name = VALUES(real_name),
  phone = VALUES(phone),
  email = VALUES(email),
  employee_no = VALUES(employee_no),
  account_status = VALUES(account_status),
  pc_enabled = VALUES(pc_enabled),
  mobile_enabled = VALUES(mobile_enabled),
  remark = VALUES(remark),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.role_code = 'SYSTEM_ADMIN' WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.role_code = 'PROJECT_MANAGER' WHERE u.username = 'pm_zhang'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.role_code = 'MAINTAINER' WHERE u.username = 'worker_li'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.role_code = 'MATERIAL_ADMIN' WHERE u.username = 'material_wang'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.role_code = 'QUALIFICATION_ADMIN' WHERE u.username = 'qualification_chen'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.role_code = 'ACCEPTANCE_USER' WHERE u.username = 'acceptor_zhao'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.role_code = 'BUSINESS_USER' WHERE u.username = 'business_sun'
ON DUPLICATE KEY UPDATE deleted_flag = 0, updated_at = CURRENT_TIMESTAMP;

-- 5. 示例项目
INSERT INTO project_info (
  project_code, project_name, platform_name, owner_unit, contractor_unit,
  project_manager_id, project_location, start_date, end_date, project_status,
  local_id, server_id, version, sync_status, operator_id, remark
) VALUES (
  'PRJ-OFFSHORE-A-2026',
  '海上平台A区防腐维修项目',
  '海上平台A',
  '海洋能源有限公司',
  '海工检维修服务有限公司',
  (SELECT id FROM sys_user WHERE username = 'pm_zhang'),
  '海上平台A区甲板与管线区域',
  DATE_SUB(CURDATE(), INTERVAL 10 DAY),
  DATE_ADD(CURDATE(), INTERVAL 60 DAY),
  'ACTIVE',
  'local-project-demo-001',
  NULL,
  1,
  'SYNCED',
  (SELECT id FROM sys_user WHERE username = 'pm_zhang'),
  '用于PC后台项目统计和移动端工单测试'
)
ON DUPLICATE KEY UPDATE
  project_name = VALUES(project_name),
  project_manager_id = VALUES(project_manager_id),
  project_status = VALUES(project_status),
  updated_at = CURRENT_TIMESTAMP;

UPDATE sys_user
SET primary_project_id = (SELECT id FROM project_info WHERE project_code = 'PRJ-OFFSHORE-A-2026')
WHERE username IN ('pm_zhang', 'worker_li', 'acceptor_zhao');

-- 6. 示例工单模板
INSERT INTO work_order_template (
  template_code, template_name, work_type, default_priority,
  default_work_content, default_material_desc, default_duration_hours,
  enabled_flag, local_id, server_id, version, sync_status, operator_id, remark
) VALUES (
  'TPL-ANTICORROSION-001',
  '防腐层修复标准模板',
  'ANTICORROSION',
  'HIGH',
  '现场清理、打磨除锈、底漆涂刷、中间漆施工、面漆修复、质量验收。',
  '环氧底漆、面漆、砂纸、清洗剂、防护胶带。',
  8.00,
  1,
  'local-template-demo-001',
  NULL,
  1,
  'SYNCED',
  (SELECT id FROM sys_user WHERE username = 'pm_zhang'),
  '用于创建防腐维修工单'
)
ON DUPLICATE KEY UPDATE
  template_name = VALUES(template_name),
  default_work_content = VALUES(default_work_content),
  updated_at = CURRENT_TIMESTAMP;

-- 7. 示例物料与二维码、库存、入库记录
INSERT INTO material_info (
  material_code, material_name, material_category, material_spec, material_model,
  unit, brand, manufacturer, safety_stock_qty, enabled_flag, trace_enabled, qrcode_required, remark
) VALUES
  ('MAT-PRIMER-001', '环氧富锌底漆', '涂料', '20kg/桶', 'EP-ZN-20', '桶', '海工涂料', '海工涂料厂', 5.000, 1, 1, 1, '防腐维修常用底漆'),
  ('MAT-TOPCOAT-001', '聚氨酯面漆', '涂料', '20kg/桶', 'PU-TOP-20', '桶', '海工涂料', '海工涂料厂', 5.000, 1, 1, 1, '防腐维修常用面漆'),
  ('MAT-SANDPAPER-001', '工业砂纸', '耗材', '80目', 'SP-80', '张', '通用', '通用耗材厂', 100.000, 1, 1, 0, '打磨除锈耗材')
ON DUPLICATE KEY UPDATE
  material_name = VALUES(material_name),
  material_spec = VALUES(material_spec),
  safety_stock_qty = VALUES(safety_stock_qty),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO material_qrcode (
  material_id, material_code, qrcode_value, batch_no, serial_no,
  generate_user_id, bind_status, qrcode_status, remark
) VALUES
  ((SELECT id FROM material_info WHERE material_code = 'MAT-PRIMER-001'), 'MAT-PRIMER-001', 'QR-MAT-PRIMER-001-B202606-001', 'B202606', '001', (SELECT id FROM sys_user WHERE username = 'material_wang'), 'BOUND', 'ACTIVE', '底漆二维码示例'),
  ((SELECT id FROM material_info WHERE material_code = 'MAT-TOPCOAT-001'), 'MAT-TOPCOAT-001', 'QR-MAT-TOPCOAT-001-B202606-001', 'B202606', '001', (SELECT id FROM sys_user WHERE username = 'material_wang'), 'BOUND', 'ACTIVE', '面漆二维码示例')
ON DUPLICATE KEY UPDATE
  qrcode_status = VALUES(qrcode_status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO material_inventory (
  material_id, material_code, warehouse_code, warehouse_name, location_code,
  batch_no, qrcode_id, current_qty, locked_qty, available_qty,
  last_in_time, inventory_status, remark
) VALUES
  ((SELECT id FROM material_info WHERE material_code = 'MAT-PRIMER-001'), 'MAT-PRIMER-001', 'WH-OFFSHORE-A', '平台A临时库', 'A-01', 'B202606', (SELECT id FROM material_qrcode WHERE qrcode_value = 'QR-MAT-PRIMER-001-B202606-001'), 30.000, 2.000, 28.000, NOW(), 'NORMAL', '底漆库存示例'),
  ((SELECT id FROM material_info WHERE material_code = 'MAT-TOPCOAT-001'), 'MAT-TOPCOAT-001', 'WH-OFFSHORE-A', '平台A临时库', 'A-02', 'B202606', (SELECT id FROM material_qrcode WHERE qrcode_value = 'QR-MAT-TOPCOAT-001-B202606-001'), 25.000, 1.000, 24.000, NOW(), 'NORMAL', '面漆库存示例'),
  ((SELECT id FROM material_info WHERE material_code = 'MAT-SANDPAPER-001'), 'MAT-SANDPAPER-001', 'WH-OFFSHORE-A', '平台A临时库', 'B-01', 'B202606', NULL, 300.000, 20.000, 280.000, NOW(), 'NORMAL', '砂纸库存示例')
ON DUPLICATE KEY UPDATE
  current_qty = VALUES(current_qty),
  locked_qty = VALUES(locked_qty),
  available_qty = VALUES(available_qty),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO material_inout_record (
  record_no, material_id, material_code, material_name, qrcode_id, qrcode_value,
  project_id, work_order_id, work_order_no, inout_type, quantity,
  before_qty, after_qty, warehouse_code, warehouse_name, location_code,
  batch_no, source_type, business_reason, operator_id, operator_name, operate_time, approval_status, remark
) VALUES
  ('IN-MAT-20260604-001', (SELECT id FROM material_info WHERE material_code = 'MAT-PRIMER-001'), 'MAT-PRIMER-001', '环氧富锌底漆', (SELECT id FROM material_qrcode WHERE qrcode_value = 'QR-MAT-PRIMER-001-B202606-001'), 'QR-MAT-PRIMER-001-B202606-001', NULL, NULL, NULL, 'IN', 30.000, 0.000, 30.000, 'WH-OFFSHORE-A', '平台A临时库', 'A-01', 'B202606', 'PC', '示例入库', (SELECT id FROM sys_user WHERE username = 'material_wang'), '王物资', NOW(), 'APPROVED', '底漆示例入库'),
  ('IN-MAT-20260604-002', (SELECT id FROM material_info WHERE material_code = 'MAT-TOPCOAT-001'), 'MAT-TOPCOAT-001', '聚氨酯面漆', (SELECT id FROM material_qrcode WHERE qrcode_value = 'QR-MAT-TOPCOAT-001-B202606-001'), 'QR-MAT-TOPCOAT-001-B202606-001', NULL, NULL, NULL, 'IN', 25.000, 0.000, 25.000, 'WH-OFFSHORE-A', '平台A临时库', 'A-02', 'B202606', 'PC', '示例入库', (SELECT id FROM sys_user WHERE username = 'material_wang'), '王物资', NOW(), 'APPROVED', '面漆示例入库')
ON DUPLICATE KEY UPDATE
  quantity = VALUES(quantity),
  after_qty = VALUES(after_qty),
  updated_at = CURRENT_TIMESTAMP;

-- 8. 示例员工与资质证书
INSERT INTO employee_info (
  user_id, employee_no, real_name, phone, department_id, position_name, employee_status, remark
) VALUES
  ((SELECT id FROM sys_user WHERE username = 'worker_li'), 'EMP-WORKER-001', '李维修', '13800000002', NULL, '防腐维修工', 'ACTIVE', '移动端维修工'),
  ((SELECT id FROM sys_user WHERE username = 'acceptor_zhao'), 'EMP-ACC-001', '赵验收', '13800000005', NULL, '现场验收员', 'ACTIVE', '移动端验收人员')
ON DUPLICATE KEY UPDATE
  real_name = VALUES(real_name),
  position_name = VALUES(position_name),
  employee_status = VALUES(employee_status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO qualification_type (
  qualification_code, qualification_name, warning_days, required_flag, enabled_flag, remark
) VALUES
  ('OFFSHORE_OPERATION', '海上作业证', 30, 1, 1, '海上平台作业必备证书'),
  ('ANTICORROSION_WORKER', '防腐施工证', 30, 1, 1, '防腐维修施工资质'),
  ('ACCEPTANCE_INSPECTOR', '现场验收员证', 30, 1, 1, '验收人员资质')
ON DUPLICATE KEY UPDATE
  qualification_name = VALUES(qualification_name),
  warning_days = VALUES(warning_days),
  required_flag = VALUES(required_flag),
  enabled_flag = VALUES(enabled_flag),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO employee_certificate (
  employee_id, qualification_type_id, certificate_no, certificate_name,
  issue_org, issue_date, valid_from, valid_to, valid_status, warning_level, remark
) VALUES
  ((SELECT id FROM employee_info WHERE employee_no = 'EMP-WORKER-001'), (SELECT id FROM qualification_type WHERE qualification_code = 'OFFSHORE_OPERATION'), 'CERT-OFFSHORE-LI-2026', '海上作业证', '海上安全培训中心', DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_ADD(CURDATE(), INTERVAL 180 DAY), 'VALID', 'NORMAL', '维修工海上作业证示例'),
  ((SELECT id FROM employee_info WHERE employee_no = 'EMP-WORKER-001'), (SELECT id FROM qualification_type WHERE qualification_code = 'ANTICORROSION_WORKER'), 'CERT-ANTI-LI-2026', '防腐施工证', '工业防腐协会', DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 365 DAY), 'VALID', 'NORMAL', '维修工防腐施工证示例'),
  ((SELECT id FROM employee_info WHERE employee_no = 'EMP-ACC-001'), (SELECT id FROM qualification_type WHERE qualification_code = 'ACCEPTANCE_INSPECTOR'), 'CERT-ACC-ZHAO-2026', '现场验收员证', '质量监督中心', DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_ADD(CURDATE(), INTERVAL 365 DAY), 'VALID', 'NORMAL', '验收员证书示例')
ON DUPLICATE KEY UPDATE
  valid_to = VALUES(valid_to),
  valid_status = VALUES(valid_status),
  updated_at = CURRENT_TIMESTAMP;

-- 9. 示例工单：一个移动端待接单，一个本周已完成用于看板统计
INSERT INTO work_order (
  work_order_no, project_id, template_id, work_title, work_type,
  work_location, work_content, required_material_desc,
  leader_id, maintainer_id, planned_start_time, planned_end_time,
  actual_start_time, actual_end_time, status, priority, acceptance_required,
  source_type, local_id, server_id, version, sync_status, operator_id, remark
) VALUES
  ('WO-20260604-001',
   (SELECT id FROM project_info WHERE project_code = 'PRJ-OFFSHORE-A-2026'),
   (SELECT id FROM work_order_template WHERE template_code = 'TPL-ANTICORROSION-001'),
   'A区甲板防腐层破损修复',
   'ANTICORROSION',
   '海上平台A区甲板东侧',
   '对甲板东侧防腐层破损区域进行打磨、除锈、补漆和现场记录。',
   '环氧富锌底漆1桶、聚氨酯面漆1桶、砂纸20张。',
   (SELECT id FROM sys_user WHERE username = 'pm_zhang'),
   (SELECT id FROM sys_user WHERE username = 'worker_li'),
   NOW(),
   DATE_ADD(NOW(), INTERVAL 2 DAY),
   NULL,
   NULL,
   'ASSIGNED',
   'HIGH',
   1,
   'PC',
   'local-work-order-demo-001',
   NULL,
   1,
   'SYNCED',
   (SELECT id FROM sys_user WHERE username = 'pm_zhang'),
   '移动端接单测试工单'),
  ('WO-20260604-002',
   (SELECT id FROM project_info WHERE project_code = 'PRJ-OFFSHORE-A-2026'),
   (SELECT id FROM work_order_template WHERE template_code = 'TPL-ANTICORROSION-001'),
   'A区管线支架防腐补漆',
   'ANTICORROSION',
   '海上平台A区管线支架',
   '对管线支架锈蚀点进行除锈、补漆并完成验收。',
   '环氧富锌底漆1桶、聚氨酯面漆1桶。',
   (SELECT id FROM sys_user WHERE username = 'pm_zhang'),
   (SELECT id FROM sys_user WHERE username = 'worker_li'),
   DATE_SUB(NOW(), INTERVAL 3 DAY),
   DATE_SUB(NOW(), INTERVAL 1 DAY),
   DATE_SUB(NOW(), INTERVAL 3 DAY),
   DATE_SUB(NOW(), INTERVAL 1 DAY),
   'COMPLETED',
   'NORMAL',
   1,
   'PC',
   'local-work-order-demo-002',
   NULL,
   3,
   'SYNCED',
   (SELECT id FROM sys_user WHERE username = 'pm_zhang'),
   '经营看板统计示例工单')
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  maintainer_id = VALUES(maintainer_id),
  actual_start_time = VALUES(actual_start_time),
  actual_end_time = VALUES(actual_end_time),
  version = VALUES(version),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO work_order_assignment (
  work_order_id, assigner_id, assignee_id, assignment_role, assignment_status,
  assigned_at, local_id, server_id, version, sync_status, operator_id, remark
) VALUES
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-001'), (SELECT id FROM sys_user WHERE username = 'pm_zhang'), (SELECT id FROM sys_user WHERE username = 'worker_li'), 'MAINTAINER', 'ASSIGNED', NOW(), 'local-assignment-demo-001', NULL, 1, 'SYNCED', (SELECT id FROM sys_user WHERE username = 'pm_zhang'), '移动端待接单派工'),
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-002'), (SELECT id FROM sys_user WHERE username = 'pm_zhang'), (SELECT id FROM sys_user WHERE username = 'worker_li'), 'MAINTAINER', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY), 'local-assignment-demo-002', NULL, 2, 'SYNCED', (SELECT id FROM sys_user WHERE username = 'pm_zhang'), '已完成派工')
ON DUPLICATE KEY UPDATE
  assignment_status = VALUES(assignment_status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO work_order_status_log (
  work_order_id, from_status, to_status, operation_type, operation_desc,
  operator_id, operation_time, local_id, server_id, version, sync_status, device_id, remark
) VALUES
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-001'), NULL, 'PENDING_ASSIGN', 'CREATE', '创建工单', (SELECT id FROM sys_user WHERE username = 'pm_zhang'), DATE_SUB(NOW(), INTERVAL 2 HOUR), 'local-status-demo-001-1', NULL, 1, 'SYNCED', NULL, '状态流转：创建'),
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-001'), 'PENDING_ASSIGN', 'ASSIGNED', 'ASSIGN', '派工给李维修', (SELECT id FROM sys_user WHERE username = 'pm_zhang'), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'local-status-demo-001-2', NULL, 1, 'SYNCED', NULL, '状态流转：派工'),
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-002'), NULL, 'PENDING_ASSIGN', 'CREATE', '创建工单', (SELECT id FROM sys_user WHERE username = 'pm_zhang'), DATE_SUB(NOW(), INTERVAL 4 DAY), 'local-status-demo-002-1', NULL, 1, 'SYNCED', NULL, '状态流转：创建'),
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-002'), 'PENDING_ASSIGN', 'ASSIGNED', 'ASSIGN', '派工给李维修', (SELECT id FROM sys_user WHERE username = 'pm_zhang'), DATE_SUB(NOW(), INTERVAL 3 DAY), 'local-status-demo-002-2', NULL, 1, 'SYNCED', NULL, '状态流转：派工'),
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-002'), 'ASSIGNED', 'IN_PROGRESS', 'START', '移动端开始施工', (SELECT id FROM sys_user WHERE username = 'worker_li'), DATE_SUB(NOW(), INTERVAL 3 DAY), 'local-status-demo-002-3', NULL, 2, 'SYNCED', 'android-demo-001', '状态流转：施工中'),
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-002'), 'IN_PROGRESS', 'PENDING_ACCEPTANCE', 'SUBMIT_ACCEPTANCE', '提交待验收', (SELECT id FROM sys_user WHERE username = 'worker_li'), DATE_SUB(NOW(), INTERVAL 2 DAY), 'local-status-demo-002-4', NULL, 3, 'SYNCED', 'android-demo-001', '状态流转：待验收'),
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-002'), 'PENDING_ACCEPTANCE', 'COMPLETED', 'COMPLETE', '验收通过并完工', (SELECT id FROM sys_user WHERE username = 'acceptor_zhao'), DATE_SUB(NOW(), INTERVAL 1 DAY), 'local-status-demo-002-5', NULL, 3, 'SYNCED', 'android-demo-001', '状态流转：完成')
ON DUPLICATE KEY UPDATE
  to_status = VALUES(to_status),
  operation_time = VALUES(operation_time),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO work_order_material (
  work_order_id, material_code, material_name, material_spec, unit,
  planned_qty, actual_qty, material_desc, required_flag, prepare_status,
  local_id, server_id, version, sync_status, operator_id, remark
) VALUES
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-001'), 'MAT-PRIMER-001', '环氧富锌底漆', '20kg/桶', '桶', 1.000, 0.000, '底漆，移动端派工单展示', 1, 'PREPARED', 'local-wo-material-001-1', NULL, 1, 'SYNCED', (SELECT id FROM sys_user WHERE username = 'pm_zhang'), '计划物料'),
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-001'), 'MAT-TOPCOAT-001', '聚氨酯面漆', '20kg/桶', '桶', 1.000, 0.000, '面漆，移动端派工单展示', 1, 'PREPARED', 'local-wo-material-001-2', NULL, 1, 'SYNCED', (SELECT id FROM sys_user WHERE username = 'pm_zhang'), '计划物料'),
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-002'), 'MAT-PRIMER-001', '环氧富锌底漆', '20kg/桶', '桶', 1.000, 1.000, '已使用底漆', 1, 'PREPARED', 'local-wo-material-002-1', NULL, 2, 'SYNCED', (SELECT id FROM sys_user WHERE username = 'worker_li'), '已完成工单计划物料')
ON DUPLICATE KEY UPDATE
  planned_qty = VALUES(planned_qty),
  actual_qty = VALUES(actual_qty),
  prepare_status = VALUES(prepare_status),
  updated_at = CURRENT_TIMESTAMP;

-- 10. 现场记录、物料使用、资质校验，用于移动端和看板
INSERT INTO work_order_record (
  work_order_id, project_id, record_no, record_type, construction_time,
  construction_user_id, construction_user_name, construction_desc, site_condition,
  abnormal_flag, weather, location_name, attachment_count, ai_result_count,
  record_status, submitted_at, client_created_at, client_updated_at,
  local_id, server_id, version, sync_status, device_id, operator_id, remark
) VALUES
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-002'), (SELECT id FROM project_info WHERE project_code = 'PRJ-OFFSHORE-A-2026'), 'REC-20260604-001', 'CONSTRUCTION', NOW(), (SELECT id FROM sys_user WHERE username = 'worker_li'), '李维修', '完成管线支架除锈和补漆，现场已清理。', '现场风力较小，作业面干燥。', 0, '晴', '海上平台A区管线支架', 2, 1, 'SUBMITTED', NOW(), NOW(), NOW(), 'local-record-offline-demo-001', NULL, 1, 'PENDING', 'android-demo-001', (SELECT id FROM sys_user WHERE username = 'worker_li'), '移动端离线保存后待同步示例')
ON DUPLICATE KEY UPDATE
  construction_desc = VALUES(construction_desc),
  sync_status = VALUES(sync_status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO work_order_material_usage (
  usage_no, work_order_id, work_order_no, project_id, material_id,
  material_code, material_name, material_spec, unit, qrcode_id, qrcode_value,
  planned_qty, used_qty, waste_qty, return_qty, usage_time,
  usage_user_id, usage_user_name, usage_location, usage_desc,
  cost_price, cost_amount, source_type, client_created_at, client_updated_at,
  local_id, server_id, version, sync_status, device_id, operator_id, remark
) VALUES (
  'USE-MAT-20260604-001',
  (SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-002'),
  'WO-20260604-002',
  (SELECT id FROM project_info WHERE project_code = 'PRJ-OFFSHORE-A-2026'),
  (SELECT id FROM material_info WHERE material_code = 'MAT-PRIMER-001'),
  'MAT-PRIMER-001',
  '环氧富锌底漆',
  '20kg/桶',
  '桶',
  (SELECT id FROM material_qrcode WHERE qrcode_value = 'QR-MAT-PRIMER-001-B202606-001'),
  'QR-MAT-PRIMER-001-B202606-001',
  1.000,
  1.000,
  0.050,
  0.000,
  DATE_SUB(NOW(), INTERVAL 1 DAY),
  (SELECT id FROM sys_user WHERE username = 'worker_li'),
  '李维修',
  '海上平台A区管线支架',
  '管线支架补漆使用',
  560.0000,
  560.0000,
  'MOBILE',
  DATE_SUB(NOW(), INTERVAL 1 DAY),
  DATE_SUB(NOW(), INTERVAL 1 DAY),
  'local-material-usage-demo-001',
  NULL,
  1,
  'SYNCED',
  'android-demo-001',
  (SELECT id FROM sys_user WHERE username = 'worker_li'),
  '经营看板物料消耗示例'
)
ON DUPLICATE KEY UPDATE
  used_qty = VALUES(used_qty),
  cost_amount = VALUES(cost_amount),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO work_order_qualification_check (
  work_order_id, employee_id, certificate_id, qualification_type_id,
  check_result, check_time, checker_id, local_id, server_id, version,
  sync_status, device_id, operator_id, remark
) VALUES
  ((SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-001'), (SELECT id FROM employee_info WHERE employee_no = 'EMP-WORKER-001'), (SELECT id FROM employee_certificate WHERE certificate_no = 'CERT-ANTI-LI-2026'), (SELECT id FROM qualification_type WHERE qualification_code = 'ANTICORROSION_WORKER'), 'PASS', NOW(), (SELECT id FROM sys_user WHERE username = 'pm_zhang'), 'local-qualification-check-demo-001', NULL, 1, 'SYNCED', NULL, (SELECT id FROM sys_user WHERE username = 'pm_zhang'), '工单派工前资质校验通过')
ON DUPLICATE KEY UPDATE
  check_result = VALUES(check_result),
  updated_at = CURRENT_TIMESTAMP;

-- 11. 设备和同步任务，用于离线同步测试
INSERT INTO device_info (
  device_id, user_id, device_name, platform, os_version, app_version,
  manufacturer, model, register_time, last_login_time, last_heartbeat_time,
  last_sync_time, online_status, device_status, sync_enabled, remark
) VALUES (
  'android-demo-001',
  (SELECT id FROM sys_user WHERE username = 'worker_li'),
  'Android-Demo-Worker',
  'ANDROID',
  'Android 13',
  '1.0.0',
  'Demo',
  'DemoPhone',
  NOW(),
  NOW(),
  NOW(),
  DATE_SUB(NOW(), INTERVAL 30 MINUTE),
  'ONLINE',
  'ACTIVE',
  1,
  '移动端离线同步测试设备'
)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  app_version = VALUES(app_version),
  last_heartbeat_time = VALUES(last_heartbeat_time),
  online_status = VALUES(online_status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO sync_task (
  sync_task_no, batch_id, device_id, operator_id, sync_direction, sync_type,
  task_status, total_count, success_count, failed_count, conflict_count,
  client_time, server_start_time, server_end_time, idempotency_key, remark
) VALUES (
  'SYNC-TASK-20260604-001',
  'batch-demo-20260604-001',
  'android-demo-001',
  (SELECT id FROM sys_user WHERE username = 'worker_li'),
  'PUSH',
  'INCREMENTAL',
  'PENDING',
  2,
  0,
  0,
  0,
  NOW(),
  NULL,
  NULL,
  'idem-batch-demo-20260604-001',
  '移动端网络恢复后的增量上传任务示例'
)
ON DUPLICATE KEY UPDATE
  task_status = VALUES(task_status),
  total_count = VALUES(total_count),
  updated_at = CURRENT_TIMESTAMP;

DELETE FROM sync_log
WHERE batch_id = 'batch-demo-20260604-001'
  AND local_id = 'local-record-offline-demo-001'
  AND entity_type = 'work_order_record';

INSERT INTO sync_log (
  sync_task_id, batch_id, device_id, operator_id, module_type, entity_type,
  action_type, local_id, server_id, entity_id, work_order_id, business_no,
  client_version, server_version, client_updated_at, server_updated_at,
  sync_status, retry_count, remark
) VALUES (
  (SELECT id FROM sync_task WHERE sync_task_no = 'SYNC-TASK-20260604-001'),
  'batch-demo-20260604-001',
  'android-demo-001',
  (SELECT id FROM sys_user WHERE username = 'worker_li'),
  'WORK_RECORD',
  'work_order_record',
  'CREATE',
  'local-record-offline-demo-001',
  NULL,
  (SELECT id FROM work_order_record WHERE record_no = 'REC-20260604-001'),
  (SELECT id FROM work_order WHERE work_order_no = 'WO-20260604-002'),
  'REC-20260604-001',
  1,
  0,
  NOW(),
  NULL,
  'PENDING',
  0,
  '施工记录离线同步日志示例'
);

-- 12. 经营看板日汇总示例
INSERT INTO report_daily_summary (
  summary_date, project_id, project_name, work_order_total, work_order_in_progress,
  work_order_completed, work_order_pending_acceptance, work_order_rejected,
  completion_rate, attendance_count, record_count, attachment_count,
  material_used_amount, material_used_qty, output_value, ai_suspected_count,
  conflict_pending_count, remark
) VALUES (
  CURDATE(),
  (SELECT id FROM project_info WHERE project_code = 'PRJ-OFFSHORE-A-2026'),
  '海上平台A区防腐维修项目',
  2,
  0,
  1,
  0,
  0,
  50.0000,
  1,
  1,
  2,
  560.0000,
  1.000,
  12800.0000,
  1,
  0,
  '经营看板今日汇总示例'
)
ON DUPLICATE KEY UPDATE
  work_order_total = VALUES(work_order_total),
  work_order_completed = VALUES(work_order_completed),
  attendance_count = VALUES(attendance_count),
  material_used_amount = VALUES(material_used_amount),
  output_value = VALUES(output_value),
  updated_at = CURRENT_TIMESTAMP;

SET FOREIGN_KEY_CHECKS = 1;
