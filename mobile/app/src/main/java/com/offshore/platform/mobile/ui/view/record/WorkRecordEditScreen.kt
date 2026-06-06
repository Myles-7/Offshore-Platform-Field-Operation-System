package com.offshore.platform.mobile.ui.view.record

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkRecordEditScreen(
    workOrderId: Long,
    editRecordId: Long?,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WorkRecordViewModel = hiltViewModel()
) {
    val state by viewModel.editState.collectAsStateWithLifecycle()

    LaunchedEffect(editRecordId) {
        if (editRecordId != null) viewModel.setupEdit(editRecordId) else viewModel.setupNew()
    }

    LaunchedEffect(state.saved, state.savedOffline) {
        if (state.saved || state.savedOffline) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editRecordId != null) "编辑施工记录" else "新增施工记录") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.desc, onValueChange = { viewModel.updateEditField(RecordEditField.DESC, it) },
                label = { Text("施工描述*") }, modifier = Modifier.fillMaxWidth(), minLines = 3
            )
            OutlinedTextField(
                value = state.siteCondition, onValueChange = { viewModel.updateEditField(RecordEditField.SITE_CONDITION, it) },
                label = { Text("现场状况") }, modifier = Modifier.fillMaxWidth(), minLines = 2
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(
                    checked = state.abnormalFlag == 1,
                    onCheckedChange = { viewModel.updateEditField(RecordEditField.ABNORMAL_FLAG, "") }
                )
                Text("有异常", modifier = Modifier.align(Alignment.CenterVertically))
            }
            if (state.abnormalFlag == 1) {
                OutlinedTextField(
                    value = state.abnormalDesc, onValueChange = { viewModel.updateEditField(RecordEditField.ABNORMAL_DESC, it) },
                    label = { Text("异常说明") }, modifier = Modifier.fillMaxWidth()
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = state.weather, onValueChange = { viewModel.updateEditField(RecordEditField.WEATHER, it) }, label = { Text("天气") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = state.temperature?.toString() ?: "", onValueChange = { viewModel.updateEditField(RecordEditField.TEMPERATURE, it) }, label = { Text("温度(℃)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = state.humidity?.toString() ?: "", onValueChange = { viewModel.updateEditField(RecordEditField.HUMIDITY, it) }, label = { Text("湿度(%)") }, modifier = Modifier.weight(1f))
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            if (state.savedOffline) Text("已离线保存，待同步", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

            Button(
                onClick = { viewModel.saveRecord(workOrderId, editRecordId, state.desc, state.siteCondition.ifBlank { null }, state.abnormalFlag, state.abnormalDesc.ifBlank { null }, state.weather.ifBlank { null }, state.temperature, state.humidity) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting && state.desc.isNotBlank()
            ) {
                if (state.isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("保 存")
            }
        }
    }
}
