<template>
  <PageShell title="同步冲突复核">
    <!-- 子 tab 切换 -->
    <el-tabs v-model="activeTab">
      <el-tab-pane label="冲突列表" name="conflicts" />
      <el-tab-pane label="同步日志" name="logs" />
      <el-tab-pane label="同步任务" name="tasks" />
    </el-tabs>

    <!-- ========== 冲突列表 ========== -->
    <template v-if="activeTab === 'conflicts'">
      <DataTable :data="conflictList" :loading="loading" :total="conflictList.length" :page-num="1" :page-size="conflictList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column prop="conflictNo" label="冲突编号" min-width="130" show-overflow-tooltip />
        <el-table-column label="冲突类型" width="150">
          <template #default="{ row }">{{ entityTypeLabel(row.entityType) }}</template>
        </el-table-column>
        <el-table-column label="工单" width="90">
          <template #default="{ row }">{{ row.workOrderId || '-' }}</template>
        </el-table-column>
        <el-table-column label="解决状态" width="110">
          <template #default="{ row }">
            <el-tag :type="resolveStatusTag(row.resolveStatus)" size="small" effect="dark">
              {{ resolveStatusLabel(row.resolveStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="本地ID" width="120">
          <template #default="{ row }">{{ row.localId || '-' }}</template>
        </el-table-column>
        <el-table-column label="服务端ID" width="100">
          <template #default="{ row }">{{ row.serverId || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row.id)">查看详情</el-button>
            <el-button
              v-if="row.resolveStatus === 'PENDING'"
              link
              type="warning"
              size="small"
              @click="openResolve(row)"
            >处理</el-button>
          </template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 同步日志 ========== -->
    <template v-if="activeTab === 'logs'">
      <DataTable :data="logList" :loading="logsLoading" :total="logList.length" :page-num="1" :page-size="logList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column label="模块" width="120">
          <template #default="{ row }">{{ row.moduleType || row.entityType || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }">{{ row.actionType || row.syncAction || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :value="(row.syncStatus as string) || (row.status as string)" enum-type="sync" />
          </template>
        </el-table-column>
        <el-table-column label="设备ID" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ row.deviceId || '-' }}</template>
        </el-table-column>
        <el-table-column label="消息" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.message || row.errorMessage || '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt as string || row.syncTime as string) }}</template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 同步任务 ========== -->
    <template v-if="activeTab === 'tasks'">
      <DataTable :data="taskList" :loading="tasksLoading" :total="taskList.length" :page-num="1" :page-size="taskList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column label="任务编号" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.taskNo || row.batchId || '-' }}</template>
        </el-table-column>
        <el-table-column label="业务类型" width="110">
          <template #default="{ row }">{{ row.businessType || row.moduleType || '-' }}</template>
        </el-table-column>
        <el-table-column label="任务状态" width="110">
          <template #default="{ row }">
            <StatusTag :value="(row.taskStatus as string) || (row.syncStatus as string)" enum-type="sync" />
          </template>
        </el-table-column>
        <el-table-column label="成功数" width="80">
          <template #default="{ row }">{{ row.successCount || '-' }}</template>
        </el-table-column>
        <el-table-column label="失败数" width="80">
          <template #default="{ row }">{{ row.failedCount || row.failCount || '-' }}</template>
        </el-table-column>
        <el-table-column label="冲突数" width="80">
          <template #default="{ row }">
            <span :style="{ color: (row.conflictCount || 0) > 0 ? '#f56c6c' : '' }">{{ row.conflictCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="设备" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.deviceId || '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt as string || row.startTime as string) }}</template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 冲突详情对比弹窗 ========== -->
    <el-dialog v-model="detailVisible" title="冲突详情" width="960px" destroy-on-close>
      <template v-if="detailItem">
        <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
          <el-descriptions-item label="冲突编号">{{ detailItem.conflictNo }}</el-descriptions-item>
          <el-descriptions-item label="解决状态">
            <el-tag :type="resolveStatusTag(detailItem.resolveStatus)" size="small">{{ resolveStatusLabel(detailItem.resolveStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="冲突类型">{{ entityTypeLabel(detailItem.entityType) }}</el-descriptions-item>
          <el-descriptions-item label="工单ID">{{ detailItem.workOrderId || '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin:12px 0 8px">数据对比</h4>
        <el-row :gutter="12">
          <!-- 本地版本 -->
          <el-col :span="12">
            <div class="compare-panel compare-panel--local">
              <div class="compare-panel__title">
                <el-tag type="warning" size="small" effect="dark">本地版本</el-tag>
                <span style="font-size:12px;color:#64748b">设备 {{ localParsed?.deviceId || '-' }}</span>
              </div>
              <pre class="compare-panel__json">{{ formatJson(detailItem.clientPayload) }}</pre>
            </div>
          </el-col>
          <!-- 服务器版本 -->
          <el-col :span="12">
            <div class="compare-panel compare-panel--server">
              <div class="compare-panel__title">
                <el-tag type="primary" size="small" effect="dark">服务器版本</el-tag>
              </div>
              <pre class="compare-panel__json">{{ formatJson(detailItem.serverPayload) }}</pre>
            </div>
          </el-col>
        </el-row>

        <!-- 差异字段高亮 -->
        <div v-if="diffFields.length" class="diff-highlight">
          <el-icon :size="16" color="#f56c6c"><WarningFilled /></el-icon>
          <span>冲突字段：</span>
          <el-tag v-for="f in diffFields" :key="f" type="danger" size="small" style="margin:2px">{{ f }}</el-tag>
        </div>
      </template>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="!detailItem || detailItem.resolveStatus !== 'PENDING'" @click="detailItem && openResolve(detailItem)">处理此冲突</el-button>
      </template>
    </el-dialog>

    <!-- ========== 处理冲突弹窗 ========== -->
    <FormDialog v-model="resolveVisible" title="处理冲突" :confirm-loading="resolveSubmitting" @confirm="submitResolve">
      <template v-if="resolveItem">
        <el-alert
          type="warning"
          title="冲突数据不能直接丢弃，请仔细对比后选择处理策略。处理后将写入操作日志。"
          :closable="false"
          show-icon
          style="margin-bottom:16px"
        />

        <el-radio-group v-model="resolveForm.resolveStrategy" style="display:flex;flex-direction:column;gap:12px;margin-bottom:16px">
          <el-radio value="KEEP_SERVER" border>
            <strong>保留服务器版本</strong>
            <p style="margin:4px 0 0;font-size:12px;color:#64748b">以服务器数据为准，丢弃本地修改</p>
          </el-radio>
          <el-radio value="KEEP_LOCAL" border>
            <strong>保留本地版本</strong>
            <p style="margin:4px 0 0;font-size:12px;color:#64748b">以移动端本地数据为准，覆盖服务器版本</p>
          </el-radio>
          <el-radio value="MERGE" border>
            <strong>手动合并</strong>
            <p style="margin:4px 0 0;font-size:12px;color:#64748b">在下方编辑最终数据</p>
          </el-radio>
        </el-radio-group>

        <el-form-item v-if="resolveForm.resolveStrategy === 'MERGE'" label="合并后数据" required>
          <el-input
            v-model="resolveForm.finalPayload"
            type="textarea"
            :rows="6"
            placeholder="编辑 JSON 格式的最终版本数据"
          />
        </el-form-item>

        <el-form-item label="处理说明" required>
          <el-input
            v-model="resolveForm.resolveComment"
            type="textarea"
            :rows="2"
            placeholder="请填写处理说明（必填）"
          />
        </el-form-item>

        <el-alert
          v-if="resolveError"
          :title="resolveError"
          type="error"
          show-icon
          :closable="false"
        />
      </template>
    </FormDialog>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { WarningFilled } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  fetchSyncConflicts, fetchSyncConflict, resolveSyncConflict,
  fetchSyncLogs, fetchSyncTasks,
  type SyncConflictItem
} from '@/api/sync';
import PageShell from '@/components/page/PageShell.vue';
import DataTable from '@/components/table/DataTable.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import FormDialog from '@/components/dialog/FormDialog.vue';
import { formatDateTime } from '@/utils/date';

/* ========== Tab ========== */
const activeTab = ref('conflicts');

/* ========== 冲突列表 ========== */
const loading = ref(false);
const conflictList = ref<SyncConflictItem[]>([]);

async function loadConflicts() {
  loading.value = true;
  try { conflictList.value = await fetchSyncConflicts(); } finally { loading.value = false; }
}

loadConflicts();

/* ========== 冲突详情 ========== */
const detailVisible = ref(false);
const detailItem = ref<SyncConflictItem | null>(null);

async function openDetail(id: number) {
  try {
    detailItem.value = await fetchSyncConflict(id);
    detailVisible.value = true;
  } catch { /* handled */ }
}

/* ========== 冲突处理 ========== */
const resolveVisible = ref(false);
const resolveSubmitting = ref(false);
const resolveItem = ref<SyncConflictItem | null>(null);
const resolveError = ref('');
const resolveForm = reactive({ resolveStrategy: 'KEEP_SERVER', finalPayload: '', resolveComment: '' });

function openResolve(row: SyncConflictItem) {
  resolveItem.value = row;
  resolveError.value = '';
  resolveForm.resolveStrategy = 'KEEP_SERVER';
  resolveForm.finalPayload = '';
  resolveForm.resolveComment = '';
  resolveVisible.value = true;
}

async function submitResolve() {
  if (!resolveForm.resolveComment.trim()) {
    resolveError.value = '处理说明不能为空';
    return;
  }
  if (resolveForm.resolveStrategy === 'MERGE' && !resolveForm.finalPayload.trim()) {
    resolveError.value = '合并后数据不能为空';
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认采用"${({ KEEP_SERVER: '保留服务器版本', KEEP_LOCAL: '保留本地版本', MERGE: '手动合并' })[resolveForm.resolveStrategy]}"策略处理此冲突？此操作不可撤销。`,
      '二次确认',
      { confirmButtonText: '确认处理', cancelButtonText: '取消', type: 'warning' }
    );
  } catch { return; }

  resolveSubmitting.value = true;
  try {
    await resolveSyncConflict(resolveItem.value!.id, {
      resolveStrategy: resolveForm.resolveStrategy,
      finalPayload: resolveForm.resolveStrategy === 'MERGE' ? resolveForm.finalPayload : undefined,
      resolveComment: resolveForm.resolveComment
    });
    ElMessage.success('冲突已处理，移动端下次同步时将获取最终结果');
    resolveVisible.value = false;
    loadConflicts();
  } catch (err: unknown) {
    resolveError.value = err instanceof Error ? err.message : '处理失败';
  } finally { resolveSubmitting.value = false; }
}

/* ========== 数据对比 ========== */
const localParsed = computed(() => {
  try { return detailItem.value?.clientPayload ? JSON.parse(detailItem.value.clientPayload) : null; } catch { return null; }
});
const serverParsed = computed(() => {
  try { return detailItem.value?.serverPayload ? JSON.parse(detailItem.value.serverPayload) : null; } catch { return null; }
});

const diffFields = computed(() => {
  if (!localParsed.value || !serverParsed.value) return [];
  const allKeys = [...new Set([...Object.keys(localParsed.value), ...Object.keys(serverParsed.value)])];
  return allKeys.filter((k) => JSON.stringify(localParsed.value[k]) !== JSON.stringify(serverParsed.value[k]));
});

function formatJson(raw?: string): string {
  if (!raw) return '(无数据)';
  try { return JSON.stringify(JSON.parse(raw), null, 2); } catch { return raw; }
}

/* ========== 同步日志 ========== */
const logsLoading = ref(false); const logList = ref<Record<string, unknown>[]>([]);
async function loadLogs() { logsLoading.value = true; try { logList.value = await fetchSyncLogs(); } finally { logsLoading.value = false; } }

/* ========== 同步任务 ========== */
const tasksLoading = ref(false); const taskList = ref<Record<string, unknown>[]>([]);
async function loadTasks() { tasksLoading.value = true; try { taskList.value = await fetchSyncTasks(); } finally { tasksLoading.value = false; } }

/* ========== 监听 Tab 切换 ========== */
import { watch } from 'vue';
watch(activeTab, (val) => {
  if (val === 'logs' && !logList.value.length) loadLogs();
  if (val === 'tasks' && !taskList.value.length) loadTasks();
});

/* ========== 标签函数 ========== */
function entityTypeLabel(t?: string) {
  const map: Record<string, string> = {
    work_order: '工单信息', work_order_record: '施工记录',
    work_order_attachment: '附件', work_order_signature: '签名记录',
    work_order_acceptance: '验收记录', work_order_material_usage: '物料使用',
    ai_result: 'AI结果', project_info: '项目信息'
  };
  return map[t || ''] || t || '-';
}
function resolveStatusLabel(s?: string) {
  return { PENDING: '待处理', RESOLVED: '已处理', IGNORED: '已忽略' }[s || ''] || s || '-';
}
function resolveStatusTag(s?: string) {
  return s === 'PENDING' ? 'danger' : s === 'RESOLVED' ? 'success' : 'info';
}
</script>

<style scoped>
.compare-panel {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}
.compare-panel--local { border-top: 3px solid #e6a23c; }
.compare-panel--server { border-top: 3px solid #409eff; }
.compare-panel__title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}
.compare-panel__json {
  margin: 0;
  padding: 10px;
  font-size: 12px;
  line-height: 1.5;
  max-height: 300px;
  overflow: auto;
  white-space: pre-wrap;
  background: #fff;
  color: #334155;
}
.diff-highlight {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 10px;
  background: #fef2f2;
  border: 1px solid #fca5a5;
  border-radius: 6px;
  flex-wrap: wrap;
}
</style>
