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
  { label: '仅本地', value: 'LOCAL_ONLY', type: 'info' },
  { label: '待同步', value: 'PENDING', type: 'warning' },
  { label: '同步中', value: 'SYNCING', type: 'primary' },
  { label: '已同步', value: 'SYNCED', type: 'success' },
  { label: '同步失败', value: 'FAILED', type: 'danger' },
  { label: '冲突待处理', value: 'CONFLICT', type: 'danger' },
  { label: '已删除', value: 'DELETED', type: 'info' },
  { label: '已忽略', value: 'IGNORED', type: 'info' }
];

export const syncModuleTypeOptions: EnumOption[] = [
  { label: '工单信息', value: 'WORK_ORDER', type: 'primary' },
  { label: '施工记录', value: 'WORK_RECORD', type: 'primary' },
  { label: '附件元数据', value: 'ATTACHMENT_META', type: 'info' },
  { label: '电子签名', value: 'SIGNATURE', type: 'warning' },
  { label: '验收记录', value: 'ACCEPTANCE', type: 'warning' },
  { label: 'PDF验收单', value: 'PDF', type: 'info' },
  { label: '物料使用', value: 'MATERIAL_USAGE', type: 'info' },
  { label: '人员资质', value: 'QUALIFICATION', type: 'info' },
  { label: 'AI结果', value: 'AI_RESULT', type: 'info' },
  { label: '知识库', value: 'KNOWLEDGE', type: 'info' },
  { label: '设备信息', value: 'DEVICE', type: 'info' },
  { label: '用户信息', value: 'USER_PROFILE', type: 'info' }
];

export const conflictTypeOptions: EnumOption[] = [
  { label: '版本冲突', value: 'VERSION_CONFLICT', type: 'warning' },
  { label: '字段冲突', value: 'FIELD_CONFLICT', type: 'warning' },
  { label: '删除后更新', value: 'UPDATE_AFTER_DELETE', type: 'danger' },
  { label: '更新后删除', value: 'DELETE_AFTER_UPDATE', type: 'danger' },
  { label: '重复创建', value: 'DUPLICATE_CREATE', type: 'warning' },
  { label: '权限冲突', value: 'PERMISSION_CONFLICT', type: 'danger' },
  { label: '文件元数据冲突', value: 'FILE_META_CONFLICT', type: 'warning' },
  { label: '验收锁定冲突', value: 'ACCEPTANCE_LOCKED_CONFLICT', type: 'danger' }
];

export const resolveStrategyOptions: EnumOption[] = [
  { label: '保留服务器版本', value: 'KEEP_SERVER', type: 'primary' },
  { label: '保留客户端版本', value: 'KEEP_CLIENT', type: 'warning' },
  { label: '人工合并', value: 'MANUAL_MERGE', type: 'warning' },
  { label: '忽略客户端变更', value: 'IGNORE_CLIENT', type: 'info' }
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
