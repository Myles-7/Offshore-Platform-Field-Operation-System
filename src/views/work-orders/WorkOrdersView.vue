<template>
  <PageShell title="工单管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增工单</el-button>
      <el-button link type="primary" :icon="Document" @click="openCreateFromTemplate">从模板创建</el-button>
      <el-button link type="primary" :icon="Setting" @click="$router.push('/work-orders/templates')">模板管理</el-button>
    </template>

    <!-- 筛选区 -->
    <SearchForm :model="query" @search="handleSearch" @reset="handleReset">
      <el-form-item label="工单编号">
        <el-input v-model="query.workOrderNo" clearable placeholder="输入工单号" style="width:160px" />
      </el-form-item>
      <el-form-item label="项目名称">
        <el-select v-model="query.projectId" clearable placeholder="全部项目" style="width:160px">
          <el-option v-for="p in projectOptions" :key="p.value" :label="p.label" :value="p.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="工单类型">
        <el-select v-model="query.workType" clearable placeholder="全部类型" style="width:130px">
          <el-option v-for="item in workTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width:130px">
          <el-option v-for="item in workOrderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级">
        <el-select v-model="query.priority" clearable placeholder="全部" style="width:120px">
          <el-option v-for="item in priorityStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <!-- 高级搜索 -->
    <el-collapse v-model="advancedOpen">
      <el-collapse-item title="高级搜索" name="advanced">
        <SearchForm :model="query" @search="handleSearch" @reset="handleReset">
          <el-form-item label="作业地点">
            <el-input v-model="query.workLocation" clearable placeholder="输入作业地点" style="width:160px" />
          </el-form-item>
          <el-form-item label="负责人">
            <el-select v-model="query.leaderId" clearable filterable placeholder="全部" style="width:140px">
              <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="维修工">
            <el-select v-model="query.maintainerId" clearable filterable placeholder="全部" style="width:140px">
              <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="query.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width:260px"
            />
          </el-form-item>
          <el-form-item label="同步状态">
            <el-select v-model="query.syncStatus" clearable placeholder="全部" style="width:130px">
              <el-option v-for="item in syncStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="是否异常">
            <el-select v-model="query.abnormalFlag" clearable placeholder="全部" style="width:110px">
              <el-option label="是" :value="1" />
              <el-option label="否" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="是否待验收">
            <el-select v-model="query.pendingAcceptance" clearable placeholder="全部" style="width:110px">
              <el-option label="是" :value="1" />
              <el-option label="否" :value="0" />
            </el-select>
          </el-form-item>
        </SearchForm>
      </el-collapse-item>
    </el-collapse>

    <!-- 表格 -->
    <DataTable
      v-model:page-num="page.pageNum"
      v-model:page-size="page.pageSize"
      :data="records"
      :loading="loading"
      :total="total"
      @update:page-num="loadData"
      @update:page-size="loadData"
    >
      <el-table-column type="index" label="序号" width="55" />
      <el-table-column prop="workOrderNo" label="工单编号" min-width="170" fixed="left">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:6px">
            <span>{{ row.workOrderNo }}</span>
            <el-tooltip v-if="isConflict(row)" content="此工单存在同步冲突，请及时处理" placement="top">
              <el-icon :size="16" color="#f56c6c"><WarningFilled /></el-icon>
            </el-tooltip>
            <el-tag v-if="row.abnormalFlag" type="danger" size="small" effect="dark">异常</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="projectName" label="项目名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="workLocation" label="作业地点" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.workLocation || '-' }}</template>
      </el-table-column>
      <el-table-column label="工单类型" width="110">
        <template #default="{ row }">{{ workTypeLabel(row.workType) }}</template>
      </el-table-column>
      <el-table-column label="工单状态" width="100">
        <template #default="{ row }"><StatusTag :value="row.status" enum-type="workOrder" /></template>
      </el-table-column>
      <el-table-column label="优先级" width="90">
        <template #default="{ row }"><StatusTag :value="row.priority" enum-type="priority" /></template>
      </el-table-column>
      <el-table-column label="负责人" width="100">
        <template #default="{ row }">{{ row.leaderName || '-' }}</template>
      </el-table-column>
      <el-table-column label="维修工" width="100">
        <template #default="{ row }">{{ row.maintainerName || '-' }}</template>
      </el-table-column>
      <el-table-column label="计划开始" width="160">
        <template #default="{ row }">{{ formatDateTime(row.plannedStartTime) }}</template>
      </el-table-column>
      <el-table-column label="计划结束" width="160">
        <template #default="{ row }">{{ formatDateTime(row.plannedEndTime) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="同步状态" width="110">
        <template #default="{ row }"><StatusTag :value="row.syncStatus" enum-type="sync" /></template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDetail(row)">查看</el-button>
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link type="primary" size="small" @click="openAssign(row)">分派</el-button>
          <el-dropdown trigger="click" @command="(cmd: string) => handleRowCommand(cmd, row)">
            <el-button link type="primary" size="small">更多<el-icon><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="status">修改状态</el-dropdown-item>
                <el-dropdown-item command="attachments">查看附件</el-dropdown-item>
                <el-dropdown-item command="pdf">查看PDF</el-dropdown-item>
                <el-dropdown-item v-if="row.status !== 'CLOSED'" command="close" divided>关闭工单</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </DataTable>

    <!-- 新增 / 编辑工单弹窗 -->
    <WorkOrderFormDialog
      ref="createDialogRef"
      v-model="createDialogVisible"
      :title="editingId ? '编辑工单' : '新增工单'"
      :initial-values="editInitialValues"
      :project-options="projectOptions"
      :user-options="userOptions"
      :material-options="materialOptions"
      @submit="handleFormSubmit"
    />

    <!-- 根据模板创建工单弹窗 -->
    <WorkOrderFromTemplateDialog
      ref="fromTemplateDialogRef"
      v-model="fromTemplateDialogVisible"
      :template-options="templateOptions"
      :project-options="projectOptions"
      :user-options="userOptions"
      @submit="handleTemplateSubmit"
    />

    <!-- 分派工单弹窗 -->
    <WorkOrderAssignDialog
      v-if="assignWorkOrderRecord"
      v-model="assignDialogVisible"
      :work-order="assignWorkOrderRecord"
      :user-options="userOptions"
      @assigned="loadData"
    />

    <!-- 修改状态弹窗 -->
    <FormDialog v-model="statusDialogVisible" title="修改工单状态" :confirm-loading="statusSubmitting" @confirm="submitStatusChange">
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="当前状态">
          <StatusTag v-if="activeWorkOrder" :value="activeWorkOrder.status" enum-type="workOrder" />
        </el-form-item>
        <el-form-item label="目标状态">
          <el-select v-model="statusForm.status" placeholder="请选择目标状态" style="width:100%">
            <el-option v-for="item in allowedTransitions(activeWorkOrder?.status)" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="statusForm.status === 'REJECTED'" label="驳回原因">
          <el-input v-model="statusForm.rejectReason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item v-if="statusForm.status === 'CLOSED'" label="关闭原因">
          <el-input v-model="statusForm.closeReason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="操作说明">
          <el-input v-model="statusForm.operationDesc" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
    </FormDialog>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowDown, Document, Plus, Setting, WarningFilled } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  changeWorkOrderStatus,
  createWorkOrder,
  createWorkOrderFromTemplate,
  deleteWorkOrder,
  fetchTemplates,
  fetchWorkOrders,
  updateWorkOrder
} from '@/api/admin';
import { fetchProjectStatistics } from '@/api/dashboard';
import { fetchMaterials } from '@/api/material';
import { fetchEmployees } from '@/api/employee';
import { fetchAllUsers, type AdminUserItem } from '@/api/system';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import FormDialog from '@/components/dialog/FormDialog.vue';
import WorkOrderFormDialog from '@/components/form/WorkOrderFormDialog.vue';
import WorkOrderFromTemplateDialog from '@/components/form/WorkOrderFromTemplateDialog.vue';
import WorkOrderAssignDialog from '@/components/form/WorkOrderAssignDialog.vue';
import { syncStatusOptions, workOrderStatusOptions, priorityStatusOptions, workTypeOptions } from '@/constants/enums';
import { defaultPageParams } from '@/constants/pagination';
import type { WorkOrderTemplateItem } from '@/types/template';
import type { MaterialItem } from '@/types/material';
import type { WorkOrderListItem, WorkOrderQueryParams, WorkOrderStatusRequest } from '@/types/work-order';
import { formatDateTime } from '@/utils/date';

