import type { SyncItemResult } from '@/api/sync';

/**
 * 离线同步 — 统一数据契约类型定义
 *
 * 与后端 dto/sync/ 和 vo/sync/ 对齐，与移动端 ApiDtos.kt 对齐
 */

/** 同步拉取请求 */
export interface SyncPullRequest {
  deviceId: string;
  cursor?: string;
  lastSyncTime?: string;
  limit?: number;
  entityTypes?: string[];
}

/** 同步拉取响应 */
export interface SyncPullItemVO {
  moduleType?: string;
  entityType?: string;
  serverId?: number;
  localId?: string;
  version: number;
  updatedAt?: string;
  deletedFlag?: number;
  operatorId?: number;
  deviceId?: string;
  payload?: Record<string, unknown>;
}

export interface SyncPullResponse {
  cursor?: string;
  serverTime?: string;
  items: SyncPullItemVO[];
}

/** 同步推送响应 */
export interface SyncPushResponse {
  taskId?: number;
  batchId?: string;
  successCount: number;
  failedCount: number;
  conflictCount: number;
  items: SyncItemResult[];
}

/** 同步确认请求 */
export interface SyncAckRequest {
  deviceId: string;
  batchId: string;
  cursor?: string;
  lastSyncCursor?: string;
}

/** 设备注册请求 */
export interface DeviceRegisterRequest {
  deviceId: string;
  deviceName?: string;
  platform?: string;
  osVersion?: string;
  appVersion?: string;
  manufacturer?: string;
  model?: string;
  operatorId?: number;
}

/** 冲突处理请求 */
export interface ConflictResolveRequest {
  resolveStrategy: string;
  finalPayload?: string;
  resolveComment: string;
}

/** 实体类型到数据库表的映射 */
export const entityTypeTableMap: Record<string, string> = {
  WORK_ORDER: 'work_order',
  WORK_RECORD: 'work_order_record',
  ATTACHMENT_META: 'work_order_attachment',
  SIGNATURE: 'work_order_signature',
  ACCEPTANCE: 'work_order_acceptance',
  PDF: 'work_order_pdf',
  MATERIAL_USAGE: 'work_order_material_usage',
  QUALIFICATION: 'employee_certificate',
  AI_RESULT: 'ai_result',
  KNOWLEDGE: 'knowledge_case',
  DEVICE: 'device_info',
  USER_PROFILE: 'sys_user'
};
