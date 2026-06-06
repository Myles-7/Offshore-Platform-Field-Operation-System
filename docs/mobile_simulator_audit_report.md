# 移动端模拟器功能完整性审计报告

> **审计日期**: 2026-06-06
> **项目**: 海上平台现场作业管理系统 — 移动端子系统
> **审计范围**: mobile/ 目录下全部 Kotlin 源代码
> **构建状态**: `./gradlew clean assembleDebug` ✅ SUCCESS
> **单元测试**: `./gradlew testDebugUnitTest` ❌ 1 FAILED (EntityMapperTest > initializationError)
> **Lint 检查**: `./gradlew lint` ✅ SUCCESS (仅有 AGP 版本过时 Warning)

---

## 一、技术栈与架构总览

| 维度 | 详情 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material3 |
| 导航 | Jetpack Navigation Compose |
| 依赖注入 | Hilt + KSP |
| 数据库 | Room 2.x (16 表, version 2, fallbackToDestructiveMigration) |
| 网络 | Retrofit + OkHttp + kotlinx.serialization |
| 相机 | CameraX (core/camera2/lifecycle/video/view) |
| 后台任务 | WorkManager (定时同步 + 单次上传) |
| 图片加载 | Coil |
| 安全 | EncryptedSharedPreferences (token 存储) |
| Java 兼容 | coreLibraryDesugaring (java.time on API < 26) |
| 编译 SDK | 35 |
| Min SDK | 24 |
| 目标 SDK | 35 |
| 路由数 | 19 个 composable 路由 |
| DAO 数 | 15 个 (10 业务 + 5 同步子系统) |
| Entity 数 | 16 个 |
| API Service | 8 个 (Auth/MobileWorkOrder/Sync/File/Material/Qualification/Ai/Knowledge) |
| ViewModel | 14 个 |
| Repository | 4 个 (Auth/WorkOrder/Sync/AiInference) |

---

## 二、页面清单与实现状态

### 2.1 认证模块

| 页面 | 路由 | 状态 | 说明 |
|------|------|------|------|
| SplashScreen | SPLASH | ✅ READY | 自动登录检测 → 跳转登录页或工单列表 |
| LoginScreen | LOGIN | ✅ READY | 用户名/密码输入、登录按钮、离线模式按钮 |
| MineScreen | MINE | ✅ READY | 用户信息卡片、离线指示器、退出登录 |

### 2.2 工单模块

| 页面 | 路由 | 状态 | 说明 |
|------|------|------|------|
| WorkOrderListScreen | WORK_ORDER_LIST | ✅ READY | 工单列表、状态筛选、下拉刷新、空状态 |
| WorkOrderDetailScreen | WORK_ORDER_DETAIL/{id} | ⚠️ PARTIAL | 基本信息完整，但 5/7 SectionCard 是占位符 |
| WorkOrderComponents | — | ✅ READY | WorkOrderCard、StatusChip、FilterBar 共享组件 |

### 2.3 施工记录模块

| 页面 | 路由 | 状态 | 说明 |
|------|------|------|------|
| WorkRecordListScreen | WORK_ORDER_RECORDS/{id} | ✅ READY | 施工记录列表、FAB 新建 |
| WorkRecordEditScreen | RECORD_CREATE/{id} / RECORD_EDIT/{id} | ⚠️ PARTIAL | 🐛 **异常复选框无法切换** |

### 2.4 媒体与附件模块

| 页面 | 路由 | 状态 | 说明 |
|------|------|------|------|
| CameraScreen | CAMERA | ✅ READY | CameraX PreviewView + ImageCapture 完整实现 |
| CameraPlaceholderScreen | (备用) | ❌ STUB | 纯占位符，点击显示 Toast "next iteration" |
| VideoRecordScreen | VIDEO_RECORD/{...} | ⚠️ PARTIAL | 录制流程完整，❌ 缺少 PreviewView 取景器 |
| AudioRecordScreen | AUDIO_RECORD/{...} | ✅ READY | 录音 UI 完整 (无需取景器) |
| AttachmentListScreen | WORK_ORDER_ATTACHMENTS/{id} | ✅ READY | 附件列表 + 音频播放控制 + 按类型图标 |