const route = useRoute();
const router = useRouter();

/* ========== 数据和分页 ========== */
const loading = ref(false);
const records = ref<WorkOrderListItem[]>([]);
const total = ref(0);
const page = reactive({ ...defaultPageParams });

/* ========== 筛选参数 ========== */
const query = reactive<{
  workOrderNo: string;
  projectId: number | '';
  workType: string;
  workLocation: string;
  status: string;
  priority: string;
  leaderId: number | '';
  maintainerId: number | '';
  dateRange: [string, string] | null;
  syncStatus: string;
  abnormalFlag: number | '';
  pendingAcceptance: number | '';
}>({
  workOrderNo: '',
  projectId: '',
  workType: '',
  workLocation: '',
  status: '',
  priority: '',
  leaderId: '',
  maintainerId: '',
  dateRange: null,
  syncStatus: '',
  abnormalFlag: '',
  pendingAcceptance: ''
});

const advancedOpen = ref<string[]>([]);

/* ========== 选项数据 ========== */
const projectOptions = ref<{ label: string; value: number }[]>([]);
const userOptions = ref<{ label: string; value: number }[]>([]);
const materialOptions = ref<{ label: string; value: number }[]>([]);
const templateOptions = ref<{ label: string; value: number; data: WorkOrderTemplateItem }[]>([]);

