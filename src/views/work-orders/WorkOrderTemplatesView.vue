<template>
  <PageShell title="工单模板管理" description="管理工单模板，支持从模板快速创建工单">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增模板</el-button>
    </template>

    <DataTable :data="records" :loading="loading" :total="records.length" :page-num="1" :page-size="records.length">
      <el-table-column type="index" label="序号" width="55" />
      <el-table-column prop="templateCode" label="模板编号" min-width="140" />
      <el-table-column prop="templateName" label="模板名称" min-width="160" />
      <el-table-column label="作业类型" width="110">
        <template #default="{ row }">{{ workTypeLabel(row.workType) }}</template>
      </el-table-column>
      <el-table-column label="默认优先级" width="100">
        <template #default="{ row }">
          <StatusTag :value="row.defaultPriority" enum-type="priority" />
        </template>
      </el-table-column>
      <el-table-column label="计划工时" width="90">
        <template #default="{ row }">{{ row.defaultDurationHours ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="启用" width="70">
        <template #default="{ row }">
          <el-tag :type="row.enabledFlag ? 'success' : 'info'" size="small">
            {{ row.enabledFlag ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </DataTable>

    <TemplateFormDialog
      v-model="dialogVisible"
      :editing-id="editingId"
      :initial-values="editingRecord"
      @submit="handleSubmit"
    />
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createTemplate, deleteTemplate, fetchTemplates, updateTemplate } from '@/api/admin';
import PageShell from '@/components/page/PageShell.vue';
import DataTable from '@/components/table/DataTable.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import TemplateFormDialog from '@/components/form/TemplateFormDialog.vue';
import { workTypeOptions } from '@/constants/enums';
import type { WorkOrderTemplateItem } from '@/types/template';
import { formatDateTime } from '@/utils/date';

const loading = ref(false);
const records = ref<WorkOrderTemplateItem[]>([]);

const dialogVisible = ref(false);
const editingId = ref<number | undefined>();
const editingRecord = ref<WorkOrderTemplateItem | null>(null);

async function loadData() {
  loading.value = true;
  try {
    records.value = await fetchTemplates();
  } finally {
    loading.value = false;
  }
}

function workTypeLabel(type?: string): string {
  return workTypeOptions.find((o) => o.value === type)?.label ?? (type || '-');
}

function openCreate() {
  editingId.value = undefined;
  editingRecord.value = null;
  dialogVisible.value = true;
}

function openEdit(row: WorkOrderTemplateItem) {
  editingId.value = row.id;
  editingRecord.value = row;
  dialogVisible.value = true;
}

async function handleSubmit(data: Record<string, unknown>, editId?: number) {
  if (editId) {
    await updateTemplate(editId, data);
    ElMessage.success('模板更新成功');
  } else {
    await createTemplate(data);
    ElMessage.success('模板创建成功');
  }
  dialogVisible.value = false;
  loadData();
}

async function confirmDelete(row: WorkOrderTemplateItem) {
  try {
    await ElMessageBox.confirm(
      `确认删除模板「${row.templateName}」？`,
      '删除模板',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    );
    await deleteTemplate(row.id);
    ElMessage.success('模板已删除');
    loadData();
  } catch { /* 用户取消 */ }
}

onMounted(() => { loadData(); });
</script>
