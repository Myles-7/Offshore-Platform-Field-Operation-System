<template>
  <PageShell title="角色权限">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="角色管理" name="roles" />
      <el-tab-pane label="权限列表" name="permissions" />
    </el-tabs>

    <!-- 角色管理 -->
    <template v-if="activeTab === 'roles'">
      <div style="margin-bottom:8px">
        <el-button type="primary" :icon="Plus" @click="openCreateRole">新增角色</el-button>
      </div>
      <DataTable :data="roleList" :loading="roleLoading" :total="roleList.length" :page-num="1" :page-size="roleList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column prop="roleCode" label="角色编码" width="130" />
        <el-table-column prop="roleName" label="角色名称" width="120" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }"><el-tag :type="row.roleType === 'SYSTEM' ? 'danger' : 'info'" size="small">{{ row.roleType === 'SYSTEM' ? '系统' : '业务' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="数据范围" width="100">
          <template #default="{ row }">{{ dataScopeLabel(row.dataScope) }}</template>
        </el-table-column>
        <el-table-column label="PC" width="60"><template #default="{ row }"><el-tag :type="row.pcEnabled ? 'success' : 'info'" size="small">{{ row.pcEnabled ? '是' : '否' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" size="small" @click="openAssignPerms(row)">分配权限</el-button>
          </template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- 权限列表 -->
    <template v-if="activeTab === 'permissions'">
      <DataTable :data="permList" :loading="permLoading" :total="permList.length" :page-num="1" :page-size="permList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column prop="permissionCode" label="权限编码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="permissionName" label="权限名称" min-width="120" show-overflow-tooltip />
        <el-table-column label="类型" width="80"><template #default="{ row }"><el-tag size="small">{{ permTypeLabel(row.permissionType) }}</el-tag></template></el-table-column>
        <el-table-column label="平台" width="70"><template #default="{ row }"><el-tag :type="row.platform === 'PC' ? 'primary' : 'success'" size="small">{{ row.platform }}</el-tag></template></el-table-column>
        <el-table-column label="路由/接口" min-width="180" show-overflow-tooltip><template #default="{ row }">{{ row.routePath || row.apiPath || '-' }}</template></el-table-column>
        <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">{{ row.status === 'ACTIVE' ? '启用' : '停用' }}</el-tag></template></el-table-column>
      </DataTable>
    </template>

    <!-- 新增角色 -->
    <FormDialog v-model="roleFormVisible" title="新增角色" :confirm-loading="roleSubmitting" @confirm="submitRole">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="角色编码" prop="roleCode"><el-input v-model="roleForm.roleCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="角色名称" prop="roleName"><el-input v-model="roleForm.roleName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="PC"><el-switch v-model="roleForm.pcEnabled" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="移动端"><el-switch v-model="roleForm.mobileEnabled" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="排序"><el-input-number v-model="roleForm.sortOrder" :min="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="roleForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
    </FormDialog>

    <!-- 分配权限 -->
    <el-dialog v-model="permAssignVisible" title="分配权限" width="600px" destroy-on-close>
      <el-checkbox-group v-model="assignPermIds" style="display:flex;flex-direction:column;gap:6px;max-height:420px;overflow-y:auto">
        <el-checkbox v-for="p in permOptions" :key="p.value" :label="p.value">
          <span>{{ p.label }}</span>
          <el-tag size="small" style="margin-left:6px">{{ p.type }}</el-tag>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="permAssignVisible = false">取消</el-button>
        <el-button type="primary" :loading="permAssignSubmitting" @click="submitAssignPerms">保存</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { request } from '@/api/request';
import PageShell from '@/components/page/PageShell.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormDialog from '@/components/dialog/FormDialog.vue';

const activeTab = ref('roles');

/* ========== 角色 ========== */
const roleLoading = ref(false); const roleList = ref<Record<string, unknown>[]>([]);
async function loadRoles() { roleLoading.value = true; try { roleList.value = await request({ url: '/admin/roles', method: 'GET' }); } finally { roleLoading.value = false; } }
const roleFormVisible = ref(false); const roleSubmitting = ref(false);
const roleFormRef = ref(); const roleForm = reactive({ roleCode: '', roleName: '', roleType: 'BUSINESS', dataScope: 'PROJECT', pcEnabled: 1, mobileEnabled: 0, sortOrder: 0, remark: '' });
const roleRules = { roleCode: [{ required: true, message: '编码必填' }], roleName: [{ required: true, message: '名称必填' }] };
function openCreateRole() { Object.assign(roleForm, { roleCode: '', roleName: '', roleType: 'BUSINESS', dataScope: 'PROJECT', pcEnabled: 1, mobileEnabled: 0, sortOrder: 0, remark: '' }); roleFormVisible.value = true; }
async function submitRole() { try { await roleFormRef.value?.validate(); } catch { return; } roleSubmitting.value = true; try { await request({ url: '/admin/roles', method: 'POST', data: { ...roleForm } }); ElMessage.success('角色创建成功'); roleFormVisible.value = false; loadRoles(); } finally { roleSubmitting.value = false; } }

/* ========== 权限分配 ========== */
const permAssignVisible = ref(false);
const permAssignSubmitting = ref(false);
const assignRoleId = ref<number | null>(null);
const assignPermIds = ref<number[]>([]);
const permOptions = ref<{ label: string; value: number; type: string }[]>([]);
function openAssignPerms(row: Record<string, unknown>) {
  assignRoleId.value = row.id as number;
  assignPermIds.value = [];
  permOptions.value = permList.value.map((p) => ({
    label: `${p.permissionName} (${p.permissionCode})`,
    value: p.id as number,
    type: permTypeLabel(p.permissionType as string)
  }));
  permAssignVisible.value = true;
}
async function submitAssignPerms() {
  permAssignSubmitting.value = true;
  try {
    await request({ url: `/admin/roles/${assignRoleId.value}/permissions`, method: 'PUT', data: { permissionIds: assignPermIds.value } });
    ElMessage.success('权限分配成功');
    permAssignVisible.value = false;
    loadRoles();
  } finally { permAssignSubmitting.value = false; }
}

/* ========== 权限 ========== */
const permLoading = ref(false); const permList = ref<Record<string, unknown>[]>([]);
async function loadPerms() { permLoading.value = true; try { permList.value = await request({ url: '/admin/permissions', method: 'GET' }); } finally { permLoading.value = false; } }
function dataScopeLabel(s?: string) { const m: Record<string, string> = { ALL: '全部', PROJECT: '所属项目', SELF: '仅自己', MATERIAL: '物料', QUALIFICATION: '资质', DASHBOARD: '看板', ACCEPTANCE: '验收' }; return m[s || ''] || s || '-'; }
function permTypeLabel(s?: string) { const m: Record<string, string> = { MENU: '菜单', BUTTON: '按钮', API: '接口' }; return m[s || ''] || s || '-'; }

onMounted(() => { loadRoles(); loadPerms(); });
</script>