### 2.5 签名与验收模块

| 页面 | 路由 | 状态 | 说明 |
|------|------|------|------|
| SignatureScreen | SIGNATURE/{id} | ⚠️ PARTIAL | Canvas 签名可用，坐标/密度未校准 |
| AcceptanceEditScreen | (在详情中导航) | ❌ STUB | 仅一个输入框，signatureFile 永远为 null |

### 2.6 PDF 模块

| 页面 | 路由 | 状态 | 说明 |
|------|------|------|------|
| PdfPreviewScreen | PDF_PREVIEW/{id} | ⚠️ PARTIAL | UI 完整，❌ 无真实 PDF 渲染器，❌ onRegenerate 未触发 |

### 2.7 同步模块

| 页面 | 路由 | 状态 | 说明 |
|------|------|------|------|
| SyncCenterScreen | SYNC_CENTER | ✅ READY | 网络状态/待处理计数/同步日志/检查点 |

### 2.8 物料与资质模块

| 页面 | 路由 | 状态 | 说明 |
|------|------|------|------|
| MaterialUsageScreen | MATERIAL_USAGE/{id} | ⚠️ PARTIAL | 列表正常，添加对话框状态未在 ViewModel 中管理 |
| QualificationStatusScreen | QUALIFICATION_STATUS | ⚠️ PARTIAL | ⚠️ **ViewModel 竞态条件** |

### 2.9 AI 与知识库模块

| 页面 | 路由 | 状态 | 说明 |
|------|------|------|------|
| AiResultsScreen | AI_RESULTS/{id} | ❌ STUB | 仅显示"Mock AI engine"免责声明，无操作按钮 |
| KnowledgeScreen | KNOWLEDGE | ⚠️ PARTIAL | 搜索无防抖，点击无详情导航 |

### 2.10 导航与框架

| 组件 | 状态 | 说明 |
|------|------|------|
| AppNavHost | ✅ READY | 19 路由完整，CameraScreen 硬编码参数有隐患 |
| Routes | ✅ READY | CAMERA_WITH_IDS 路由已定义但未使用 (死代码) |
| OfflineComponents | ✅ READY | OfflineBanner / ConflictHintBanner 完整 |
| HomeScreen | ✅ READY | 静态首页 |

---

## 三、数据层审计

### 3.1 Room 数据库 (16 表)

| 表 | Entity | DAO | 状态 |
|----|--------|-----|------|
| work_order | LocalWorkOrderEntity | WorkOrderDao | ✅ READY |
| work_order_record | LocalWorkOrderRecordEntity | WorkOrderRecordDao | ✅ READY |
| work_order_attachment | LocalWorkOrderAttachmentEntity | AttachmentDao | ✅ READY |
| work_order_signature | LocalSignatureEntity | SignatureDao | ✅ READY |
| work_order_acceptance | LocalAcceptanceEntity | AcceptanceDao | ✅ READY |
| work_order_pdf | LocalPdfEntity | PdfDao | ✅ READY |
| material_requirement | LocalMaterialRequirementEntity | MaterialRequirementDao | ✅ READY |
| material_usage | LocalMaterialUsageEntity | MaterialUsageDao | ✅ READY |
| qualification_status | LocalQualificationStatusEntity | QualificationStatusDao | ✅ READY |
| ai_result | LocalAiResultEntity | AiResultDao | ✅ READY |
| knowledge_case | LocalKnowledgeCaseEntity | KnowledgeCaseDao | ✅ READY |
| sync_queue | LocalSyncQueueEntity | SyncQueueDao | ✅ READY |
| sync_log | LocalSyncLogEntity | SyncLogDao | ✅ READY |
| sync_checkpoint | LocalSyncCheckpointEntity | SyncCheckpointDao | ✅ READY |
| conflict_hint | LocalConflictHintEntity | ConflictHintDao | ✅ READY |
| device_info | LocalDeviceInfoEntity | DeviceInfoDao | ✅ READY |

