import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { currentUserApi, loginApi, logoutApi } from '@/api/auth';
import type { CurrentUser, LoginRequest } from '@/types/auth';

const tokenKey = 'offshore_admin_token';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(tokenKey) || '');
  const user = ref<CurrentUser | null>(null);

  const roleCodes = computed(() => user.value?.roleCodes || []);
  const permissionCodes = computed(() => user.value?.permissionCodes || []);
  const isLoggedIn = computed(() => Boolean(token.value));

  function setToken(nextToken: string) {
    token.value = nextToken;
    localStorage.setItem(tokenKey, nextToken);
  }

  function clearSession() {
    token.value = '';
    user.value = null;
    localStorage.removeItem(tokenKey);
  }

  async function login(payload: Omit<LoginRequest, 'platform'>) {
    const result = await loginApi({ ...payload, platform: 'PC' });
    setToken(result.token);
    user.value = result;
    return result;
  }

  async function loadCurrentUser() {
    if (!token.value) {
      return null;
    }
    user.value = await currentUserApi();
    return user.value;
  }

  async function logout() {
    if (token.value) {
      await logoutApi().catch(() => undefined);
    }
    clearSession();
  }

  function hasPermission(permission?: string) {
    if (!permission) {
      return true;
    }
    return permissionCodes.value.includes(permission);
  }

  function hasRole(role: string) {
    return roleCodes.value.includes(role);
  }

  return {
    token,
    user,
    roleCodes,
    permissionCodes,
    isLoggedIn,
    setToken,
    clearSession,
    login,
    logout,
    loadCurrentUser,
    hasPermission,
    hasRole
  };
});
