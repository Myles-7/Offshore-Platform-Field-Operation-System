<template>
  <PageShell title="施工记录">
    <SearchForm :model="filters" @search="handleSearch" @reset="handleReset">
      <el-form-item label="工单ID">
        <el-input-number v-model="filters.workOrderId" :min="1" placeholder="输入工单ID" style="width:180px" />
      </el-form-item>
      <el-form-item label="异常">
        <el-select v-model="filters.abnormal" clearable placeholder="全部" style="width:110px">
          <el-option label="是" :value="1" /><el-option label="否" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="同步状态">
        <el-select v-model="filters.syncStatus" clearable placeholder="全部" style="width:130px">
          <el-option v-for="s in syncStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <AppState v-if="!filters.workOrderId" type="empty" title="请输入工单 ID 查看施工记录" />

    <template v-else>
      <div class="detail-card" style="margin-bottom:12px">
        <header class="detail-card__header"><h3>工单 {{ filters.workOrderId }} 施工记录</h3></header>
      </div>

      <DataTable :data="filteredRecords" :loading="loading" :total="filteredRecords.length" :page-num="1" :page-size="filteredRecords.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column prop="recordNo" label="记录编号" width="130" show-overflow-tooltip />
        <el-table-column prop="recordType" label="类型" width="90" />
        <el-table-column label="施工时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.constructionTime || row.submittedAt) }}</template>
        </el-table-column>
        <el-table-column label="施工人员" width="100">
          <template #default="{ row }">{{ row.constructionUserName || `ID:${row.constructionUserId}` || '-' }}</template>
        </el-table-column>
        <el-table-column prop="constructionDesc" label="施工描述" min-width="160" show-overflow-tooltip />
        <el-table-column label="异常" width="70">
          <template #default="{ row }"><el-tag v-if="row.abnormalFlag" type="danger" size="small" effect="dark">异常</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column label="附件数" width="70"><template #default="{ row }">{{ row.attachmentCount || 0 }}</template></el-table-column>
        <el-table-column label="AI数" width="60"><template #default="{ row }">{{ row.aiResultCount || 0 }}</template></el-table-column>
        <el-table-column label="同步" width="100"><template #default="{ row }"><StatusTag :value="row.syncStatus" enum-type="sync" /></template></el-table-column>
      </DataTable>
    </template>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { request } from '@/api/request';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import AppState from '@/components/state/AppState.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import { syncStatusOptions } from '@/constants/enums';
import { formatDateTime } from '@/utils/date';

const loading = ref(false);
const records = ref<Record<string, unknown>[]>([]);
const filters = reactive({ workOrderId: undefined as number | undefined, abnormal: '' as number | string, syncStatus: '' });

const filteredRecords = computed(() => records.value.filter((r) => {
  if (filters.abnormal !== '' && r.abnormalFlag !== filters.abnormal) return false;
  if (filters.syncStatus && r.syncStatus !== filters.syncStatus) return false;
  return true;
}));

async function loadRecords() { if (!filters.workOrderId) { records.value = []; return; } loading.value = true; try { records.value = await request({ url: `/admin/work-orders/${filters.workOrderId}/records`, method: 'GET' }); } finally { loading.value = false; } }
function handleSearch() { loadRecords(); }
function handleReset() { filters.abnormal = ''; filters.syncStatus = ''; }
onMounted(loadRecords);
</script>

<style scoped>
.detail-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; }
.detail-card__header { display: flex; align-items: center; justify-content: space-between; }
.detail-card__header h3 { margin: 0; font-size: 15px; color: #334155; }
</style>
