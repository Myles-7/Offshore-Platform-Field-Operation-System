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

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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

SET FOREIGN_KEY_CHECKS = 1;
