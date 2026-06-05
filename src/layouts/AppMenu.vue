<template>
  <el-menu :default-active="route.path" :collapse="collapsed" router class="app-menu">
    <template v-for="item in visibleMenus" :key="item.path">
      <el-sub-menu v-if="item.children?.length" :index="item.path">
        <template #title>
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </template>
        <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
          <el-icon><component :is="child.icon" /></el-icon>
          <span>{{ child.title }}</span>
        </el-menu-item>
      </el-sub-menu>
      <el-menu-item v-else :index="item.path">
        <el-icon><component :is="item.icon" /></el-icon>
        <template #title>{{ item.title }}</template>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { filterMenusByAccess } from '@/constants/access';
import { adminMenus } from '@/constants/page-meta';
import { useAuthStore } from '@/stores/auth';

defineProps<{
  collapsed: boolean;
}>();

const route = useRoute();
const authStore = useAuthStore();

const visibleMenus = computed(() => filterMenusByAccess(adminMenus, authStore.user));
</script>
