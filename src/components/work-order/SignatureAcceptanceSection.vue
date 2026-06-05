<template>
  <div class="detail-card">
    <header class="detail-card__header">
      <h3>电子签名</h3>
      <el-button link type="primary" size="small" :icon="Refresh" @click="$emit('refresh')">刷新</el-button>
    </header>

    <AppState v-if="loading" type="loading" />
    <AppState v-else-if="!signatures.length" type="empty" title="暂无签名" />

    <div v-else class="signature-grid">
      <div v-for="sig in signatures" :key="sig.id" class="signature-card" @click="previewSig = sig">
        <div class="signature-card__img">
          <img v-if="sig.fileId" :src="previewUrl(sig.fileId)" loading="lazy" style="max-width:100%;max-height:160px" />
          <el-icon v-else :size="48" color="#cbd5e1"><EditPen /></el-icon>
        </div>
        <div class="signature-card__info">
          <strong>{{ sig.signerName || '-' }}</strong>
          <span>{{ roleLabel(sig.signatureRole) }}</span>
          <small>{{ formatDateTime(sig.signedAt) }}</small>
          <StatusTag :value="sig.signatureStatus || sig.syncStatus" :enum-type="'sync'" />
        </div>
      </div>
    </div>

    <!-- 签名大图预览 -->
    <FilePreviewDialog
      v-model="sigPreviewVisible"
      :file-id="previewSig?.fileId"
      :file-type="'SIGNATURE'"
      :title="`签名 — ${previewSig?.signerName || '-'}`"
      :signer-name="previewSig?.signerName"
      :signature-role="previewSig?.signatureRole"
      :signed-at="previewSig?.signedAt"
      :signature-status="previewSig?.signatureStatus"
    />
  </div>

  <!-- 验收记录 -->
  <div class="detail-card">
    <header class="detail-card__header">
      <h3>验收记录</h3>
      <div style="display:flex;gap:8px">
        <el-button
          v-if="acceptanceList.length && !allLocked"
          type="primary"
          size="small"
          @click="openReviewDialog"
        >验收复核</el-button>
        <el-button
          v-if="acceptanceList.length && !hasPdfGenerated"
          type="success"
          size="small"
          @click="handleGeneratePdf"
        >生成 PDF</el-button>
        <el-button link type="primary" size="small" :icon="Refresh" @click="$emit('refresh')">刷新</el-button>
      </div>
    </header>

    <AppState v-if="emptyState" type="empty" title="暂无验收记录" />

    <el-table v-else :data="acceptanceList" border size="small">
      <el-table-column prop="acceptanceNo" label="验收编号" width="140" />
      <el-table-column label="验收状态" width="110">
        <template #default="{ row }">
          <el-tag :type="acceptStatusTag(row.acceptanceStatus)" size="small">
            {{ acceptStatusLabel(row.acceptanceStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="acceptanceResult" label="结果" width="90" />
      <el-table-column prop="acceptanceOpinion" label="验收意见" min-width="140" show-overflow-tooltip />
      <el-table-column prop="problemDesc" label="问题描述" min-width="120" show-overflow-tooltip />
      <el-table-column label="签名数" width="70">
        <template #default="{ row }">{{ row.signatureCount || 0 }}</template>
      </el-table-column>
      <el-table-column label="锁定" width="70">
        <template #default="{ row }">
          <el-tag :type="row.lockedFlag ? 'danger' : 'success'" size="small">
            {{ row.lockedFlag ? '已锁定' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="同步" width="100">
        <template #default="{ row }"><StatusTag :value="row.syncStatus" enum-type="sync" /></template>
      </el-table-column>
      <el-table-column label="更新时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
    </el-table>
  </div>

  <!-- PDF 验收单 -->
  <div class="detail-card">
    <header class="detail-card__header">
      <h3>PDF 验收单</h3>
      <div style="display:flex;gap:8px">
        <el-button
          v-if="pdfList.length"
          type="primary"
          size="small"
          @click="handlePdfDownload"
        >下载最新 PDF</el-button>
        <el-button link type="primary" size="small" :icon="Refresh" @click="$emit('refresh')">刷新</el-button>
      </div>
    </header>

    <AppState v-if="!pdfList.length" type="empty" title="暂无 PDF" />

    <el-table v-else :data="pdfList" border size="small">
      <el-table-column prop="pdfNo" label="PDF编号" width="140" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="pdfStatusTag(row.pdfStatus)" size="small">{{ pdfStatusLabel(row.pdfStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="锁定" width="70">
        <template #default="{ row }">
          <el-tag :type="row.lockedFlag ? 'danger' : 'success'" size="small">{{ row.lockedFlag ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="生成时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.generatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openPdfPreview(row)">预览</el-button>
          <a v-if="row.downloadUrl" :href="row.downloadUrl" target="_blank" style="margin-left:8px">下载</a>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <!-- 验收复核弹窗 -->
  <FormDialog v-model="reviewVisible" title="验收复核" :confirm-loading="reviewSubmitting" @confirm="submitReview">
    <el-form :model="reviewForm" label-width="100px">
      <el-form-item label="验收结果">
        <el-radio-group v-model="reviewForm.acceptanceStatus">
          <el-radio-button value="PASSED">审核通过</el-radio-button>
          <el-radio-button value="REJECTED">驳回验收</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="验收意见">
        <el-input v-model="reviewForm.acceptanceOpinion" type="textarea" :rows="2" placeholder="验收意见" />
      </el-form-item>
      <el-form-item v-if="reviewForm.acceptanceStatus === 'REJECTED'" label="驳回原因" required>
        <el-input v-model="reviewForm.rejectReason" type="textarea" :rows="2" placeholder="请填写驳回原因" />
      </el-form-item>
      <el-alert type="info" show-icon :closable="false" title="AI 仅作辅助，最终以人工验收为准" />
    </el-form>
  </FormDialog>

  <!-- PDF 预览 -->
  <FilePreviewDialog
    v-model="pdfPreviewVisible"
    :file-id="previewPdf?.fileId"
    :file-type="'PDF'"
    :title="previewPdf?.pdfNo || 'PDF 验收单'"
    :allow-delete="false"
    :protected="true"
  />
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { EditPen, Refresh } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { pdfDownloadUrl, generatePdf, reviewAcceptance } from '@/api/acceptance';
import { fetchFilePreviewUrl } from '@/api/file';
import type { AcceptanceVO, PdfVO, SignatureVO } from '@/api/acceptance';
import AppState from '@/components/state/AppState.vue';
import FormDialog from '@/components/dialog/FormDialog.vue';
import FilePreviewDialog from '@/components/file/FilePreviewDialog.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import { formatDateTime } from '@/utils/date';

const props = withDefaults(
  defineProps<{
    workOrderId: number;
    signatures: SignatureVO[];
    acceptanceList: AcceptanceVO[];
    pdfList: PdfVO[];
    loading?: boolean;
  }>(),
  { loading: false }
);

const emit = defineEmits<{
  refresh: [];
}>();

/* ========== 签名预览 ========== */
const sigPreviewVisible = ref(false);
const previewSig = ref<SignatureVO | null>(null);

function previewUrl(fileId: string) {
  return fileId ? fetchFilePreviewUrl(fileId) : '';
}

function roleLabel(role: string) {
  const map: Record<string, string> = { MAINTAINER: '维修工', LEADER: '负责人', ACCEPTOR: '验收人' };
  return map[role] || role || '-';
}

const allLocked = computed(() => props.acceptanceList.every((a) => a.lockedFlag));
const hasPdfGenerated = computed(() => props.pdfList.some((p) => p.pdfStatus === 'GENERATED' || p.pdfStatus === 'ARCHIVED'));

/* ========== 验收复核 ========== */
const reviewVisible = ref(false);
const reviewSubmitting = ref(false);
const reviewForm = reactive({
  acceptanceStatus: 'PASSED',
  acceptanceOpinion: '',
  rejectReason: ''
});

function openReviewDialog() {
  reviewForm.acceptanceStatus = 'PASSED';
  reviewForm.acceptanceOpinion = '';
  reviewForm.rejectReason = '';
  reviewVisible.value = true;
}

async function submitReview() {
  if (reviewForm.acceptanceStatus === 'REJECTED' && !reviewForm.rejectReason.trim()) {
    ElMessage.warning('驳回验收必须填写原因');
    return;
  }
  reviewSubmitting.value = true;
  try {
    await reviewAcceptance(props.workOrderId, {
      acceptanceStatus: reviewForm.acceptanceStatus,
      acceptanceResult: reviewForm.acceptanceStatus === 'PASSED' ? 'PASSED' : 'REJECTED',
      acceptanceOpinion: reviewForm.acceptanceOpinion || undefined,
      rejectReason: reviewForm.acceptanceStatus === 'REJECTED' ? reviewForm.rejectReason : undefined
    });
    ElMessage.success(reviewForm.acceptanceStatus === 'PASSED' ? '验收通过' : '验收已驳回，工单状态已回退');
    reviewVisible.value = false;
    emit('refresh');
  } finally {
    reviewSubmitting.value = false;
  }
}

/* ========== PDF 生成 ========== */
async function handleGeneratePdf() {
  try {
    const pdf = await generatePdf(props.workOrderId);
    ElMessage.success(`PDF 验收单已生成：${pdf.pdfNo}，关键验收记录已锁定`);
    emit('refresh');
  } catch {
    // error handled by interceptor
  }
}

/* ========== PDF 预览与下载 ========== */
const pdfPreviewVisible = ref(false);
const previewPdf = ref<PdfVO | null>(null);

function openPdfPreview(row: PdfVO) {
  previewPdf.value = row;
  pdfPreviewVisible.value = true;
}

function handlePdfDownload() {
  window.open(pdfDownloadUrl(props.workOrderId), '_blank');
}

/* ========== 标签 ========== */
function acceptStatusLabel(s: string) {
  const map: Record<string, string> = { PENDING: '待验收', PASSED: '已通过', REJECTED: '已驳回', LOCKED: '已锁定' };
  return map[s] || s || '-';
}
function acceptStatusTag(s: string) {
  if (s === 'PASSED' || s === 'LOCKED') return 'success';
  if (s === 'REJECTED') return 'danger';
  return 'warning';
}
function pdfStatusLabel(s: string) {
  const map: Record<string, string> = { GENERATING: '生成中', GENERATED: '已生成', FAILED: '失败', ARCHIVED: '已归档' };
  return map[s] || s || '-';
}
function pdfStatusTag(s: string) {
  if (s === 'GENERATED' || s === 'ARCHIVED') return 'success';
  if (s === 'FAILED') return 'danger';
  return 'warning';
}

// 修复拼写错误
const emptyState = computed(() => !props.acceptanceList.length);

defineExpose({ refreshSignatures: () => emit('refresh') });
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
.signature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
}
.signature-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.15s;
}
.signature-card:hover {
  box-shadow: 0 2px 8px rgb(0 0 0 / 8%);
}
.signature-card__img {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  background: #f9fafb;
}
.signature-card__info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px;
  font-size: 12px;
}
.signature-card__info strong {
  font-size: 13px;
}
.signature-card__info small {
  color: #64748b;
}
</style>
