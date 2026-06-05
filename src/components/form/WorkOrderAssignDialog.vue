<template>
  <el-dialog
    :model-value="modelValue"
    title="派工"
    width="820px"
    destroy-on-close
    @close="$emit('update:modelValue', false)"
  >
    <!-- 工单摘要 -->
    <el-descriptions v-if="workOrder" :column="2" border size="small" style="margin-bottom:16px">
      <el-descriptions-item label="工单编号">{{ workOrder.workOrderNo }}</el-descriptions-item>
      <el-descriptions-item label="项目名称">{{ workOrder.projectName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="作业地点">{{ workOrder.workLocation || '-' }}</el-descriptions-item>
      <el-descriptions-item label="计划开始">{{ formatDateTime(workOrder.plannedStartTime) }}</el-descriptions-item>
    </el-descriptions>

    <!-- 加载中 -->
    <AppState v-if="loadingCandidates" type="loading" />

    <!-- 候选人表格 -->
    <template v-else>
      <div class="assign-section-title">资质候选人员</div>
      <el-alert
        v-if="candidates.length === 0"
        type="warning"
        title="当前没有符合条件的在册人员"
        :closable="false"
        show-icon
        style="margin-bottom:12px"
      />
      <el-table
        v-else
        ref="tableRef"
        :data="candidates"
        border
        stripe
        highlight-current-row
        max-height="340"
        @current-change="handleCandidateSelect"
      >
        <el-table-column label="" width="44">
          <template #default="{ row }">
            <el-radio
              :model-value="selectedCandidate?.employeeId"
              :value="row.employeeId"
              :disabled="!canSelect(row)"
              @change="handleCandidateSelect(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="employeeName" label="姓名" width="90">
          <template #default="{ row }">{{ row.employeeName || `员工${row.employeeId}` }}</template>
        </el-table-column>
        <el-table-column label="工种" width="100">
          <template #default="{ row }">{{ row.positionName || '-' }}</template>
        </el-table-column>
        <el-table-column label="资质等级" width="100">
          <template #default="{ row }">{{ row.certificateName || '-' }}</template>
        </el-table-column>
        <el-table-column label="证书状态" width="110">
          <template #default="{ row }">
            <el-tag :type="certStatusType(row.validStatus)" size="small" effect="dark">
              {{ certStatusLabel(row.validStatus) }}
            </el-tag>
            <el-tooltip v-if="row.validStatus === 'EXPIRING'" content="证书即将到期，请尽快续期" placement="top">
              <el-icon :size="14" color="#e6a23c" style="margin-left:4px;cursor:help"><WarningFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="到期时间" width="110">
          <template #default="{ row }">{{ row.validTo ? formatDate(row.validTo) : '-' }}</template>
        </el-table-column>
        <el-table-column label="当前任务数" width="100">
          <template #default="{ row }">{{ row.currentTasks ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="匹配度" width="80">
          <template #default="{ row }">
            <el-tag :type="matchTagType(row)" size="small">{{ matchLabel(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否可选" width="80">
          <template #default="{ row }">
            <el-tag :type="canSelect(row) ? 'success' : 'danger'" size="small">
              {{ canSelect(row) ? '可选' : row.blockReason }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="检查结果" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.message }}</template>
        </el-table-column>
      </el-table>

      <el-divider />

      <!-- 分派表单 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-select v-model="form.leaderId" filterable clearable placeholder="选填" style="width:100%">
                <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="已选维修工">
              <el-input :model-value="selectedCandidateLabel" readonly placeholder="请在表格中选择" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分派角色">
              <el-select v-model="form.assignmentRole" placeholder="默认" style="width:100%">
                <el-option label="主维修工" value="MAINTAINER" />
                <el-option label="辅助工" value="ASSISTANT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计工时(h)">
              <el-input-number v-model="form.estimatedHours" :min="0" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 强制分派 -->
        <el-form-item v-if="selectedCandidate && !canSelect(selectedCandidate)" label="强制分派原因" prop="forceAssignReason">
          <el-input v-model="form.forceAssignReason" type="textarea" :rows="2" placeholder="所选人员不满足资质要求，如需强制分派请填写原因" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="派工备注" />
        </el-form-item>
      </el-form>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
    </template>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button
        type="primary"
        :loading="submitting"
        :disabled="!selectedCandidate"
        @click="handleSubmit"
      >
        确认派工
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { WarningFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { fetchQualificationCandidates, fetchEmployees, fetchEmployeeCertificates } from '@/api/qualification';
import AppState from '@/components/state/AppState.vue';
import type { CertificateVO, EmployeeVO, QualificationCheckVO, WorkOrderListItem } from '@/types/work-order';
import { formatDateTime } from '@/utils/date';

interface CandidateRow extends QualificationCheckVO {
  employeeName?: string;
  positionName?: string;
  certificateName?: string;
  validTo?: string;
  validStatus?: string;
  currentTasks?: number;
  blockReason?: string;
}

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    workOrder: WorkOrderListItem | null;
    userOptions: { label: string; value: number }[];
  }>(),
  { modelValue: false, workOrder: null, userOptions: () => [] }
);

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  assigned: [];
}>();

const formRef = ref();
const submitting = ref(false);
const errorMessage = ref('');
const loadingCandidates = ref(false);
const candidates = ref<CandidateRow[]>([]);
const selectedCandidate = ref<CandidateRow | null>(null);

const form = reactive({
  leaderId: '' as number | string,
  assignmentRole: 'MAINTAINER' as string,
  estimatedHours: undefined as number | undefined,
  forceAssignReason: '',
  remark: ''
});

const rules = {
  forceAssignReason: [
    {
      validator: (_rule: unknown, value: string, callback: (err?: Error) => void) => {
        if (selectedCandidate.value && !canSelect(selectedCandidate.value) && !value.trim()) {
          callback(new Error('强制分派必须填写原因'));
          return;
        }
        callback();
      },
      trigger: 'blur'
    }
  ]
};

/* ========== 候选人员加载 ========== */
async function loadCandidates() {
  if (!props.workOrder) return;
  loadingCandidates.value = true;
  candidates.value = [];
  selectedCandidate.value = null;

  try {
    const [checks, employees] = await Promise.all([
      fetchQualificationCandidates(props.workOrder.id),
      fetchEmployees()
    ]);

    // 为每个候选补充员工信息和证书状态
    const rows: CandidateRow[] = [];
    for (const check of checks) {
      const emp = employees.find((e: EmployeeVO) => e.id === check.employeeId);
      let certName = '';
      let validTo = '';
      let validStatus = '';

      try {
        const certs: CertificateVO[] = await fetchEmployeeCertificates(check.employeeId);
        const latestCert = certs[0];
        if (latestCert) {
          certName = latestCert.certificateName || '';
          validTo = latestCert.validTo || '';
          validStatus = latestCert.validStatus || '';
        }
      } catch {
        // 证书查询失败不影响候选人列表
      }

      const row: CandidateRow = {
        ...check,
        employeeName: emp?.realName || emp?.employeeNo || '',
        positionName: emp?.positionName || '',
        certificateName: certName,
        validTo,
        validStatus,
        currentTasks: undefined
      };
      rows.push(row);
    }

    candidates.value = rows;
  } finally {
    loadingCandidates.value = false;
  }
}

/* ========== 选择逻辑 ========== */
function canSelect(row: CandidateRow): boolean {
  if (row.checkResult === 'PASSED') return true;
  if (row.validStatus === 'EXPIRED' || row.validStatus === 'REVOKED') {
    row.blockReason = '证书已过期';
    return false;
  }
  if (row.checkResult === 'FAILED') {
    row.blockReason = '资质不符';
    return false;
  }
  return false;
}

const selectedCandidateLabel = computed(() => {
  const c = selectedCandidate.value;
  if (!c) return '';
  return `${c.employeeName || `员工${c.employeeId}`}${c.certificateName ? ` (${c.certificateName})` : ''}`;
});

function handleCandidateSelect(row: CandidateRow | null) {
  selectedCandidate.value = row;
}

/* ========== 显示标签 ========== */
function matchLabel(row: CandidateRow): string {
  if (row.checkResult === 'PASSED' && row.validStatus === 'VALID') return '符合';
  if (row.checkResult === 'PASSED' && row.validStatus === 'EXPIRING') return '临期';
  return '不符';
}

function matchTagType(row: CandidateRow): string {
  if (row.checkResult === 'PASSED' && row.validStatus === 'VALID') return 'success';
  if (row.checkResult === 'PASSED' && row.validStatus === 'EXPIRING') return 'warning';
  return 'danger';
}

function certStatusType(status?: string): string {
  switch (status) {
    case 'VALID': return 'success';
    case 'EXPIRING': return 'warning';
    case 'EXPIRED':
    case 'REVOKED': return 'danger';
    default: return 'info';
  }
}

function certStatusLabel(status?: string): string {
  switch (status) {
    case 'VALID': return '有效';
    case 'EXPIRING': return '即将到期';
    case 'EXPIRED': return '已过期';
    case 'REVOKED': return '已吊销';
    default: return status || '-';
  }
}

function formatDate(value?: string): string {
  if (!value) return '-';
  return value.slice(0, 10);
}

/* ========== 提交分派 ========== */
async function handleSubmit() {
  errorMessage.value = '';
  if (!selectedCandidate.value) {
    ElMessage.warning('请选择维修工');
    return;
  }
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  submitting.value = true;
  try {
    const { assignWorkOrder } = await import('@/api/admin');
    await assignWorkOrder(props.workOrder!.id, {
      leaderId: form.leaderId || undefined,
      maintainerId: selectedCandidate.value.employeeId,
      assignmentRole: form.assignmentRole || undefined,
      remark: [
        form.remark,
        form.estimatedHours ? `预计工时：${form.estimatedHours}h` : '',
        form.forceAssignReason ? `强制分派原因：${form.forceAssignReason}` : ''
      ].filter(Boolean).join('；') || undefined
    });
    ElMessage.success('分派成功，工单状态已变为已派工，移动端可同步该工单');
    emit('update:modelValue', false);
    emit('assigned');
  } catch (err: unknown) {
    errorMessage.value = err instanceof Error ? err.message : '分派失败';
  } finally {
    submitting.value = false;
  }
}

// 监听弹窗打开
import { watch } from 'vue';
watch(() => props.modelValue, (val) => {
  if (val) loadCandidates();
});
</script>

<style scoped>
.assign-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 8px;
}
</style>
