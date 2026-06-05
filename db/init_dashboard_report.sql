-- 海上平台现场作业管理系统 - 经营看板统计SQL与可选汇总表
-- MySQL 8.0
-- 说明：
--   1. 工单数量、完成率、项目/人员当前状态适合直接SQL实时统计。
--   2. 本周产值、物料成本、对账单导出适合通过 daily summary 汇总，避免大表反复聚合。
--   3. 本脚本不创建业务数据，只提供可选汇总表和典型统计SQL。

SET NAMES utf8mb4;

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

-- 1. 进行中工单数：适合实时统计
-- IN_PROGRESS 表示施工中；如业务希望包含已派工，可把 ASSIGNED 也纳入。
SELECT COUNT(*) AS in_progress_work_order_count
FROM work_order
WHERE deleted_flag = 0
  AND status = 'IN_PROGRESS';

-- 2. 今日出勤人数：适合实时统计，基于施工记录去重
SELECT COUNT(DISTINCT construction_user_id) AS today_attendance_count
FROM work_order_record
WHERE deleted_flag = 0
  AND construction_time >= CURDATE()
  AND construction_time < DATE_ADD(CURDATE(), INTERVAL 1 DAY);

-- 3. 本周完工产值：建议优先从 report_daily_summary 汇总
SELECT COALESCE(SUM(output_value), 0) AS weekly_output_value
FROM report_daily_summary
WHERE deleted_flag = 0
  AND summary_date >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
  AND summary_date < DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY);

-- 4. 工单完成率：适合实时统计
SELECT
  COUNT(*) AS total_count,
  SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_count,
  CASE
    WHEN COUNT(*) = 0 THEN 0
    ELSE ROUND(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) / COUNT(*) * 100, 2)
  END AS completion_rate_percent
FROM work_order
WHERE deleted_flag = 0
  AND status <> 'CLOSED';

-- 5. 项目维度统计：适合实时统计；大屏高并发时可走 report_daily_summary
SELECT
  p.id AS project_id,
  p.project_name,
  COUNT(w.id) AS total_count,
  SUM(CASE WHEN w.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_count,
  SUM(CASE WHEN w.status = 'PENDING_ACCEPTANCE' THEN 1 ELSE 0 END) AS pending_acceptance_count,
  SUM(CASE WHEN w.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_count,
  CASE
    WHEN COUNT(w.id) = 0 THEN 0
    ELSE ROUND(SUM(CASE WHEN w.status = 'COMPLETED' THEN 1 ELSE 0 END) / COUNT(w.id) * 100, 2)
  END AS completion_rate_percent
FROM project_info p
LEFT JOIN work_order w ON w.project_id = p.id AND w.deleted_flag = 0
WHERE p.deleted_flag = 0
GROUP BY p.id, p.project_name
ORDER BY completed_count DESC, total_count DESC;

-- 6. 人员维度统计：适合实时统计，支持项目经理查看所属项目人员产出
SELECT
  u.id AS user_id,
  u.real_name,
  COUNT(DISTINCT w.id) AS assigned_work_order_count,
  SUM(CASE WHEN w.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_work_order_count,
  COUNT(r.id) AS record_count,
  MAX(r.construction_time) AS last_record_time
FROM sys_user u
LEFT JOIN work_order w ON w.maintainer_id = u.id AND w.deleted_flag = 0
LEFT JOIN work_order_record r ON r.construction_user_id = u.id AND r.deleted_flag = 0
WHERE u.deleted_flag = 0
GROUP BY u.id, u.real_name
ORDER BY completed_work_order_count DESC, record_count DESC;

-- 7. 物料消耗统计：小数据量可实时，大数据量建议按日汇总
SELECT
  m.material_code,
  m.material_name,
  SUM(u.used_qty) AS used_qty,
  SUM(COALESCE(u.cost_amount, 0)) AS material_cost_amount
FROM work_order_material_usage u
JOIN material_info m ON m.id = u.material_id
WHERE u.deleted_flag = 0
GROUP BY m.material_code, m.material_name
ORDER BY material_cost_amount DESC, used_qty DESC;

-- 8. Excel对账单导出：建议按时间范围查询明细，并记录 operation_log
SELECT
  w.work_order_no,
  p.project_name,
  w.work_title,
  w.status,
  w.actual_start_time,
  w.actual_end_time,
  w.maintainer_id,
  mu.material_code,
  mu.material_name,
  mu.used_qty,
  mu.cost_price,
  mu.cost_amount,
  a.acceptance_no,
  a.acceptance_result,
  a.acceptance_time,
  pdf.pdf_no
FROM work_order w
JOIN project_info p ON p.id = w.project_id
LEFT JOIN work_order_material_usage mu ON mu.work_order_id = w.id AND mu.deleted_flag = 0
LEFT JOIN work_order_acceptance a ON a.work_order_id = w.id AND a.deleted_flag = 0
LEFT JOIN work_order_pdf pdf ON pdf.work_order_id = w.id AND pdf.deleted_flag = 0
WHERE w.deleted_flag = 0
  AND w.actual_end_time >= :startTime
  AND w.actual_end_time < :endTime
ORDER BY p.project_name, w.work_order_no, mu.material_code;

-- 推荐补充索引，若前序脚本未创建可按需执行：
-- ALTER TABLE work_order ADD INDEX idx_dashboard_status_time (status, actual_end_time, project_id);
-- ALTER TABLE work_order ADD INDEX idx_dashboard_project_status_time (project_id, status, actual_end_time);
-- ALTER TABLE work_order_record ADD INDEX idx_dashboard_record_user_time (construction_user_id, construction_time);
-- ALTER TABLE work_order_material_usage ADD INDEX idx_dashboard_material_time (material_id, usage_time, project_id);
-- ALTER TABLE work_order_acceptance ADD INDEX idx_dashboard_acceptance_time (acceptance_time, acceptance_result);
-- ALTER TABLE sync_conflict ADD INDEX idx_dashboard_conflict_pending (resolve_status, created_at);
