package com.offshore.platform.mobile.ui.view.workorder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offshore.platform.mobile.domain.enums.Priority
import com.offshore.platform.mobile.domain.enums.SyncStatus
import com.offshore.platform.mobile.domain.enums.WorkOrderStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDetailScreen(
    workOrderId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToRecords: () -> Unit = {},
    onNavigateToAttachments: () -> Unit = {},
    onNavigateToSignature: () -> Unit = {},
    onNavigateToPdf: () -> Unit = {},
    onNavigateToAi: () -> Unit = {},
    viewModel: WorkOrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(workOrderId) {
        viewModel.loadWorkOrder(workOrderId)
        viewModel.loadMaterials(workOrderId)
        viewModel.loadQualificationCheck(workOrderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.workOrder?.workOrderNo ?: "工单详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.isOffline) {
                        Icon(
                            Icons.Default.SignalWifiOff,
                            contentDescription = "离线",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            uiState.error != null && uiState.workOrder == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error ?: "加载失败", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                val wo = uiState.workOrder ?: return@Scaffold
                val status = WorkOrderStatus.fromCodeOrNull(wo.status)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status banner
                    status?.let { s ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = s.color.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusChip(s.displayName, s.color)
                                Spacer(modifier = Modifier.width(8.dp))
                                if (wo.conflictFlag == 1) {
                                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("冲突待处理", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                }
                                val sync = SyncStatus.fromCodeOrNull(wo.syncStatus)
                                if (sync != null && sync != SyncStatus.SYNCED) {
                                    SyncStatusChip(sync)
                                }
                            }
                        }
                    }

                    // Status message
                    uiState.statusMessage?.let {
                        Snackbar(
                            containerColor = if (uiState.isOffline) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.primary
                        ) {
                            Text(it, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    // Basic info card
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DetailRow("工单编号", wo.workOrderNo)
                            DetailRow("项目名称", wo.projectName ?: "-")
                            DetailRow("作业标题", wo.workTitle)
                            DetailRow("作业类型", wo.workType ?: "-")
                            DetailRow("作业地点", wo.workLocation ?: "-")
                            DetailRow("作业内容", wo.workContentSummary ?: "-")
                            val priority = Priority.fromCodeOrNull(wo.priority)
                            DetailRow("优先级", priority?.displayName ?: "-")
                            DetailRow("负责人", wo.leaderName ?: "-")
                            DetailRow("维修工", wo.maintainerName ?: "-")
                            DetailRow("计划开始", wo.plannedStartTime ?: "-")
                            DetailRow("计划结束", wo.plannedEndTime ?: "-")
                            if (wo.actualStartTime != null) DetailRow("实际开始", wo.actualStartTime)
                            if (wo.actualEndTime != null) DetailRow("实际结束", wo.actualEndTime)
                        }
                    }

                    // Status actions (stage 10)
                    StatusActionSection(
                        status = wo.status,
                        isSubmitting = uiState.isSubmitting,
                        onAccept = viewModel::acceptWorkOrder,
                        onStart = viewModel::startWorkOrder,
                        onSubmitAcceptance = { viewModel.submitForAcceptance("施工完成") }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Navigation cards: record, materials, qualifications, signature, PDF, AI, sync
                    SectionCard(
                        title = "施工记录",
                        onClick = onNavigateToRecords,
                        trailing = { Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(20.dp)) }
                    ) {
                        Text("查看施工记录列表", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SectionCard(
                        title = "附件信息",
                        onClick = onNavigateToAttachments,
                        trailing = { Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(20.dp)) }
                    ) {
                        Text("查看附件列表", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SectionCard(title = "物料需求") {
                        uiState.materials.forEach { mat ->
                            Text(mat.materialName ?: "-", style = MaterialTheme.typography.bodySmall)
                        }
                        if (uiState.materials.isEmpty()) Text("暂无物料信息", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SectionCard(title = "资质校验") {
                        uiState.qualificationChecks.take(3).forEach { check ->
                            val msg = runCatching { check["message"].toString() }.getOrDefault("")
                            Text(msg, style = MaterialTheme.typography.bodySmall)
                        }
                        if (uiState.qualificationChecks.isEmpty()) Text("暂无资质数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SectionCard(
                        title = "签名状态",
                        onClick = onNavigateToSignature,
                        trailing = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp)) }
                    ) {
                        Text("前往签名", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    SectionCard(
                        title = "PDF状态",
                        onClick = onNavigateToPdf,
                        trailing = { Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) }
                    ) {
                        Text("查看/生成PDF验收单", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    SectionCard(
                        title = "AI识别结果",
                        onClick = onNavigateToAi,
                        trailing = { Icon(Icons.Default.Psychology, null, modifier = Modifier.size(20.dp)) }
                    ) {
                        Text("查看AI缺陷识别结果", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row {
        Text(
            "$label：",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SectionCard(
    title: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick ?: {}
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                trailing?.invoke()
            }
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun StatusActionSection(
    status: String,
    isSubmitting: Boolean,
    onAccept: () -> Unit,
    onStart: () -> Unit,
    onSubmitAcceptance: () -> Unit
) {
    val allowed = WorkOrderStatus.fromCodeOrNull(status)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("操作", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (allowed == WorkOrderStatus.ASSIGNED) {
                    Button(onClick = onAccept, enabled = !isSubmitting) { Text("接单") }
                }
                if (allowed == WorkOrderStatus.ACCEPTED) {
                    Button(onClick = onStart, enabled = !isSubmitting) { Text("开始施工") }
                }
                if (allowed == WorkOrderStatus.IN_PROGRESS) {
                    Button(onClick = onSubmitAcceptance, enabled = !isSubmitting) { Text("提交验收") }
                }
            }
        }
    }
}
