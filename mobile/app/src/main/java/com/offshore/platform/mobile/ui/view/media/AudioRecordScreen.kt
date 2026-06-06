package com.offshore.platform.mobile.ui.view.media

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecordScreen(
    workOrderId: Long,
    recordId: Long?,
    workOrderNo: String,
    onNavigateBack: () -> Unit,
    onAudioSaved: () -> Unit,
    viewModel: MediaCaptureViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(workOrderId) { viewModel.init(workOrderId, recordId, workOrderNo, "AUDIO") }
    LaunchedEffect(state.saved) { if (state.saved) onAudioSaved() }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startRecording(context) else viewModel.setPermissionDenied()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("语音备注") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.permissionDenied -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("需要录音权限")
                    Button(onClick = { permLauncher.launch(Manifest.permission.RECORD_AUDIO) }) { Text("授予权限") }
                }
                state.isRecording -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Mic, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text("录音中: ${state.recordingDuration}s", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::stopRecording, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("停止录音") }
                }
                state.capturedFile != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Mic, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("录音完成: ${state.capturedFile?.name ?: ""}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("大小: ${(state.capturedFile?.length() ?: 0) / 1024}KB · 时长: ${state.recordingDuration}s", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = viewModel::reset) { Text("重新录音") }
                        Button(onClick = { viewModel.saveFile() }) { Text("确认保存") }
                    }
                }
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Mic, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("工单: $workOrderNo", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { permLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                        Icon(Icons.Default.Mic, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("开始录音")
                    }
                }
            }
            if (state.savedOffline) Snackbar(modifier = Modifier.align(Alignment.BottomCenter)) { Text("已离线保存，待上传") }
        }
    }
}
