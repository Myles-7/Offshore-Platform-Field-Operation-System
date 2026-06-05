<template>
  <PageShell title="项目管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增项目</el-button>
    </template>

    <SearchForm :model="filters" @search="handleSearch" @reset="handleReset">
      <el-form-item label="搜索">
        <el-input v-model="filters.keyword" clearable placeholder="项目编号/名称" style="width:200px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.status" clearable placeholder="全部" style="width:120px">
          <el-option label="进行中" value="ACTIVE" />
          <el-option label="已暂停" value="SUSPENDED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <DataTable :data="filteredList" :loading="loading" :total="filteredList.length" :page-num="1" :page-size="filteredList.length">
      <el-table-column type="index" label="序号" width="55" />
      <el-table-column prop="projectCode" label="项目编号" min-width="120" show-overflow-tooltip />
      <el-table-column prop="projectName" label="项目名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="platformName" label="平台名称" width="130" show-overflow-tooltip />
      <el-table-column prop="ownerUnit" label="业主单位" width="130" show-overflow-tooltip />
      <el-table-column prop="contractorUnit" label="施工单位" width="130" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="projectStatusTag(row.projectStatus)" size="small">{{ projectStatusLabel(row.projectStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </DataTable>

    <FormDialog v-model="formVisible" :title="editingId ? '编辑项目' : '新增项目'" :confirm-loading="submitting" @confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="wo-form">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="项目编号" prop="projectCode"><el-input v-model="form.projectCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目名称" prop="projectName"><el-input v-model="form.projectName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="平台名称"><el-input v-model="form.platformName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="业主单位"><el-input v-model="form.ownerUnit" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="施工单位"><el-input v-model="form.contractorUnit" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="项目地点"><el-input v-model="form.projectLocation" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目经理ID"><el-input-number v-model="form.projectManagerId" :min="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="结束日期"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="form.projectStatus" style="width:100%">
                <el-option label="进行中" value="ACTIVE" /><el-option label="已暂停" value="SUSPENDED" />
                <el-option label="已完成" value="COMPLETED" /><el-option label="已关闭" value="CLOSED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
    </FormDialog>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { request } from '@/api/request';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormDialog from '@/components/dialog/FormDialog.vue';

/* ========== 数据 ========== */
const loading = ref(false);
const list = ref<Record<string, unknown>[]>([]);
const filters = reactive({ keyword: '', status: '' });

const filteredList = computed(() => list.value.filter((r) => {
  if (filters.keyword && !`${r.projectCode || ''}${r.projectName || ''}`.toLowerCase().includes(filters.keyword.toLowerCase())) return false;
  if (filters.status && r.projectStatus !== filters.status) return false;
  return true;
}));

async function loadData() { loading.value = true; try { const res: any = await request({ url: '/admin/projects', method: 'GET', params: { pageNum: 1, pageSize: 200 } }); list.value = res?.records || []; } finally { loading.value = false; } }
function handleSearch() {}; function handleReset() { filters.keyword = ''; filters.status = ''; }

/* ========== 表单 ========== */
const formVisible = ref(false); const submitting = ref(false); const editingId = ref<number | undefined>();
const formRef = ref();
const form = reactive({ projectCode: '', projectName: '', platformName: '', ownerUnit: '', contractorUnit: '', projectLocation: '', projectManagerId: undefined as number | undefined, startDate: '', endDate: '', projectStatus: 'ACTIVE', remark: '' });
const rules = { projectCode: [{ required: true, message: '项目编号必填' }], projectName: [{ required: true, message: '项目名称必填' }] };

function resetForm() { Object.assign(form, { projectCode: '', projectName: '', platformName: '', ownerUnit: '', contractorUnit: '', projectLocation: '', projectManagerId: undefined, startDate: '', endDate: '', projectStatus: 'ACTIVE', remark: '' }); }
function openCreate() { editingId.value = undefined; resetForm(); formVisible.value = true; }
function openEdit(row: Record<string, unknown>) { editingId.value = row.id as number; Object.assign(form, row); formVisible.value = true; }

async function submitForm() {
  try { await formRef.value?.validate(); } catch { return; }
  submitting.value = true;
  try {
    if (editingId.value) await request({ url: `/admin/projects/${editingId.value}`, method: 'PUT', data: { ...form } });
    else await request({ url: '/admin/projects', method: 'POST', data: { ...form } });
    ElMessage.success(editingId.value ? '项目更新成功' : '项目创建成功'); formVisible.value = false; loadData();
  } finally { submitting.value = false; }
}

async function confirmDelete(row: Record<string, unknown>) {
  try { await ElMessageBox.confirm(`确认删除项目「${row.projectName}」？`, '删除项目', { type: 'error' }); await request({ url: `/admin/projects/${row.id}`, method: 'DELETE' }); ElMessage.success('项目已删除'); loadData(); } catch { /* 取消 */ }
}

function projectStatusTag(s?: string) { return s === 'ACTIVE' ? 'success' : s === 'SUSPENDED' ? 'warning' : s === 'COMPLETED' ? 'info' : 'info'; }
function projectStatusLabel(s?: string) { const m: Record<string, string> = { ACTIVE: '进行中', SUSPENDED: '已暂停', COMPLETED: '已完成', CLOSED: '已关闭' }; return m[s || ''] || s || '-'; }

onMounted(loadData);
</script>

<style scoped>.wo-form { max-height: 500px; overflow-y: auto; padding-right: 4px; }</style>
