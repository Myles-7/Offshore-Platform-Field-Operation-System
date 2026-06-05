import { request } from '@/api/request';

export interface AiDefectBox {
  id: number;
  aiResultId: number;
  defectType: string;
  confidence: number;
  x: number;
  y: number;
  width: number;
  height: number;
  boxLabel?: string;
}

export interface AiResultItem {
  id: number;
  aiResultNo: string;
  workOrderId: number;
  workOrderNo?: string;
  projectId?: number;
  recordId?: number;
  attachmentId?: number;
  fileId?: string;
  modelId?: number;
  modelCode?: string;
  modelVersion?: string;
  inferCostMs?: number;
  defectType: string;
  confidence: number;
  suspectedDefectFlag?: number;
  defectCount?: number;
  resultSummary?: string;
  reviewStatus: string;
  reviewedFlag?: number;
  version?: number;
  syncStatus?: string;
  updatedAt?: string;
  boxes?: AiDefectBox[];
  auxiliaryNotice?: string;
}

export interface AiModelItem {
  id: number;
  modelCode: string;
  modelName: string;
  modelVersion: string;
  modelType?: string;
  runtimeType?: string;
  deploySide?: string;
  confidenceThreshold?: number;
  activeFlag?: number;
  modelStatus?: string;
}

/* ========== AI 结果 ========== */

/** 工单 AI 结果列表 */
export function fetchWorkOrderAiResults(workOrderId: number) {
  return request<AiResultItem[]>({
    url: `/admin/work-orders/${workOrderId}/ai-results`,
    method: 'GET'
  });
}

/** AI 结果详情 */
export function fetchAiResult(id: number) {
  return request<AiResultItem>({
    url: `/ai/results/${id}`,
    method: 'GET'
  });
}

/** 人工复核 AI 结果 */
export function reviewAiResult(id: number, data: {
  reviewStatus: string;
  confirmedDefectType?: string;
  reviewOpinion?: string;
  acceptanceSuggestion?: string;
}) {
  return request<AiResultItem>({
    url: `/admin/ai/results/${id}/review`,
    method: 'POST',
    data
  });
}

/* ========== AI 模型 ========== */

/** 模型列表 */
export function fetchAiModels() {
  return request<AiModelItem[]>({
    url: '/admin/ai/models',
    method: 'GET'
  });
}

/** 创建模型 */
export function createAiModel(data: {
  modelCode: string;
  modelName: string;
  modelVersion: string;
  modelType?: string;
  runtimeType?: string;
  deploySide?: string;
  confidenceThreshold?: number;
}) {
  return request<AiModelItem>({
    url: '/admin/ai/models',
    method: 'POST',
    data
  });
}

/** 激活模型 */
export function activateAiModel(id: number) {
  return request<AiModelItem>({
    url: `/admin/ai/models/${id}/activate`,
    method: 'PUT'
  });
}
