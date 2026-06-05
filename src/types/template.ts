export interface WorkOrderTemplateItem {
  id: number;
  templateCode: string;
  templateName: string;
  workType?: string;
  defaultPriority?: string;
  defaultWorkContent?: string;
  defaultMaterialDesc?: string;
  defaultDurationHours?: number;
  enabledFlag?: number;
  version?: number;
  syncStatus?: string;
  createdAt?: string;
  remark?: string;
}
