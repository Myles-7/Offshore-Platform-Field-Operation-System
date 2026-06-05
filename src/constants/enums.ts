export interface EnumOption {
  label: string;
  value: string;
  type?: 'success' | 'warning' | 'info' | 'primary' | 'danger';
}

export const workOrderStatusOptions: EnumOption[] = [
  { label: '待派工', value: 'PENDING_ASSIGN', type: 'warning' },
  { label: '已派工', value: 'ASSIGNED', type: 'primary' },
  { label: '已接收', value: 'ACCEPTED', type: 'primary' },
  { label: '施工中', value: 'IN_PROGRESS', type: 'warning' },
  { label: '待验收', value: 'PENDING_ACCEPTANCE', type: 'warning' },
  { label: '已完成', value: 'COMPLETED', type: 'success' },
  { label: '已驳回', value: 'REJECTED', type: 'danger' },
  { label: '已关闭', value: 'CLOSED', type: 'info' }
];

export const qualificationStatusOptions: EnumOption[] = [
  { label: '有效', value: 'VALID', type: 'success' },
  { label: '即将到期', value: 'EXPIRING_SOON', type: 'warning' },
  { label: '已过期', value: 'EXPIRED', type: 'danger' },
  { label: '缺失', value: 'MISSING', type: 'info' }
];

export const syncStatusOptions: EnumOption[] = [
  { label: '待同步', value: 'PENDING', type: 'warning' },
  { label: '同步中', value: 'SYNCING', type: 'primary' },
  { label: '已同步', value: 'SYNCED', type: 'success' },
  { label: '同步失败', value: 'FAILED', type: 'danger' },
  { label: '冲突待处理', value: 'CONFLICT', type: 'danger' }
];

export const aiReviewStatusOptions: EnumOption[] = [
  { label: '待复核', value: 'PENDING_REVIEW', type: 'warning' },
  { label: '已确认', value: 'CONFIRMED', type: 'success' },
  { label: '误报', value: 'FALSE_POSITIVE', type: 'info' },
  { label: '已忽略', value: 'IGNORED', type: 'info' }
];

export const priorityStatusOptions: EnumOption[] = [
  { label: '紧急', value: 'URGENT', type: 'danger' },
  { label: '高', value: 'HIGH', type: 'warning' },
  { label: '普通', value: 'NORMAL', type: 'info' },
  { label: '低', value: 'LOW', type: 'info' }
];

export const workTypeOptions: EnumOption[] = [
  { label: '防腐作业', value: 'ANTICORROSION', type: 'primary' },
  { label: '维修作业', value: 'REPAIR', type: 'warning' },
  { label: '巡检作业', value: 'INSPECTION', type: 'info' },
  { label: '保养作业', value: 'MAINTENANCE', type: 'info' }
];

export function getEnumOption(options: EnumOption[], value?: string): EnumOption {
  return options.find((item) => item.value === value) ?? { label: value || '-', value: value || '', type: 'info' };
}