async function loadProjectOptions() {
  try {
    const list = await fetchProjectStatistics();
    projectOptions.value = list.map((p) => ({ label: p.projectName ?? `项目${p.projectId}`, value: p.projectId }));
  } catch { /* 非关键 */ }
}

async function loadUserOptions() {
  try {
    const result = await fetchAllUsers();
    userOptions.value = (result.records ?? []).map((u: AdminUserItem) => ({
      label: `${u.realName} (${u.username})`,
      value: u.id
    }));
    // 同时加载员工列表作为补充
    const employees = await fetchEmployees();
    for (const emp of employees) {
      if (!userOptions.value.some((u) => u.value === emp.id)) {
        userOptions.value.push({ label: emp.realName || `员工${emp.id}`, value: emp.id });
      }
    }
  } catch { /* 非关键 */ }
}

async function loadMaterialOptions() {
  try {
    const list: MaterialItem[] = await fetchMaterials();
    materialOptions.value = list.map((m) => ({ label: `${m.materialName} (${m.materialCode})`, value: m.id }));
  } catch { /* 非关键 */ }
}

async function loadTemplateOptions() {
  try {
    const list = await fetchTemplates();
    templateOptions.value = list.map((t) => ({
      label: `${t.templateName} (${t.templateCode})`,
      value: t.id,
      data: t
    }));
  } catch { /* 非关键 */ }
}

/* ========== 数据加载 ========== */
function buildParams(): WorkOrderQueryParams {
  const params: WorkOrderQueryParams = { pageNum: page.pageNum, pageSize: page.pageSize };
  if (query.workOrderNo) params.workOrderNo = query.workOrderNo;
  if (query.projectId) params.projectId = Number(query.projectId);
  if (query.workType) params.workType = query.workType;
  if (query.workLocation) params.workLocation = query.workLocation;
  if (query.status) params.status = query.status;
  if (query.priority) params.priority = query.priority;
  if (query.leaderId) params.leaderId = Number(query.leaderId);
  if (query.maintainerId) params.maintainerId = Number(query.maintainerId);
  if (query.syncStatus) params.syncStatus = query.syncStatus;
  if (query.abnormalFlag !== '' && query.abnormalFlag !== null) params.abnormalFlag = Number(query.abnormalFlag);
  if (query.dateRange) {
    params.plannedStartTimeStart = `${query.dateRange[0]} 00:00:00`;
    params.plannedStartTimeEnd = `${query.dateRange[1]} 23:59:59`;
  }
  if (Number(query.pendingAcceptance) === 1) params.status = 'PENDING_ACCEPTANCE';
  return params;
}

