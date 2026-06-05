import { request } from '@/api/request';
import type { CurrentUser, LoginRequest, LoginResponse } from '@/types/auth';

export function loginApi(data: LoginRequest) {
  return request<LoginResponse>({
    url: '/auth/login',
    method: 'POST',
    data
  });
}

export function logoutApi() {
  return request<void>({
    url: '/auth/logout',
    method: 'POST'
  });
}

export function currentUserApi() {
  return request<CurrentUser>({
    url: '/auth/current',
    method: 'GET'
  });
}
