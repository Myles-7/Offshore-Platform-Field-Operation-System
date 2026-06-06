package com.offshore.platform.mobile.ui.view.signature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptanceEditScreen(
    workOrderId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToSignature: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AcceptanceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(workOrderId) { viewModel.init(workOrderId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提交验收") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("验收意见", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = state.opinion,
                onValueChange = viewModel::setOpinion,
                label = { Text("验收意见") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Text("签名", style = MaterialTheme.typography.titleSmall)
            if (state.signatureFile != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("签名文件: ${state.signatureFile?.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(onClick = onNavigateToSignature) {
                    Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("重新签名")
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("尚未完成签名", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onNavigateToSignature, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Gesture, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("前往签名")
                        }
                    }
                }
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            if (state.savedOffline) Text("已离线保存，待同步", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

            Button(
                onClick = viewModel::submitAcceptance,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting && state.opinion.isNotBlank() && state.signatureFile != null
            ) {
                if (state.isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("提交验收")
            }
        }
    }
}
