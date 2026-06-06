package com.offshore.platform.mobile.ui.view.record

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderRecordEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkRecordListScreen(
    workOrderId: Long,
    onNavigateBack: () -> Unit,
    onCreateRecord: () -> Unit,
    onEditRecord: (Long) -> Unit,
    viewModel: WorkRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(workOrderId) {
        viewModel.loadRecords(workOrderId)
        viewModel.loadAttachments(workOrderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("施工记录") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRecord) {
                Icon(Icons.Default.Add, contentDescription = "新增施工记录")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            uiState.records.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("暂无施工记录，点击 + 新增") }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(padding)) {
                items(uiState.records) { record ->
                    RecordCard(
                        record = record,
                        attachmentCount = uiState.attachments.count { it.recordId == record.id },
                        onClick = {
                            val id = record.id
                            if (id > 0) onEditRecord(id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordCard(record: LocalWorkOrderRecordEntity, attachmentCount: Int, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick, elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("记录 #${record.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(record.constructionTime ?: "-", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(4.dp))
            Text(record.constructionDesc ?: "-", style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            if (record.abnormalFlag == 1) {
                Spacer(Modifier.height(4.dp))
                Text("异常：${record.abnormalDesc ?: ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (attachmentCount > 0) { Icon(Icons.Default.AttachFile, null, Modifier.size(14.dp)); Text("$attachmentCount", style = MaterialTheme.typography.labelSmall) }
                Text("状态: ${record.syncStatus}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
