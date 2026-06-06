# 离线同步 — 端到端联调清单

> 建议使用真机 Android + 本地后端 + PC 浏览器完成联调验证。

## 场景 1：维修工正常同步流程

- [ ] 1.1 维修工打开 App，自动注册设备 (POST /api/sync/device/register)
- [ ] 1.2 维修工 pull 自己的工单列表 (POST /api/sync/pull)
- [ ] 1.3 断网后查看工单详情 — 本地数据显示正常
- [ ] 1.4 断网后新增施工记录 — 显示"已离线保存，待同步"
- [ ] 1.5 断网后拍摄施工照片 — 照片保存本地，水印正确
- [ ] 1.6 断网后手写签名 — 签名保存本地
- [ ] 1.7 断网后提交验收意见 — 验收记录保存本地
- [ ] 1.8 断网后生成 PDF 元数据 — 元数据保存本地
- [ ] 1.9 断网后 AI 识别结果 — 保存本地 AI 结果
- [ ] 1.10 恢复网络 → 文件先上传 → 业务元数据 push → pull → ack
- [ ] 1.11 同步中心显示"已同步"，最近同步时间更新

## 场景 2：PC 后台查看同步数据

- [ ] 2.1 PC 后台查看工单列表 — 显示同步状态标签
- [ ] 2.2 PC 后台查看施工记录 — 记录有 syncStatus
- [ ] 2.3 PC 后台查看附件 — 可预览/下载
- [ ] 2.4 PC 后台查看签名 — 签名图可见
- [ ] 2.5 PC 后台查看 PDF — PDF 可预览/下载
- [ ] 2.6 PC 后台查看 AI 结果 — 检测框和缺陷类型可见
- [ ] 2.7 PC 后台查看物料使用 — 使用量正确
- [ ] 2.8 经营看板 — 能统计完工数据

## 场景 3：同步冲突与复核

- [ ] 3.1 移动端离线修改工单
- [ ] 3.2 PC 同时修改同一工单 version+1
- [ ] 3.3 移动端 push 旧版本 → 产生 VERSION_CONFLICT
- [ ] 3.4 PC 后台看到冲突记录 (sync_conflict)
- [ ] 3.5 PC 管理员查看冲突详情 — 左右对比 JSON
- [ ] 3.6 PC 管理员使用 KEEP_SERVER 策略处理
- [ ] 3.7 冲突状态变为 RESOLVED
- [ ] 3.8 操作日志记录冲突处理
- [ ] 3.9 工单版本日志记录变更
- [ ] 3.10 移动端 pull → 获取到冲突已处理的最终版本

## 场景 4：物料使用追溯

- [ ] 4.1 维修工离线记录物料使用
- [ ] 4.2 同步后 PC 后台能看到物料使用记录
- [ ] 4.3 物料库存正确扣减
- [ ] 4.4 出入库记录能追溯

## 场景 5：人员资质校验

- [ ] 5.1 维修工证书即将到期 → PC 后台预警
- [ ] 5.2 移动端 pull 获取资质状态
- [ ] 5.3 派工时自动校验人员资质

## 场景 6：AI 辅助验收闭环

- [ ] 6.1 移动端拍施工照片 → 本地 AI 接口识别缺陷
- [ ] 6.2 AI 结果同步到服务端
- [ ] 6.3 PC 后台查看 AI 结果和缺陷框
- [ ] 6.4 PC 完成人工复核 (确认/误报/忽略)
- [ ] 6.5 移动端 pull 获取复核结果

## 场景 7：端到端回溯

- [ ] 7.1 从工单能回溯到：派工记录 → 施工记录 → 附件 → 签名 → 验收 → PDF → AI → 物料
- [ ] 7.2 所有记录的 syncStatus 正确
- [ ] 7.3 sync_task / sync_log 记录完整
- [ ] 7.4 冲突全生命周期可追溯

---

## 已验证（自动化测试）

| 场景 | 测试文件 | 结果 |
|------|----------|------|
| 设备注册 + 心跳 | QualificationSyncControllerTest | ✅ 通过 |
| sync/pull 返回工单 | QualificationSyncControllerTest | ✅ 通过 |
| sync/push CREATE record | QualificationSyncControllerTest | ✅ 通过 |
| sync/push CREATE attachment | QualificationSyncControllerTest | ✅ 通过 |
| version 冲突生成 | QualificationSyncControllerTest | ✅ 通过 |
| 管理员查看冲突列表 | QualificationSyncControllerTest | ✅ 通过 |
| 管理员 KEEP_SERVER 处理 | QualificationSyncControllerTest | ✅ 通过 |
| 操作日志记录 | QualificationSyncControllerTest | ✅ 通过 |
| 审计轨迹 | QualificationSyncControllerTest | ✅ 通过 |
