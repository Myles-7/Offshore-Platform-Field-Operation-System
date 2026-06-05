<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="dialogWidth"
    destroy-on-close
    @close="$emit('update:modelValue', false)"
  >
    <!-- 图片预览 -->
    <div v-if="isImage" class="file-preview__image">
      <el-image :src="previewUrl" fit="contain" style="max-height:68vh;width:100%" />
    </div>

    <!-- 视频预览 -->
    <div v-else-if="isVideo" class="file-preview__video">
      <video :src="previewUrl" controls style="max-width:100%;max-height:68vh">
        您的浏览器不支持视频播放。
      </video>
    </div>

    <!-- 音频预览 -->
    <div v-else-if="isAudio" class="file-preview__audio">
      <div class="audio-placeholder">
        <el-icon :size="64" color="#1d4ed8"><Headset /></el-icon>
        <p>{{ fileName }}</p>
      </div>
      <audio :src="previewUrl" controls style="width:100%;margin-top:16px" />
    </div>

    <!-- PDF 预览 -->
    <div v-else-if="isPdf" class="file-preview__pdf">
      <iframe
        v-if="fileId"
        :src="previewUrl"
        style="width:100%;height:68vh;border:0"
      />
    </div>

    <!-- 签名图片 -->
    <div v-else-if="isSignature" class="file-preview__signature">
      <div v-if="showSignatureCard" class="signature-card">
        <el-descriptions :column="2" border size="small" style="margin-bottom:12px">
          <el-descriptions-item label="签名角色">{{ signRoleLabel }}</el-descriptions-item>
          <el-descriptions-item label="签名人">{{ signerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="签名时间">{{ signedAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ signatureStatus || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="file-preview__image">
        <el-image :src="previewUrl" fit="contain" style="max-height:50vh;width:100%" />
      </div>
    </div>

    <!-- 不支持预览 -->
    <div v-else class="file-preview__unsupported">
      <el-icon :size="64" color="#64748b"><Document /></el-icon>
      <p>暂不支持在线预览此文件类型</p>
    </div>

    <!-- 文件信息 -->
    <div class="file-preview__info">
      <span>{{ fileName }}</span>
      <span v-if="displaySize" style="color:#64748b;margin-left:auto">{{ displaySize }}</span>
    </div>

    <template #footer>
      <div style="display:flex;gap:8px">
        <el-button v-if="allowDelete" type="danger" plain @click="handleDelete">删除</el-button>
        <div style="flex:1" />
        <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
        <el-button type="primary" @click="handleDownload">下载</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Document, Headset } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { deleteFile, fetchFileDownloadUrl, fetchFilePreviewUrl } from '@/api/file';

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    fileId?: string;
    fileName?: string;
    fileSize?: number;
    fileType?: string;
    title?: string;
    /** 签名额外信息 */
    signatureRole?: string;
    signerName?: string;
    signedAt?: string;
    signatureStatus?: string;
    /** 是否允许删除 */
    allowDelete?: boolean;
    /** 是否保护（如PDF验收单） */
    protected?: boolean;
  }>(),
  {
    modelValue: false,
    fileId: '',
    fileName: '',
    fileSize: undefined,
    fileType: '',
    title: '文件预览',
    allowDelete: false,
    protected: false
  }
);

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  deleted: [];
}>();

/* ========== 类型判断 ========== */
const isImage = computed(() => {
  const t = props.fileType?.toUpperCase();
  return t === 'PHOTO' || t === 'AI_IMAGE' || t === 'CERT' || t === 'QRCODE';
});
const isVideo = computed(() => props.fileType?.toUpperCase() === 'VIDEO');
const isAudio = computed(() => props.fileType?.toUpperCase() === 'AUDIO');
const isPdf = computed(() => props.fileType?.toUpperCase() === 'PDF');
const isSignature = computed(() => props.fileType?.toUpperCase() === 'SIGNATURE');
const showSignatureCard = computed(() => !!props.signerName || !!props.signatureRole);

const dialogWidth = computed(() => {
  if (isPdf.value) return '960px';
  if (isImage.value || isSignature.value) return '720px';
  if (isVideo.value) return '800px';
  return '600px';
});

const previewUrl = computed(() => props.fileId ? fetchFilePreviewUrl(props.fileId) : '');
const downloadUrl = computed(() => props.fileId ? fetchFileDownloadUrl(props.fileId) : '');

const signRoleLabel = computed(() => {
  switch (props.signatureRole) {
    case 'MAINTAINER': return '维修工';
    case 'LEADER': return '负责人';
    case 'ACCEPTOR': return '验收人';
    default: return props.signatureRole || '-';
  }
});

const displaySize = computed(() => {
  if (props.fileSize == null) return '';
  if (props.fileSize < 1024) return `${props.fileSize} B`;
  if (props.fileSize < 1024 * 1024) return `${(props.fileSize / 1024).toFixed(1)} KB`;
  return `${(props.fileSize / (1024 * 1024)).toFixed(1)} MB`;
});

/* ========== 操作 ========== */
function handleDownload() {
  if (downloadUrl.value) {
    window.open(downloadUrl.value, '_blank');
  }
}

async function handleDelete() {
  if (!props.fileId) return;
  try {
    if (props.protected) {
      ElMessage.warning('PDF 验收单已锁定，不能随意删除');
      return;
    }
    await ElMessageBox.confirm(
      `确认删除文件「${props.fileName || props.fileId}」？`,
      '删除文件',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    );
    await deleteFile(props.fileId);
    ElMessage.success('文件已作废');
    emit('update:modelValue', false);
    emit('deleted');
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.file-preview__info {
  display: flex;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  font-size: 13px;
}
.file-preview__unsupported,
.audio-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: 12px;
  color: #64748b;
}
.signature-card {
  padding: 0 0 4px;
}
</style>
