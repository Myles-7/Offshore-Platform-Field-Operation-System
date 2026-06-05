import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import AdminLayout from '@/layouts/AdminLayout.vue';
import { canAccess, getDefaultHome, routeAccess } from '@/constants/access';
import { useAuthStore } from '@/stores/auth';

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: () => {
      const authStore = useAuthStore();
      return getDefaultHome(authStore.user);
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '经营看板', ...routeAccess.dashboard }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/projects/ProjectsView.vue'),
        meta: { title: '项目管理', ...routeAccess.projects }
      },
      {
        path: 'work-orders',
        name: 'WorkOrders',
        component: () => import('@/views/work-orders/WorkOrdersView.vue'),
        meta: { title: '工单管理', ...routeAccess.workOrders }
      },
      {
        path: 'work-orders/templates',
        name: 'WorkOrderTemplates',
        component: () => import('@/views/work-orders/WorkOrderTemplatesView.vue'),
        meta: { title: '工单模板', ...routeAccess.workOrders }
      },
      {
        path: 'work-orders/:id',
        name: 'WorkOrderDetail',
        component: () => import('@/views/work-orders/WorkOrderDetailView.vue'),
        meta: { title: '工单详情', ...routeAccess.workOrders }
      },
      {
        path: 'records',
        name: 'Records',
        component: () => import('@/views/records/RecordsView.vue'),
        meta: { title: '施工记录', ...routeAccess.records }
      },
      {
        path: 'acceptance',
        name: 'Acceptance',
        component: () => import('@/views/acceptance/AcceptanceView.vue'),
        meta: { title: '验收归档', ...routeAccess.acceptance }
      },
      {
        path: 'materials',
        name: 'Materials',
        component: () => import('@/views/materials/MaterialsView.vue'),
        meta: { title: '物料追溯', ...routeAccess.materials }
      },
      {
        path: 'qualifications',
        name: 'Qualifications',
        component: () => import('@/views/qualifications/QualificationsView.vue'),
        meta: { title: '人员资质', ...routeAccess.qualifications }
      },
      {
        path: 'sync/conflicts',
        name: 'SyncConflicts',
        component: () => import('@/views/sync/SyncConflictsView.vue'),
        meta: { title: '同步冲突', ...routeAccess.syncConflicts }
      },
      {
        path: 'ai/review',
        name: 'AiReview',
        component: () => import('@/views/ai/AiReviewView.vue'),
        meta: { title: 'AI 辅助验收', ...routeAccess.aiReview }
      },
      {
        path: 'knowledge/cases',
        name: 'KnowledgeCases',
        component: () => import('@/views/knowledge/KnowledgeView.vue'),
        meta: { title: '知识库', ...routeAccess.workOrders }
      },
      {
        path: 'reports/reconciliation',
        name: 'Reports',
        component: () => import('@/views/reports/ReportsView.vue'),
        meta: { title: '经营报表', ...routeAccess.reports }
      },
      {
        path: 'system/users',
        name: 'SystemUsers',
        component: () => import('@/views/system/SystemUsersView.vue'),
        meta: { title: '用户管理', ...routeAccess.system }
      },
      {
        path: 'system/roles',
        name: 'SystemRoles',
        component: () => import('@/views/system/SystemRolesView.vue'),
        meta: { title: '角色权限', ...routeAccess.system }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/logs/LogsView.vue'),
        meta: { title: '操作日志', ...routeAccess.logs }
      },
      {
        path: '403',
        name: 'Forbidden',
        component: () => import('@/views/error/ForbiddenView.vue'),
        meta: { title: '无权限', skipAccessCheck: true }
      },
      {
        path: '404',
        name: 'NotFound',
        component: () => import('@/views/error/NotFoundView.vue'),
        meta: { title: '页面不存在', skipAccessCheck: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  if (to.meta.public) {
    if (to.path === '/login' && authStore.token) {
      if (!authStore.user) {
        await authStore.loadCurrentUser().catch(() => authStore.clearSession());
      }
      if (authStore.user) {
        return getDefaultHome(authStore.user);
      }
    }
    return true;
  }
  if (!authStore.token) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  if (!authStore.user) {
    await authStore.loadCurrentUser().catch(() => {
      authStore.clearSession();
    });
    if (!authStore.user) {
      return { path: '/login', query: { redirect: to.fullPath } };
    }
  }
  if (!to.meta.skipAccessCheck && !canAccess(authStore.user, to.meta)) {
    return '/403';
  }
  return true;
});

router.afterEach((to) => {
  document.title = `${String(to.meta.title || 'PC后台')} - 海上平台现场作业管理系统`;
});

export default router;
