<template>
  <PageShell title="物料追溯">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增物料</el-button>
    </template>

    <!-- 搜索 + 类型切换 -->
    <SearchForm :model="filters" @search="handleSearch" @reset="handleReset">
      <el-form-item label="搜索">
        <el-input v-model="filters.keyword" clearable placeholder="物料编码/名称" style="width:200px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.enabledFlag" clearable placeholder="全部" style="width:120px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <!-- 表格 -->
    <DataTable :data="filteredList" :loading="loading" :total="filteredList.length" :page-num="1" :page-size="filteredList.length">
      <el-table-column type="index" label="序号" width="55" />
      <el-table-column prop="materialCode" label="物料编码" min-width="130" show-overflow-tooltip />
      <el-table-column prop="materialName" label="物料名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="materialCategory" label="类别" width="100" />
      <el-table-column prop="materialSpec" label="规格" width="120" show-overflow-tooltip />
      <el-table-column prop="unit" label="单位" width="70" />
      <el-table-column label="安全库存" width="90">
        <template #default="{ row }">{{ row.safetyStockQty ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.enabledFlag ? 'success' : 'info'" size="small">{{ row.enabledFlag ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="追溯" width="70">
        <template #default="{ row }">
          <el-tag :type="row.traceEnabled ? 'primary' : 'info'" size="small">{{ row.traceEnabled ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link type="primary" size="small" @click="openInventory(row)">库存</el-button>
          <el-button link type="primary" size="small" @click="openInout(row, 'IN')">入库</el-button>
          <el-button link type="primary" size="small" @click="openInout(row, 'OUT')">出库</el-button>
          <el-button link type="primary" size="small" @click="openInout(row, 'CHECK')">盘点</el-button>
          <el-button link type="primary" size="small" @click="openQrcode(row)">二维码</el-button>
          <el-button link type="danger" size="small" @click="confirmToggle(row)">{{ row.enabledFlag ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </DataTable>

    <!-- 新增/编辑弹窗 -->
    <FormDialog v-model="formVisible" :title="editingId ? '编辑物料' : '新增物料'" :confirm-loading="formSubmitting" @confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="物料编码" prop="materialCode"><el-input v-model="form.materialCode" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称" prop="materialName"><el-input v-model="form.materialName" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="类别"><el-input v-model="form.materialCategory" placeholder="如防腐涂料" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规格"><el-input v-model="form.materialSpec" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位"><el-input v-model="form.unit" placeholder="如 桶、米" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="安全库存"><el-input-number v-model="form.safetyStockQty" :min="0" :precision="1" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="启用追溯">
              <el-switch v-model="form.traceEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="需二维码">
              <el-switch v-model="form.qrcodeRequired" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
    </FormDialog>

    <!-- 库存弹窗 -->
    <el-dialog v-model="inventoryVisible" title="库存详情" width="700px" destroy-on-close>
      <el-table :data="inventoryList" border size="small" v-loading="inventoryLoading">
        <el-table-column prop="warehouseCode" label="仓库编码" width="110" />
        <el-table-column prop="warehouseName" label="仓库名称" width="120" />
        <el-table-column prop="batchNo" label="批次号" width="120" />
        <el-table-column prop="currentQty" label="当前库存" width="100" />
        <el-table-column prop="availableQty" label="可用库存" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.inventoryStatus === 'NORMAL' ? 'success' : 'danger'" size="small">{{ row.inventoryStatus || '-' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer><el-button @click="inventoryVisible = false">关闭</el-button></template>
    </el-dialog>

    <!-- 出入库/盘点弹窗 -->
    <FormDialog v-model="inoutVisible" :title="inoutTitle" :confirm-loading="inoutSubmitting" @confirm="submitInout">
      <el-form ref="inoutFormRef" :model="inoutForm" :rules="inoutRules" label-width="100px">
        <el-form-item label="物料">{{ inoutMaterial?.materialName }} ({{ inoutMaterial?.materialCode }})</el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="inoutForm.quantity" :min="0.01" :precision="2" style="width:100%" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="仓库"><el-input v-model="inoutForm.warehouseCode" placeholder="仓库编码" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号"><el-input v-model="inoutForm.batchNo" placeholder="批次号" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="inoutType === 'OUT'" label="工单">
          <el-input-number v-model="inoutForm.workOrderId" :min="1" placeholder="关联工单ID" style="width:100%" />
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="inoutForm.businessReason" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="inoutForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
    </FormDialog>

    <!-- 二维码弹窗 -->
    <el-dialog v-model="qrcodeVisible" title="二维码" width="500px" destroy-on-close>
      <div v-if="qrcodeItem" style="display:flex;flex-direction:column;align-items:center;gap:12px">
        <div style="font-weight:600">{{ qrcodeItem.materialCode }}</div>
        <el-image
          v-if="qrcodeItem.qrcodeFileId"
          :src="`/api/files/${qrcodeItem.qrcodeFileId}/preview`"
          style="width:240px;height:240px"
          fit="contain"
        />
        <div style="color:#64748b;font-size:12px">{{ qrcodeItem.qrcodeValue }}</div>
        <div style="display:flex;gap:8px">
          <el-button type="primary" size="small" @click="downloadQrcode">下载</el-button>
          <el-button size="small" @click="printQrcode">打印</el-button>
          <el-button size="small" @click="qrcodeVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createMaterial, fetchInventory, fetchMaterials, generateQrcode,
  materialInbound, materialOutbound, materialStocktaking, updateMaterial,
  type MaterialItem, type InventoryItem, type MaterialQrcodeItem
} from '@/api/material';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormDialog from '@/components/dialog/FormDialog.vue';

/* ========== 数据 ========== */
const loading = ref(false);
const list = ref<MaterialItem[]>([]);
const filters = reactive({ keyword: '', enabledFlag: '' as number | string });

const filteredList = computed(() =>
  list.value.filter((m) => {
    if (filters.keyword && !`${m.materialCode}${m.materialName}`.toLowerCase().includes(filters.keyword.toLowerCase())) return false;
    if (filters.enabledFlag !== '' && filters.enabledFlag !== null && m.enabledFlag !== filters.enabledFlag) return false;
    return true;
  })
);

async function loadData() { loading.value = true; try { list.value = await fetchMaterials(); } finally { loading.value = false; } }
function handleSearch() {}
function handleReset() { filters.keyword = ''; filters.enabledFlag = ''; }

/* ========== 新增/编辑 ========== */
const formVisible = ref(false);
const formSubmitting = ref(false);
const editingId = ref<number | undefined>();
const formRef = ref();
const form = reactive({
  materialCode: '', materialName: '', materialCategory: '', materialSpec: '', unit: '',
  safetyStockQty: undefined as number | undefined, enabledFlag: 1, traceEnabled: 1, qrcodeRequired: 0, remark: ''
});
const rules = {
  materialCode: [{ required: true, message: '物料编码必填', trigger: 'blur' }],
  materialName: [{ required: true, message: '物料名称必填', trigger: 'blur' }]
};

function openCreate() { editingId.value = undefined; resetForm(); formVisible.value = true; }
function openEdit(row: MaterialItem) {
  editingId.value = row.id;
  Object.assign(form, {
    materialCode: row.materialCode, materialName: row.materialName, materialCategory: row.materialCategory || '',
    materialSpec: row.materialSpec || '', unit: row.unit || '',
    safetyStockQty: row.safetyStockQty, enabledFlag: row.enabledFlag ?? 1, traceEnabled: row.traceEnabled ?? 1,
    qrcodeRequired: row.qrcodeRequired ?? 0, remark: ''
  });
  formVisible.value = true;
}
function resetForm() { Object.assign(form, { materialCode: '', materialName: '', materialCategory: '', materialSpec: '', unit: '', safetyStockQty: undefined, enabledFlag: 1, traceEnabled: 1, qrcodeRequired: 0, remark: '' }); }

async function submitForm() {
  try { await formRef.value?.validate(); } catch { return; }
  formSubmitting.value = true;
  try {
    if (editingId.value) await updateMaterial(editingId.value, { ...form }); else await createMaterial({ ...form });
    ElMessage.success(editingId.value ? '物料更新成功' : '物料新增成功');
    formVisible.value = false; loadData();
  } finally { formSubmitting.value = false; }
}

async function confirmToggle(row: MaterialItem) {
  const action = row.enabledFlag ? '停用' : '启用';
  try {
    await ElMessageBox.confirm(`确认${action}物料「${row.materialName}」？`, `${action}物料`, { type: 'warning' });
    await updateMaterial(row.id, { ...row, enabledFlag: row.enabledFlag ? 0 : 1 });
    ElMessage.success(`物料已${action}`); loadData();
  } catch { /* 取消 */ }
}

/* ========== 库存 ========== */
const inventoryVisible = ref(false);
const inventoryLoading = ref(false);
const inventoryList = ref<InventoryItem[]>([]);
async function openInventory(row: MaterialItem) {
  inventoryVisible.value = true; inventoryLoading.value = true;
  try { inventoryList.value = await fetchInventory(row.id); } finally { inventoryLoading.value = false; }
}

/* ========== 出入库/盘点 ========== */
const inoutVisible = ref(false);
const inoutSubmitting = ref(false);
const inoutType = ref<'IN' | 'OUT' | 'CHECK'>('IN');
const inoutMaterial = ref<MaterialItem | null>(null);
const inoutFormRef = ref();
const inoutForm = reactive({ quantity: undefined as number | undefined, warehouseCode: '', warehouseName: '', batchNo: '', workOrderId: undefined as number | undefined, businessReason: '', remark: '' });
const inoutRules = { quantity: [{ required: true, message: '数量必填', trigger: 'blur' }] };
const inoutTitle = computed(() => ({ IN: '入库', OUT: '出库', CHECK: '盘点' })[inoutType.value]);

function openInout(row: MaterialItem, type: 'IN' | 'OUT' | 'CHECK') {
  inoutType.value = type; inoutMaterial.value = row;
  Object.assign(inoutForm, { quantity: undefined, warehouseCode: '', warehouseName: '', batchNo: '', workOrderId: undefined, businessReason: '', remark: '' });
  inoutVisible.value = true;
}

async function submitInout() {
  try { await inoutFormRef.value?.validate(); } catch { return; }
  inoutSubmitting.value = true;
  try {
    const payload = { materialId: inoutMaterial.value!.id, quantity: inoutForm.quantity, warehouseCode: inoutForm.warehouseCode || undefined, warehouseName: inoutForm.warehouseName || undefined, batchNo: inoutForm.batchNo || undefined, workOrderId: inoutForm.workOrderId || undefined, businessReason: inoutForm.businessReason || undefined, remark: inoutForm.remark || undefined };
    if (inoutType.value === 'IN') await materialInbound(payload);
    else if (inoutType.value === 'OUT') await materialOutbound(payload);
    else await materialStocktaking(payload);
    ElMessage.success(`${inoutTitle.value}成功，流水已记录`);
    inoutVisible.value = false; loadData();
  } finally { inoutSubmitting.value = false; }
}

/* ========== 二维码 ========== */
const qrcodeVisible = ref(false);
const qrcodeItem = ref<MaterialQrcodeItem | null>(null);
async function openQrcode(row: MaterialItem) {
  try {
    qrcodeItem.value = await generateQrcode(row.id, { batchNo: '', remark: '' });
    qrcodeVisible.value = true;
  } catch { /* 已处理 */ }
}
function downloadQrcode() {
  if (qrcodeItem.value?.qrcodeFileId) window.open(`/api/files/${qrcodeItem.value.qrcodeFileId}/download`, '_blank');
}
function printQrcode() {
  if (qrcodeItem.value?.qrcodeFileId) window.open(`/api/files/${qrcodeItem.value.qrcodeFileId}/preview`, '_blank');
}

onMounted(loadData);
</script>
