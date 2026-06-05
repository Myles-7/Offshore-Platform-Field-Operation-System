<template>
  <PageShell title="验收归档" description="待验收、已验收、已驳回工单集中管理">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="待验收" name="pending" />
      <el-tab-pane label="已验收" name="passed" />
      <el-tab-pane label="已驳回" name="rejected" />
    </el-tabs>

    <SearchForm :model="filters" @search="loadData" @reset="resetFilters">
      <el-form-item label="项目">
        <el-select v-model="filters.projectId" clearable filterable placeholder="全部" style="width:180px">
          <el-option v-for="p in projectOptions" :key="p.value" :label="p.label" :value="p.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker v-model="filters.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" format="YYYY-MM-DD" value-format="YYYY-MM-DD HH:mm:ss" style="width:260px" />
      </el-form-item>
    </SearchForm>

    <DataTable :data="filteredList" :loading="loading" :total="filteredList.length" :page-num="1" :page-size="filteredList.length">
      <el-table-column type="index" label="序号" width="55" />
      <el-table-column label="工单编号" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="goDetail(row.id)">{{ row.workOrderNo }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="projectName" label="项目" min-width="120" show-overflow-tooltip />
      <el-table-column prop="workTitle" label="作业标题" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><StatusTag :value="row.status" enum-type="workOrder" /></template>
      </el-table-column>
      <el-table-column prop="maintainerName" label="维修工" width="90" />
      <el-table-column label="计划结束" width="160">
        <template #default="{ row }">{{ formatDateTime(row.plannedEndTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="goDetail(row.id)">详情</el-button>
          <el-button link type="primary" size="small" @click="openPdf(row.id)">PDF</el-button>
        </template>
      </el-table-column>
    </DataTable>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { pdfDownloadUrl } from '@/api/acceptance';
import { fetchWorkOrders } from '@/api/admin';
import { fetchProjectStatistics } from '@/api/dashboard';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import type { WorkOrderListItem } from '@/types/work-order';
import { formatDateTime } from '@/utils/date';

const router = useRouter();
const activeTab = ref('pending');

const loading = ref(false);
const list = ref<WorkOrderListItem[]>([]);
const filters = reactive({ projectId: '' as number | string, dateRange: null as [string, string] | null });
const projectOptions = ref<{ label: string; value: number }[]>([]);

const statusMap: Record<string, string> = {
  pending: 'PENDING_ACCEPTANCE',
  passed: 'COMPLETED',
  rejected: 'REJECTED'
};

const filteredList = computed(() => {
  let result = list.value;
  if (filters.projectId) result = result.filter((r) => r.projectId == filters.projectId);
  return result;
});

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchWorkOrders({ pageNum: 1, pageSize: 200, status: statusMap[activeTab.value] });
    list.value = res.records ?? [];
  } finally { loading.value = false; }
}

function resetFilters() { filters.projectId = ''; filters.dateRange = null; }

function goDetail(id: number) { router.push(`/work-orders/${id}`); }
function openPdf(workOrderId: number) { window.open(pdfDownloadUrl(workOrderId), '_blank'); }

onMounted(async () => {
  try {
    const projs = await fetchProjectStatistics();
    projectOptions.value = projs.map((p) => ({ label: p.projectName ?? `项目${p.projectId}`, value: p.projectId }));
  } catch { /* */ }
  loadData();
});
</script>
