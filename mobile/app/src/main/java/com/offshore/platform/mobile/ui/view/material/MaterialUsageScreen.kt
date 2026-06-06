package com.offshore.platform.mobile.ui.view.material

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
import com.offshore.platform.mobile.data.local.entity.LocalMaterialUsageEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialUsageScreen(
    workOrderId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MaterialUsageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var matName by remember { mutableStateOf("") }
    var matCode by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var batchNo by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    LaunchedEffect(workOrderId) { viewModel.load(workOrderId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("物料使用记录") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null) } }
    ) { padding ->
        if (showAdd) {
            AlertDialog(
                onDismissRequest = { showAdd = false },
                title = { Text("记录物料使用") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(matName, { matName = it }, label = { Text("物料名称*") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(matCode, { matCode = it }, label = { Text("物料编码") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(unit, { unit = it }, label = { Text("单位") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(qty, { qty = it }, label = { Text("数量*") }, modifier = Modifier.weight(1f))
                        }
                        OutlinedTextField(batchNo, { batchNo = it }, label = { Text("批次号") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(remark, { remark = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.recordUsage(matName, matCode.ifBlank { null }, unit.ifBlank { null }, qty.toDoubleOrNull() ?: 0.0, batchNo.ifBlank { null }, remark.ifBlank { null })
                        showAdd = false; matName = ""; matCode = ""; unit = ""; qty = ""; batchNo = ""; remark = ""
                    }) { Text("保存") }
                },
                dismissButton = { OutlinedButton(onClick = { showAdd = false }) { Text("取消") } }
            )
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(padding)) {
            if (state.savedOffline) { item { Text("已离线保存，待同步", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) } }
            if (state.usages.isEmpty()) { item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("暂无物料使用记录") } } }
            items(state.usages) { usage ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(usage.materialName, fontWeight = FontWeight.Bold)
                            Text("${usage.usageQty} ${usage.unit ?: ""}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(usage.usageTime ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}
