<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="720px"
    destroy-on-close
    @close="$emit('update:modelValue', false)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="wo-form">
      <el-row :gutter="16">
        <!-- 所属项目 -->
        <el-col :span="12">
          <el-form-item label="所属项目" prop="projectId">
            <el-select v-model="form.projectId" filterable placeholder="请选择项目" style="width:100%">
              <el-option v-for="p in projectOptions" :key="p.value" :label="p.label" :value="p.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <!-- 工单类型 -->
        <el-col :span="12">
          <el-form-item label="工单类型" prop="workType">
            <el-select v-model="form.workType" clearable placeholder="请选择类型" style="width:100%">
              <el-option v-for="item in workTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 作业标题 -->
      <el-form-item label="作业标题" prop="workTitle">
        <el-input v-model="form.workTitle" placeholder="请输入作业标题" />
      </el-form-item>

      <!-- 作业地点 -->
      <el-form-item label="作业地点" prop="workLocation">
        <el-input v-model="form.workLocation" placeholder="例如：C平台二层甲板" />
      </el-form-item>

      <el-row :gutter="16">
        <!-- 负责人 -->
        <el-col :span="12">
          <el-form-item label="负责人">
            <el-select v-model="form.leaderId" filterable clearable placeholder="选填" style="width:100%">
              <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <!-- 维修工 -->
        <el-col :span="12">
          <el-form-item label="维修工">
            <el-select v-model="form.maintainerId" filterable clearable placeholder="选填" style="width:100%">
              <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <!-- 计划开始时间 -->
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
        <!-- 计划结束时间 -->
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

      <!-- 优先级 -->
      <el-form-item label="优先级" prop="priority">
        <el-radio-group v-model="form.priority">
          <el-radio-button
            v-for="item in priorityStatusOptions"
            :key="item.value"
            :value="item.value"
          >{{ item.label }}</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- 作业内容 -->
      <el-form-item label="作业内容" prop="workContent">
        <el-input v-model="form.workContent" type="textarea" :rows="4" placeholder="描述作业内容" />
      </el-form-item>

      <!-- 所需物料 -->
      <el-form-item label="所需物料">
        <el-select
          v-model="form.selectedMaterialIds"
          multiple
          filterable
          placeholder="请选择所需物料"
          style="width:100%"
          @change="handleMaterialChange"
        >
          <el-option v-for="m in materialOptions" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
        <div v-if="form.selectedMaterialIds.length" style="margin-top:6px;color:#64748b;font-size:12px">
          已选 {{ form.selectedMaterialIds.length }} 种物料：{{ selectedMaterialNames }}
        </div>
      </el-form-item>

      <!-- 所需物料说明 -->
      <el-form-item label="物料说明">
        <el-input v-model="form.requiredMaterialDesc" type="textarea" :rows="2" placeholder="补充物料要求或替代选择说明" />
      </el-form-item>

      <!-- 安全注意事项 -->
      <el-form-item label="安全注意事项">
        <el-input v-model="form.safetyNotes" type="textarea" :rows="2" placeholder="填写安全注意事项" />
      </el-form-item>

      <!-- 附件说明 -->
      <el-form-item label="附件说明">
        <el-input v-model="form.attachmentDesc" type="textarea" :rows="2" placeholder="填写附件说明或前置条件照片要求" />
      </el-form-item>

      <!-- 备注 -->
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他备注" />
      </el-form-item>

      <!-- 保存失败时展示后端错误 -->
      <el-alert v-if="submitError" :title="submitError" type="error" show-icon :closable="false" style="margin-bottom:8px" />
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { priorityStatusOptions, workTypeOptions } from '@/constants/enums';

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    title?: string;
    initialValues?: Record<string, unknown>;
    projectOptions: { label: string; value: number }[];
    userOptions: { label: string; value: number }[];
    materialOptions: { label: string; value: number }[];
  }>(),
  {
    title: '新增工单',
    initialValues: () => ({})
  }
);

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  submit: [data: Record<string, unknown>];
}>();

const formRef = ref();
const submitting = ref(false);
const submitError = ref('');

/* ========== 表单数据 ========== */
const form = reactive({
  projectId: (props.initialValues.projectId as number) || '',
  workTitle: (props.initialValues.workTitle as string) || '',
  workType: (props.initialValues.workType as string) || '',
  workLocation: (props.initialValues.workLocation as string) || '',
  workContent: (props.initialValues.workContent as string) || '',
  leaderId: (props.initialValues.leaderId as number) || '',
  maintainerId: (props.initialValues.maintainerId as number) || '',
  plannedStartTime: (props.initialValues.plannedStartTime as string) || '',
  plannedEndTime: (props.initialValues.plannedEndTime as string) || '',
  priority: (props.initialValues.priority as string) || 'NORMAL',
  requiredMaterialDesc: (props.initialValues.requiredMaterialDesc as string) || '',
  selectedMaterialIds: (props.initialValues.selectedMaterialIds as number[]) || [],
  safetyNotes: (props.initialValues.safetyNotes as string) || '',
  attachmentDesc: (props.initialValues.attachmentDesc as string) || '',
  remark: (props.initialValues.remark as string) || ''
});

/* ========== 校验规则 ========== */
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
  projectId: [{ required: true, message: '项目必填', trigger: 'change' }],
  workTitle: [{ required: true, message: '作业标题不能为空', trigger: 'blur' }],
  workLocation: [{ required: true, message: '作业地点必填', trigger: 'blur' }],
  workContent: [{ required: true, message: '作业内容必填', trigger: 'blur' }],
  plannedStartTime: [{ required: true, message: '计划开始时间必填', trigger: 'change' }],
  plannedEndTime: [
    { required: true, message: '计划结束时间必填', trigger: 'change' },
    { validator: validateEndTime, trigger: 'change' }
  ],
  priority: [{ required: true, message: '优先级必填', trigger: 'change' }]
};

/* ========== 物料选中名称 ========== */
const selectedMaterialNames = computed(() =>
  form.selectedMaterialIds
    .map((id) => props.materialOptions.find((m) => m.value === id)?.label || `ID:${id}`)
    .join('、')
);

function handleMaterialChange(ids: number[]) {
  // 校验物料是否存在（通过 options 前端预校验）
  const invalid = ids.filter((id) => !props.materialOptions.some((m) => m.value === id));
  if (invalid.length) {
    ElMessage.warning(`物料 ID ${invalid.join(', ')} 不存在，已过滤`);
    form.selectedMaterialIds = ids.filter((id) => !invalid.includes(id));
  }
}

/* ========== 提交 ========== */
async function handleSubmit() {
  submitError.value = '';
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  submitting.value = true;
  try {
    const payload: Record<string, unknown> = {
      projectId: form.projectId,
      workTitle: form.workTitle,
      workType: form.workType || undefined,
      workLocation: form.workLocation,
      workContent: form.workContent,
      leaderId: form.leaderId || undefined,
      maintainerId: form.maintainerId || undefined,
      plannedStartTime: form.plannedStartTime,
      plannedEndTime: form.plannedEndTime,
      priority: form.priority,
      requiredMaterialDesc: form.requiredMaterialDesc || undefined,
      safetyNotes: form.safetyNotes || undefined,
      attachmentDesc: form.attachmentDesc || undefined,
      remark: form.remark || undefined,
      // 保留所选物料 ID 传给后端
      materialIds: form.selectedMaterialIds.length ? form.selectedMaterialIds : undefined
    };
    emit('submit', payload);
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
