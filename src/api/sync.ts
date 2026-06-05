import { request } from '@/api/request';

export interface SyncConflictItem {
  id: number;
  conflictNo: string;
  entityType: string;
  localId?: string;
  serverId?: number;
  workOrderId?: number;
  conflictType?: string;
  resolveStatus: string;
  clientPayload?: string;
  serverPayload?: string;
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
  return request<Record<string, unknown>[]>({
    url: '/sync/logs',
    method: 'GET'
  });
}

/** 同步任务 */
export function fetchSyncTasks() {
  return request<Record<string, unknown>[]>({
    url: '/sync/tasks',
    method: 'GET'
  });
}
