import axios, { type AxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { useAuthStore } from '@/stores/auth';
import type { ApiResponse, RequestOptions } from '@/types/api';
import { apiBaseUrl } from '@/utils/env';

const service = axios.create({
  baseURL: apiBaseUrl,
  timeout: 30000
});

service.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }
  return config;
});

service.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const message = error?.response?.data?.message || error.message || '网络请求失败';
    if (status === 401) {
      const authStore = useAuthStore();
      authStore.clearSession();
      router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } });
    } else if (status === 403) {
      ElMessage.error('无权限访问该资源');
      if (router.currentRoute.value.path !== '/403') {
        router.replace('/403');
      }
    } else {
      ElMessage.error(message);
    }
    return Promise.reject(error);
  }
);

export async function request<T>(config: AxiosRequestConfig, options: RequestOptions = {}): Promise<T> {
  const response = await service.request<ApiResponse<T>>({
    ...config,
    headers: {
      ...(config.headers || {}),
      'X-Request-Id': crypto.randomUUID?.() || `${Date.now()}`
    }
  });
  const body = response.data;
  if (body?.code === 200) {
    return body.data;
  }
  const message = body?.message || '请求处理失败';
  if (body?.code === 401) {
    const authStore = useAuthStore();
    authStore.clearSession();
    router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } });
  } else if (body?.code === 403 && config.url !== '/auth/login') {
    router.replace('/403');
  }
  if (options.showError !== false) {
    ElMessage.error(message);
  }
  throw new Error(message);
}
