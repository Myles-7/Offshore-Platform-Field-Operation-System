import { request } from '@/api/request';

export interface SyncConflictItem {
  id: number;
  conflictNo: string;
  moduleType?: string;
  entityType: string;
  localId?: string;
  serverId?: number;
  workOrderId?: number;
  conflictType?: string;
  resolveStatus: string;
  resolveStrategy?: string;
  resolveTime?: string;
  resolveComment?: string;
  clientPayload?: string;
  serverPayload?: string;
  deviceId?: string;
  operatorId?: number;
  clientVersion?: number;
  serverVersion?: number;
}

/** 同步推送单项 */
export interface SyncPushItem {
  moduleType: string;
  entityType: string;
  actionType: string;
  localId?: string;
  serverId?: number;
  version?: number;
  updatedAt?: string;
  payload?: Record<string, unknown>;
  fileId?: string;
  checksum?: string;
  deletedFlag?: number;
  syncStatus?: string;
  deviceId?: string;
  operatorId?: number;
  conflictFlag?: number;
}

/** 同步推送请求 */
export interface SyncPushRequest {
  deviceId: string;
  batchId: string;
  clientTime: string;
  appVersion?: string;
  operatorId?: number;
  items: SyncPushItem[];
}

/** 单条同步结果 */
export interface SyncItemResult {
  localId?: string;
  serverId?: number;
  version: number;
  syncStatus?: string;
  conflictId?: number;
  message?: string;
  entityType?: string;
  moduleType?: string;
  actionType?: string;
}

/** 同步任务项 */
export interface SyncTaskItem {
  id: number;
  syncTaskNo?: string;
  taskNo?: string;
  batchId?: string;
  deviceId?: string;
  syncDirection?: string;
  syncType?: string;
  taskStatus?: string;
  syncStatus?: string;
  totalCount?: number;
  successCount?: number;
  failedCount?: number;
  failCount?: number;
  conflictCount?: number;
  retryCount?: number;
  businessType?: string;
  moduleType?: string;
  startTime?: string;
  createdAt?: string;
  updatedAt?: string;
  errorMessage?: string;
}

/** 同步日志项 */
export interface SyncLogItem {
  id: number;
  syncTaskId?: number;
  batchId?: string;
  deviceId?: string;
  moduleType?: string;
  entityType?: string;
  actionType?: string;
  syncAction?: string;
  localId?: string;
  serverId?: number;
  entityId?: number;
  workOrderId?: number;
  clientVersion?: number;
  serverVersion?: number;
  syncStatus?: string;
  conflictId?: number;
  errorCode?: string;
  errorMessage?: string;
  message?: string;
  syncTime?: string;
  createdAt?: string;
}

/** 冲突列表 */
export function fetchSyncConflicts() {
  return request<SyncConflictItem[]>({
    url: '/admin/sync/conflicts',
    method: 'GET'
  });
}

/** 冲突详情 */
export function fetchSyncConflict(id: number) {
  return request<SyncConflictItem>({
    url: `/admin/sync/conflicts/${id}`,
    method: 'GET'
  });
}

/** 处理冲突 */
export function resolveSyncConflict(id: number, data: {
  resolveStrategy: string;
  finalPayload?: string;
  resolveComment: string;
}) {
  return request<SyncConflictItem>({
    url: `/admin/sync/conflicts/${id}/resolve`,
    method: 'POST',
    data
  });
}

/** 同步日志 */
export function fetchSyncLogs() {
  return request<SyncLogItem[]>({
    url: '/sync/logs',
    method: 'GET'
  });
}

/** 同步任务 */
export function fetchSyncTasks() {
  return request<SyncTaskItem[]>({
    url: '/sync/tasks',
    method: 'GET'
  });
}
