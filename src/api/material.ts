import { request } from '@/api/request';

export interface MaterialItem {
  id: number;
  materialCode: string;
  materialName: string;
  materialCategory?: string;
  materialSpec?: string;
  unit?: string;
  safetyStockQty?: number;
  enabledFlag?: number;
  traceEnabled?: number;
  qrcodeRequired?: number;
  updatedAt?: string;
}

export interface InventoryItem {
  id: number;
  materialId: number;
  materialCode: string;
  warehouseCode?: string;
  warehouseName?: string;
  batchNo?: string;
  currentQty: number;
  availableQty: number;
  inventoryStatus?: string;
}

export interface MaterialInoutItem {
  id: number;
  recordNo: string;
  materialId: number;
  materialCode: string;
  materialName: string;
  inoutType: string;
  quantity: number;
  beforeQty?: number;
  afterQty?: number;
}

export interface MaterialUsageItem {
  id: number;
  usageNo: string;
  workOrderId: number;
  materialId: number;
  materialCode: string;
  materialName: string;
  usedQty: number;
  wasteQty?: number;
  returnQty?: number;
  usageTime?: string;
  syncStatus?: string;
}

export interface MaterialQrcodeItem {
  id: number;
  materialId: number;
  materialCode: string;
  qrcodeValue: string;
  qrcodeFileId?: string;
  batchNo?: string;
  serialNo?: string;
  qrcodeStatus?: string;
}

/* ========== 物料 CRUD ========== */
export function fetchMaterials() { return request<MaterialItem[]>({ url: '/admin/materials', method: 'GET' }); }
export function createMaterial(data: Record<string, unknown>) { return request<MaterialItem>({ url: '/admin/materials', method: 'POST', data }); }
export function updateMaterial(id: number, data: Record<string, unknown>) { return request<MaterialItem>({ url: `/admin/materials/${id}`, method: 'PUT', data }); }
export function deleteMaterial(id: number) { return request<void>({ url: `/admin/materials/${id}`, method: 'DELETE' }); }

/* ========== 库存 ========== */
export function fetchInventory(materialId: number) { return request<InventoryItem[]>({ url: `/admin/materials/${materialId}/inventory`, method: 'GET' }); }

/* ========== 出入库 & 盘点 ========== */
export function materialInbound(data: Record<string, unknown>) { return request<MaterialInoutItem>({ url: '/admin/materials/inbound', method: 'POST', data }); }
export function materialOutbound(data: Record<string, unknown>) { return request<MaterialInoutItem>({ url: '/admin/materials/outbound', method: 'POST', data }); }
export function materialStocktaking(data: Record<string, unknown>) { return request<MaterialInoutItem>({ url: '/admin/materials/stocktaking', method: 'POST', data }); }

/* ========== 二维码 ========== */
export function generateQrcode(materialId: number, data?: Record<string, unknown>) { return request<MaterialQrcodeItem>({ url: `/admin/materials/${materialId}/qrcode`, method: 'POST', data }); }
export function getQrcodeByCode(code: string) { return request<MaterialQrcodeItem>({ url: `/admin/materials/qrcode/${code}`, method: 'GET' }); }

/* ========== 工单物料追溯 ========== */
export function fetchWorkOrderMaterialUsage(workOrderId: number) { return request<MaterialUsageItem[]>({ url: `/admin/work-orders/${workOrderId}/material-usage`, method: 'GET' }); }
