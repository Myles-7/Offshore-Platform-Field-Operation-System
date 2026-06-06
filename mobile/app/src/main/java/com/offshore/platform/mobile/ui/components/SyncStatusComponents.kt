package com.offshore.platform.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.offshore.platform.mobile.domain.enums.SyncStatus

/**
 * A compact status badge showing the sync status of a single entity.
 *
 * Usage:
 *   SyncStatusBadge(syncStatus = "SYNCED")
 *   SyncStatusBadge(syncStatus = "PENDING", onClickRetry = { viewModel.retrySync(item) })
 */
@Composable
fun SyncStatusBadge(
    syncStatus: String,
    onClickRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val status = SyncStatus.fromCode(syncStatus)
    val (label, color, icon) = when (status) {
        SyncStatus.LOCAL_ONLY -> Triple("仅本地", MaterialTheme.colorScheme.outline, Icons.Default.PhoneAndroid)
        SyncStatus.PENDING -> Triple("待同步", Color(0xFFE65100), Icons.Default.Schedule)
        SyncStatus.SYNCING -> Triple("同步中", MaterialTheme.colorScheme.primary, Icons.Default.Sync)
        SyncStatus.SYNCED -> Triple("已同步", MaterialTheme.colorScheme.primary, Icons.Default.CheckCircle)
        SyncStatus.FAILED -> Triple("同步失败", MaterialTheme.colorScheme.error, Icons.Default.Error)
        SyncStatus.CONFLICT -> Triple("存在冲突", MaterialTheme.colorScheme.error, Icons.Default.Warning)
        SyncStatus.DELETED -> Triple("已作废", MaterialTheme.colorScheme.outline, Icons.Default.Delete)
        SyncStatus.IGNORED -> Triple("已忽略", MaterialTheme.colorScheme.outline, Icons.Default.RemoveCircleOutline)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
        if (status == SyncStatus.FAILED && onClickRetry != null) {
            Spacer(modifier = Modifier.width(6.dp))
            TextButton(
                onClick = onClickRetry,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(20.dp)
            ) {
                Text("重试", style = MaterialTheme.typography.labelSmall)
            }
        }
        if (status == SyncStatus.CONFLICT) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "(等待后台复核)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Sync summary row showing counts for pending/failed/conflict.
 */
@Composable
fun SyncSummaryBar(
    pendingCount: Int,
    failedCount: Int,
    conflictCount: Int,
    filePendingCount: Int,
    lastSyncTime: String?,
    onSyncClick: () -> Unit,
    isSyncing: Boolean,
    isOnline: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "数据同步",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else if (!isOnline) {
                    Icon(Icons.Default.WifiOff, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CountChip("待同步", pendingCount, Color(0xFFE65100))
                CountChip("同步失败", failedCount, MaterialTheme.colorScheme.error)
                CountChip("冲突", conflictCount, MaterialTheme.colorScheme.error)
                CountChip("文件待传", filePendingCount, MaterialTheme.colorScheme.tertiary)
            }

            lastSyncTime?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "最近同步：$it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun CountChip(label: String, count: Int, color: Color) {
    if (count > 0) {
        Surface(color = color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
            Text(
                text = "$label $count",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
