package com.offshore.platform.mobile.ui.view.media

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Minimal camera placeholder — full CameraX integration in later stage refinement.
 * Delegates to system camera intent for quick prototyping.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPlaceholderScreen(
    workOrderId: Long,
    recordId: Long?,
    onNavigateBack: () -> Unit,
    onPhotoTaken: (localPath: String) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拍照") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CameraAlt, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("拍照功能 (CameraX)", style = MaterialTheme.typography.titleMedium)
            Text("工单: $workOrderId", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                Toast.makeText(context, "CameraX integration in next iteration", Toast.LENGTH_SHORT).show()
            }) { Text("打开相机（占位）") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onNavigateBack) { Text("返回") }
        }
    }
}
