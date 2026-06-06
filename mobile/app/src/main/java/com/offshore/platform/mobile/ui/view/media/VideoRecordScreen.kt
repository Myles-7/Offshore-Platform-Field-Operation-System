package com.offshore.platform.mobile.ui.view.media

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offshore.platform.mobile.util.MediaRecorderVideoRecorder
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoRecordScreen(
    workOrderId: Long,
    recordId: Long?,
    workOrderNo: String,
    onNavigateBack: () -> Unit,
    onVideoSaved: () -> Unit,
    viewModel: MediaCaptureViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(workOrderId) {
        viewModel.init(workOrderId, recordId, workOrderNo, "VIDEO")
    }
    LaunchedEffect(state.saved) { if (state.saved) onVideoSaved() }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Start recording using VideoRecorder interface
            viewModel.startVideoRecording(context)
        } else {
            viewModel.setPermissionDenied()
        }
    }

    // Release recorder on exit
    DisposableEffect(Unit) {
        onDispose { viewModel.releaseRecorder() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("录像") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.permissionDenied -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("需要相机和麦克风权限")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }) {
                        Text("授予权限")
                    }
                }
                state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = viewModel::reset) { Text("重新录制") }
                }
                state.isRecording -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FiberManualRecord, null, Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text("录制中: ${state.recordingDuration}s",
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.width(200.dp))
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = viewModel::stopRecording,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Stop, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("停止录制")
                    }
                }
                state.capturedFile != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Videocam, null, Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("录制完成: ${state.capturedFile?.name ?: ""}",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "大小: ${(state.capturedFile?.length() ?: 0) / 1024}KB · 时长: ${state.recordingDuration}s",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = viewModel::reset) { Text("重新录制") }
                        Button(onClick = { viewModel.saveVideoFile() }) { Text("确认保存") }
                    }
                }
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Videocam, null, Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("工单: $workOrderNo", style = MaterialTheme.typography.titleMedium)
                    Text("(MediaRecorder Video)", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = {
                        permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                    }) {
                        Icon(Icons.Default.Videocam, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("开始录像")
                    }
                }
            }
            if (state.savedOffline) Snackbar(modifier = Modifier.align(Alignment.BottomCenter)) {
                Text("已离线保存，待上传")
            }
        }
    }
}
