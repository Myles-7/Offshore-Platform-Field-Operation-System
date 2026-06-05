import { request } from '@/api/request';
import type {
  DashboardOverview,
  MaterialStatItem,
  OutputValueItem,
  PersonStatItem,
  ProjectStatItem
} from '@/types/dashboard';

/** 概览 KPI */
export function fetchDashboardOverview() {
  return request<DashboardOverview>({
    url: '/admin/dashboard/overview',
    method: 'GET'
  });
}

/** 工单统计（包含概览 KPI + 状态分布 items） */
export function fetchWorkOrderStatistics() {
  return request<DashboardOverview>({
    url: '/admin/dashboard/work-order-statistics',
    method: 'GET'
  });
}

/** 项目维度统计 */
export function fetchProjectStatistics() {
  return request<ProjectStatItem[]>({
    url: '/admin/dashboard/project-statistics',
    method: 'GET'
  });
}

/** 人员维度统计 */
export function fetchPersonStatistics() {
  return request<PersonStatItem[]>({
    url: '/admin/dashboard/person-statistics',
    method: 'GET'
  });
}

/** 物料消耗排行 */
export function fetchMaterialStatistics() {
  return request<MaterialStatItem[]>({
    url: '/admin/dashboard/material-statistics',
    method: 'GET'
  });
}

/** 产值统计 */
export function fetchOutputValue() {
  return request<OutputValueItem[]>({
    url: '/admin/dashboard/output-value',
    method: 'GET'
  });
}
