<template>
  <PageShell title="故障案例与维修工艺">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="故障案例" name="cases" />
      <el-tab-pane label="维修工艺" name="processes" />
    </el-tabs>

    <!-- ========== 故障案例 ========== -->
    <template v-if="activeTab === 'cases'">
      <div style="display:flex;gap:8px;align-items:center;margin-bottom:12px;flex-wrap:wrap">
        <el-button type="primary" :icon="Plus" @click="openCreateCase">新增案例</el-button>
        <el-input v-model="caseFilter.keyword" placeholder="搜索标题/故障类型" clearable style="width:220px" />
        <el-select v-model="caseFilter.equipmentType" clearable placeholder="设备类型" style="width:150px">
          <el-option v-for="t in caseEquipmentTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="caseFilter.faultType" clearable placeholder="故障类型" style="width:150px">
          <el-option v-for="t in caseFaultTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="caseFilter.workType" clearable placeholder="工单类型" style="width:130px">
          <el-option v-for="t in workTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </div>

      <DataTable :data="filteredCases" :loading="caseLoading" :total="filteredCases.length" :page-num="1" :page-size="filteredCases.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column prop="caseNo" label="编号" width="120" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="equipmentType" label="设备类型" width="110" />
        <el-table-column prop="faultType" label="故障类型" width="100" />
        <el-table-column prop="faultPhenomenon" label="故障现象" min-width="150" show-overflow-tooltip />
        <el-table-column label="关联工单" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.workType" size="small">{{ workTypeLabel(row.workType) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="来源工单" width="100">
          <template #default="{ row }">{{ row.sourceWorkOrderId || '-' }}</template>
        </el-table-column>
        <el-table-column label="启用" width="70">
          <template #default="{ row }">
            <el-tag :type="row.enabledFlag ? 'success' : 'info'" size="small">{{ row.enabledFlag ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="版本" width="60">
          <template #default="{ row }">v{{ row.version || 1 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openCaseDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openEditCase(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="confirmDeleteCase(row)">删除</el-button>
          </template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 维修工艺 ========== -->
    <template v-if="activeTab === 'processes'">
      <div style="display:flex;gap:8px;align-items:center;margin-bottom:12px;flex-wrap:wrap">
        <el-button type="primary" :icon="Plus" @click="openCreateProcess">新增工艺</el-button>
        <el-input v-model="processFilter.keyword" placeholder="搜索工艺名称/编号" clearable style="width:220px" />
        <el-select v-model="processFilter.equipmentType" clearable placeholder="设备类型" style="width:150px">
          <el-option v-for="t in processEquipmentTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="processFilter.workType" clearable placeholder="工单类型" style="width:130px">
          <el-option v-for="t in workTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </div>

      <DataTable :data="filteredProcesses" :loading="processLoading" :total="filteredProcesses.length" :page-num="1" :page-size="filteredProcesses.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column prop="processCode" label="工艺编号" width="120" show-overflow-tooltip />
        <el-table-column prop="processName" label="工艺名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="equipmentType" label="设备类型" width="110" />
        <el-table-column prop="processType" label="工艺类型" width="110" />
        <el-table-column label="关联工单" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.workType" size="small">{{ workTypeLabel(row.workType) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="durationEstimate" label="预估工时" width="90" />
        <el-table-column label="版本" width="60">
          <template #default="{ row }">v{{ row.version || 1 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openProcessDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openEditProcess(row)">编辑</el-button>
          </template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 案例详情弹窗 ========== -->
    <el-dialog v-model="caseDetailVisible" title="故障案例详情" width="780px" destroy-on-close>
      <template v-if="caseDetail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="编号">{{ caseDetail.caseNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ caseDetail.title }}</el-descriptions-item>
          <el-descriptions-item label="设备类型">{{ caseDetail.equipmentType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="故障类型">{{ caseDetail.faultType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联工单类型">{{ workTypeLabel(caseDetail.workType) }}</el-descriptions-item>
          <el-descriptions-item label="来源工单">{{ caseDetail.sourceWorkOrderId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="故障现象" :span="2">{{ caseDetail.faultPhenomenon || '-' }}</el-descriptions-item>
          <el-descriptions-item label="故障原因" :span="2">{{ caseDetail.faultCause || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理方法" :span="2">{{ caseDetail.solution || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预防措施" :span="2">{{ caseDetail.preventiveMeasures || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ caseDetail.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="版本">v{{ caseDetail.version || 1 }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- ========== 案例表单弹窗 ========== -->
    <FormDialog v-model="caseFormVisible" :title="caseEditingId ? '编辑故障案例' : '新增故障案例'" :confirm-loading="caseSubmitting" @confirm="submitCase">
      <el-form ref="caseFormRef" :model="caseForm" :rules="caseRules" label-width="100px" class="wo-form">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="编号" prop="caseNo"><el-input v-model="caseForm.caseNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="标题" prop="title"><el-input v-model="caseForm.title" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="设备类型"><el-input v-model="caseForm.equipmentType" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="故障类型"><el-input v-model="caseForm.faultType" /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="工单类型"><el-select v-model="caseForm.workType" clearable style="width:100%"><el-option v-for="t in workTypeOptions" :key="t.value" :label="t.label" :value="t.value" /></el-select></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="来源工单ID"><el-input-number v-model="caseForm.sourceWorkOrderId" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="故障现象"><el-input v-model="caseForm.faultPhenomenon" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="故障原因"><el-input v-model="caseForm.faultCause" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="处理方法"><el-input v-model="caseForm.solution" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="预防措施"><el-input v-model="caseForm.preventiveMeasures" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="附件ID"><el-input v-model="caseForm.attachmentIds" placeholder="逗号分隔的 fileId" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="6"><el-form-item label="启用"><el-switch v-model="caseForm.enabledFlag" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="caseForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
    </FormDialog>

    <!-- ========== 工艺详情弹窗 ========== -->
    <el-dialog v-model="processDetailVisible" title="维修工艺详情" width="780px" destroy-on-close>
      <template v-if="processDetail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="编号">{{ processDetail.processCode }}</el-descriptions-item>
          <el-descriptions-item label="名称">{{ processDetail.processName }}</el-descriptions-item>
          <el-descriptions-item label="设备类型">{{ processDetail.equipmentType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="工艺类型">{{ processDetail.processType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联工单类型">{{ workTypeLabel(processDetail.workType) }}</el-descriptions-item>
          <el-descriptions-item label="预估工时">{{ processDetail.durationEstimate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="工艺步骤" :span="2">{{ processDetail.processSteps || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所需工具" :span="2">{{ processDetail.toolsRequired || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所需物料" :span="2">{{ processDetail.materialRequired || '-' }}</el-descriptions-item>
          <el-descriptions-item label="安全措施" :span="2">{{ processDetail.safetyMeasures || '-' }}</el-descriptions-item>
          <el-descriptions-item label="质量标准" :span="2">{{ processDetail.qualityStandard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ processDetail.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="版本">v{{ processDetail.version || 1 }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- ========== 工艺表单弹窗 ========== -->
    <FormDialog v-model="processFormVisible" :title="processEditingId ? '编辑维修工艺' : '新增维修工艺'" :confirm-loading="processSubmitting" @confirm="submitProcess">
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px" class="wo-form">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="工艺编号" prop="processCode"><el-input v-model="processForm.processCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工艺名称" prop="processName"><el-input v-model="processForm.processName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="设备类型"><el-input v-model="processForm.equipmentType" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="工艺类型"><el-input v-model="processForm.processType" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="预估工时"><el-input v-model="processForm.durationEstimate" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="工单类型"><el-select v-model="processForm.workType" clearable style="width:100%"><el-option v-for="t in workTypeOptions" :key="t.value" :label="t.label" :value="t.value" /></el-select></el-form-item>
        <el-form-item label="工艺步骤"><el-input v-model="processForm.processSteps" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="所需工具"><el-input v-model="processForm.toolsRequired" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="所需物料"><el-input v-model="processForm.materialRequired" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="安全措施"><el-input v-model="processForm.safetyMeasures" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="质量标准"><el-input v-model="processForm.qualityStandard" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="processForm.enabledFlag" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="processForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
    </FormDialog>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  fetchKnowledgeCases, createKnowledgeCase, updateKnowledgeCase, deleteKnowledgeCase,
  fetchMaintenanceProcesses, createMaintenanceProcess, updateMaintenanceProcess,
  type KnowledgeCaseItem, type MaintenanceProcessItem
} from '@/api/knowledge';
import PageShell from '@/components/page/PageShell.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormDialog from '@/components/dialog/FormDialog.vue';
import { workTypeOptions } from '@/constants/enums';

/* ========== Tab ========== */
const activeTab = ref('cases');

/* ========== 工具函数 ========== */
function workTypeLabel(t?: string) { return workTypeOptions.find((o) => o.value === t)?.label || t || '-'; }

/* ========== 故障案例 ========== */
const caseLoading = ref(false);
const caseList = ref<KnowledgeCaseItem[]>([]);
const caseFilter = reactive({ keyword: '', equipmentType: '', faultType: '', workType: '' });

const caseEquipmentTypes = computed(() => [...new Set(caseList.value.map((c) => c.equipmentType || '').filter(Boolean))]);
const caseFaultTypes = computed(() => [...new Set(caseList.value.map((c) => c.faultType || '').filter(Boolean))]);

const filteredCases = computed(() => caseList.value.filter((c) => {
  if (caseFilter.keyword && !`${c.title}${c.faultType || ''}${c.caseNo || ''}`.toLowerCase().includes(caseFilter.keyword.toLowerCase())) return false;
  if (caseFilter.equipmentType && c.equipmentType !== caseFilter.equipmentType) return false;
  if (caseFilter.faultType && c.faultType !== caseFilter.faultType) return false;
  if (caseFilter.workType && c.workType !== caseFilter.workType) return false;
  return true;
}));

async function loadCases() { caseLoading.value = true; try { caseList.value = await fetchKnowledgeCases(); } finally { caseLoading.value = false; } }
loadCases();

/* ========== 案例表单 ========== */
const caseDetailVisible = ref(false); const caseDetail = ref<KnowledgeCaseItem | null>(null);
const caseFormVisible = ref(false); const caseSubmitting = ref(false); const caseEditingId = ref<number | undefined>();
const caseFormRef = ref();
const caseForm = reactive<Record<string, unknown>>({ caseNo: '', title: '', equipmentType: '', faultType: '', faultPhenomenon: '', faultCause: '', solution: '', preventiveMeasures: '', workType: '', sourceWorkOrderId: undefined, attachmentIds: '', enabledFlag: 1, remark: '' });
const caseRules = { caseNo: [{ required: true, message: '编号必填' }], title: [{ required: true, message: '标题必填' }] };

function resetCaseForm() { Object.assign(caseForm, { caseNo: '', title: '', equipmentType: '', faultType: '', faultPhenomenon: '', faultCause: '', solution: '', preventiveMeasures: '', workType: '', sourceWorkOrderId: undefined, attachmentIds: '', enabledFlag: 1, remark: '' }); }
function openCreateCase() { caseEditingId.value = undefined; resetCaseForm(); caseFormVisible.value = true; }
function openEditCase(row: KnowledgeCaseItem) { caseEditingId.value = row.id; Object.assign(caseForm, row); caseFormVisible.value = true; }
function openCaseDetail(row: KnowledgeCaseItem) { caseDetail.value = row; caseDetailVisible.value = true; }

async function submitCase() {
  try { await caseFormRef.value?.validate(); } catch { return; }
  caseSubmitting.value = true;
  try {
    if (caseEditingId.value) await updateKnowledgeCase(caseEditingId.value, caseForm); else await createKnowledgeCase(caseForm);
    ElMessage.success(caseEditingId.value ? '案例更新成功' : '案例创建成功'); caseFormVisible.value = false; loadCases();
  } finally { caseSubmitting.value = false; }
}
async function confirmDeleteCase(row: KnowledgeCaseItem) {
  try { await ElMessageBox.confirm(`确认删除案例「${row.title}」？`, '删除案例', { type: 'error' }); await deleteKnowledgeCase(row.id); ElMessage.success('案例已删除'); loadCases(); } catch { /* 取消 */ }
}

/* ========== 维修工艺 ========== */
const processLoading = ref(false);
const processList = ref<MaintenanceProcessItem[]>([]);
const processFilter = reactive({ keyword: '', equipmentType: '', workType: '' });
const processEquipmentTypes = computed(() => [...new Set(processList.value.map((p) => p.equipmentType || '').filter(Boolean))]);
const filteredProcesses = computed(() => processList.value.filter((p) => {
  if (processFilter.keyword && !`${p.processName}${p.processCode || ''}`.toLowerCase().includes(processFilter.keyword.toLowerCase())) return false;
  if (processFilter.equipmentType && p.equipmentType !== processFilter.equipmentType) return false;
  if (processFilter.workType && p.workType !== processFilter.workType) return false;
  return true;
}));

async function loadProcesses() { processLoading.value = true; try { processList.value = await fetchMaintenanceProcesses(); } finally { processLoading.value = false; } }
loadProcesses();

/* ========== 工艺表单 ========== */
const processDetailVisible = ref(false); const processDetail = ref<MaintenanceProcessItem | null>(null);
const processFormVisible = ref(false); const processSubmitting = ref(false); const processEditingId = ref<number | undefined>();
const processFormRef = ref();
const processForm = reactive<Record<string, unknown>>({ processCode: '', processName: '', equipmentType: '', processType: '', processSteps: '', toolsRequired: '', materialRequired: '', safetyMeasures: '', qualityStandard: '', durationEstimate: '', workType: '', enabledFlag: 1, remark: '' });
const processRules = { processCode: [{ required: true, message: '编号必填' }], processName: [{ required: true, message: '名称必填' }] };

function resetProcessForm() { Object.assign(processForm, { processCode: '', processName: '', equipmentType: '', processType: '', processSteps: '', toolsRequired: '', materialRequired: '', safetyMeasures: '', qualityStandard: '', durationEstimate: '', workType: '', enabledFlag: 1, remark: '' }); }
function openCreateProcess() { processEditingId.value = undefined; resetProcessForm(); processFormVisible.value = true; }
function openEditProcess(row: MaintenanceProcessItem) { processEditingId.value = row.id; Object.assign(processForm, row); processFormVisible.value = true; }
function openProcessDetail(row: MaintenanceProcessItem) { processDetail.value = row; processDetailVisible.value = true; }

async function submitProcess() {
  try { await processFormRef.value?.validate(); } catch { return; }
  processSubmitting.value = true;
  try {
    if (processEditingId.value) await updateMaintenanceProcess(processEditingId.value, processForm); else await createMaintenanceProcess(processForm);
    ElMessage.success(processEditingId.value ? '工艺更新成功' : '工艺创建成功'); processFormVisible.value = false; loadProcesses();
  } finally { processSubmitting.value = false; }
}
</script>

<style scoped>
.wo-form { max-height: 500px; overflow-y: auto; padding-right: 4px; }
</style>
