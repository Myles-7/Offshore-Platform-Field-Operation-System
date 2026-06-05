<template>
  <el-dialog
    :model-value="modelValue"
    title="根据模板创建工单"
    width="720px"
    destroy-on-close
    @close="$emit('update:modelValue', false)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="wo-form">
      <!-- 选择模板 -->
      <el-form-item label="选择模板" prop="templateId">
        <el-select v-model="form.templateId" filterable placeholder="请选择工单模板" style="width:100%" @change="onTemplateChange">
          <el-option v-for="t in templateOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>

      <template v-if="selectedTemplate">
        <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
          <el-descriptions-item label="模板编号">{{ selectedTemplate.templateCode }}</el-descriptions-item>
          <el-descriptions-item label="模板名称">{{ selectedTemplate.templateName }}</el-descriptions-item>
          <el-descriptions-item label="作业类型">{{ selectedTemplate.workType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="默认优先级">{{ selectedTemplate.defaultPriority || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属项目" prop="projectId">
            <el-select v-model="form.projectId" filterable placeholder="请选择项目" style="width:100%">
              <el-option v-for="p in projectOptions" :key="p.value" :label="p.label" :value="p.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工单编号">
            <el-input v-model="form.workOrderNo" placeholder="自动生成" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="作业标题" prop="workTitle">
        <el-input v-model="form.workTitle" placeholder="请输入作业标题" />
      </el-form-item>

      <el-form-item label="作业地点" prop="workLocation">
        <el-input v-model="form.workLocation" placeholder="例如：C平台二层甲板" />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="负责人">
            <el-select v-model="form.leaderId" filterable clearable placeholder="选填" style="width:100%">
              <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="维修工">
            <el-select v-model="form.maintainerId" filterable clearable placeholder="选填" style="width:100%">
              <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="计划开始时间" prop="plannedStartTime">
            <el-date-picker
              v-model="form.plannedStartTime"
              type="datetime"
              placeholder="选择开始时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width:100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计划结束时间" prop="plannedEndTime">
            <el-date-picker
              v-model="form.plannedEndTime"
              type="datetime"
              placeholder="选择结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width:100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="需验收">
        <el-switch v-model="form.acceptanceRequired" :active-value="1" :inactive-value="0" />
      </el-form-item>

      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
      </el-form-item>

      <el-alert v-if="submitError" :title="submitError" type="error" show-icon :closable="false" style="margin-bottom:8px" />
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">创建工单</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import type { WorkOrderTemplateItem } from '@/types/template';

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    templateOptions: { label: string; value: number; data: WorkOrderTemplateItem }[];
    projectOptions: { label: string; value: number }[];
    userOptions: { label: string; value: number }[];
  }>(),
  {
    modelValue: false,
    templateOptions: () => [],
    projectOptions: () => [],
    userOptions: () => []
  }
);

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  submit: [templateId: number, data: Record<string, unknown>];
}>();

const formRef = ref();
const submitting = ref(false);
const submitError = ref('');

const form = reactive({
  templateId: '' as number | string,
  projectId: '' as number | string,
  workOrderNo: '',
  workTitle: '',
  workLocation: '',
  leaderId: '' as number | string,
  maintainerId: '' as number | string,
  plannedStartTime: '',
  plannedEndTime: '',
  acceptanceRequired: 1,
  remark: ''
});

const selectedTemplate = computed(() => {
  if (form.templateId === '') return null;
  return props.templateOptions.find((t) => t.value === form.templateId)?.data ?? null;
});

const validateEndTime = (_rule: unknown, _value: unknown, callback: (err?: Error) => void) => {
  if (form.plannedStartTime && form.plannedEndTime) {
    if (new Date(form.plannedEndTime) <= new Date(form.plannedStartTime)) {
      callback(new Error('结束时间不能早于开始时间'));
      return;
    }
  }
  callback();
};

const rules = {
  templateId: [{ required: true, message: '请选择模板', trigger: 'change' }],
  projectId: [{ required: true, message: '项目必填', trigger: 'change' }],
  workTitle: [{ required: true, message: '作业标题不能为空', trigger: 'blur' }],
  workLocation: [{ required: true, message: '作业地点必填', trigger: 'blur' }],
  plannedStartTime: [{ required: true, message: '计划开始时间必填', trigger: 'change' }],
  plannedEndTime: [
    { required: true, message: '计划结束时间必填', trigger: 'change' },
    { validator: validateEndTime, trigger: 'change' }
  ]
};

function onTemplateChange(templateId: number) {
  const tpl = props.templateOptions.find((t) => t.value === templateId)?.data;
  if (!tpl) return;
  // 不覆盖用户已填写的内容，只填充模板默认值
  if (!form.workTitle) form.workTitle = tpl.templateName;
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  submitting.value = true;
  try {
    emit('submit', form.templateId as number, {
      projectId: form.projectId,
      workOrderNo: form.workOrderNo || undefined,
      workTitle: form.workTitle,
      workLocation: form.workLocation,
      leaderId: form.leaderId || undefined,
      maintainerId: form.maintainerId || undefined,
      plannedStartTime: form.plannedStartTime,
      plannedEndTime: form.plannedEndTime,
      acceptanceRequired: form.acceptanceRequired,
      remark: form.remark || undefined
    });
  } finally {
    submitting.value = false;
  }
}

defineExpose({ setError: (msg: string) => { submitError.value = msg; } });
</script>

<style scoped>
.wo-form {
  max-height: 520px;
  overflow-y: auto;
  padding-right: 4px;
}
</style>
