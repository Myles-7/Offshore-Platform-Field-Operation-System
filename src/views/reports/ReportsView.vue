<template>
  <PageShell title="经营报表">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="对账单" name="reconciliation" />
      <el-tab-pane label="工单产值" name="workOrders" />
      <el-tab-pane label="物料消耗" name="materials" />
    </el-tabs>

    <!-- ========== 对账单 ========== -->
    <template v-if="activeTab === 'reconciliation'">
      <SearchForm :model="reconFilters" @search="loadReconciliation" @reset="resetReconFilters">
        <el-form-item label="项目">
          <el-select v-model="reconFilters.projectId" clearable filterable placeholder="全部" style="width:180px">
            <el-option v-for="p in projectOptions" :key="p.value" :label="p.label" :value="p.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="reconFilters.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width:260px" />
        </el-form-item>
        <el-form-item label="工单状态">
          <el-select v-model="reconFilters.status" clearable placeholder="全部" style="width:120px">
            <el-option v-for="s in workOrderStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
      </SearchForm>

      <!-- 导出前预览统计 -->
      <div v-if="reconList.length" class="recon-summary">
        <span>共 <strong>{{ reconList.length }}</strong> 个项目</span>
        <span>工单总数：<strong>{{ reconTotalWorkOrders }}</strong></span>
        <span>已完成：<strong>{{ reconTotalCompleted }}</strong></span>
        <span>总产值：<strong>{{ reconTotalValue }}</strong></span>
        <el-button type="primary" size="small" :icon="Download" @click="exportReconciliation">导出 Excel</el-button>
      </div>

      <DataTable :data="filteredRecon" :loading="reconLoading" :total="filteredRecon.length" :page-num="1" :page-size="filteredRecon.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column label="项目名称" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.projectName || '-' }}</template>
        </el-table-column>
        <el-table-column label="工单总数" width="90">
          <template #default="{ row }">{{ row.workOrderTotal || 0 }}</template>
        </el-table-column>
        <el-table-column label="已完成" width="80">
          <template #default="{ row }">{{ row.completed || 0 }}</template>
        </el-table-column>
        <el-table-column label="物料金额" width="120">
          <template #default="{ row }">{{ formatMoney(row.materialAmount) }}</template>
        </el-table-column>
        <el-table-column label="完工产值" width="120">
          <template #default="{ row }">{{ formatMoney(row.outputValue) }}</template>
        </el-table-column>
      </DataTable>

      <el-alert v-if="exportError" :title="exportError" type="error" show-icon :closable="false" style="margin-top:8px" />
    </template>

    <!-- ========== 工单产值 ========== -->
    <template v-if="activeTab === 'workOrders'">
      <SearchForm :model="woFilters" @search="loadWorkOrderReport" @reset="resetWoFilters">
        <el-form-item label="项目">
          <el-select v-model="woFilters.projectId" clearable filterable placeholder="全部" style="width:180px">
            <el-option v-for="p in projectOptions" :key="p.value" :label="p.label" :value="p.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="woFilters.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width:260px" />
        </el-form-item>
        <el-form-item label="人员">
          <el-select v-model="woFilters.userId" clearable filterable placeholder="全部" style="width:140px">
            <el-option v-for="u in personOptions" :key="u.value" :label="u.label" :value="u.value" />
          </el-select>
        </el-form-item>
      </SearchForm>

      <DataTable :data="outputValueList" :loading="outputLoading" :total="outputValueList.length" :page-num="1" :page-size="outputValueList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column label="日期" width="110">
          <template #default="{ row }">{{ row.summaryDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="项目" min-width="140">
          <template #default="{ row }">{{ row.projectName || '-' }}</template>
        </el-table-column>
        <el-table-column label="完工产值" width="130">
          <template #default="{ row }">{{ formatMoney(row.outputValue) }}</template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 物料消耗 ========== -->
    <template v-if="activeTab === 'materials'">
      <SearchForm :model="matFilters" @search="loadMaterialReport" @reset="resetMatFilters">
        <el-form-item label="项目">
          <el-select v-model="matFilters.projectId" clearable filterable placeholder="全部" style="width:180px">
            <el-option v-for="p in projectOptions" :key="p.value" :label="p.label" :value="p.value" />
          </el-select>
        </el-form-item>
      </SearchForm>

      <DataTable :data="materialStats" :loading="matLoading" :total="materialStats.length" :page-num="1" :page-size="materialStats.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column label="物料名称" min-width="140">
          <template #default="{ row }">{{ row.materialName || row.material_code || '-' }}</template>
        </el-table-column>
        <el-table-column label="使用数量" width="100">
          <template #default="{ row }">{{ row.usedQty || row.used_qty || row.value || '-' }}</template>
        </el-table-column>
        <el-table-column label="项目" min-width="140">
          <template #default="{ row }">{{ row.projectName || row.project_name || '-' }}</template>
        </el-table-column>
      </DataTable>
    </template>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { Download } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchProjectStatistics } from '@/api/dashboard';
