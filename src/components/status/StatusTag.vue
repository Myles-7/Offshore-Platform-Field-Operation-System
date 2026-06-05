<template>
  <el-tag :type="option.type">{{ option.label }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {
  aiReviewStatusOptions,
  getEnumOption,
  qualificationStatusOptions,
  syncStatusOptions,
  workOrderStatusOptions
} from '@/constants/enums';

const props = defineProps<{
  value?: string;
  enumType: 'workOrder' | 'qualification' | 'sync' | 'aiReview';
}>();

const optionsMap = {
  workOrder: workOrderStatusOptions,
  qualification: qualificationStatusOptions,
  sync: syncStatusOptions,
  aiReview: aiReviewStatusOptions
};

const option = computed(() => getEnumOption(optionsMap[props.enumType], props.value));
</script>
