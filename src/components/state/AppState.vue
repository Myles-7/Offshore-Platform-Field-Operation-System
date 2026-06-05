<template>
  <div class="app-state" :class="`app-state--${type}`">
    <el-empty v-if="type === 'empty'" :description="title || '暂无数据'" />
    <template v-else>
      <el-icon class="app-state__icon">
        <CircleClose v-if="type === 'error'" />
        <Lock v-else-if="type === 'permission'" />
        <Loading v-else />
      </el-icon>
      <div class="app-state__title">{{ resolvedTitle }}</div>
      <div v-if="description" class="app-state__desc">{{ description }}</div>
      <el-button v-if="actionText" type="primary" @click="$emit('action')">{{ actionText }}</el-button>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { CircleClose, Loading, Lock } from '@element-plus/icons-vue';

const props = withDefaults(
  defineProps<{
    type?: 'loading' | 'empty' | 'error' | 'permission';
    title?: string;
    description?: string;
    actionText?: string;
  }>(),
  {
    type: 'empty'
  }
);

defineEmits<{
  action: [];
}>();

const titleMap = {
  loading: '加载中',
  empty: '暂无数据',
  error: '加载失败',
  permission: '无权访问'
};

const resolvedTitle = computed(() => props.title || titleMap[props.type]);
</script>
