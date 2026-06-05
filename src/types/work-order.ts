export interface WorkOrderListItem {
  id: number;
  workOrderNo: string;
  projectId: number;
  projectName?: string;
  templateId?: number;
  workTitle: string;
  workType?: string;
  workLocation?: string;
  workContent?: string;
  requiredMaterialDesc?: string;
  leaderId?: number;
  leaderName?: string;
  maintainerId?: number;
  maintainerName?: string;
  plannedStartTime?: string;
  plannedEndTime?: string;
  actualStartTime?: string;
  actualEndTime?: string;
  status: string;
  priority?: string;
  rejectReason?: string;
  closeReason?: string;
  acceptanceRequired?: number;
  sourceType?: string;
  version?: number;
  syncStatus?: string;
  abnormalFlag?: number;
  createdAt?: string;
  updatedAt?: string;
  remark?: string;
}

/** 工单查询参数，对齐后端 WorkOrderQueryRequest */
export interface WorkOrderQueryParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  sortField?: string;
  sortOrder?: 'asc' | 'desc';
  projectId?: number;
  workOrderNo?: string;
  workType?: string;
  workLocation?: string;
  status?: string;
  priority?: string;
  maintainerId?: number;
  leaderId?: number;
  plannedStartTimeStart?: string;
  plannedStartTimeEnd?: string;
  syncStatus?: string;
  abnormalFlag?: number;
}

/** 工单状态变更参数，对齐后端 WorkOrderStatusRequest */
export interface WorkOrderStatusRequest {
  status: string;
  operationDesc?: string;
  rejectReason?: string;
  closeReason?: string;
}

/** 工单派工参数，对齐后端 WorkOrderAssignRequest */
export interface WorkOrderAssignRequest {
  leaderId?: number;
  maintainerId: number;
  assignmentRole?: string;
  remark?: string;
}

/* ========== 工单详情（对齐后端 WorkOrderDetailVO） ========== */

export interface AssignmentVO {
  id: number;
  workOrderId: number;
  assignerId: number;
  assigneeId: number;
  assignmentRole: string;
  assignmentStatus: string;
  assignedAt: string;
  remark?: string;
}

export interface StatusFlowVO {
  id: number;
  fromStatus: string;
  toStatus: string;
  operationType: string;
  operationDesc: string;
  operatorId: number;
  operationTime: string;
}

export interface MaterialRequirementVO {
  id: number;
  materialCode: string;
  materialName: string;
  materialSpec?: string;
  unit?: string;
  plannedQty?: string;
  actualQty?: string;
  prepareStatus?: string;
}

export interface SummaryVO {
  id: number;
  type: string;
  title: string;
  status: string;
  time: string;
}

export interface SyncSummaryVO {
  syncStatus: string;
  conflictCount: number;
  hasConflict: boolean;
}

export interface WorkOrderDetailVO {
  workOrder: WorkOrderListItem;
  project: Record<string, unknown>;
  assignments: AssignmentVO[];
  statusFlow: StatusFlowVO[];
  requiredMaterials: MaterialRequirementVO[];
  constructionRecordSummary: SummaryVO[];
  attachmentSummary: SummaryVO[];
  acceptanceRecords: SummaryVO[];
  signatureRecords: SummaryVO[];
  pdfRecords: SummaryVO[];
  materialUsage: SummaryVO[];
  aiResults: SummaryVO[];
  syncSummary: SyncSummaryVO;
}

/* ========== 施工记录 ========== */

export interface WorkRecordVO {
  id: number;
  serverId?: number;
  localId?: string;
  workOrderId: number;
  projectId?: number;
  recordNo?: string;
  recordType?: string;
  constructionTime?: string;
  constructionUserId?: number;
  constructionUserName?: string;
  constructionDesc?: string;
  siteCondition?: string;
  abnormalFlag?: number;
  abnormalDesc?: string;
  weather?: string;
  temperature?: number;
  humidity?: number;
  locationName?: string;
  attachmentCount?: number;
  aiResultCount?: number;
  recordStatus?: string;
  submittedAt?: string;
  confirmedBy?: number;
  confirmedAt?: string;
  version?: number;
  updatedAt?: string;
  syncStatus?: string;
  conflictFlag?: number;
  checkItems?: MobileCheckItemVO[];
  attachments?: MobileAttachmentVO[];
  aiResults?: AiSummaryVO[];
}

export interface MobileCheckItemVO {
  id: number;
  workOrderId?: number;
  recordId?: number;
  itemCode?: string;
  itemName?: string;
  itemType?: string;
  itemDesc?: string;
  requiredFlag?: number;
  checkResult?: string;
  checkValue?: string;
  checkUnit?: string;
  abnormalFlag?: number;
  abnormalDesc?: string;
  attachmentCount?: number;
  aiResultCount?: number;
  syncStatus?: string;
  conflictFlag?: number;
}

export interface MobileAttachmentVO {
  id: number;
  workOrderId: number;
  recordId: number;
  fileId: string;
  attachmentType: string;
  attachmentName: string;
  attachmentDesc?: string;
  captureTime?: string;
  captureUserName?: string;
  uploadStatus?: string;
  syncStatus?: string;
  conflictFlag?: number;
}

export interface AiSummaryVO {
  id: number;
  aiResultNo: string;
  defectType: string;
  suspectedDefectFlag: number;
  defectCount: number;
  reviewStatus: string;
  inferTime?: string;
}

/* ========== 附件 ========== */

export interface WorkOrderAttachmentVO {
  id: number;
  workOrderId: number;
  recordId?: number;
  fileId: string;
  attachmentType: string;
  attachmentName: string;
  attachmentDesc?: string;
  captureTime?: string;
  watermarkText?: string;
  previewUrl?: string;
  downloadUrl?: string;
  syncStatus?: string;
}

/* ========== 资质检查（对齐后端 QualificationCheckVO） ========== */

export interface QualificationCheckVO {
  id: number;
  workOrderId: number;
  employeeId: number;
  certificateId?: number;
  qualificationTypeId?: number;
  checkResult: string;
  message: string;
}

/* ========== 员工与证书 ========== */

export interface EmployeeVO {
  id: number;
  userId?: number;
  employeeNo?: string;
  realName?: string;
  phone?: string;
  positionName?: string;
  employeeStatus?: string;
}

export interface CertificateVO {
  id: number;
  employeeId: number;
  qualificationTypeId?: number;
  certificateNo?: string;
  certificateName?: string;
  validTo?: string;
  validStatus?: string;
  warningLevel?: string;
  fileId?: string;
}
