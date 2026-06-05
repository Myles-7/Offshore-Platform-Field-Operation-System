<template>
  <PageShell title="工单管理">
    <SearchForm :model="query" @search="loadData" @reset="resetQuery">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="工单号/标题" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" class="search-form__select">
          <el-option v-for="item in workOrderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <DataTable
      v-model:page-num="page.pageNum"
      v-model:page-size="page.pageSize"
      :data="records"
      :loading="loading"
      :total="total"
      @update:page-num="loadData"
      @update:page-size="loadData"
    >
      <el-table-column prop="workOrderNo" label="工单号" min-width="150" />
      <el-table-column prop="projectName" label="项目" min-width="160" />
      <el-table-column prop="workTitle" label="作业标题" min-width="180" />
      <el-table-column prop="workLocation" label="地点" min-width="160" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <StatusTag :value="row.status" enum-type="workOrder" />
        </template>
      </el-table-column>
      <el-table-column label="同步" width="120">
        <template #default="{ row }">
          <StatusTag :value="row.syncStatus" enum-type="sync" />
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
    </DataTable>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { fetchWorkOrders } from '@/api/admin';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import { workOrderStatusOptions } from '@/constants/enums';
import { defaultPageParams } from '@/constants/pagination';
import type { WorkOrderListItem } from '@/types/work-order';
import { formatDateTime } from '@/utils/date';

const loading = ref(false);
const records = ref<WorkOrderListItem[]>([]);
const total = ref(0);
const page = reactive({ ...defaultPageParams });
const query = reactive({
  keyword: '',
  status: ''
});

async function loadData() {
  loading.value = true;
  try {
    const result = await fetchWorkOrders({
      ...page,
      keyword: query.keyword || undefined
    });
    records.value = result.records || [];
    total.value = result.total || 0;
  } finally {
    loading.value = false;
  }
}

function resetQuery() {
  query.keyword = '';
  query.status = '';
  page.pageNum = 1;
  loadData();
}

onMounted(loadData);
</script>
