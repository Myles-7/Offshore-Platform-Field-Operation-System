<template>
  <el-descriptions :column="column" border>
    <el-descriptions-item v-for="item in items" :key="item.label" :label="item.label">
      {{ item.value || '-' }}
    </el-descriptions-item>
  </el-descriptions>
</template>

<script setup lang="ts">
defineProps<{
  column?: number;
  items: Array<{
    label: string;
    value?: string | number | null;
  }>;
}>();
</script>