### 3.2 关键数据层发现

| 项目 | 状态 | 详情 |
|------|------|------|
| Entity → Schema 一致性 | ✅ | schema/2.json 与实体声明一致 |
| RoomTypeConverters | ✅ | LocalDateTime / 可空 Int/Long 转换完整 |
| EntityMapper | ✅ | 覆盖 WorkOrder/Record/Attachment 三种核心实体 |
| SyncDataMapper | 🐛 **严重Bug** | `getByServerId` 是死代码存根，始终返回 null——**冲突检测完全失效** |
| SyncDataMapper 映射覆盖 | ⚠️ | Signature/Acceptance/PDF/MaterialUsage 4 种实体无拉取映射 |
| 离线优先模式 | ✅ | WorkOrderRepository.executeStatusAction() 乐观更新→入队→API |
| SyncWorker | ✅ | 15分钟周期 + 单次即时同步，完善的 Result 处理 |

---

## 四、网络层审计

| 组件 | 状态 | 说明 |
|------|------|------|
| ApiConfig | ✅ READY | 超时配置合理 (connect 30s, read 30s, write 120s) |
| ApiClient | ✅ READY | 三拦截器 (Logging/Auth/DeviceId)，配置完整 |
| AuthApi | ✅ READY | login/logout/current 三个端点 |
| MobileWorkOrderApi | ✅ READY | 11 个端点，覆盖全部工单操作 |
| SyncApi | ✅ READY | device/pull/push/ack/heartbeat/tasks/logs |
| FileApi | ✅ READY | upload/batch/chunk/preview/download/delete |
| MaterialApi | ✅ READY | 接口完整 |
| QualificationApi | ✅ READY | 接口完整 |
| AiApi | ✅ READY | 接口完整 |
| KnowledgeApi | ✅ READY | 接口完整 |
| BaseRepository | ✅ READY | safeApiCall 完整，含 IO 异常分类处理 |
| NetworkMonitor | ✅ READY | ConnectivityManager callback → StateFlow |

---

## 五、关键问题按模块列出

### 🐛 P0 — 主链路必须修复 (4 项)

| # | 模块 | 问题 | 文件:行号 | 影响 | 修复建议 |
|---|------|------|-----------|------|----------|
| P0-1 | 同步拉取 | SyncDataMapper.getByServerId() 始终返回 null，冲突检测完全失效 | SyncDataMapper.kt:309-314 | 所有从服务器拉取的工单/记录/附件数据会静默覆盖本地未同步变更 | 在 WorkOrderDao/WorkOrderRecordDao/AttachmentDao 中实现真实的 `@Query("SELECT * FROM ... WHERE serverId = :serverId LIMIT 1")` 方法 |
| P0-2 | 工单详情 | 5/7 SectionCard 是占位符，施工记录/附件/签名/PDF/AI 无导航入口 | WorkOrderDetailScreen.kt:154-171 | 用户无法从详情页进入关键子功能 | 为每个 SectionCard 添加 onClick 导航到对应路由页面 |
| P0-3 | 验收 | AcceptanceViewModel 签名文件从未赋值，提交无异常处理 | AcceptanceViewModel.kt / AcceptanceEditScreen.kt | 验收流程完全不可用 | 增加签名捕获集成、照片附件、try-catch 错误处理 |
| P0-4 | 认证 | LoginScreen 使用独立 hiltViewModel() 导致状态不共享 | [已在前序对话中修复] | 登录页永远停留在 Loading 状态 | ✅ 已修复 (共享 ViewModel) |

### 🟠 P1 — 演示和验收必须修复 (8 项)

