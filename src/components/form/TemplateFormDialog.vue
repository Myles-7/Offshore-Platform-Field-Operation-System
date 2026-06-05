<template>
  <el-dialog
    :model-value="modelValue"
    :title="editingId ? '编辑模板' : '新增模板'"
    width="640px"
    destroy-on-close
    @close="$emit('update:modelValue', false)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="模板编号" prop="templateCode">
            <el-input v-model="form.templateCode" placeholder="TPL-XXX" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模板名称" prop="templateName">
            <el-input v-model="form.templateName" placeholder="如：标准防腐作业模板" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="作业类型">
            <el-select v-model="form.workType" clearable placeholder="请选择" style="width:100%">
              <el-option v-for="item in workTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="默认优先级">
            <el-select v-model="form.defaultPriority" style="width:100%">
              <el-option v-for="item in priorityStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="计划工时(h)">
        <el-input-number v-model="form.defaultDurationHours" :min="0" :precision="1" style="width:100%" />
      </el-form-item>
      <el-form-item label="默认作业内容">
        <el-input v-model="form.defaultWorkContent" type="textarea" :rows="4" placeholder="描述默认作业内容" />
      </el-form-item>
      <el-form-item label="默认物料说明">
        <el-input v-model="form.defaultMaterialDesc" type="textarea" :rows="2" placeholder="描述默认所需物料" />
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="form.enabledFlag" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { priorityStatusOptions, workTypeOptions } from '@/constants/enums';
import type { WorkOrderTemplateItem } from '@/types/template';

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    editingId?: number;
    initialValues?: WorkOrderTemplateItem | null;
  }>(),
  { modelValue: false, editingId: undefined, initialValues: null }
);

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  submit: [data: Record<string, unknown>, editingId?: number];
}>();

const formRef = ref();
const submitting = ref(false);

const init = props.initialValues;

const form = reactive({
  templateCode: init?.templateCode || '',
  templateName: init?.templateName || '',
  workType: init?.workType || '',
  defaultPriority: init?.defaultPriority || 'NORMAL',
  defaultWorkContent: init?.defaultWorkContent || '',
  defaultMaterialDesc: init?.defaultMaterialDesc || '',
  defaultDurationHours: init?.defaultDurationHours ?? undefined,
  enabledFlag: init?.enabledFlag ?? 1,
  remark: init?.remark || ''
});

const rules = {
  templateCode: [{ required: true, message: '模板编号必填', trigger: 'blur' }],
  templateName: [{ required: true, message: '模板名称必填', trigger: 'blur' }]
};

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  submitting.value = true;
  try {
    emit('submit', { ...form }, props.editingId);
  } finally {
    submitting.value = false;
  }
}
</script>
