# PC 后台冲突复核操作指南

> 路径: `/sync/conflicts`

## 功能概述

PC 后台冲突复核页面提供以下能力:
1. **冲突列表**: 查看所有未处理和已处理的同步冲突
2. **冲突详情**: 左右对比移动端版本和服务端版本
3. **字段级合并**: 逐字段选择保留服务端还是客户端值
4. **策略处理**: KEEP_SERVER / KEEP_CLIENT / MANUAL_MERGE / IGNORE_CLIENT
5. **同步日志**: 查看每个同步任务的详细日志
6. **同步任务**: 查看设备同步任务汇总

## 页面结构

### Tab 1: 冲突列表
- 显示 conflictNo / entityType / workOrderId / resolveStatus / localId / serverId
- 筛选: 冲突类型 / 工单 / 状态
- 操作: 查看详情 / 处理

### Tab 2: 同步日志
- 显示每条同步数据的处理结果
- 字段: moduleType / actionType / syncStatus / deviceId / message / time

### Tab 3: 同步任务
- 显示每个同步批次的汇总
- 字段: taskNo / businessType / taskStatus / successCount / failedCount / conflictCount / deviceId

## 冲突详情弹窗

- 左侧: 移动端版本 (clientPayload, JSON 格式)
- 右侧: 服务端版本 (serverPayload, JSON 格式)
- 差异字段: 红色高亮

## 处理策略

| 策略 | 说明 | 业务表影响 |
|------|------|------------|
| KEEP_SERVER | 保留服务端版本 | 不修改业务表 |
| KEEP_CLIENT | 保留客户端版本 | 使用 client_payload 更新业务表 version+1 |
| MANUAL_MERGE | 字段级合并 | 逐字段选择值 → 合并 → 更新业务表 |
| IGNORE_CLIENT | 忽略客户端变更 | 不修改业务表 |

## 字段级合并 UI

选择 MANUAL_MERGE 策略后:
1. 表格显示所有字段: 字段名 / 移动端值 / 服务端值 / 选择按钮
2. 冲突字段 (值不同) 红色高亮，默认选中 SERVER
3. 非冲突字段默认展示服务端值，不可切换
4. 提交时自动生成 resolvedPayload (JSON)

## 权限控制

- 系统管理员: 可处理全部冲突
- 项目经理: 只能处理所属项目工单的冲突
- 其他角色: 只读或无权限

## 处理后

1. sync_conflict.resolveStatus = RESOLVED (or IGNORED)
2. sync_conflict.resolverId/resolveTime/resolveComment 写入
3. 业务表 version+1, updatedAt 更新
4. work_order_version_log 写入
5. operation_log 写入 (操作类型: RESOLVE_SYNC_CONFLICT)
6. 移动端下次 pull 获取最终版本

## 工单详情中的同步状态

- 工单列表: syncStatus 标签列
- 工单详情: 同步状态/冲突提示区域
- 如有冲突: 顶部横幅 + 跳转 `/sync/conflicts` 按钮
- 施工记录时间线: 每条记录显示 syncStatus
- 附件/签名/PDF/AI结果: 均在区域入口显示 syncStatus
