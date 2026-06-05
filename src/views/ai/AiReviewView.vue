<template>
  <PageShell title="AI 辅助验收">
    <template #actions>
      <el-alert type="info" show-icon :closable="false" title="AI 结果仅作辅助，最终验收以人工确认为准" />
    </template>

    <!-- 子 tab 切换 -->
    <el-tabs v-model="activeTab">
      <el-tab-pane label="AI 识别结果" name="results" />
      <el-tab-pane label="模型管理" name="models" />
    </el-tabs>

    <!-- ========== AI 结果列表 ========== -->
    <template v-if="activeTab === 'results'">
      <!-- 按工单筛选 -->
      <SearchForm :model="resultFilters" @search="loadResults" @reset="resetResultFilters">
        <el-form-item label="工单">
          <el-input-number v-model="resultFilters.workOrderId" :min="1" placeholder="输入工单ID" style="width:200px" />
        </el-form-item>
        <el-form-item label="复核状态">
          <el-select v-model="resultFilters.reviewStatus" clearable placeholder="全部" style="width:130px">
            <el-option label="待复核" value="PENDING_REVIEW" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="误报" value="FALSE_POSITIVE" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
      </SearchForm>

      <DataTable :data="filteredResults" :loading="resultsLoading" :total="filteredResults.length" :page-num="1" :page-size="filteredResults.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column prop="aiResultNo" label="编号" width="120" show-overflow-tooltip />
        <el-table-column label="工单" width="90">
          <template #default="{ row }">
            <el-button link type="primary" size="small" v-if="row.workOrderId" @click="goDetail(row.workOrderId)">{{ row.workOrderNo || row.workOrderId }}</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="缺陷类型" width="100">
          <template #default="{ row }">{{ defectLabel(row.defectType) }}</template>
        </el-table-column>
        <el-table-column label="置信度" width="90">
          <template #default="{ row }">
            <span :style="{ color: row.confidence < 0.7 ? '#f56c6c' : row.confidence < 0.9 ? '#e6a23c' : '#16a34a' }">
              {{ row.confidence != null ? `${(row.confidence * 100).toFixed(1)}%` : '-' }}
            </span>
            <el-tooltip v-if="row.confidence < 0.7" content="低置信度，建议人工复核" placement="top">
              <el-icon :size="14" color="#f56c6c" style="margin-left:2px;cursor:help"><WarningFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="缺陷数" width="70">
          <template #default="{ row }">{{ row.defectCount ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="疑似" width="70">
          <template #default="{ row }">
            <el-tag :type="row.suspectedDefectFlag ? 'danger' : 'success'" size="small">
              {{ row.suspectedDefectFlag ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="复核状态" width="110">
          <template #default="{ row }">
            <StatusTag :value="row.reviewStatus" enum-type="aiReview" />
          </template>
        </el-table-column>
        <el-table-column label="同步" width="100">
          <template #default="{ row }"><StatusTag :value="row.syncStatus" enum-type="sync" /></template>
        </el-table-column>
        <el-table-column label="模型" width="100">
          <template #default="{ row }">{{ row.modelCode || '-' }}</template>
        </el-table-column>
        <el-table-column label="推理耗时" width="90">
          <template #default="{ row }">{{ row.inferCostMs != null ? `${row.inferCostMs}ms` : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openAiDetail(row)">查看</el-button>
            <el-button
              v-if="row.reviewStatus === 'PENDING_REVIEW'"
              link
              type="warning"
              size="small"
              @click="openReview(row)"
            >复核</el-button>
          </template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== 模型管理 ========== -->
    <template v-if="activeTab === 'models'">
      <div style="margin-bottom:8px">
        <el-button type="primary" :icon="Plus" @click="openCreateModel">新增模型</el-button>
      </div>

      <DataTable :data="modelList" :loading="modelsLoading" :total="modelList.length" :page-num="1" :page-size="modelList.length">
        <el-table-column type="index" label="序号" width="55" />
        <el-table-column prop="modelCode" label="模型编码" width="120" />
        <el-table-column prop="modelName" label="模型名称" min-width="140" />
        <el-table-column prop="modelVersion" label="版本" width="80" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ row.modelType || row.runtimeType || '-' }}</template>
        </el-table-column>
        <el-table-column label="部署端" width="80">
          <template #default="{ row }">{{ row.deploySide || '-' }}</template>
        </el-table-column>
        <el-table-column label="置信阈值" width="90">
          <template #default="{ row }">{{ row.confidenceThreshold != null ? `${(row.confidenceThreshold * 100).toFixed(0)}%` : '-' }}</template>
        </el-table-column>
        <el-table-column label="激活" width="70">
          <template #default="{ row }">
            <el-tag :type="row.activeFlag ? 'success' : 'info'" size="small">{{ row.activeFlag ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button v-if="!row.activeFlag" link type="primary" size="small" @click="confirmActivate(row)">激活</el-button>
            <span v-else style="color:#64748b;font-size:12px">已激活</span>
          </template>
        </el-table-column>
      </DataTable>
    </template>

    <!-- ========== AI 结果详情弹窗 ========== -->
    <el-dialog v-model="aiDetailVisible" title="AI 识别结果详情" width="860px" destroy-on-close>
      <template v-if="aiDetail">
        <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
          <el-descriptions-item label="编号">{{ aiDetail.aiResultNo }}</el-descriptions-item>
          <el-descriptions-item label="工单">{{ aiDetail.workOrderNo || aiDetail.workOrderId }}</el-descriptions-item>
          <el-descriptions-item label="缺陷类型">{{ defectLabel(aiDetail.defectType) }}</el-descriptions-item>
          <el-descriptions-item label="置信度">
            <span :style="{ color: aiDetail.confidence < 0.7 ? '#f56c6c' : '#16a34a' }">
              {{ `${(aiDetail.confidence * 100).toFixed(1)}%` }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="缺陷数">{{ aiDetail.defectCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="模型">{{ aiDetail.modelCode || '-' }} v{{ aiDetail.modelVersion || '-' }}</el-descriptions-item>
          <el-descriptions-item label="推理耗时">{{ aiDetail.inferCostMs != null ? `${aiDetail.inferCostMs}ms` : '-' }}</el-descriptions-item>
          <el-descriptions-item label="复核状态">
            <StatusTag :value="aiDetail.reviewStatus" enum-type="aiReview" />
          </el-descriptions-item>
        </el-descriptions>

        <!-- 原始照片 -->
        <div v-if="aiDetail.fileId" style="margin-bottom:16px">
          <h4>原始施工照片</h4>
          <el-image
            :src="`/api/files/${aiDetail.fileId}/preview`"
            fit="contain"
            style="max-height:360px;width:100%"
          />
        </div>

        <!-- 检测框 -->
        <div v-if="aiDetail.boxes?.length" style="margin-bottom:16px">
          <h4>检测框（{{ aiDetail.boxes.length }}个）</h4>
          <el-table :data="aiDetail.boxes" border size="small">
            <el-table-column prop="defectType" label="缺陷类型" width="100" />
            <el-table-column label="置信度" width="90">
              <template #default="{ row }">{{ `${(row.confidence * 100).toFixed(1)}%` }}</template>
            </el-table-column>
            <el-table-column label="坐标(x,y,w,h)" width="160">
              <template #default="{ row }">{{ `(${row.x},${row.y}) ${row.width}x${row.height}` }}</template>
            </el-table-column>
            <el-table-column prop="boxLabel" label="标签" min-width="100" show-overflow-tooltip />
          </el-table>
        </div>

        <div v-if="aiDetail.resultSummary" style="margin-bottom:16px">
          <h4>识别摘要</h4>
          <p style="color:#64748b;white-space:pre-wrap">{{ aiDetail.resultSummary }}</p>
        </div>

        <el-alert :title="aiDetail.auxiliaryNotice || 'AI 结果仅作辅助，最终验收以人工确认为准'" type="info" show-icon :closable="false" />
      </template>
    </el-dialog>

    <!-- ========== 复核弹窗 ========== -->
    <FormDialog v-model="reviewVisible" title="AI 结果复核" :confirm-loading="reviewSubmitting" @confirm="submitReview">
      <template v-if="reviewItem">
        <el-descriptions :column="2" border size="small" style="margin-bottom:12px">
          <el-descriptions-item label="编号">{{ reviewItem.aiResultNo }}</el-descriptions-item>
          <el-descriptions-item label="缺陷类型">{{ defectLabel(reviewItem.defectType) }}</el-descriptions-item>
          <el-descriptions-item label="置信度">{{ `${(reviewItem.confidence * 100).toFixed(1)}%` }}</el-descriptions-item>
          <el-descriptions-item label="疑似标志">{{ reviewItem.suspectedDefectFlag ? '是' : '否' }}</el-descriptions-item>
        </el-descriptions>

        <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="110px">
          <el-form-item label="复核结论" prop="reviewStatus">
            <el-radio-group v-model="reviewForm.reviewStatus">
              <el-radio-button value="CONFIRMED">确认属实</el-radio-button>
              <el-radio-button value="FALSE_POSITIVE">人工确认误报</el-radio-button>
              <el-radio-button value="IGNORED">已忽略</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="确认缺陷类型">
            <el-select v-model="reviewForm.confirmedDefectType" clearable placeholder="留空沿用AI判定" style="width:100%">
              <el-option v-for="d in defectOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="复核意见" prop="reviewOpinion">
            <el-input v-model="reviewForm.reviewOpinion" type="textarea" :rows="3" placeholder="填写复核意见" />
          </el-form-item>
          <el-form-item label="验收建议">
            <el-input v-model="reviewForm.acceptanceSuggestion" type="textarea" :rows="2" placeholder="可选，对最终验收的建议" />
          </el-form-item>
        </el-form>

        <el-alert title="AI 仅作辅助，不得自动决定最终验收结论" type="warning" show-icon :closable="false" />
      </template>
    </FormDialog>

    <!-- ========== 新增模型弹窗 ========== -->
    <FormDialog v-model="modelVisible" title="新增 AI 模型" :confirm-loading="modelSubmitting" @confirm="submitModel">
      <el-form ref="modelFormRef" :model="modelForm" :rules="modelRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="模型编码" prop="modelCode"><el-input v-model="modelForm.modelCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型名称" prop="modelName"><el-input v-model="modelForm.modelName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="版本"><el-input v-model="modelForm.modelVersion" placeholder="1.0.0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模型类型"><el-input v-model="modelForm.modelType" placeholder="如 缺陷检测" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="运行时">
              <el-select v-model="modelForm.runtimeType" style="width:100%">
                <el-option value="TFLite" label="TFLite" />
                <el-option value="ONNX" label="ONNX" />
                <el-option value="NCNN" label="NCNN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署端">
              <el-select v-model="modelForm.deploySide" style="width:100%">
                <el-option value="MOBILE" label="移动端" />
                <el-option value="SERVER" label="服务端" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="置信阈值">
          <el-slider v-model="modelForm.confidenceThreshold" :min="0.1" :max="0.99" :step="0.05" show-input style="width:100%" />
        </el-form-item>
      </el-form>
    </FormDialog>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Plus, WarningFilled } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  fetchWorkOrderAiResults, reviewAiResult, fetchAiModels,
  createAiModel, activateAiModel,
  type AiResultItem, type AiModelItem
} from '@/api/ai';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import FormDialog from '@/components/dialog/FormDialog.vue';

const router = useRouter();

/* ========== 缺陷枚举 ========== */
const defectOptions = [
  { label: '起皮', value: 'PEELING' },
  { label: '裂纹', value: 'CRACK' },
  { label: '锈蚀', value: 'RUST' },
  { label: '破损', value: 'DAMAGE' },
  { label: '鼓泡', value: 'BUBBLE' },
  { label: '正常', value: 'NORMAL' },
  { label: '未知', value: 'UNKNOWN' }
];
function defectLabel(d?: string) { return defectOptions.find((o) => o.value === d)?.label || d || '-'; }

/* ========== Tab ========== */
const activeTab = ref('results');

/* ========== AI 结果列表 ========== */
const resultsLoading = ref(false);
const allResults = ref<AiResultItem[]>([]);
const resultFilters = reactive({ workOrderId: undefined as number | undefined, reviewStatus: '' });

const filteredResults = computed(() =>
  allResults.value.filter((r) => {
    if (resultFilters.reviewStatus && r.reviewStatus !== resultFilters.reviewStatus) return false;
    return true;
  })
);

async function loadResults() {
  if (!resultFilters.workOrderId) {
    allResults.value = [];
    return;
  }
  resultsLoading.value = true;
  try { allResults.value = await fetchWorkOrderAiResults(resultFilters.workOrderId); } finally { resultsLoading.value = false; }
}
function resetResultFilters() { resultFilters.workOrderId = undefined; resultFilters.reviewStatus = ''; allResults.value = []; }

function goDetail(workOrderId: number) { router.push(`/work-orders/${workOrderId}`); }

/* ========== AI 结果详情 ========== */
const aiDetailVisible = ref(false);
const aiDetail = ref<AiResultItem | null>(null);
async function openAiDetail(row: AiResultItem) {
  aiDetail.value = row;
  aiDetailVisible.value = true;
}

/* ========== 复核 ========== */
const reviewVisible = ref(false); const reviewSubmitting = ref(false);
const reviewItem = ref<AiResultItem | null>(null);
const reviewFormRef = ref(); const reviewForm = reactive({ reviewStatus: 'CONFIRMED', confirmedDefectType: '', reviewOpinion: '', acceptanceSuggestion: '' });
const reviewRules = { reviewOpinion: [{ required: true, message: '复核意见必填', trigger: 'blur' }] };

function openReview(row: AiResultItem) {
  reviewItem.value = row;
  reviewForm.reviewStatus = 'CONFIRMED';
  reviewForm.confirmedDefectType = '';
  reviewForm.reviewOpinion = '';
  reviewForm.acceptanceSuggestion = '';
  reviewVisible.value = true;
}

async function submitReview() {
  try { await reviewFormRef.value?.validate(); } catch { return; }
  reviewSubmitting.value = true;
  try {
    await reviewAiResult(reviewItem.value!.id, {
      reviewStatus: reviewForm.reviewStatus,
      confirmedDefectType: reviewForm.confirmedDefectType || undefined,
      reviewOpinion: reviewForm.reviewOpinion || undefined,
      acceptanceSuggestion: reviewForm.acceptanceSuggestion || undefined
    });
    ElMessage.success('复核已提交，操作日志已记录');
    reviewVisible.value = false;
    if (resultFilters.workOrderId) loadResults();
  } finally { reviewSubmitting.value = false; }
}

/* ========== 模型 ========== */
const modelsLoading = ref(false); const modelList = ref<AiModelItem[]>([]);
async function loadModels() { modelsLoading.value = true; try { modelList.value = await fetchAiModels(); } finally { modelsLoading.value = false; } }

const modelVisible = ref(false); const modelSubmitting = ref(false);
const modelFormRef = ref(); const modelForm = reactive({ modelCode: '', modelName: '', modelVersion: '', modelType: '', runtimeType: '', deploySide: '', confidenceThreshold: 0.8 });
const modelRules = { modelCode: [{ required: true, message: '编码必填' }], modelName: [{ required: true, message: '名称必填' }] };

function openCreateModel() {
  Object.assign(modelForm, { modelCode: '', modelName: '', modelVersion: '', modelType: '', runtimeType: '', deploySide: '', confidenceThreshold: 0.8 });
  modelVisible.value = true;
}
async function submitModel() {
  try { await modelFormRef.value?.validate(); } catch { return; }
  modelSubmitting.value = true;
  try { await createAiModel({ ...modelForm }); ElMessage.success('模型创建成功'); modelVisible.value = false; loadModels(); } finally { modelSubmitting.value = false; }
}
async function confirmActivate(row: AiModelItem) {
  try {
    await ElMessageBox.confirm(`确认激活模型「${row.modelName} v${row.modelVersion}」？`, '激活模型', { type: 'warning' });
    await activateAiModel(row.id);
    ElMessage.success('模型已激活'); loadModels();
  } catch { /* 取消 */ }
}

onMounted(loadModels);
</script>
