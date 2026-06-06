package com.offshore.platform.mobile.ui.view.signature

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offshore.platform.mobile.data.local.entity.LocalSignatureEntity
import com.offshore.platform.mobile.util.DateTimeUtil
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureScreen(
    workOrderId: Long,
    onNavigateBack: () -> Unit,
    onSignatureSaved: (String) -> Unit,
    viewModel: SignatureViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current
    val canvasWidth = remember { 800f }; val canvasHeight = remember { 400f }

    LaunchedEffect(workOrderId) { viewModel.init(workOrderId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("电子签名") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("请在下方签名区域签名", style = MaterialTheme.typography.bodyMedium)
            Text("工单: $workOrderId", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val pos = change.position
                            viewModel.addPoint(pos.x, pos.y)
                        }
                    }
                ) {
                    drawRect(Color.White)
                    val path = Path()
                    state.points.forEachIndexed { i, pt ->
                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                    }
                    drawPath(path, Color.Black, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = viewModel::clear) { Text("清除") }
                Button(
                    onClick = {
                        viewModel.save(context, canvasWidth, canvasHeight, density.density)
                    },
                    enabled = state.points.isNotEmpty() && !state.isSaving
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text("保存签名")
                }
            }

            if (state.saved) {
                Text("签名已保存", color = MaterialTheme.colorScheme.primary)
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(800)
                    onSignatureSaved(state.localId ?: "sig-ok")
                }
            }
            if (state.savedOffline) Text("已离线保存，待同步", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