async function loadData() {
  loading.value = true;
  try {
    const result = await fetchWorkOrders(buildParams());
    records.value = result.records ?? [];
    total.value = Number(result.total) || 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() { page.pageNum = 1; loadData(); }

function handleReset() {
  Object.assign(query, {
    workOrderNo: '', projectId: '', workType: '', workLocation: '',
    status: '', priority: '', leaderId: '', maintainerId: '',
    dateRange: null, syncStatus: '', abnormalFlag: '', pendingAcceptance: ''
  });
  page.pageNum = 1;
  loadData();
}

/* ========== 首页带条件跳转 ========== */
function applyIncomingQuery() {
  const q = route.query;
  if (q.status) query.status = String(q.status);
  if (q.projectId) query.projectId = Number(q.projectId);
  if (q.keyword) query.workOrderNo = String(q.keyword);
  if (Object.keys(q).length > 0) loadData();
}

/* ========== 冲突判断 ========== */
function isConflict(row: WorkOrderListItem): boolean { return row.syncStatus === 'CONFLICT'; }

/* ========== 工单类型标签 ========== */
function workTypeLabel(type?: string): string {
  return workTypeOptions.find((o) => o.value === type)?.label ?? (type || '-');
}

/* ========== 新增 / 编辑工单 ========== */
const createDialogVisible = ref(false);
const createDialogRef = ref<InstanceType<typeof WorkOrderFormDialog>>();
const editingId = ref<number | undefined>();
const editInitialValues = ref<Record<string, unknown>>({});

function openCreate() {
  editingId.value = undefined;
  editInitialValues.value = {};
  createDialogVisible.value = true;
}

function openEdit(row: WorkOrderListItem) {
  editingId.value = row.id;
  editInitialValues.value = {
    projectId: row.projectId,
    workTitle: row.workTitle,
    workType: row.workType,
    workLocation: row.workLocation,
    workContent: row.workContent,
    leaderId: row.leaderId,
    maintainerId: row.maintainerId,
    plannedStartTime: row.plannedStartTime,
    plannedEndTime: row.plannedEndTime,
    priority: row.priority,
    requiredMaterialDesc: row.requiredMaterialDesc,
    remark: row.remark
  };
  createDialogVisible.value = true;
}

async function handleFormSubmit(data: Record<string, unknown>) {
  try {
    if (editingId.value) {
      await updateWorkOrder(editingId.value, data);
      ElMessage.success('工单更新成功');
      createDialogVisible.value = false;
    } else {
      await createWorkOrder(data);
      ElMessage.success('工单创建成功，移动端可同步该工单');
      createDialogVisible.value = false;
    }
    loadData();
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : '保存失败';
    createDialogRef.value?.setError(message);
  }
}

/* ========== 分派工单 ========== */
const assignDialogVisible = ref(false);
const assignWorkOrderRecord = ref<WorkOrderListItem | null>(null);

/* ========== 根据模板创建工单 ========== */
const fromTemplateDialogVisible = ref(false);
const fromTemplateDialogRef = ref<InstanceType<typeof WorkOrderFromTemplateDialog>>();

function openCreateFromTemplate() {
  fromTemplateDialogVisible.value = true;
}

async function handleTemplateSubmit(templateId: number, data: Record<string, unknown>) {
  try {
    await createWorkOrderFromTemplate(templateId, data);
    ElMessage.success('工单创建成功，移动端可同步该工单');
    fromTemplateDialogVisible.value = false;
    loadData();
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : '创建失败';
    fromTemplateDialogRef.value?.setError(message);
  }
}

/* ========== 操作按钮 ========== */
function openDetail(row: WorkOrderListItem) { router.push(`/work-orders/${row.id}`); }
function openAssign(row: WorkOrderListItem) {
  assignWorkOrderRecord.value = row;
  assignDialogVisible.value = true;
}

function handleRowCommand(command: string, row: WorkOrderListItem) {
  switch (command) {
    case 'status': openStatusDialog(row); break;
    case 'attachments': ElMessage.info(`查看附件：${row.workOrderNo}`); break;
    case 'pdf': ElMessage.info(`查看PDF：${row.workOrderNo}`); break;
    case 'close': confirmClose(row); break;
    case 'delete': confirmDelete(row); break;
  }
}

/* ========== 修改状态弹窗 ========== */
const statusDialogVisible = ref(false);
const statusSubmitting = ref(false);
const activeWorkOrder = ref<WorkOrderListItem | null>(null);
const statusForm = reactive<WorkOrderStatusRequest>({ status: '', operationDesc: '' });

const transitionMap: Record<string, { label: string; value: string }[]> = {
  PENDING_ASSIGN: [{ label: '已派工', value: 'ASSIGNED' }, { label: '已关闭', value: 'CLOSED' }],
  ASSIGNED: [{ label: '施工中', value: 'IN_PROGRESS' }, { label: '已驳回', value: 'REJECTED' }, { label: '已关闭', value: 'CLOSED' }],
  IN_PROGRESS: [{ label: '待验收', value: 'PENDING_ACCEPTANCE' }, { label: '已驳回', value: 'REJECTED' }, { label: '已关闭', value: 'CLOSED' }],
  PENDING_ACCEPTANCE: [{ label: '已完成', value: 'COMPLETED' }, { label: '已驳回', value: 'REJECTED' }],
  COMPLETED: [{ label: '已关闭', value: 'CLOSED' }],
  REJECTED: [{ label: '施工中', value: 'IN_PROGRESS' }, { label: '已关闭', value: 'CLOSED' }],
  ACCEPTED: [{ label: '施工中', value: 'IN_PROGRESS' }, { label: '已关闭', value: 'CLOSED' }]
};

function allowedTransitions(currentStatus?: string) { return currentStatus ? transitionMap[currentStatus] ?? [] : []; }

function openStatusDialog(row: WorkOrderListItem) {
  activeWorkOrder.value = row;
  statusForm.status = '';
  statusForm.operationDesc = '';
  statusForm.rejectReason = '';
  statusForm.closeReason = '';
  statusDialogVisible.value = true;
}

async function submitStatusChange() {
  if (!activeWorkOrder.value || !statusForm.status) { ElMessage.warning('请选择目标状态'); return; }
  statusSubmitting.value = true;
  try {
    await changeWorkOrderStatus(activeWorkOrder.value.id, {
      status: statusForm.status,
      operationDesc: statusForm.operationDesc || undefined,
      rejectReason: statusForm.status === 'REJECTED' ? statusForm.rejectReason || undefined : undefined,
      closeReason: statusForm.status === 'CLOSED' ? statusForm.closeReason || undefined : undefined
    });
    ElMessage.success('状态修改成功');
    statusDialogVisible.value = false;
    loadData();
  } finally { statusSubmitting.value = false; }
}

/* ========== 关闭工单 ========== */
async function confirmClose(row: WorkOrderListItem) {
  try {
    await ElMessageBox.confirm(
      `确认关闭工单「${row.workOrderNo}」？关闭后工单不再流转。`,
      '关闭工单',
      { confirmButtonText: '确认关闭', cancelButtonText: '取消', type: 'warning' }
    );
    await changeWorkOrderStatus(row.id, { status: 'CLOSED', operationDesc: 'PC后台关闭' });
    ElMessage.success('工单已关闭');
    loadData();
  } catch { /* 用户取消 */ }
}

/* ========== 删除工单 ========== */
async function confirmDelete(row: WorkOrderListItem) {
  try {
    await ElMessageBox.confirm(
      `确认删除工单「${row.workOrderNo}」？删除后不可恢复。`,
      '删除工单',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    );
    await deleteWorkOrder(row.id);
    ElMessage.success('工单已删除');
    loadData();
  } catch { /* 用户取消 */ }
}

/* ========== 生命周期 ========== */
onMounted(() => {
  loadProjectOptions();
  loadUserOptions();
  loadMaterialOptions();
  loadTemplateOptions();
  loadData();
  applyIncomingQuery();
});
</script>
