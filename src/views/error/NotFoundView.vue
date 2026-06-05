<template>
  <AppState type="error" title="页面不存在" description="当前访问的地址不存在或已被移除" action-text="返回首页" @action="goHome" />
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { getDefaultHome } from '@/constants/access';
import AppState from '@/components/state/AppState.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

function goHome() {
  router.push(getDefaultHome(authStore.user));
}
</script>
