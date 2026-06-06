package com.offshore.platform.mobile.ui.view.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offshore.platform.mobile.data.local.entity.LocalSyncLogEntity
import com.offshore.platform.mobile.domain.enums.NetworkStatus
import com.offshore.platform.mobile.data.repository.SyncSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncCenterScreen(
    viewModel: SyncViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("同步中心") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Network status card
            item {
                NetworkStatusCard(networkStatus)
            }

            // Sync action card
            item {
                SyncActionCard(
                    pendingCount = uiState.pendingCount,
                    isSyncing = uiState.isSyncing,
                    lastSyncResult = uiState.lastSyncResult,
                    lastError = uiState.lastError,
                    isOnline = networkStatus != NetworkStatus.DISCONNECTED,
                    onSyncClick = { viewModel.triggerSync() }
                )
            }

            // Checkpoint card
            if (uiState.checkpoints.isNotEmpty()) {
                item {
                    CheckpointCard(uiState.checkpoints.first().lastSuccessTime)
                }
            }

            // Recent logs header
            if (uiState.recentLogs.isNotEmpty()) {
                item {
                    Text(
                        text = "最近同步记录",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Log entries
            items(uiState.recentLogs) { log ->
                SyncLogCard(log)
            }

            // Empty state
            if (uiState.recentLogs.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无同步记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkStatusCard(status: NetworkStatus) {
    val (icon, label, color) = when (status) {
        NetworkStatus.CONNECTED -> Triple(Icons.Default.Wifi, "已连接", MaterialTheme.colorScheme.primary)
        NetworkStatus.METERED -> Triple(Icons.Default.Wifi, "按流量计费", MaterialTheme.colorScheme.tertiary)
        NetworkStatus.DISCONNECTED -> Triple(Icons.Default.WifiOff, "离线", MaterialTheme.colorScheme.error)
        NetworkStatus.UNKNOWN -> Triple(Icons.Default.QuestionMark, "未知", MaterialTheme.colorScheme.outline)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "网络状态", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = label, style = MaterialTheme.typography.bodyLarge, color = color)
            }
        }
    }
}

@Composable
private fun SyncActionCard(
    pendingCount: Int,
    isSyncing: Boolean,
    lastSyncResult: SyncSummary?,
    lastError: String?,
    isOnline: Boolean,
    onSyncClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "数据同步",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (pendingCount > 0) {
                    Badge { Text("$pendingCount") }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (pendingCount > 0) "待同步：$pendingCount 项" else "数据已是最新",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            lastSyncResult?.let {
                Text(
                    text = "上次同步：${it.lastSyncTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            lastError?.let {
                Text(
                    text = "错误：$it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSyncClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = isOnline && !isSyncing
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("同步中…")
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始同步")
                }
            }
        }
    }
}

@Composable
private fun CheckpointCard(lastSuccessTime: String?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "最近同步成功：${lastSuccessTime ?: "无"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SyncLogCard(log: LocalSyncLogEntity) {
    val (icon, color) = when (log.syncStatus) {
        "SYNCED" -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        "FAILED" -> Icons.Default.Error to MaterialTheme.colorScheme.error
        "CONFLICT" -> Icons.Default.Warning to MaterialTheme.colorScheme.error
        else -> Icons.Default.Schedule to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "${log.syncDirection} / ${log.syncType}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "成功 ${log.successCount} | 失败 ${log.failedCount} | 冲突 ${log.conflictCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = log.createdAt ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
