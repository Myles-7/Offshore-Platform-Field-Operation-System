/** 概览/工单统计接口返回 (DashboardVO) */
export interface DashboardOverview {
  /** 进行中工单数 */
  inProgressWorkOrderCount: number;
  /** 今日出勤人数 */
  todayAttendanceCount: number;
  /** 本周完工产值 */
  weeklyCompletedOutputValue: number;
  /** 待验收工单数 */
  pendingAcceptanceWorkOrderCount: number;
  /** 异常工单数 */
  abnormalWorkOrderCount: number;
  /** 工单完成率 (百分比) */
  completionRate: number;
  /** 资质临期数量 */
  certificateExpiringCount?: number;
  /** 库存预警数量 */
  inventoryWarningCount?: number;
  /** 未处理同步冲突数量 */
  pendingConflictCount?: number;
  /** AI待复核数量 */
  pendingAiReviewCount?: number;
  /** 工单统计 items（仅 work-order-statistics 接口返回） */
  items?: DashboardStatItem[];
}

/** 工单状态统计项 */
export interface DashboardStatItem {
  status: string;
  count: number;
}

/** 项目统计 */
export interface ProjectStatItem {
  projectId: number;
  projectName: string;
  total: number;
  completed: number;
  inProgress: number;
  completionRate: number;
}

/** 人员统计 */
export interface PersonStatItem {
  userId: number;
  /** TODO: 后端补充 userName 字段 */
  userName?: string;
  recordCount: number;
}

/** 物料消耗排行 */
export interface MaterialStatItem {
  materialName: string;
  usedQty: number;
}

/** 产值记录 */
export interface OutputValueItem {
  summaryDate: string;
  projectId: number;
  projectName: string;
  outputValue: number;
}
