<template>
  <PageShell title="用户管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
    </template>

    <SearchForm :model="filters" @search="handleSearch" @reset="handleReset">
      <el-form-item label="搜索">
        <el-input v-model="filters.keyword" clearable placeholder="用户名/姓名" style="width:200px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.status" clearable placeholder="全部" style="width:120px">
          <el-option label="正常" value="ACTIVE" />
          <el-option label="禁用" value="DISABLED" />
          <el-option label="锁定" value="LOCKED" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <DataTable :data="filteredList" :loading="loading" :total="filteredList.length" :page-num="1" :page-size="filteredList.length">
      <el-table-column type="index" label="序号" width="55" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="90" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="employeeNo" label="工号" width="100" />
      <el-table-column label="PC" width="60"><template #default="{ row }"><el-tag :type="row.pcEnabled ? 'success' : 'info'" size="small">{{ row.pcEnabled ? '是' : '否' }}</el-tag></template></el-table-column>
      <el-table-column label="移动端" width="70"><template #default="{ row }"><el-tag :type="row.mobileEnabled ? 'success' : 'info'" size="small">{{ row.mobileEnabled ? '是' : '否' }}</el-tag></template></el-table-column>
      <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="accountStatusTag(row.accountStatus)" size="small">{{ accountStatusLabel(row.accountStatus) }}</el-tag></template></el-table-column>
      <el-table-column label="角色" min-width="120"><template #default="{ row }">{{ (row.roleCodes as string[])?.join(', ') || '-' }}</template></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link type="success" size="small" @click="openAssignRoles(row)">分配角色</el-button>
          <el-button link type="danger" size="small" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </DataTable>

    <FormDialog v-model="formVisible" :title="editingId ? '编辑用户' : '新增用户'" :confirm-loading="submitting" @confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="wo-form">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="用户名" prop="username"><el-input v-model="form.username" :disabled="!!editingId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="editingId ? '新密码(留空不变)' : '密码'" :prop="editingId ? undefined : 'password'"><el-input v-model="form.password" type="password" show-password /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工号"><el-input v-model="form.employeeNo" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="PC登录"><el-switch v-model="form.pcEnabled" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="移动登录"><el-switch v-model="form.mobileEnabled" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-select v-model="form.accountStatus" style="width:100%"><el-option label="正常" value="ACTIVE" /><el-option label="禁用" value="DISABLED" /><el-option label="锁定" value="LOCKED" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
    </FormDialog>
    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleAssignVisible" title="分配角色" width="500px" destroy-on-close>
      <el-checkbox-group v-model="assignRoleIds" style="display:flex;flex-direction:column;gap:8px">
        <el-checkbox v-for="r in roleOptions" :key="r.value" :label="r.value">
          <span>{{ r.label }}</span>
          <el-tag size="small" style="margin-left:6px" :type="r.label.includes('系统管理员') ? 'danger' : 'info'">{{ r.label.includes('系统管理员') ? '系统' : '业务' }}</el-tag>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleAssignVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleAssignSubmitting" @click="submitAssignRoles">保存</el-button>
      </template>
    </el-dialog>
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

const loading = ref(false);
const list = ref<Record<string, unknown>[]>([]);
const filters = reactive({ keyword: '', status: '' });
const filteredList = computed(() => list.value.filter((r) => {
  if (filters.keyword && !`${r.username || ''}${r.realName || ''}`.toLowerCase().includes(filters.keyword.toLowerCase())) return false;
  if (filters.status && r.accountStatus !== filters.status) return false;
  return true;
}));
async function loadData() { loading.value = true; try { const res: any = await request({ url: '/admin/users', method: 'GET', params: { pageNum: 1, pageSize: 200 } }); list.value = res?.records || []; } finally { loading.value = false; } }
function handleSearch() {}; function handleReset() { filters.keyword = ''; filters.status = ''; }

const formVisible = ref(false); const submitting = ref(false); const editingId = ref<number | undefined>();
const formRef = ref();
const form = reactive({ username: '', password: '', realName: '', phone: '', email: '', employeeNo: '', accountStatus: 'ACTIVE', pcEnabled: 1, mobileEnabled: 1, remark: '' });
const rules = computed(() => ({
  username: [{ required: true, message: '用户名必填' }],
  realName: [{ required: true, message: '姓名必填' }],
  password: editingId.value ? [] : [{ required: true, message: '密码必填', min: 6 }]
}));
function resetForm() { Object.assign(form, { username: '', password: '', realName: '', phone: '', email: '', employeeNo: '', accountStatus: 'ACTIVE', pcEnabled: 1, mobileEnabled: 1, remark: '' }); }
function openCreate() { editingId.value = undefined; resetForm(); formVisible.value = true; }
function openEdit(row: Record<string, unknown>) { editingId.value = row.id as number; Object.assign(form, { username: row.username, password: '', realName: row.realName, phone: row.phone, email: row.email, employeeNo: row.employeeNo, accountStatus: row.accountStatus, pcEnabled: row.pcEnabled, mobileEnabled: row.mobileEnabled, remark: '' }); formVisible.value = true; }
async function submitForm() {
  try { await formRef.value?.validate(); } catch { return; }
  submitting.value = true;
  try {
    const payload = editingId.value ? { ...form, password: form.password || undefined } : form;
    if (editingId.value) await request({ url: `/admin/users/${editingId.value}`, method: 'PUT', data: payload });
    else await request({ url: '/admin/users', method: 'POST', data: payload });
    ElMessage.success(editingId.value ? '用户更新成功' : '用户创建成功'); formVisible.value = false; loadData();
  } finally { submitting.value = false; }
}
async function confirmDelete(row: Record<string, unknown>) { try { await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '删除用户', { type: 'error' }); await request({ url: `/admin/users/${row.id}`, method: 'DELETE' }); ElMessage.success('用户已删除'); loadData(); } catch { /* */ } }
function accountStatusTag(s?: string) { return s === 'ACTIVE' ? 'success' : s === 'DISABLED' ? 'info' : 'danger'; }
function accountStatusLabel(s?: string) { const m: Record<string, string> = { ACTIVE: '正常', DISABLED: '禁用', LOCKED: '锁定', EXPIRED: '过期' }; return m[s || ''] || s || '-'; }

/* ========== 角色分配 ========== */
const roleAssignVisible = ref(false);
const roleAssignSubmitting = ref(false);
const assignUserId = ref<number | null>(null);
const assignRoleIds = ref<number[]>([]);
const roleOptions = ref<{ label: string; value: number }[]>([]);

async function openAssignRoles(row: Record<string, unknown>) {
  assignUserId.value = row.id as number;
  assignRoleIds.value = (row.roleIds as number[])?.slice() || [];
  // 加载角色选项
  try {
    const roles: any = await request({ url: '/admin/roles', method: 'GET' });
    roleOptions.value = (roles || []).map((r: any) => ({
      label: `${r.roleName} (${r.roleCode})`,
      value: r.id
    }));
  } catch { roleOptions.value = []; }
  roleAssignVisible.value = true;
}

async function submitAssignRoles() {
  roleAssignSubmitting.value = true;
  try {
    await request({ url: `/admin/users/${assignUserId.value}/roles`, method: 'PUT', data: { roleIds: assignRoleIds.value } });
    ElMessage.success('角色分配成功');
    roleAssignVisible.value = false;
    loadData();
  } finally { roleAssignSubmitting.value = false; }
}

onMounted(loadData);
</script>

<style scoped>.wo-form { max-height: 500px; overflow-y: auto; padding-right: 4px; }</style>
