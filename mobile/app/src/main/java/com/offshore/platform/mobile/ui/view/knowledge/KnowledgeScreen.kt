package com.offshore.platform.mobile.ui.view.knowledge

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
import com.offshore.platform.mobile.data.local.entity.LocalKnowledgeCaseEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeScreen(viewModel: KnowledgeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("知识库") })
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(query, { query = it; viewModel.search(it) }, label = { Text("搜索知识库") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) })
            Spacer(Modifier.height(8.dp))
            val display = if (state.searchQuery.isNotBlank()) state.searchResults else state.cases
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (display.isEmpty()) { item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("暂无数据") } } }
                items(display) { case -> KnowledgeCard(case) }
            }
        }
    }
}

@Composable
private fun KnowledgeCard(case: LocalKnowledgeCaseEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(case.title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(case.description ?: "", style = MaterialTheme.typography.bodySmall, maxLines = 3)
            case.keywords?.let { Text("关键词: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
        }
    }
}
