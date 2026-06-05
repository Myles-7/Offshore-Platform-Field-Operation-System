<template>
  <PageShell :title="`工单详情 — ${detail?.workOrder.workOrderNo || '-'}`">
    <template #actions>
      <el-button @click="$router.back()">返回列表</el-button>
      <el-button type="primary" @click="openAssign">分派</el-button>
      <el-button v-if="detail?.workOrder.status !== 'CLOSED'" type="warning" @click="openStatus">修改状态</el-button>
    </template>

    <AppState v-if="pageError" type="error" action-text="重试" @action="loadDetail" />
    <AppState v-else-if="loading && !detail" type="loading" />

    <template v-else-if="detail">
      <!-- 同步冲突提示 -->
      <el-alert
        v-if="detail.syncSummary.hasConflict"
        type="error"
        title="此工单存在同步冲突"
        :description="`冲突数量：${detail.syncSummary.conflictCount}，请前往同步冲突页面处理。`"
        show-icon
        :closable="false"
      />

      <!-- 1. 工单基础信息 -->
      <div class="detail-card">
        <header class="detail-card__header">
          <h3>工单基础信息</h3>
          <div style="display:flex;gap:8px;align-items:center">
            <StatusTag :value="detail.workOrder.status" enum-type="workOrder" />
            <StatusTag v-if="detail.workOrder.priority" :value="detail.workOrder.priority" enum-type="priority" />
            <el-tag v-if="detail.workOrder.abnormalFlag" type="danger" size="small" effect="dark">异常</el-tag>
            <StatusTag :value="detail.workOrder.syncStatus" enum-type="sync" />
          </div>
        </header>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="工单编号">{{ detail.workOrder.workOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="作业标题">{{ detail.workOrder.workTitle }}</el-descriptions-item>
          <el-descriptions-item label="工单类型">{{ workTypeLabel(detail.workOrder.workType) }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ detail.workOrder.projectName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="作业地点">{{ detail.workOrder.workLocation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ detail.workOrder.leaderName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="维修工">{{ detail.workOrder.maintainerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划开始">{{ formatDateTime(detail.workOrder.plannedStartTime) }}</el-descriptions-item>
          <el-descriptions-item label="计划结束">{{ formatDateTime(detail.workOrder.plannedEndTime) }}</el-descriptions-item>
          <el-descriptions-item label="实际开始">{{ formatDateTime(detail.workOrder.actualStartTime) }}</el-descriptions-item>
          <el-descriptions-item label="实际结束">{{ formatDateTime(detail.workOrder.actualEndTime) }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ detail.workOrder.sourceType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detail.workOrder.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="1" border style="margin-top:12px">
          <el-descriptions-item label="作业内容">{{ detail.workOrder.workContent || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所需物料">{{ detail.workOrder.requiredMaterialDesc || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.workOrder.rejectReason" label="驳回原因">
            <span style="color:#f56c6c">{{ detail.workOrder.rejectReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.workOrder.closeReason" label="关闭原因">
            <span style="color:#909399">{{ detail.workOrder.closeReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.workOrder.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 2. 工单状态流转图 -->
      <div class="detail-card">
        <header class="detail-card__header">
          <h3>状态流转</h3>
          <el-button link type="primary" size="small" @click="loadStatusFlow">刷新</el-button>
        </header>
        <AppState v-if="statusFlowLoading" type="loading" />
        <el-steps v-else-if="statusFlow.length" :active="statusFlow.length - 1" align-center finish-status="success">
          <el-step
            v-for="sf in statusFlow"
            :key="sf.id"
            :title="statusLabel(sf.toStatus)"
            :description="sf.operationDesc || sf.operationType"
          >
            <template #subtitle>
              <small style="color:#64748b">{{ formatDateTime(sf.operationTime) }}</small>
            </template>
          </el-step>
        </el-steps>
        <AppState v-else type="empty" title="暂无状态流转记录" />
      </div>

      <!-- 3. 派工信息 -->
      <div v-if="detail.assignments.length" class="detail-card">
        <header class="detail-card__header"><h3>派工信息</h3></header>
        <el-table :data="detail.assignments" border size="small">
          <el-table-column type="index" label="序号" width="55" />
          <el-table-column prop="assignerId" label="分派人ID" width="100" />
          <el-table-column prop="assigneeId" label="维修工ID" width="100" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">{{ row.assignmentRole === 'MAINTAINER' ? '主维修工' : row.assignmentRole }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">{{ row.assignmentStatus }}</template>
          </el-table-column>
          <el-table-column prop="assignedAt" label="分派时间" width="160" />
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 4. 施工记录时间线 -->
      <div class="detail-card">
        <header class="detail-card__header">
          <h3>施工记录</h3>
          <el-button link type="primary" size="small" @click="loadRecords">刷新</el-button>
        </header>
        <AppState v-if="recordsLoading" type="loading" />
        <el-timeline v-else-if="records.length">
          <el-timeline-item
            v-for="rec in records"
            :key="rec.id"
            :timestamp="formatDateTime(rec.constructionTime || rec.submittedAt)"
            placement="top"
            :type="rec.abnormalFlag ? 'danger' : 'primary'"
            :hollow="rec.conflictFlag ? true : false"
          >
            <div class="timeline-card">
              <div class="timeline-card__header">
                <strong>{{ rec.constructionUserName || `用户${rec.constructionUserId}` }}</strong>
                <span class="timeline-card__meta">
                  {{ rec.recordType || '-' }} | 地点：{{ rec.locationName || '-' }}
                  <el-tag v-if="rec.abnormalFlag" type="danger" size="small" effect="dark">异常</el-tag>
                  <StatusTag :value="rec.syncStatus" enum-type="sync" />
                </span>
              </div>
              <p>{{ rec.constructionDesc || '-' }}</p>
              <div v-if="rec.siteCondition" style="color:#64748b;font-size:12px">现场情况：{{ rec.siteCondition }}</div>
              <div v-if="rec.weather" style="color:#64748b;font-size:12px">
                天气：{{ rec.weather }}
                <template v-if="rec.temperature != null"> | 温度：{{ rec.temperature }}°C</template>
                <template v-if="rec.humidity != null"> | 湿度：{{ rec.humidity }}%</template>
              </div>

              <!-- 附件摘要 -->
              <div v-if="rec.attachments?.length" class="timeline-sub">
                <span>附件（{{ rec.attachmentCount || rec.attachments.length }}）：</span>
                <el-tag
                  v-for="att in rec.attachments"
                  :key="att.id"
                  size="small"
                  style="margin:2px"
                  :type="att.conflictFlag ? 'danger' : 'info'"
                >
                  {{ att.attachmentType }}: {{ att.attachmentName }}
                  <el-tag v-if="att.uploadStatus" size="small" effect="plain" style="margin-left:2px">
                    {{ att.uploadStatus }}
                  </el-tag>
                </el-tag>
              </div>

              <!-- AI 识别摘要 -->
              <div v-if="rec.aiResults?.length" class="timeline-sub">
                <span style="color:#7c3aed">AI 识别（{{ rec.aiResultCount || rec.aiResults.length }}）：</span>
                <el-tag
                  v-for="ai in rec.aiResults"
                  :key="ai.id"
                  size="small"
                  style="margin:2px"
                  :type="ai.suspectedDefectFlag ? 'warning' : 'success'"
                >
                  {{ ai.defectType }}（{{ ai.defectCount }}处）
                  <span style="font-size:10px;margin-left:2px">{{ ai.reviewStatus }}</span>
                </el-tag>
              </div>

              <!-- 检查项 -->
              <div v-if="rec.checkItems?.length" class="timeline-sub">
                <span>检查项（{{ rec.checkItems.length }}）：</span>
                <template v-for="ci in rec.checkItems" :key="ci.id">
                  <el-tag
                    size="small"
                    style="margin:2px"
                    :type="ci.abnormalFlag ? 'danger' : ci.checkResult === 'PASSED' ? 'success' : 'info'"
                  >
                    {{ ci.itemName }}: {{ ci.checkValue || ci.checkResult || '-' }}
                    {{ ci.checkUnit || '' }}
                  </el-tag>
                </template>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <AppState v-else type="empty" title="暂无施工记录" />
      </div>

      <!-- 5. 附件 -->
      <AttachmentSection
        :work-order-id="workOrderId"
        :attachments="attachmentList"
        :loading="attachmentsLoading"
        @refresh="loadAttachments"
      />

      <!-- 6 & 7 & 8. 签名 + 验收 + PDF -->
      <SignatureAcceptanceSection
        :work-order-id="workOrderId"
        :signatures="signatureList"
        :acceptance-list="acceptanceList"
        :pdf-list="pdfList"
        :loading="signatureLoading"
        @refresh="loadSignatureAcceptance"
      />

      <!-- 9. 物料与AI -->
      <el-row :gutter="16">
        <el-col :span="12">
          <div class="detail-card">
            <header class="detail-card__header"><h3>物料使用</h3></header>
            <AppState v-if="!detail.materialUsage.length" type="empty" title="暂无物料使用记录" />
            <ul v-else class="summary-list">
              <li v-for="s in detail.materialUsage" :key="s.id">
                <el-icon><Box /></el-icon>
                <span>{{ s.title }}</span>
                <span style="color:#64748b;margin-left:auto">{{ s.time }} | {{ s.status }}</span>
              </li>
            </ul>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="detail-card">
            <header class="detail-card__header"><h3>AI 识别结果</h3></header>
            <AppState v-if="!detail.aiResults.length" type="empty" title="暂无AI结果" />
            <ul v-else class="summary-list">
              <li v-for="s in detail.aiResults" :key="s.id">
                <el-icon><Aim /></el-icon>
                <span>{{ s.title }}</span>
                <span style="color:#64748b;margin-left:auto">{{ s.time }} | {{ s.status }}</span>
              </li>
            </ul>
          </div>
        </el-col>
      </el-row>

      <!-- 10. 操作日志入口 -->
      <div class="detail-card">
        <header class="detail-card__header">
          <h3>操作日志</h3>
          <el-button link type="primary" size="small" @click="loadAuditTrail">查看</el-button>
        </header>
        <el-dialog v-model="auditVisible" title="操作日志" width="800px" append-to-body>
          <el-table :data="auditTrail" border size="small" max-height="400">
            <el-table-column type="index" label="序号" width="55" />
            <el-table-column prop="moduleName" label="模块" width="100" />
            <el-table-column prop="operationType" label="操作类型" width="120" />
            <el-table-column prop="businessNo" label="业务编号" width="160" />
            <el-table-column prop="operatorName" label="操作人" width="100" />
            <el-table-column prop="resultStatus" label="结果" width="80" />
            <el-table-column prop="operationTime" label="时间" width="160" />
            <el-table-column prop="requestPath" label="接口" min-width="140" show-overflow-tooltip />
          </el-table>
        </el-dialog>
      </div>
    </template>

    <!-- 分派弹窗 -->
    <WorkOrderAssignDialog
      v-if="detail"
      v-model="assignDialogVisible"
      :work-order="detail.workOrder"
      :user-options="userOptions"
      @assigned="onAssigned"
    />

    <!-- 修改状态弹窗 -->
    <FormDialog v-model="statusDialogVisible" title="修改状态" :confirm-loading="statusSubmitting" @confirm="submitStatus">
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="当前状态">
          <StatusTag v-if="detail" :value="detail.workOrder.status" enum-type="workOrder" />
        </el-form-item>
        <el-form-item label="目标状态">
          <el-select v-model="statusForm.status" placeholder="选择目标状态" style="width:100%">
            <el-option v-for="item in allowedTransitions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="statusForm.status === 'REJECTED'" label="驳回原因">
          <el-input v-model="statusForm.rejectReason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="操作说明">
          <el-input v-model="statusForm.operationDesc" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
    </FormDialog>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { Aim, Box } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { changeWorkOrderStatus, fetchAuditTrail, fetchStatusFlow, fetchWorkOrderDetail, fetchWorkOrderRecords, fetchWorkOrderAttachments } from '@/api/admin';
import { fetchAllUsers, type AdminUserItem } from '@/api/system';
import { fetchSignatures, fetchAcceptanceRecords, fetchPdfs, type SignatureVO, type AcceptanceVO, type PdfVO } from '@/api/acceptance';
import PageShell from '@/components/page/PageShell.vue';
import AppState from '@/components/state/AppState.vue';
import FormDialog from '@/components/dialog/FormDialog.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import WorkOrderAssignDialog from '@/components/form/WorkOrderAssignDialog.vue';
import AttachmentSection from '@/components/work-order/AttachmentSection.vue';
import SignatureAcceptanceSection from '@/components/work-order/SignatureAcceptanceSection.vue';
import { workTypeOptions } from '@/constants/enums';
import type { StatusFlowVO, WorkOrderDetailVO, WorkOrderStatusRequest, WorkRecordVO, WorkOrderAttachmentVO } from '@/types/work-order';
import { formatDateTime } from '@/utils/date';

const route = useRoute();
const workOrderId = computed(() => Number(route.params.id));

/* ========== 详情数据 ========== */
const loading = ref(false);
const pageError = ref(false);
const detail = ref<WorkOrderDetailVO | null>(null);

async function loadDetail() {
  loading.value = true;
  pageError.value = false;
  try {
    detail.value = await fetchWorkOrderDetail(workOrderId.value);
  } catch {
    pageError.value = true;
  } finally {
    loading.value = false;
  }
}

/* ========== 状态流转 ========== */
const statusFlowLoading = ref(false);
const statusFlow = ref<StatusFlowVO[]>([]);

async function loadStatusFlow() {
  statusFlowLoading.value = true;
  try {
    statusFlow.value = await fetchStatusFlow(workOrderId.value);
  } finally {
    statusFlowLoading.value = false;
  }
}

function statusLabel(status: string): string {
  const map: Record<string, string> = {
    PENDING_ASSIGN: '待派工', ASSIGNED: '已派工', ACCEPTED: '已接收',
    IN_PROGRESS: '施工中', PENDING_ACCEPTANCE: '待验收', COMPLETED: '已完成',
    REJECTED: '已驳回', CLOSED: '已关闭'
  };
  return map[status] || status;
}

/* ========== 施工记录 ========== */
const recordsLoading = ref(false);
const records = ref<WorkRecordVO[]>([]);

async function loadRecords() {
  recordsLoading.value = true;
  try {
    records.value = await fetchWorkOrderRecords(workOrderId.value);
  } finally {
    recordsLoading.value = false;
  }
}

/* ========== 附件、签名、验收、PDF ========== */
const attachmentsLoading = ref(false);
const attachmentList = ref<WorkOrderAttachmentVO[]>([]);
const signatureLoading = ref(false);
const signatureList = ref<SignatureVO[]>([]);
const acceptanceList = ref<AcceptanceVO[]>([]);
const pdfList = ref<PdfVO[]>([]);

async function loadAttachments() {
  attachmentsLoading.value = true;
  try {
    attachmentList.value = await fetchWorkOrderAttachments(workOrderId.value);
  } catch {
    attachmentList.value = [];
  } finally {
    attachmentsLoading.value = false;
  }
}

async function loadSignatureAcceptance() {
  signatureLoading.value = true;
  try {
    const [sigs, accs, pdfs] = await Promise.all([
      fetchSignatures(workOrderId.value),
      fetchAcceptanceRecords(workOrderId.value),
      fetchPdfs(workOrderId.value)
    ]);
    signatureList.value = sigs;
    acceptanceList.value = accs;
    pdfList.value = pdfs;
  } catch {
    // 静默处理
  } finally {
    signatureLoading.value = false;
  }
}

/* ========== 审计日志 ========== */
const auditVisible = ref(false);
const auditTrail = ref<Record<string, unknown>[]>([]);

async function loadAuditTrail() {
  try {
    auditTrail.value = await fetchAuditTrail(workOrderId.value);
    auditVisible.value = true;
  } catch {
    ElMessage.error('加载操作日志失败');
  }
}

/* ========== 用户选项 ========== */
const userOptions = ref<{ label: string; value: number }[]>([]);

async function loadUserOptions() {
  try {
    const result = await fetchAllUsers();
    userOptions.value = (result.records ?? []).map((u: AdminUserItem) => ({
      label: `${u.realName} (${u.username})`,
      value: u.id
    }));
  } catch { /* 非关键 */ }
}

/* ========== 工具函数 ========== */
function workTypeLabel(type?: string): string {
  return workTypeOptions.find((o) => o.value === type)?.label ?? (type || '-');
}

/* ========== 分派 ========== */
const assignDialogVisible = ref(false);

function openAssign() {
  assignDialogVisible.value = true;
}

function onAssigned() {
  loadDetail();
  loadStatusFlow();
}

/* ========== 状态修改 ========== */
const statusDialogVisible = ref(false);
const statusSubmitting = ref(false);
const statusForm = reactive<WorkOrderStatusRequest>({ status: '', operationDesc: '' });

const transitionMap: Record<string, { label: string; value: string }[]> = {
  PENDING_ASSIGN: [{ label: '已派工', value: 'ASSIGNED' }, { label: '已关闭', value: 'CLOSED' }],
  ASSIGNED: [{ label: '施工中', value: 'IN_PROGRESS' }, { label: '已驳回', value: 'REJECTED' }, { label: '已关闭', value: 'CLOSED' }],
  IN_PROGRESS: [{ label: '待验收', value: 'PENDING_ACCEPTANCE' }, { label: '已驳回', value: 'REJECTED' }, { label: '已关闭', value: 'CLOSED' }],
  PENDING_ACCEPTANCE: [{ label: '已完成', value: 'COMPLETED' }, { label: '已驳回', value: 'REJECTED' }],
  COMPLETED: [{ label: '已关闭', value: 'CLOSED' }],
  REJECTED: [{ label: '施工中', value: 'IN_PROGRESS' }, { label: '已关闭', value: 'CLOSED' }],
  ACCEPTED: [{ label: '施工中', value: 'IN_PROGRESS' }, { label: '已关闭', value: 'CLOSED' }]
};

const allowedTransitions = computed(() => {
  const st = detail.value?.workOrder.status;
  return st ? (transitionMap[st] ?? []) : [];
});

function openStatus() {
  statusForm.status = '';
  statusForm.operationDesc = '';
  statusForm.rejectReason = '';
  statusForm.closeReason = '';
  statusDialogVisible.value = true;
}

async function submitStatus() {
  if (!statusForm.status) { ElMessage.warning('请选择目标状态'); return; }
  statusSubmitting.value = true;
  try {
    await changeWorkOrderStatus(workOrderId.value, {
      status: statusForm.status,
      operationDesc: statusForm.operationDesc || undefined,
      rejectReason: statusForm.status === 'REJECTED' ? statusForm.rejectReason || undefined : undefined,
      closeReason: statusForm.status === 'CLOSED' ? statusForm.closeReason || undefined : undefined
    });
    ElMessage.success('状态修改成功');
    statusDialogVisible.value = false;
    loadDetail();
    loadStatusFlow();
  } finally {
    statusSubmitting.value = false;
  }
}

/* ========== 生命周期 ========== */
onMounted(() => {
  loadDetail().then(() => {
    loadStatusFlow();
    loadRecords();
  });
  loadUserOptions();
  loadAttachments();
  loadSignatureAcceptance();
});
</script>

<style scoped>
.detail-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.detail-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.detail-card__header h3 {
  margin: 0;
  font-size: 15px;
  color: #334155;
}

.timeline-card {
  padding: 8px 12px;
  border-radius: 6px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
}

.timeline-card__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.timeline-card__meta {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
  font-size: 12px;
}

.timeline-card p {
  margin: 4px 0;
  color: #334155;
}

.timeline-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.summary-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.summary-list li {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;
  font-size: 13px;
  border-bottom: 1px solid #f3f4f6;
}
</style>
