package com.offshore.platform.mobile.ui.view.workorder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderEntity
import com.offshore.platform.mobile.domain.enums.Priority
import com.offshore.platform.mobile.domain.enums.SyncStatus
import com.offshore.platform.mobile.domain.enums.WorkOrderStatus

@Composable
fun WorkOrderCard(
    workOrder: LocalWorkOrderEntity,
    onClick: () -> Unit
) {
    val status = WorkOrderStatus.fromCodeOrNull(workOrder.status)
    val priority = Priority.fromCodeOrNull(workOrder.priority)
    val syncStatus = SyncStatus.fromCodeOrNull(workOrder.syncStatus)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top row: number + status chip + priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workOrder.workOrderNo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (syncStatus != null && syncStatus != SyncStatus.SYNCED) {
                        SyncStatusChip(syncStatus)
                    }
                    if (workOrder.conflictFlag == 1) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "冲突",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (status != null) {
                        StatusChip(
                            label = status.displayName,
                            color = status.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Text(
                text = workOrder.workTitle,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = workOrder.projectName ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (priority != null) {
                    Text(
                        text = priority.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = priority.color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Location + time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = workOrder.workLocation ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "详情",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Planned time
            if (workOrder.plannedStartTime != null) {
                Text(
                    text = "${workOrder.plannedStartTime} ~ ${workOrder.plannedEndTime ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// ---- Shared chips ----

@Composable
fun StatusChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SyncStatusChip(status: SyncStatus) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = status.color.copy(alpha = 0.12f)
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = status.color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WorkOrderFilterBar(
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = WorkOrderStatus.entries.indexOfFirst { it.code == selectedStatus }
            .coerceAtLeast(0),
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 8.dp
    ) {
        Tab(
            selected = selectedStatus == null,
            onClick = { onStatusSelected(null) },
            text = { Text("全部", style = MaterialTheme.typography.labelSmall) }
        )
        WorkOrderStatus.entries.take(5).forEach { status ->
            Tab(
                selected = selectedStatus == status.code,
                onClick = { onStatusSelected(status.code) },
                text = {
                    Text(
                        status.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedStatus == status.code) status.color else Color.Unspecified
                    )
                }
            )
        }
    }
}
