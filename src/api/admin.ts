import { request } from '@/api/request';
import type { PageParams, PageResult } from '@/types/api';
import type { WorkOrderListItem } from '@/types/work-order';

export function fetchWorkOrders(params: PageParams) {
  return request<PageResult<WorkOrderListItem>>({
    url: '/admin/work-orders',
    method: 'GET',
    params
  });
}