import { request } from '@/api/request';
import { fetchAllUsers } from '@/api/system';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import { workOrderStatusOptions } from '@/constants/enums';

/* ========== Tab ========== */
const activeTab = ref('reconciliation');

/* ========== 项目选项 ========== */
const projectOptions = ref<{ label: string; value: number }[]>([]);
const personOptions = ref<{ label: string; value: number }[]>([]);

onMounted(async () => {
  try { const list = await fetchProjectStatistics(); projectOptions.value = list.map((p) => ({ label: p.projectName || `项目${p.projectId}`, value: p.projectId })); } catch { /* */ }
  try { const res = await fetchAllUsers(); personOptions.value = (res.records || []).map((u: { id: number; realName: string; username: string }) => ({ label: `${u.realName} (${u.username})`, value: u.id })); } catch { /* */ }
});

/* ========== 对账单 ========== */
const reconLoading = ref(false);
const reconList = ref<Record<string, unknown>[]>([]);
const reconFilters = reactive({ projectId: '' as number | string, status: '', dateRange: null as [string, string] | null });
const exportError = ref('');

const filteredRecon = computed(() => {
  let list = reconList.value;
  if (reconFilters.projectId) list = list.filter((r) => r.projectId == reconFilters.projectId);
  return list;
});
const reconTotalWorkOrders = computed(() => filteredRecon.value.reduce((s, r) => s + Number(r.workOrderTotal || 0), 0));
const reconTotalCompleted = computed(() => filteredRecon.value.reduce((s, r) => s + Number(r.completed || 0), 0));
const reconTotalValue = computed(() => formatMoney(filteredRecon.value.reduce((s, r) => s + Number(r.outputValue || 0), 0)));

async function loadReconciliation() {
  reconLoading.value = true;
  try { reconList.value = await request({ url: '/admin/reports/reconciliation', method: 'GET' }) as Record<string, unknown>[]; } finally { reconLoading.value = false; }
}
function resetReconFilters() { reconFilters.projectId = ''; reconFilters.status = ''; reconFilters.dateRange = null; }

async function exportReconciliation() {
  exportError.value = '';
  if (!filteredRecon.value.length) {
    try { await ElMessageBox.confirm('无数据可导出，是否仍要继续？', '空数据导出', { type: 'warning' }); } catch { return; }
  }
  try {
    window.open('/api/admin/reports/reconciliation/export', '_blank');
    ElMessage.success('导出已开始，操作日志已记录');
  } catch { exportError.value = '导出失败'; }
}
loadReconciliation();

/* ========== 工单产值 ========== */
const outputLoading = ref(false);
const outputValueList = ref<Record<string, unknown>[]>([]);
const woFilters = reactive({ projectId: '' as number | string, userId: '' as number | string, dateRange: null as [string, string] | null });

async function loadWorkOrderReport() { outputLoading.value = true; try { outputValueList.value = await request({ url: '/admin/dashboard/output-value', method: 'GET' }) as Record<string, unknown>[]; } finally { outputLoading.value = false; } }
function resetWoFilters() { woFilters.projectId = ''; woFilters.userId = ''; woFilters.dateRange = null; }
loadWorkOrderReport();

/* ========== 物料消耗 ========== */
const matLoading = ref(false);
const materialStats = ref<Record<string, unknown>[]>([]);
const matFilters = reactive({ projectId: '' as number | string });

async function loadMaterialReport() { matLoading.value = true; try { materialStats.value = await request({ url: '/admin/dashboard/material-statistics', method: 'GET' }) as Record<string, unknown>[]; } finally { matLoading.value = false; } }
function resetMatFilters() { matFilters.projectId = ''; }
loadMaterialReport();

/* ========== 工具函数 ========== */
function formatMoney(value: unknown): string {
  const n = Number(value || 0);
  return n >= 10000 ? `¥${(n / 10000).toFixed(1)}万` : `¥${n.toLocaleString()}`;
}
</script>

<style scoped>
.recon-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 16px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #334155;
}
</style>
