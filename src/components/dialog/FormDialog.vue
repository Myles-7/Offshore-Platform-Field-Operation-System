<template>
  <el-dialog :model-value="modelValue" :title="title" width="640px" destroy-on-close @close="$emit('update:modelValue', false)">
    <slot />
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="confirmLoading" @click="$emit('confirm')">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: boolean;
  title: string;
  confirmLoading?: boolean;
}>();

defineEmits<{
  'update:modelValue': [value: boolean];
  confirm: [];
}>();
</script>
