<template>
  <div class="detail-card">
    <header class="detail-card__header">
      <h3>附件</h3>
      <div style="display:flex;gap:8px;align-items:center">
        <el-input v-model="searchText" placeholder="搜索附件名" clearable size="small" style="width:180px" />
        <el-button link type="primary" size="small" :icon="Refresh" @click="loadAttachments">刷新</el-button>
      </div>
    </header>

    <AppState v-if="loading" type="loading" />
    <AppState v-else-if="!filteredAttachments.length" type="empty" title="暂无附件" />

    <div v-else class="attach-grid">
      <div
        v-for="att in filteredAttachments"
        :key="att.id"
        class="attach-card"
        :class="{ 'attach-card--conflict': att.syncStatus === 'CONFLICT' }"
        @click="openPreview(att)"
      >
        <!-- 缩略图 / 类型图标 -->
        <div class="attach-card__thumb">
          <img v-if="isImage(att)" :src="previewUrl(att.fileId)" loading="lazy" class="attach-thumb-img" />
          <el-icon v-else-if="isVideo(att)" :size="36" color="#7c3aed"><VideoCameraFilled /></el-icon>
          <el-icon v-else-if="isAudio(att)" :size="36" color="#059669"><Headset /></el-icon>
          <el-icon v-else-if="att.attachmentType === 'PDF'" :size="36" color="#dc2626"><Document /></el-icon>
          <el-icon v-else-if="att.attachmentType === 'SIGNATURE'" :size="36" color="#2563eb"><EditPen /></el-icon>
          <el-icon v-else :size="36" color="#64748b"><Paperclip /></el-icon>
        </div>

        <!-- 信息 -->
        <div class="attach-card__body">
          <strong class="attach-card__name" :title="att.attachmentName">{{ att.attachmentName }}</strong>
          <div class="attach-card__tags">
            <el-tag size="small" :type="typeTag(att)">{{ typeLabel(att) }}</el-tag>
            <StatusTag :value="att.syncStatus" enum-type="sync" />
            <el-tag v-if="att.watermarkText" size="small" effect="plain" type="info">含水印</el-tag>
          </div>
          <!-- 水印信息 -->
          <div v-if="att.watermarkText" class="attach-card__watermark">
            <el-icon :size="12"><InfoFilled /></el-icon>
            <span>{{ att.watermarkText }}</span>
          </div>
          <div class="attach-card__meta">
            <span>{{ att.captureTime ? formatDate(att.captureTime) : '-' }}</span>
            <span v-if="att.attachmentDesc"> | {{ att.attachmentDesc }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="attach-card__actions">
          <el-button size="small" link type="primary" @click.stop="openPreview(att)">预览</el-button>
          <el-button
            size="small"
            link
            type="danger"
            :disabled="att.attachmentType === 'PDF'"
            @click.stop="handleDelete(att)"
          >作废</el-button>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <FilePreviewDialog
      v-model="previewVisible"
      :file-id="previewFile?.fileId"
      :file-name="previewFile?.attachmentName"
      :file-type="previewFile?.attachmentType"
      :title="previewFile?.attachmentName || '文件预览'"
      :allow-delete="previewFile?.attachmentType !== 'PDF'"
      :protected="previewFile?.attachmentType === 'PDF'"
      @deleted="loadAttachments"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { Document, EditPen, Headset, InfoFilled, Paperclip, Refresh, VideoCameraFilled } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { deleteFile, fetchFilePreviewUrl } from '@/api/file';
import AppState from '@/components/state/AppState.vue';
import FilePreviewDialog from '@/components/file/FilePreviewDialog.vue';
import StatusTag from '@/components/status/StatusTag.vue';

interface AttachmentItem {
  id: number;
  workOrderId: number;
  recordId?: number;
  fileId: string;
  attachmentType: string;
  attachmentName: string;
  attachmentDesc?: string;
  captureTime?: string;
  watermarkText?: string;
  syncStatus?: string;
}

const props = withDefaults(
  defineProps<{
    workOrderId: number;
    attachments: AttachmentItem[];
    loading?: boolean;
  }>(),
  { loading: false }
);

const emit = defineEmits<{
  refresh: [];
}>();

const searchText = ref('');
const previewVisible = ref(false);
const previewFile = ref<AttachmentItem | null>(null);

const filteredAttachments = computed(() => {
  if (!searchText.value) return props.attachments;
  const kw = searchText.value.toLowerCase();
  return props.attachments.filter(
    (a) => a.attachmentName?.toLowerCase().includes(kw) || a.attachmentType?.toLowerCase().includes(kw)
  );
});

function previewUrl(fileId: string): string {
  return fileId ? fetchFilePreviewUrl(fileId) : '';
}

function isImage(att: AttachmentItem): boolean {
  const t = att.attachmentType?.toUpperCase();
  return t === 'PHOTO' || t === 'AI_IMAGE' || t === 'CERT' || t === 'QRCODE' || t === 'SIGNATURE';
}
function isVideo(att: AttachmentItem): boolean { return att.attachmentType?.toUpperCase() === 'VIDEO'; }
function isAudio(att: AttachmentItem): boolean { return att.attachmentType?.toUpperCase() === 'AUDIO'; }

function typeLabel(att: AttachmentItem): string {
  const map: Record<string, string> = {
    PHOTO: '照片', VIDEO: '视频', AUDIO: '语音', PDF: 'PDF',
    SIGNATURE: '签名', AI_IMAGE: 'AI结果', CERT: '证书', QRCODE: '二维码', OTHER: '其他'
  };
  return map[att.attachmentType] || att.attachmentType;
}

function typeTag(att: AttachmentItem): string {
  const t = att.attachmentType;
  if (t === 'PHOTO' || t === 'AI_IMAGE') return 'primary';
  if (t === 'VIDEO') return 'success';
  if (t === 'AUDIO') return 'success';
  if (t === 'PDF') return 'danger';
  if (t === 'SIGNATURE') return 'warning';
  return 'info';
}

function formatDate(val?: string): string {
  if (!val) return '-';
  return val.slice(0, 10);
}

function openPreview(att: AttachmentItem) {
  previewFile.value = att;
  previewVisible.value = true;
}

async function handleDelete(att: AttachmentItem) {
  if (att.attachmentType === 'PDF') {
    ElMessage.warning('PDF 验收单已锁定，不能随意删除');
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认作废附件「${att.attachmentName}」？`,
      '作废附件',
      { confirmButtonText: '确认作废', cancelButtonText: '取消', type: 'error' }
    );
    await deleteFile(att.fileId);
    ElMessage.success('附件已作废');
    emit('refresh');
  } catch {
    // 用户取消
  }
}

function loadAttachments() {
  emit('refresh');
}
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
.attach-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 10px;
}
.attach-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.15s;
}
.attach-card:hover {
  box-shadow: 0 2px 8px rgb(0 0 0 / 8%);
}
.attach-card--conflict {
  border-color: #f56c6c;
  background: #fef2f2;
}
.attach-card__thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  min-width: 64px;
  border-radius: 6px;
  background: #f3f4f6;
  overflow: hidden;
}
.attach-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.attach-card__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.attach-card__name {
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.attach-card__tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.attach-card__watermark {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: #2563eb;
}
.attach-card__meta {
  font-size: 11px;
  color: #64748b;
}
.attach-card__actions {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
</style>
