-- 海上平台现场作业管理系统 - 物料追溯模块表
-- MySQL 8.0
-- 前置依赖：
--   1. init_system_permission.sql 中的 sys_user
--   2. init_work_order.sql 中的 project_info、work_order、work_order_material
-- 设计重点：
--   PC后台支持入库、出库、盘点；移动端可查看所需物料并离线记录实际使用；
--   支持按物料编号、二维码、工单号、项目查询完整追溯链路。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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

SET FOREIGN_KEY_CHECKS = 1;
