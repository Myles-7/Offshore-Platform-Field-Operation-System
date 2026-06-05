<template>
  <PageShell title="人员资质">
    <template #actions>
      <el-button v-if="activeTab === 'employees'" type="primary" :icon="Plus" @click="openCreateEmployee">新增员工</el-button>
    </template>

    <!-- 子 tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="员工信息" name="employees" />
      <el-tab-pane label="证书管理" name="certificates" />
      <el-tab-pane label="到期预警" name="warnings" />
    </el-tabs>

    <!-- ========== 员工信息 Tab ========== -->
    <template v-if="activeTab === 'employees'">
      <DataTable :data="employeeList" :loading="loading" :total="employeeList.length" :page-num="1" :page-size="employeeList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column prop="realName" label="姓名" min-width="90" />
        <el-table-column prop="employeeNo" label="工号" width="110" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="positionName" label="岗位" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.employeeStatus === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.employeeStatus === 'ACTIVE' ? '在职' : row.employeeStatus || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEditEmployee(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="openCertificates(row)">证书</el-button>
            <el-button link type="danger" size="small" @click="confirmDeleteEmployee(row)">删除</el-button>
          </template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 证书管理 Tab ========== -->
    <template v-if="activeTab === 'certificates'">
      <div style="display:flex;gap:12px;margin-bottom:12px">
        <el-select v-model="certEmployeeFilter" filterable clearable placeholder="选择员工" style="width:220px">
          <el-option v-for="e in employeeList" :key="e.id" :label="`${e.realName} (${e.employeeNo})`" :value="e.id" />
        </el-select>
        <el-button type="primary" :icon="Plus" :disabled="!certEmployeeFilter" @click="openCreateCert">新增证书</el-button>
      </div>

      <DataTable :data="filteredCertList" :loading="certLoading" :total="filteredCertList.length" :page-num="1" :page-size="filteredCertList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column label="员工" width="90">
          <template #default="{ row }">{{ row.employeeName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="certificateName" label="证书类型" width="120" />
        <el-table-column prop="certificateNo" label="证书编号" width="140" show-overflow-tooltip />
        <el-table-column prop="issueOrg" label="发证机构" width="130" show-overflow-tooltip />
        <el-table-column label="发证日期" width="110">
          <template #default="{ row }">{{ row.issueDate ? row.issueDate.slice(0, 10) : '-' }}</template>
        </el-table-column>
        <el-table-column label="到期日期" width="110">
          <template #default="{ row }">
            <span :style="{ color: row.validStatus === 'EXPIRED' ? '#f56c6c' : row.validStatus === 'EXPIRING' ? '#e6a23c' : '' }">
              {{ row.validTo ? row.validTo.slice(0, 10) : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="证书状态" width="100">
          <template #default="{ row }">
            <el-tag :type="certStatusType(row.validStatus)" size="small" effect="dark">{{ certStatusLabel(row.validStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEditCert(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="confirmDeleteCert(row)">删除</el-button>
          </template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 到期预警 Tab ========== -->
    <template v-if="activeTab === 'warnings'">
      <SearchForm :model="warningFilters" @search="loadCertWarnings" @reset="resetWarnings">
        <el-form-item label="证书类型">
          <el-select v-model="warningFilters.certType" clearable placeholder="全部" style="width:150px">
            <el-option v-for="t in uniqueCertTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警状态">
          <el-select v-model="warningFilters.status" clearable placeholder="全部" style="width:130px">
            <el-option label="已过期" value="EXPIRED" />
            <el-option label="即将到期" value="EXPIRING" />
          </el-select>
        </el-form-item>
      </SearchForm>

      <DataTable :data="filteredWarnings" :loading="warnLoading" :total="filteredWarnings.length" :page-num="1" :page-size="filteredWarnings.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column label="员工" width="90">
          <template #default="{ row }">{{ row.employeeName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="certificateName" label="证书类型" width="120" />
        <el-table-column prop="certificateNo" label="证书编号" width="140" show-overflow-tooltip />
        <el-table-column label="到期日期" width="110">
          <template #default="{ row }">{{ row.validTo ? row.validTo.slice(0, 10) : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="certStatusType(row.validStatus)" size="small" effect="dark">{{ certStatusLabel(row.validStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警级别" width="90">
          <template #default="{ row }">
            <el-tag :type="row.warningLevel === 'HIGH' ? 'danger' : 'warning'" size="small">{{ row.warningLevel === 'HIGH' ? '严重' : '一般' }}</el-tag>
          </template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 员工弹窗 ========== -->
    <FormDialog v-model="empVisible" :title="empEditingId ? '编辑员工' : '新增员工'" :confirm-loading="empSubmitting" @confirm="submitEmployee">
      <el-form ref="empFormRef" :model="empForm" :rules="empRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="姓名" prop="realName"><el-input v-model="empForm.realName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="empForm.employeeNo" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="empForm.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="岗位"><el-input v-model="empForm.positionName" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="状态">
          <el-select v-model="empForm.employeeStatus" style="width:100%">
            <el-option label="在职" value="ACTIVE" />
            <el-option label="离职" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="empForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
    </FormDialog>

    <!-- ========== 证书弹窗 ========== -->
    <FormDialog v-model="certVisible" :title="certEditingId ? '编辑证书' : '新增证书'" :confirm-loading="certSubmitting" @confirm="submitCert">
      <el-form ref="certFormRef" :model="certForm" :rules="certRules" label-width="100px">
        <el-form-item label="员工">{{ certEmployeeName }}</el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="证书类型" prop="certificateName"><el-input v-model="certForm.certificateName" placeholder="如 海上作业证" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="证书编号" prop="certificateNo"><el-input v-model="certForm.certificateNo" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="发证机构"><el-input v-model="certForm.issueOrg" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="发证日期"><el-date-picker v-model="certForm.issueDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="到期日期" prop="validTo"><el-date-picker v-model="certForm.validTo" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="certForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
    </FormDialog>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchEmployees, fetchEmployeeCertificates } from '@/api/qualification';
import type { CertificateVO } from '@/types/work-order';
import { request } from '@/api/request';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormDialog from '@/components/dialog/FormDialog.vue';

/* ========== Tab ========== */
const activeTab = ref('employees');
function onTabChange(tab: string) {
  if (tab === 'certificates' || tab === 'warnings') loadAllCertificates();
}

/* ========== 员工数据 ========== */
const loading = ref(false);
const employeeList = ref<{ id: number; userId?: number; employeeNo?: string; realName?: string; phone?: string; positionName?: string; employeeStatus?: string }[]>([]);

async function loadEmployees() {
  loading.value = true;
  try { employeeList.value = await fetchEmployees(); } finally { loading.value = false; }
}

/* ========== 员工表单 ========== */
const empVisible = ref(false); const empSubmitting = ref(false); const empEditingId = ref<number | undefined>();
const empFormRef = ref(); const empForm = reactive({ realName: '', employeeNo: '', phone: '', positionName: '', employeeStatus: 'ACTIVE', remark: '' });
const empRules = { realName: [{ required: true, message: '姓名必填' }], employeeNo: [{ required: true, message: '工号必填' }] };

function openCreateEmployee() { empEditingId.value = undefined; resetEmpForm(); empVisible.value = true; }
function openEditEmployee(row: typeof employeeList.value[number]) {
  empEditingId.value = row.id;
  Object.assign(empForm, { realName: row.realName || '', employeeNo: row.employeeNo || '', phone: row.phone || '', positionName: row.positionName || '', employeeStatus: row.employeeStatus || 'ACTIVE', remark: '' });
  empVisible.value = true;
}
function resetEmpForm() { Object.assign(empForm, { realName: '', employeeNo: '', phone: '', positionName: '', employeeStatus: 'ACTIVE', remark: '' }); }

async function submitEmployee() {
  try { await empFormRef.value?.validate(); } catch { return; }
  empSubmitting.value = true;
  try {
    if (empEditingId.value) await request({ url: `/admin/employees/${empEditingId.value}`, method: 'PUT', data: { ...empForm } });
    else await request({ url: '/admin/employees', method: 'POST', data: { ...empForm } });
    ElMessage.success(empEditingId.value ? '员工更新成功' : '员工新增成功'); empVisible.value = false; loadEmployees();
  } finally { empSubmitting.value = false; }
}

async function confirmDeleteEmployee(row: typeof employeeList.value[number]) {
  try {
    await ElMessageBox.confirm(`确认删除员工「${row.realName}」？`, '删除员工', { type: 'error' });
    await request({ url: `/admin/employees/${row.id}`, method: 'DELETE' });
    ElMessage.success('员工已删除'); loadEmployees();
  } catch { /* 取消 */ }
}

/* ========== 证书数据 ========== */
const certLoading = ref(false);
const certEmployeeFilter = ref<number | string>('');
const allCertList = ref<Record<string, unknown>[]>([]);
const filteredCertList = computed(() => certEmployeeFilter.value ? allCertList.value.filter((c) => c.employeeId === certEmployeeFilter.value) : allCertList.value);
const certEmployeeName = computed(() => {
  const emp = employeeList.value.find((e) => e.id === certEmployeeFilter.value);
  return emp ? `${emp.realName} (${emp.employeeNo})` : '-';
});

async function loadAllCertificates() {
  certLoading.value = true;
  try {
    const results: { id: number; employeeId: number; employeeName: string; certificateName?: string; certificateNo?: string; issueOrg?: string; issueDate?: string; validTo?: string; validStatus?: string; warningLevel?: string }[] = [];
    for (const emp of employeeList.value) {
      try {
        const certs: CertificateVO[] = await fetchEmployeeCertificates(emp.id);
        for (const c of certs) results.push({ ...c, employeeName: emp.realName || '' });
      } catch { /* skip */ }
    }
    allCertList.value = results as unknown as Record<string, unknown>[];
  } finally { certLoading.value = false; }
}

/* ========== 证书表单 ========== */
const certVisible = ref(false); const certSubmitting = ref(false); const certEditingId = ref<number | undefined>();
const certFormRef = ref(); const certForm = reactive({ qualificationTypeId: 1, certificateName: '', certificateNo: '', issueOrg: '', issueDate: '', validTo: '', validStatus: '', remark: '' });
const certRules = { certificateName: [{ required: true, message: '证书类型必填' }], certificateNo: [{ required: true, message: '证书编号必填' }], validTo: [{ required: true, message: '到期日期必填' }] };

function openCreateCert() { certEditingId.value = undefined; resetCertForm(); certVisible.value = true; }
function openEditCert(row: Record<string, unknown>) {
  certEditingId.value = row.id as number;
  Object.assign(certForm, { certificateName: row.certificateName || '', certificateNo: row.certificateNo || '', issueOrg: row.issueOrg || '', issueDate: row.issueDate || '', validTo: row.validTo || '', validStatus: row.validStatus || '', remark: '' });
  certVisible.value = true;
}
function resetCertForm() { Object.assign(certForm, { qualificationTypeId: 1, certificateName: '', certificateNo: '', issueOrg: '', issueDate: '', validTo: '', validStatus: '', remark: '' }); }

async function submitCert() {
  try { await certFormRef.value?.validate(); } catch { return; }
  if (!certEmployeeFilter.value) { ElMessage.warning('请先选择员工'); return; }
  certSubmitting.value = true;
  try {
    if (certEditingId.value && certEditingId.value > 0) {
      await request({ url: `/admin/certificates/${certEditingId.value}`, method: 'PUT', data: { ...certForm, employeeId: certEmployeeFilter.value } });
    } else {
      await request({ url: `/admin/employees/${certEmployeeFilter.value}/certificates`, method: 'POST', data: { ...certForm } });
    }
    ElMessage.success(certEditingId.value ? '证书更新成功' : '证书新增成功'); certVisible.value = false; loadAllCertificates();
  } finally { certSubmitting.value = false; }
}

async function confirmDeleteCert(row: Record<string, unknown>) {
  try {
    await ElMessageBox.confirm(`确认删除证书「${row.certificateNo}」？`, '删除证书', { type: 'error' });
    await request({ url: `/admin/certificates/${row.id}`, method: 'DELETE' });
    ElMessage.success('证书已删除'); loadAllCertificates();
  } catch { /* 取消 */ }
}

/* ========== 预警 ========== */
const warnLoading = ref(false);
const warningFilters = reactive({ certType: '', status: '' });
const uniqueCertTypes = computed(() => [...new Set(allCertList.value.map((c) => c.certificateName as string).filter(Boolean))]);
const filteredWarnings = computed(() => {
  let list = allCertList.value.filter((c) => {
    const s = c.validStatus as string;
    return s === 'EXPIRED' || s === 'EXPIRING' || c.warningLevel;
  });
  if (warningFilters.certType) list = list.filter((c) => c.certificateName === warningFilters.certType);
  if (warningFilters.status) list = list.filter((c) => c.validStatus === warningFilters.status);
  return list;
});

async function loadCertWarnings() { loadAllCertificates(); }
function resetWarnings() { warningFilters.certType = ''; warningFilters.status = ''; }

function openCertificates(row: typeof employeeList.value[number]) {
  certEmployeeFilter.value = row.id;
  activeTab.value = 'certificates';
  loadAllCertificates();
}

/* ========== 显示 ========== */
function certStatusType(s?: string) {
  switch (s) { case 'VALID': return 'success'; case 'EXPIRING': return 'warning'; case 'EXPIRED': case 'REVOKED': return 'danger'; default: return 'info'; }
}
function certStatusLabel(s?: string) {
  switch (s) { case 'VALID': return '有效'; case 'EXPIRING': return '即将到期'; case 'EXPIRED': return '已过期'; case 'REVOKED': return '已吊销'; default: return s || '-'; }
}

onMounted(loadEmployees);
</script>
