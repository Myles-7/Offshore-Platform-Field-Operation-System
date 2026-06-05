<template>
  <PageShell title="操作日志与审计轨迹">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="操作日志" name="logs" />
      <el-tab-pane label="工单审计轨迹" name="audit" />
      <el-tab-pane label="同步审计" name="sync" />
    </el-tabs>

    <!-- ========== 操作日志 ========== -->
    <template v-if="activeTab === 'logs'">
      <SearchForm :model="logFilters" @search="handleLogSearch" @reset="resetLogFilters">
        <el-form-item label="用户">
          <el-select v-model="logFilters.operatorId" clearable filterable placeholder="全部" style="width:160px">
            <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="logFilters.module" clearable placeholder="全部" style="width:120px">
            <el-option label="工单" value="WORK_ORDER" />
            <el-option label="物料" value="MATERIAL" />
            <el-option label="资质" value="QUALIFICATION" />
            <el-option label="验收" value="ACCEPTANCE" />
            <el-option label="同步" value="SYNC" />
            <el-option label="AI" value="AI" />
            <el-option label="看板" value="DASHBOARD" />
            <el-option label="知识库" value="KNOWLEDGE" />
            <el-option label="系统" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="logFilters.opType" clearable placeholder="全部" style="width:140px">
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="分派" value="ASSIGN" />
            <el-option label="导出" value="EXPORT" />
            <el-option label="登录" value="LOGIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="logFilters.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" format="YYYY-MM-DD" value-format="YYYY-MM-DD HH:mm:ss" style="width:260px" />
        </el-form-item>
      </SearchForm>

      <DataTable :data="filteredLogs" :loading="logLoading" :total="filteredLogs.length" :page-num="1" :page-size="filteredLogs.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column prop="operatorName" label="操作人" width="90" />
        <el-table-column label="模块" width="80">
          <template #default="{ row }">{{ moduleLabel(row.moduleName) }}</template>
        </el-table-column>
        <el-table-column label="操作类型" width="90">
          <template #default="{ row }">{{ opTypeLabel(row.operationType) }}</template>
        </el-table-column>
        <el-table-column label="业务类型" width="100">
          <template #default="{ row }">{{ row.businessType || '-' }}</template>
        </el-table-column>
        <el-table-column label="业务编号" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.businessNo || row.businessId || '-' }}</template>
        </el-table-column>
        <el-table-column label="角色" width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.roleCode || '-' }}</template>
        </el-table-column>
        <el-table-column label="平台" width="70">
          <template #default="{ row }"><el-tag :type="row.platform === 'MOBILE' ? 'success' : 'primary'" size="small">{{ row.platform || 'PC' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="结果" width="70">
          <template #default="{ row }"><el-tag :type="row.resultStatus === 'SUCCESS' ? 'success' : 'danger'" size="small">{{ row.resultStatus === 'SUCCESS' ? '成功' : '失败' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="接口" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.requestMethod }} {{ row.requestPath }}</template>
        </el-table-column>
        <el-table-column label="IP" width="120"><template #default="{ row }">{{ row.requestIp || '-' }}</template></el-table-column>
        <el-table-column label="设备" width="110" show-overflow-tooltip><template #default="{ row }">{{ row.deviceId || '-' }}</template></el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.operationTime) }}</template>
        </el-table-column>
        <el-table-column label="" width="50" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openLogDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 工单审计轨迹 ========== -->
    <template v-if="activeTab === 'audit'">
      <SearchForm :model="auditFilters" @search="loadAuditTrail" @reset="resetAuditFilters">
        <el-form-item label="工单ID" required>
          <el-input-number v-model="auditFilters.workOrderId" :min="1" placeholder="输入工单ID" style="width:200px" />
        </el-form-item>
      </SearchForm>

      <AppState v-if="!auditFilters.workOrderId" type="empty" title="请输入工单 ID 查看审计轨迹" />
      <DataTable v-else :data="auditList" :loading="auditLoading" :total="auditList.length" :page-num="1" :page-size="auditList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.operationTime || row.eventTime || row.time as string) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ row.eventType || row.operationType || row.actionType || '-' }}</template>
        </el-table-column>
        <el-table-column label="描述" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.description || row.operationDesc || row.title || row.message || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作人" width="90">
          <template #default="{ row }">{{ row.operatorName || row.operator_name || '-' }}</template>
        </el-table-column>
        <el-table-column label="平台" width="70">
          <template #default="{ row }"><el-tag size="small">{{ row.platform || '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="结果" width="70">
          <template #default="{ row }"><el-tag :type="row.resultStatus === 'SUCCESS' ? 'success' : 'danger'" size="small">{{ row.resultStatus === 'SUCCESS' ? '成功' : '失败' }}</el-tag></template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 同步审计轨迹 ========== -->
    <template v-if="activeTab === 'sync'">
      <DataTable :data="syncAuditList" :loading="syncAuditLoading" :total="syncAuditList.length" :page-num="1" :page-size="syncAuditList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.operationTime || row.syncTime || row.time as string) }}</template>
        </el-table-column>
        <el-table-column label="设备" width="120" show-overflow-tooltip><template #default="{ row }">{{ row.deviceId || '-' }}</template></el-table-column>
        <el-table-column label="操作" width="90"><template #default="{ row }">{{ row.operationType || row.syncAction || '-' }}</template></el-table-column>
        <el-table-column label="描述" min-width="180" show-overflow-tooltip><template #default="{ row }">{{ row.description || row.message || '-' }}</template></el-table-column>
        <el-table-column label="结果" width="70"><template #default="{ row }"><el-tag :type="row.resultStatus === 'SUCCESS' ? 'success' : 'danger'" size="small">{{ row.resultStatus === 'SUCCESS' ? '成功' : '失败' }}</el-tag></template></el-table-column>
      </DataTable>
    </template>

    <!-- ========== 日志详情弹窗 ========== -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px" destroy-on-close>
      <el-descriptions v-if="logDetail" :column="2" border size="small">
        <el-descriptions-item label="操作人">{{ logDetail.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ logDetail.roleCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ moduleLabel(logDetail.moduleName as string) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ opTypeLabel(logDetail.operationType as string) }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ logDetail.businessType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务编号">{{ logDetail.businessNo || logDetail.businessId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="平台">{{ logDetail.platform || 'PC' }}</el-descriptions-item>
        <el-descriptions-item label="设备">{{ logDetail.deviceId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ logDetail.requestMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求路径">{{ logDetail.requestPath || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求IP">{{ logDetail.requestIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户端">{{ logDetail.userAgent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结果"><el-tag :type="logDetail.resultStatus === 'SUCCESS' ? 'success' : 'danger'" size="small">{{ logDetail.resultStatus === 'SUCCESS' ? '成功' : '失败' }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="错误码">{{ logDetail.errorCode || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="logDetail.errorMessage" label="错误信息" :span="2"><span style="color:#f56c6c">{{ logDetail.errorMessage }}</span></el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDateTime(logDetail.operationTime as string) }}</el-descriptions-item>
        <el-descriptions-item label="TraceId">{{ logDetail.traceId || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { request } from '@/api/request';
import { fetchAllUsers, type AdminUserItem } from '@/api/system';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import AppState from '@/components/state/AppState.vue';
import { formatDateTime } from '@/utils/date';

const activeTab = ref('logs');

/* ========== 工具函数 ========== */
function moduleLabel(m?: string) {
  const map: Record<string, string> = { WORK_ORDER: '工单', MATERIAL: '物料', QUALIFICATION: '资质', ACCEPTANCE: '验收', SYNC: '同步', AI: 'AI', DASHBOARD: '看板', KNOWLEDGE: '知识库', FILE: '文件', SYSTEM: '系统' };
  return map[m || ''] || m || '-';
}
function opTypeLabel(t?: string) {
  const map: Record<string, string> = { CREATE: '创建', UPDATE: '更新', DELETE: '删除', ASSIGN: '分派', EXPORT: '导出', LOGIN: '登录', REVIEW: '复核', GENERATE: '生成', DOWNLOAD: '下载', IMPORT: '导入' };
  return map[t || ''] || t || '-';
}

/* ========== 用户选项 ========== */
const userOptions = ref<{ label: string; value: number }[]>([]);
onMounted(async () => { try { const res = await fetchAllUsers(); userOptions.value = (res.records ?? []).map((u: AdminUserItem) => ({ label: `${u.realName} (${u.username})`, value: u.id })); } catch { /* */ } });

/* ========== 操作日志 ========== */
const logLoading = ref(false); const logList = ref<Record<string, unknown>[]>([]);
const logFilters = reactive({ operatorId: '' as number | string, module: '', opType: '', dateRange: null as [string, string] | null });

const filteredLogs = computed(() => logList.value.filter((r) => {
  if (logFilters.operatorId && r.operatorId != logFilters.operatorId) return false;
  if (logFilters.module && r.moduleName !== logFilters.module) return false;
  if (logFilters.opType && !(r.operationType as string || '').includes(logFilters.opType)) return false;
  return true;
}));

async function loadLogs() { logLoading.value = true; try { logList.value = await request({ url: '/admin/operation-logs', method: 'GET' }); } finally { logLoading.value = false; } }
function handleLogSearch() {}; function resetLogFilters() { logFilters.operatorId = ''; logFilters.module = ''; logFilters.opType = ''; logFilters.dateRange = null; }

/* ========== 日志详情 ========== */
const detailVisible = ref(false); const logDetail = ref<Record<string, unknown> | null>(null);
function openLogDetail(row: Record<string, unknown>) { logDetail.value = row; detailVisible.value = true; }

/* ========== 工单审计轨迹 ========== */
const auditLoading = ref(false); const auditList = ref<Record<string, unknown>[]>([]);
const auditFilters = reactive({ workOrderId: undefined as number | undefined });
async function loadAuditTrail() { if (!auditFilters.workOrderId) { auditList.value = []; return; } auditLoading.value = true; try { auditList.value = await request({ url: `/admin/work-orders/${auditFilters.workOrderId}/audit-trail`, method: 'GET' }); } finally { auditLoading.value = false; } }
function resetAuditFilters() { auditFilters.workOrderId = undefined; auditList.value = []; }

/* ========== 同步审计轨迹 ========== */
const syncAuditLoading = ref(false); const syncAuditList = ref<Record<string, unknown>[]>([]);
async function loadSyncAudit() { syncAuditLoading.value = true; try { syncAuditList.value = await request({ url: '/admin/sync/audit-trail', method: 'GET' }); } finally { syncAuditLoading.value = false; } }

watch(activeTab, (v) => {
  if (v === 'sync' && !syncAuditList.value.length) loadSyncAudit();
});

onMounted(loadLogs);
</script>
