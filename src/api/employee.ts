import { request } from '@/api/request';
import type { EmployeeItem } from '@/types/employee';

/** 员工列表 */
export function fetchEmployees() {
  return request<EmployeeItem[]>({
    url: '/admin/employees',
    method: 'GET'
  });
}