| # | 模块 | 问题 | 文件:行号 | 修复建议 |
|---|------|------|-----------|----------|
| P1-1 | PDF | PdfPreviewScreen 无真实 PDF 渲染器，只显示文件图标 | PdfPreviewScreen.kt | 集成 AndroidPdfViewer 或 WebView PDF.js 渲染 |
| P1-2 | PDF | onRegenerate 回调在 PdfPreviewScreen 中从未被调用 | PdfPreviewScreen.kt | 在重新生成按钮 onClick 中触发 viewModel.generatePdf() |
| P1-3 | 视频 | VideoRecordScreen 缺少 CameraX PreviewView 取景器 | VideoRecordScreen.kt | 添加 PreviewView，参考 CameraScreen 的实现 |
| P1-4 | 施工记录 | 异常 Checkbox 无法切换——点击后设置的是 abnormalDesc 而非 abnormalFlag | WorkRecordEditScreen.kt:58 | 修改 onCheckedChange 为 `viewModel.updateEditField(RecordEditField.ABNORMAL_FLAG, if (state.abnormalFlag == 1) "0" else "1")` |
| P1-5 | 签名 | SignatureScreen Canvas 坐标是 dp，renderSignatureToFile 硬编码 800x400px | SignatureScreen.kt / SignatureViewModel.kt | 使用 Canvas 实际测量尺寸 scaling 到目标 Bitmap |
| P1-6 | 签名 | 签名同步 payload 使用字符串拼接 JSON，特殊字符会破坏格式 | SignatureViewModel.kt:90-91 | 使用 kotlinx.serialization 构建 JSON |
| P1-7 | 资质 | QualificationStatusViewModel 存在竞态条件——状态快照过期 | QualificationStatusViewModel.kt:29-44 | 使用局部变量而非 _uiState.value 进行警告计算 |
| P1-8 | 单元测试 | EntityMapperTest 初始化失败 (ClassNotFoundException) | EntityMapperTest.kt | 检查 kotlinx.serialization/json 测试依赖配置 |

### 🟡 P2 — 体验优化 (7 项)

| # | 模块 | 问题 | 修复建议 |
|---|------|------|----------|
| P2-1 | 知识库 | 搜索无防抖，每次按键都触发 DAO 查询 | 添加 300ms debounce |
| P2-2 | 知识库 | 卡片无点击详情导航 | 添加 onClick → 详情页或底部弹窗 |
| P2-3 | AI | AiResultsScreen 无人工复核按钮 (确认/驳回) | 添加审批操作并关联 AiReviewStatus 状态变更 |
| P2-4 | 物料 | 添加对话框状态在 Compose remember 中，配置变更时丢失 | 移至 ViewModel StateFlow 管理 |
| P2-5 | 导航 | CameraScreen 路由使用硬编码 workOrderId=0L, workOrderNo="UNKNOWN" | 使用 CAMERA_WITH_IDS 路由传递正确参数 |
| P2-6 | 数据库 | AppDatabase 使用 fallbackToDestructiveMigration() | 实现正式的 Migration(1→2) |
| P2-7 | AppLogger | 文档声称发布版抑制日志，但代码未检查 BuildConfig.DEBUG | 添加 `if (BuildConfig.DEBUG) Log.d(...)` |

### 🔵 P3 — 后续增强 (5 项)

| # | 模块 | 问题 | 说明 |
|---|------|------|------|
| P3-1 | AI | TFLiteAiInferenceEngine.infer() 返回 null，只有 Mock 可用 | 需要端侧 .tflite 模型或云端推理 |
| P3-2 | 文件 | 分片上传 FileApi 接口已定义但未验证集成 | 大文件上传需要分片逻辑 |
| P3-3 | 水印 | WatermarkUtil 不处理 EXIF 方向标签，照片可能旋转 | 使用 ExifInterface 检测并旋转后再加水印 |
| P3-4 | TokenManager | prefs() 每次调用都创建新 MasterKey 实例 | 缓存 EncryptedSharedPreferences 实例 |
| P3-5 | SyncDataMapper | Signature/Acceptance/PDF/MaterialUsage 四种实体无服务器拉取映射 | 添加 processPullItem 分支和 EntityMapper 扩展 |

---

## 六、依赖后端但接口未验证

