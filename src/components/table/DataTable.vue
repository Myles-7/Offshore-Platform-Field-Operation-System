<template>
  <div class="data-table">
    <el-table v-loading="loading" :data="data" border stripe>
      <slot />
      <template #empty>
        <AppState type="empty" />
      </template>
    </el-table>
    <div class="data-table__pagination">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        @update:current-page="$emit('update:pageNum', $event)"
        @update:page-size="$emit('update:pageSize', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts" generic="T">
import AppState from '@/components/state/AppState.vue';

defineProps<{
  data: T[];
  loading?: boolean;
  total: number;
  pageNum: number;
  pageSize: number;
}>();

defineEmits<{
  'update:pageNum': [value: number];
  'update:pageSize': [value: number];
}>();
</script>

<style scoped>
.data-table {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  padding: 16px;
}
.data-table__pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}
</style>
