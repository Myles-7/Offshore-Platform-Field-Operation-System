import { request } from '@/api/request';
import type { PageResult } from '@/types/api';

export interface AdminUserItem {
  id: number;
  username: string;
  realName: string;
  phone: string;
  employeeNo: string;
  accountStatus: string;
}

/** PC后台用户列表（用于 leader/maintainer dropdown） */
export function fetchAllUsers() {
  return request<PageResult<AdminUserItem>>({
    url: '/admin/users',
    method: 'GET',
    params: { pageNum: 1, pageSize: 200 }
  });
}
