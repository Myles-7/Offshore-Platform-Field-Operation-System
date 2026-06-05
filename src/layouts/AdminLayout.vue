<template>
  <el-container class="admin-layout">
    <el-aside class="admin-layout__aside" :width="effectiveCollapsed ? '64px' : '232px'">
      <div class="admin-layout__brand">
        <el-icon><Ship /></el-icon>
        <span v-if="!effectiveCollapsed">现场作业管理</span>
      </div>
      <AppMenu :collapsed="effectiveCollapsed" />
    </el-aside>
    <el-container>
      <el-header class="admin-layout__header">
        <div class="admin-layout__header-left">
          <el-button :icon="effectiveCollapsed ? Expand : Fold" text @click="appStore.toggleSidebar" />
          <div>
            <h1>{{ pageTitle }}</h1>
            <span class="admin-layout__subtitle">PC 后台</span>
          </div>
        </div>
        <el-dropdown trigger="click" @command="handleUserCommand">
          <button class="admin-layout__user" type="button">
            <el-avatar :size="32">{{ userInitial }}</el-avatar>
            <span class="admin-layout__user-name">{{ authStore.user?.realName || authStore.user?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <div class="admin-layout__user-card">
                <strong>{{ authStore.user?.realName || '-' }}</strong>
                <span>{{ authStore.user?.username }}</span>
                <small>{{ roleText }}</small>
              </div>
              <el-dropdown-item divided command="home">默认首页</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="admin-layout__main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowDown, Expand, Fold, Ship } from '@element-plus/icons-vue';
import { getDefaultHome } from '@/constants/access';
import AppMenu from '@/layouts/AppMenu.vue';
import { pageTitleMap } from '@/constants/page-meta';
import { useAppStore } from '@/stores/app';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();
const authStore = useAuthStore();
const isNarrowScreen = ref(false);

const pageTitle = computed(() => pageTitleMap[route.path] || 'PC后台');
const userInitial = computed(() => (authStore.user?.realName || authStore.user?.username || 'U').slice(0, 1));
const roleText = computed(() => authStore.user?.roleCodes.join(' / ') || '未加载角色');
const effectiveCollapsed = computed(() => appStore.sidebarCollapsed || isNarrowScreen.value);

function syncViewportState() {
  isNarrowScreen.value = window.innerWidth < 960;
}

async function handleUserCommand(command: string) {
  if (command === 'logout') {
    await authStore.logout();
    router.replace('/login');
    return;
  }
  if (command === 'home') {
    router.push(getDefaultHome(authStore.user));
  }
}

onMounted(() => {
  syncViewportState();
  window.addEventListener('resize', syncViewportState);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncViewportState);
});
</script>
