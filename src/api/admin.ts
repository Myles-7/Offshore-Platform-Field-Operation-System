import { request } from '@/api/request';
import type { PageResult } from '@/types/api';
import type {
  WorkOrderListItem,
  WorkOrderQueryParams,
  WorkOrderStatusRequest,
  WorkOrderDetailVO,
  StatusFlowVO,
  WorkRecordVO,
  WorkOrderAttachmentVO
} from '@/types/work-order';
import type { WorkOrderTemplateItem } from '@/types/template';

/* ========== 工单 ========== */

/** 工单分页列表 */
export function fetchWorkOrders(params: WorkOrderQueryParams) {
  const cleanParams: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== '') {
      cleanParams[key] = value;
    }
  }
  return request<PageResult<WorkOrderListItem>>({
    url: '/admin/work-orders',
    method: 'GET',
    params: cleanParams
  });
}

/** 工单详情 */
export function fetchWorkOrderDetail(id: number) {
  return request<WorkOrderDetailVO>({
    url: `/admin/work-orders/${id}`,
    method: 'GET'
  });
}

/** 工单状态流 */
export function fetchStatusFlow(id: number) {
  return request<StatusFlowVO[]>({
    url: `/admin/work-orders/${id}/status-flow`,
    method: 'GET'
  });
}

/** 创建工单 */
export function createWorkOrder(data: Record<string, unknown>) {
  return request<WorkOrderListItem>({
    url: '/admin/work-orders',
    method: 'POST',
    data
  });
}

/** 更新工单 */
export function updateWorkOrder(id: number, data: Record<string, unknown>) {
  return request<WorkOrderListItem>({
    url: `/admin/work-orders/${id}`,
    method: 'PUT',
    data
  });
}

/** 删除工单（逻辑删除） */
export function deleteWorkOrder(id: number) {
  return request<void>({
    url: `/admin/work-orders/${id}`,
    method: 'DELETE'
  });
}

/** 工单派工 */
export function assignWorkOrder(id: number, data: Record<string, unknown>) {
  return request<WorkOrderListItem>({
    url: `/admin/work-orders/${id}/assign`,
    method: 'POST',
    data
  });
}

/** 修改工单状态 */
export function changeWorkOrderStatus(id: number, data: WorkOrderStatusRequest) {
  return request<WorkOrderListItem>({
    url: `/admin/work-orders/${id}/status`,
    method: 'POST',
    data
  });
}

/** 工单附件列表 */
export function fetchWorkOrderAttachments(workOrderId: number) {
  return request<WorkOrderAttachmentVO[]>({
    url: `/admin/work-orders/${workOrderId}/attachments`,
    method: 'GET'
  });
}

/** 工单施工记录列表 */
export function fetchWorkOrderRecords(workOrderId: number) {
  return request<WorkRecordVO[]>({
    url: `/admin/work-orders/${workOrderId}/records`,
    method: 'GET'
  });
}

/** 施工记录详情 */
export function fetchWorkRecord(recordId: number) {
  return request<WorkRecordVO>({
    url: `/admin/work-records/${recordId}`,
    method: 'GET'
  });
}

/** 工单审计轨迹 */
export function fetchAuditTrail(workOrderId: number) {
  return request<Record<string, unknown>[]>({
    url: `/admin/work-orders/${workOrderId}/audit-trail`,
    method: 'GET'
  });
}

/* ========== 工单模板 ========== */

/** 模板列表 */
export function fetchTemplates() {
  return request<WorkOrderTemplateItem[]>({
    url: '/admin/work-order-templates',
    method: 'GET'
  });
}

/** 创建模板 */
export function createTemplate(data: Record<string, unknown>) {
  return request<WorkOrderTemplateItem>({
    url: '/admin/work-order-templates',
    method: 'POST',
    data
  });
}

/** 更新模板 */
export function updateTemplate(id: number, data: Record<string, unknown>) {
  return request<WorkOrderTemplateItem>({
    url: `/admin/work-order-templates/${id}`,
    method: 'PUT',
    data
  });
}

/** 删除模板 */
export function deleteTemplate(id: number) {
  return request<void>({
    url: `/admin/work-order-templates/${id}`,
    method: 'DELETE'
  });
}

/** 根据模板创建工单 */
export function createWorkOrderFromTemplate(templateId: number, data: Record<string, unknown>) {
  return request<WorkOrderListItem>({
    url: `/admin/work-orders/from-template/${templateId}`,
    method: 'POST',
    data
  });
}
