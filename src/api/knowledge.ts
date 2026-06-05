import { request } from '@/api/request';

export interface KnowledgeCaseItem {
  id: number;
  caseNo: string;
  title: string;
  equipmentType?: string;
  faultType?: string;
  faultPhenomenon?: string;
  faultCause?: string;
  solution?: string;
  preventiveMeasures?: string;
  workType?: string;
  enabledFlag?: number;
  sourceWorkOrderId?: number;
  attachmentIds?: string;
  version?: number;
  updatedAt?: string;
  remark?: string;
}

export interface MaintenanceProcessItem {
  id: number;
  processCode: string;
  processName: string;
  equipmentType?: string;
  processType?: string;
  processSteps?: string;
  toolsRequired?: string;
  materialRequired?: string;
  safetyMeasures?: string;
  qualityStandard?: string;
  durationEstimate?: string;
  workType?: string;
  enabledFlag?: number;
  version?: number;
  updatedAt?: string;
  remark?: string;
}

/* ========== 故障案例 ========== */
export function fetchKnowledgeCases() { return request<KnowledgeCaseItem[]>({ url: '/admin/knowledge/cases', method: 'GET' }); }
export function createKnowledgeCase(data: Record<string, unknown>) { return request<KnowledgeCaseItem>({ url: '/admin/knowledge/cases', method: 'POST', data }); }
export function updateKnowledgeCase(id: number, data: Record<string, unknown>) { return request<KnowledgeCaseItem>({ url: `/admin/knowledge/cases/${id}`, method: 'PUT', data }); }
export function deleteKnowledgeCase(id: number) { return request<void>({ url: `/admin/knowledge/cases/${id}`, method: 'DELETE' }); }

/* ========== 维修工艺 ========== */
export function fetchMaintenanceProcesses() { return request<MaintenanceProcessItem[]>({ url: '/admin/knowledge/processes', method: 'GET' }); }
export function createMaintenanceProcess(data: Record<string, unknown>) { return request<MaintenanceProcessItem>({ url: '/admin/knowledge/processes', method: 'POST', data }); }
export function updateMaintenanceProcess(id: number, data: Record<string, unknown>) { return request<MaintenanceProcessItem>({ url: `/admin/knowledge/processes/${id}`, method: 'PUT', data }); }
