package com.offshore.platform.mobile.ui.view.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderAttachmentEntity
import com.offshore.platform.mobile.ui.view.record.WorkRecordViewModel
import com.offshore.platform.mobile.util.AudioPlayerManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentListScreen(
    workOrderId: Long,
    onNavigateBack: () -> Unit,
    onAddPhoto: () -> Unit,
    viewModel: WorkRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(workOrderId) { viewModel.loadAttachments(workOrderId) }

    // Release audio player on exit
    DisposableEffect(Unit) {
        onDispose { AudioPlayerManager.release() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("附件") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPhoto) { Icon(Icons.Default.CameraAlt, null) }
        }
    ) { padding ->
        if (uiState.attachments.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无附件，点击 + 拍照", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(padding)) {
                items(uiState.attachments) { att ->
                    AttachmentCard(att)
                }
            }
        }
    }
}

@Composable
private fun AttachmentCard(att: LocalWorkOrderAttachmentEntity) {
    val playerState by AudioPlayerManager.state.collectAsStateWithLifecycle()
    val isThisPlaying = att.attachmentType == "AUDIO" &&
        playerState.currentFile?.absolutePath == att.localFilePath &&
        (playerState.state == AudioPlayerManager.State.PLAYING || playerState.state == AudioPlayerManager.State.PAUSED)

    val icon = when (att.attachmentType) {
        "PHOTO" -> Icons.Default.Photo
        "VIDEO" -> Icons.Default.Videocam
        "AUDIO" -> Icons.Default.Mic
        "SIGNATURE" -> Icons.Default.Draw
        "PDF" -> Icons.Default.PictureAsPdf
        else -> Icons.Default.AttachFile
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(att.attachmentName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${att.attachmentType} · ${att.captureTime ?: ""}",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("上传: ${att.uploadStatus} · 同步: ${att.syncStatus}",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                if (att.durationSeconds > 0) {
                    Text("时长: ${att.durationSeconds}秒",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }

            // Audio playback controls
            if (att.attachmentType == "AUDIO") {
                AudioPlaybackControls(
                    attachment = att,
                    isThisPlaying = isThisPlaying,
                    playerState = playerState
                )
            }
        }

        // Progress bar when this audio is playing
        if (isThisPlaying && playerState.durationMs > 0) {
            Column(Modifier.padding(horizontal = 12.dp)) {
                LinearProgressIndicator(
                    progress = { playerState.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(playerState.positionFormatted, style = MaterialTheme.typography.labelSmall)
                    Text(playerState.durationFormatted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun AudioPlaybackControls(
    attachment: LocalWorkOrderAttachmentEntity,
    isThisPlaying: Boolean,
    playerState: AudioPlayerManager.PlayerState
) {
    val file = attachment.localFilePath?.let { File(it) }
    val fileExists = file?.exists() == true

    if (isThisPlaying) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = {
                    if (playerState.state == AudioPlayerManager.State.PLAYING) {
                        AudioPlayerManager.pause()
                    } else {
                        AudioPlayerManager.resume()
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    if (playerState.state == AudioPlayerManager.State.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playerState.state == AudioPlayerManager.State.PLAYING) "暂停" else "继续",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = { AudioPlayerManager.stop() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Stop, "停止", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    } else {
        // Not playing — show play button or error state
        if (fileExists) {
            IconButton(
                onClick = { file?.let { AudioPlayerManager.play(it) } },
                modifier = Modifier.size(32.dp)
            ) {
                if (playerState.state == AudioPlayerManager.State.LOADING &&
                    playerState.currentFile?.absolutePath == attachment.localFilePath) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.PlayArrow, "播放", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Icon(Icons.Default.ErrorOutline, "文件不存在或已被清理",
                Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
        }
    }
}
