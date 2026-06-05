import {
  Aim,
  Box,
  DataAnalysis,
  Document,
  Files,
  Finished,
  FolderOpened,
  Key,
  List,
  Operation,
  Setting,
  User
} from '@element-plus/icons-vue';
import { routeAccess } from '@/constants/access';
import type { AppMenuItem } from '@/types/menu';

export const pageTitleMap: Record<string, string> = {
  '/dashboard': '经营看板',
  '/projects': '项目管理',
  '/work-orders': '工单管理',
  '/records': '施工记录',
  '/acceptance': '验收归档',
  '/materials': '物料追溯',
  '/qualifications': '人员资质',
  '/sync/conflicts': '同步冲突',
  '/ai/review': 'AI 辅助验收',
  '/system/users': '用户管理',
  '/system/roles': '角色权限',
  '/logs': '操作日志',
  '/403': '无权限',
  '/404': '页面不存在'
};

export const adminMenus: AppMenuItem[] = [
  { title: '经营看板', path: '/dashboard', icon: DataAnalysis, ...routeAccess.dashboard },
  { title: '项目管理', path: '/projects', icon: FolderOpened, ...routeAccess.projects },
  { title: '工单管理', path: '/work-orders', icon: List, ...routeAccess.workOrders },
  { title: '施工记录', path: '/records', icon: Document, ...routeAccess.records },
  { title: '验收归档', path: '/acceptance', icon: Finished, ...routeAccess.acceptance },
  { title: '物料追溯', path: '/materials', icon: Box, ...routeAccess.materials },
  { title: '人员资质', path: '/qualifications', icon: User, ...routeAccess.qualifications },
  { title: '同步冲突', path: '/sync/conflicts', icon: Operation, ...routeAccess.syncConflicts },
  { title: 'AI 辅助验收', path: '/ai/review', icon: Aim, ...routeAccess.aiReview },
  {
    title: '系统管理',
    path: '/system',
    icon: Setting,
    ...routeAccess.system,
    children: [
      { title: '用户管理', path: '/system/users', icon: User, ...routeAccess.system },
      { title: '角色权限', path: '/system/roles', icon: Key, ...routeAccess.system }
    ]
  },
  { title: '操作日志', path: '/logs', icon: Files, ...routeAccess.logs }
];