| API | 移动端调用 | 验证状态 | 验证命令 |
|-----|-----------|----------|----------|
| POST /api/auth/login | ✅ | ✅ 验证通过 (200) | `curl -X POST localhost:8082/api/auth/login -d '{"loginName":"worker_li","password":"123456"}'` |
| GET /api/mobile/work-orders | ✅ | ✅ 验证通过 (200) | `curl -H "Authorization: Bearer \$TOKEN" localhost:8082/api/mobile/work-orders` |
| GET /api/mobile/work-orders/{id} | ✅ | ⬜ 未单独验证 | `curl -H "Authorization: Bearer \$TOKEN" localhost:8082/api/mobile/work-orders/1` |
| POST /api/mobile/work-orders/{id}/accept | ✅ | ⬜ 未单独验证 | `curl -X POST -H "Authorization: Bearer \$TOKEN" localhost:8082/api/mobile/work-orders/1/accept` |
| POST /api/mobile/work-orders/{id}/start | ✅ | ⬜ 未单独验证 | |
| POST /api/mobile/work-orders/{id}/feedback | ✅ | ⬜ 未单独验证 | |
| POST /api/mobile/work-orders/{id}/submit-acceptance | ✅ | ⬜ 未单独验证 | |
| GET /api/mobile/work-orders/{id}/materials | ✅ | ⬜ 未单独验证 | |
| GET /api/mobile/work-orders/{id}/qualification-check | ✅ | ⬜ 未单独验证 | |
| POST /api/sync/push | ✅ | ⬜ 未单独验证 | |
| POST /api/sync/pull | ✅ | ⬜ 未单独验证 | |
| POST /api/files/upload | ⚠️ | ⬜ 未验证 | 需要 multipart form-data 测试 |
| POST /api/sync/device/register | ✅ (login时) | ⬜ 未单独验证 | |
| POST /api/sync/device/heartbeat | ✅ (定时) | ⬜ 未单独验证 | |

---

## 七、存在崩溃风险

| 风险 | 位置 | 触发条件 | 严重程度 |
|------|------|----------|----------|
| RoomTypeConverters 日期解析崩溃 | RoomTypeConverters.kt | 日期格式不是 `yyyy-MM-dd HH:mm:ss` | 🔴 高 |
| QualificationStatusViewModel 竞态条件 | QualificationStatusViewModel.kt:29-44 | 网络数据加载后警告计算读到过期快照 | 🟡 中 |
| SignatureScreen localId null 时 "sig-ok" 硬编码 | SignatureScreen.kt | 下游依赖 ID 的功能数据丢失 | 🟡 中 |
| NetworkModule 中 ApiClient 未初始化 NPE | NetworkModule.kt | Hilt 注入时序异常 | 🟢 低 (Hilt 保证) |
| BaseRepository (Unit as T) 强制转换 | BaseRepository.kt:52 | body.data 为 null 且 T 非 Unit 类型 | 🟢 低 (罕见场景) |

---

## 八、需要真机测试的功能

| 功能 | 原因 |
|------|------|
| 相机拍照 | 模拟器相机行为与真机不同，需要验证 CameraX 预览、闪光灯、前后摄像头切换 |
| 视频录制 | 模拟器通常不支持 CameraX VideoCapture，必须真机验证 |
| 录音 | 模拟器可能无麦克风输入 |
| 电子签名 | 触摸笔画在模拟器 vs 真机差异大 |
| 网络切换 | 真机飞行模式 → WiFi → 4G 切换测试离线同步 |
| 文件存储 | FileProvider 路径在真机和模拟器上可能不同 |
| GPS 定位 | 施工记录位置坐标在模拟器上无真实 GPS |
| Coil 图片加载 | 模拟器网络配置可能导致加载失败 |

---

## 九、验证命令清单

