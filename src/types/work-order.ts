export interface WorkOrderListItem {
  id: number;
  workOrderNo: string;
  projectName: string;
  workTitle: string;
  workLocation: string;
  status: string;
  priority: string;
  plannedStartTime?: string;
  plannedEndTime?: string;
  maintainerName?: string;
  syncStatus?: string;
  abnormalFlag?: number;
  updatedAt?: string;
}
