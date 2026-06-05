import { request } from '@/api/request';

export interface SignatureVO {
  id: number;
  serverId?: number;
  localId?: string;
  workOrderId: number;
  acceptanceId?: number;
  fileId: string;
  signatureRole: string;
  signerUserId?: number;
  signerName: string;
  signedAt?: string;
  signatureStatus?: string;
  version?: number;
  updatedAt?: string;
  syncStatus?: string;
  previewUrl?: string;
  downloadUrl?: string;
}

export interface AcceptanceVO {
  id: number;
  acceptanceNo: string;
  workOrderId: number;
  projectId?: number;
  workOrderNo?: string;
  projectName?: string;
  acceptanceStatus: string;
  acceptanceResult?: string;
  acceptanceOpinion?: string;
  problemDesc?: string;
  rectificationRequired?: number;
  pdfGeneratedFlag?: number;
  lockedFlag?: number;
  signatureCount?: number;
  version?: number;
  updatedAt?: string;
  syncStatus?: string;
}

export interface PdfVO {
  id: number;
  pdfNo: string;
  workOrderId: number;
  acceptanceId?: number;
  fileId: string;
  pdfStatus: string;
  lockedFlag?: number;
  generatedAt?: string;
  previewUrl?: string;
  downloadUrl?: string;
  version?: number;
  updatedAt?: string;
  syncStatus?: string;
}

/** 查询工单签名 */
export function fetchSignatures(workOrderId: number) {
  return request<SignatureVO[]>({
    url: `/admin/work-orders/${workOrderId}/signatures`,
    method: 'GET'
  });
}

/** 查询工单验收记录 */
export function fetchAcceptanceRecords(workOrderId: number) {
  return request<AcceptanceVO[]>({
    url: `/admin/work-orders/${workOrderId}/acceptance`,
    method: 'GET'
  });
}

/** 验收复核 */
export function reviewAcceptance(workOrderId: number, data: {
  acceptanceStatus: string;
  acceptanceResult?: string;
  acceptanceOpinion?: string;
  rejectReason?: string;
}) {
  return request<AcceptanceVO>({
    url: `/admin/work-orders/${workOrderId}/acceptance/review`,
    method: 'POST',
    data
  });
}

/** 生成 PDF 验收单 */
export function generatePdf(workOrderId: number) {
  return request<PdfVO>({
    url: `/admin/work-orders/${workOrderId}/pdf/generate`,
    method: 'POST'
  });
}

/** 查询 PDF 列表 */
export function fetchPdfs(workOrderId: number) {
  return request<PdfVO[]>({
    url: `/admin/work-orders/${workOrderId}/pdf`,
    method: 'GET'
  });
}

/** PDF 下载重定向 URL */
export function pdfDownloadUrl(workOrderId: number): string {
  return `/api/admin/work-orders/${workOrderId}/pdf/download`;
}