```bash
# 1. 清理构建
cd mobile && ./gradlew clean assembleDebug

# 2. 单元测试
./gradlew testDebugUnitTest

# 3. Lint 检查
./gradlew lint

# 4. 安装到模拟器
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 5. 启动应用
adb shell am start -n com.offshore.platform.mobile/.MainActivity

# 6. 查看应用日志 (过滤 OkHttp + 应用 tag)
adb logcat -s OkHttp:V OffshoreMobile:D

# 7. 查看特定崩溃
adb logcat -b crash

# 8. 查看 Room 数据库
adb shell run-as com.offshore.platform.mobile cat databases/offshore_mobile.db | sqlite3

# 9. 模拟网络断开
adb shell svc wifi disable
adb shell svc data disable

# 10. 模拟网络恢复
adb shell svc wifi enable
adb shell svc data enable

# 11. 测试 API 连通性 (从模拟器内部)
adb shell ping -c 3 10.0.2.2

# 12. 查看 SharedPreferences
adb shell run-as com.offshore.platform.mobile cat shared_prefs/offshore_secure_token.xml

# 13. 查看同步队列
adb shell run-as com.offshore.platform.mobile sqlite3 databases/offshore_mobile.db "SELECT count(*) FROM sync_queue WHERE sync_status='PENDING'"
```

---

## 十、下一轮最应该修复的 10 个任务

按优先级排序：

| 优先级 | 任务 | 预估工时 | 影响范围 |
|--------|------|----------|----------|
| **P0** | 修复 SyncDataMapper.getByServerId() —— 实现真实的 DAO 查询方法，恢复冲突检测机制 | 2h | 离线同步全链路 |
| **P0** | WorkOrderDetailScreen 5 个占位 SectionCard 添加导航入口 | 3h | 工单详情页完整性 |
| **P0** | AcceptanceViewModel/AcceptanceEditScreen 验收流程实现 | 4h | 验收流程可用性 |
| **P1** | WorkRecordEditScreen 异常复选框 Bug 修复 | 0.5h | 施工记录功能 |
| **P1** | VideoRecordScreen 添加 CameraX PreviewView 取景器 | 3h | 视频录制可用性 |
| **P1** | PdfPreviewScreen 集成 PDF 渲染器 + 修复 onRegenerate | 4h | PDF 查看功能 |
| **P1** | SignatureScreen 坐标 scaling 修复 + JSON payload 规范化 | 2h | 签名可靠性 |
| **P1** | 修复 EntityMapperTest 单元测试 | 1h | CI 可接入 |
| **P2** | 知识库搜索防抖 + 卡片点击详情 | 1h | 用户体验 |
| **P2** | 资质 ViewModel 竞态条件修复 | 1h | 资质显示正确性 |

---

## 十一、总体评估

### 已完成度估算

| 层次 | 完成度 | 说明 |
|------|--------|------|
| 基础设施 (网络/DI/导航/权限) | 95% | 几乎完整 |
| 数据层 (实体/DAO/离线同步) | 85% | SyncDataMapper 有严重 Bug |
| 认证流程 | 95% | ViewModel 共享问题已修复 |
| 工单列表与详情 | 70% | 5/7 SectionCard 存根 |
| 施工记录 | 85% | 异常复选框 Bug |
| 拍照与附件 | 80% | 拍照可用，视频无取景器 |
| 签名与验收 | 30% | 验收几乎不可用 |
| PDF | 60% | 逻辑完整但无渲染 |
| 同步子系统 | 90% | SyncDataMapper 有 Bug |
| 物料/资质/AI/知识库 | 50% | 页面有但交互不完整 |
| **整体** | **~75%** | 主链路可走通，但多个子功能需要修复 |

### 核心结论

1. **架构设计优秀**：离线优先、Room + SyncQueue + WorkManager 的设计模式完整
2. **代码质量良好**：Hilt DI、安全 Token 存储、异常分类处理都到位
3. **但实现缺口明显**：约 25% 的功能是占位符或存根，验收/PDF/视频等子功能需要大量工作
4. **一个严重 Bug**：SyncDataMapper 冲突检测机制完全失效，是离线数据一致性的阻断问题
5. **11 个功能依赖后端接口未验证**，建议尽快进行端到端联调测试
