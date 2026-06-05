<template>
  <AppState type="permission" description="当前账号没有访问该页面的权限" action-text="返回首页" @action="$router.push('/dashboard')" />
</template>

<script setup lang="ts">
import AppState from '@/components/state/AppState.vue';
</script>
