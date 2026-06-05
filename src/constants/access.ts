import type { RouteMeta } from 'vue-router';
import type { CurrentUser } from '@/types/auth';
import type { AppMenuItem } from '@/types/menu';

export const roleCodes = {
  systemAdmin: ['SYSTEM_ADMIN', 'SYS_ADMIN'],
  projectManager: ['PROJECT_MANAGER'],
  worker: ['MAINTAINER', 'WORKER'],
  materialManager: ['MATERIAL_ADMIN', 'MATERIAL_MANAGER'],
  qualificationManager: ['QUALIFICATION_ADMIN', 'QUALIFICATION_MANAGER'],
  acceptor: ['ACCEPTANCE_USER', 'ACCEPTOR'],
  businessUser: ['BUSINESS_USER']
} as const;

const allAdminRoles = [
  ...roleCodes.systemAdmin,
  ...roleCodes.projectManager,
  ...roleCodes.materialManager,
  ...roleCodes.qualificationManager,
  ...roleCodes.acceptor,
  ...roleCodes.businessUser
];

export const routeAccess = {
  dashboard: {
    roles: [...roleCodes.systemAdmin, ...roleCodes.projectManager, ...roleCodes.businessUser],
    permissions: ['MENU_DASHBOARD']
  },
  projects: {
    roles: [...roleCodes.systemAdmin, ...roleCodes.projectManager],
    permissions: ['MENU_PROJECT', 'MENU_WORK_ORDER']
  },
  workOrders: {
    roles: [...roleCodes.systemAdmin, ...roleCodes.projectManager, ...roleCodes.acceptor],
    permissions: ['MENU_WORK_ORDER', 'MENU_ACCEPTANCE']
  },
  records: {
    roles: [...roleCodes.systemAdmin, ...roleCodes.projectManager, ...roleCodes.acceptor],
    permissions: ['MENU_WORK_ORDER', 'MENU_ACCEPTANCE']
  },
  acceptance: {
    roles: [...roleCodes.systemAdmin, ...roleCodes.projectManager, ...roleCodes.acceptor],
    permissions: ['MENU_ACCEPTANCE']
  },
  materials: {
    roles: [...roleCodes.systemAdmin, ...roleCodes.materialManager],
    permissions: ['MENU_MATERIAL']
  },
  qualifications: {
    roles: [...roleCodes.systemAdmin, ...roleCodes.qualificationManager],
    permissions: ['MENU_QUALIFICATION']
  },
  syncConflicts: {
    roles: [...roleCodes.systemAdmin, ...roleCodes.projectManager],
    permissions: ['MENU_SYNC_CONFLICT']
  },
  aiReview: {
    roles: [...roleCodes.systemAdmin, ...roleCodes.projectManager, ...roleCodes.acceptor],
    permissions: ['MENU_AI_REVIEW']
  },
  system: {
    roles: [...roleCodes.systemAdmin],
    permissions: ['MENU_SYSTEM']
  },
  logs: {
    roles: [...roleCodes.systemAdmin],
    permissions: ['MENU_SYSTEM']
  },
  reports: {
    roles: [...roleCodes.systemAdmin, ...roleCodes.businessUser],
    permissions: ['REPORT_EXPORT']
  }
} satisfies Record<string, { roles: string[]; permissions?: string[] }>;

export function hasAnyRole(userRoles: string[], allowedRoles?: readonly string[]) {
  if (!allowedRoles?.length) {
    return true;
  }
  return userRoles.some((role) => allowedRoles.includes(role));
}

export function hasAnyPermission(userPermissions: string[], allowedPermissions?: string[]) {
  if (!allowedPermissions?.length) {
    return true;
  }
  return userPermissions.some((permission) => allowedPermissions.includes(permission));
}

export function canAccess(user: CurrentUser | null, access?: Pick<RouteMeta, 'roles' | 'permissions'>) {
  if (!access) {
    return true;
  }
  if (!user) {
    return false;
  }
  if (hasAnyRole(user.roleCodes, roleCodes.systemAdmin)) {
    return true;
  }
  const hasRoleRule = Boolean(access.roles?.length);
  const hasPermissionRule = Boolean(access.permissions?.length);
  if (!hasRoleRule && !hasPermissionRule) {
    return true;
  }
  const roleMatched = hasRoleRule && hasAnyRole(user.roleCodes, access.roles);
  const permissionMatched = hasPermissionRule && hasAnyPermission(user.permissionCodes, access.permissions);
  return roleMatched || permissionMatched;
}

export function filterMenusByAccess(menus: AppMenuItem[], user: CurrentUser | null): AppMenuItem[] {
  return menus
    .map((item) => {
      const children = item.children ? filterMenusByAccess(item.children, user) : undefined;
      if (children?.length) {
        return { ...item, children };
      }
      return canAccess(user, item) ? { ...item, children: undefined } : null;
    })
    .filter((item): item is AppMenuItem => Boolean(item));
}

export function getDefaultHome(user: CurrentUser | null) {
  if (!user) {
    return '/login';
  }
  if (hasAnyRole(user.roleCodes, [...roleCodes.systemAdmin, ...roleCodes.projectManager, ...roleCodes.businessUser])) {
    return '/dashboard';
  }
  if (hasAnyRole(user.roleCodes, roleCodes.materialManager)) {
    return '/materials';
  }
  if (hasAnyRole(user.roleCodes, roleCodes.qualificationManager)) {
    return '/qualifications';
  }
  if (hasAnyRole(user.roleCodes, roleCodes.acceptor)) {
    return '/acceptance';
  }
  if (hasAnyRole(user.roleCodes, allAdminRoles)) {
    return '/dashboard';
  }
  return '/403';
}
