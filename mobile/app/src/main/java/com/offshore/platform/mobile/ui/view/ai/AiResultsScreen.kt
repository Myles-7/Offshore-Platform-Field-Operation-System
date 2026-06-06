package com.offshore.platform.mobile.ui.view.ai

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offshore.platform.mobile.data.local.entity.LocalAiResultEntity
import com.offshore.platform.mobile.domain.enums.AiReviewStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiResultsScreen(
    workOrderId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AiResultsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(workOrderId) { viewModel.load(workOrderId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI辅助识别") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(Modifier.width(6.dp))
                        Text("AI识别结果仅供参考，最终以人工验收为准",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                    if (state.results.isEmpty() && !state.isLoading) {
                        Spacer(Modifier.height(4.dp))
                        Text("（使用Mock AI引擎 — TFLite模型未部署）",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.results.isEmpty() && !state.isLoading) {
                    item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("暂无AI识别结果") } }
                }
                items(state.results) { result -> AiResultCard(result) }
            }
        }
    }
}

@Composable
private fun AiResultCard(result: LocalAiResultEntity) {
    val review = AiReviewStatus.fromCode(result.reviewStatus)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SmartToy, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("缺陷类型: ${result.defectType ?: "-"}", fontWeight = FontWeight.Bold)
                Text("置信度: ${(result.confidence * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                StatusChip(label = review.displayName, color = review.color)
            }
        }
    }
}

@Composable
fun StatusChip(label: String, color: androidx.compose.ui.graphics.Color) {
    Surface(shape = MaterialTheme.shapes.extraSmall, color = color.copy(alpha = 0.12f)) {
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}
