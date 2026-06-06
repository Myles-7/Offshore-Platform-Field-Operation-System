package com.offshore.platform.mobile.ui.view.qualification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offshore.platform.mobile.data.local.entity.LocalQualificationStatusEntity
import com.offshore.platform.mobile.domain.enums.QualificationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualificationStatusScreen(
    viewModel: QualificationStatusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(topBar = { TopAppBar(title = { Text("我的资质") }) }) { padding ->
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(padding)) {
            state.warnings.forEach { warn ->
                item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) { Text(warn, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall) } }
            }
            item { Text("共 ${state.certificates.size} 项资质", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
            items(state.certificates) { cert -> CertificateCard(cert) }
            if (state.certificates.isEmpty() && !state.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("暂无资质数据") } }
            }
        }
    }
}

@Composable
private fun CertificateCard(cert: LocalQualificationStatusEntity) {
    val status = QualificationStatus.fromCode(cert.validStatus)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VerifiedUser, null, Modifier.size(24.dp), tint = status.color)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(cert.certificateName ?: "资质", fontWeight = FontWeight.Bold)
                Text("编号: ${cert.certificateNo ?: "-"}", style = MaterialTheme.typography.labelSmall)
                Text("有效期至: ${cert.validTo ?: "-"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(label = status.displayName, color = status.color)
        }
    }
}

@Composable
fun StatusChip(label: String, color: androidx.compose.ui.graphics.Color) {
    Surface(shape = MaterialTheme.shapes.extraSmall, color = color.copy(alpha = 0.12f)) {
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}
