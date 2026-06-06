package com.offshore.platform.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offshore.platform.mobile.util.NetworkMonitor

/**
 * Banner showing current connectivity state.
 * Display at top of every screen that supports offline mode.
 */
@Composable
fun OfflineBanner(networkMonitor: NetworkMonitor) {
    val status by networkMonitor.status.collectAsStateWithLifecycle()
    val msg = when (status) {
        com.offshore.platform.mobile.domain.enums.NetworkStatus.DISCONNECTED -> "离线模式 — 数据将在有网时自动同步"
        com.offshore.platform.mobile.domain.enums.NetworkStatus.METERED -> "按流量计费 — 大文件仅WiFi上传"
        else -> null
    }
    msg?.let {
        Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SignalWifiOff, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.width(6.dp))
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
fun ConflictHintBanner(workOrderId: Long, hasConflict: Boolean) {
    if (!hasConflict) return
    Surface(color = Color(0xFFFFF3E0), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, Modifier.size(14.dp), tint = Color(0xFFE65100))
            Spacer(Modifier.width(6.dp))
            Text("该工单存在待复核冲突，请勿重复提交", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
        }
    }
}
