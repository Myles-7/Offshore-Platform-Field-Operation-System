package com.offshore.platform.mobile.ui.view.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderAttachmentEntity
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderRecordEntity
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.repository.WorkRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkRecordViewModel @Inject constructor(
    private val repository: WorkRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkRecordListUiState())
    val uiState: StateFlow<WorkRecordListUiState> = _uiState.asStateFlow()

    private val _editState = MutableStateFlow(WorkRecordEditUiState())
    val editState: StateFlow<WorkRecordEditUiState> = _editState.asStateFlow()

    private var recordsFlow: Flow<List<LocalWorkOrderRecordEntity>>? = null

    fun loadRecords(workOrderId: Long) {
        recordsFlow = repository.observeRecords(workOrderId)
        viewModelScope.launch {
            recordsFlow?.collect { list ->
                _uiState.value = _uiState.value.copy(records = list, isLoading = false)
            }
        }
    }

    fun loadAttachments(workOrderId: Long) {
        viewModelScope.launch {
            repository.observeAttachments(workOrderId).collect { attachments ->
                _uiState.value = _uiState.value.copy(attachments = attachments)
            }
        }
    }

    fun saveRecord(
        workOrderId: Long,
        editRecordId: Long?,
        desc: String,
        siteCondition: String?,
        abnormalFlag: Int,
        abnormalDesc: String?,
        weather: String?,
        temperature: Double?,
        humidity: Double?
    ) {
        viewModelScope.launch {
            _editState.value = _editState.value.copy(isSubmitting = true)
            val result = if (editRecordId == null) {
                repository.createRecord(
                    workOrderId = workOrderId,
                    constructionDesc = desc,
                    siteCondition = siteCondition,
                    abnormalFlag = abnormalFlag,
                    abnormalDesc = abnormalDesc,
                    weather = weather,
                    temperature = temperature,
                    humidity = humidity,
                    locationName = null, latitude = null, longitude = null
                )
            } else {
                repository.updateRecord(
                    recordId = editRecordId,
                    constructionDesc = desc,
                    siteCondition = siteCondition,
                    abnormalFlag = abnormalFlag,
                    abnormalDesc = abnormalDesc,
                    weather = weather,
                    temperature = temperature,
                    humidity = humidity,
                    locationName = null, latitude = null, longitude = null
                )
            }
            _editState.value = when (result) {
                is NetworkResult.Success -> _editState.value.copy(isSubmitting = false, saved = true)
                is NetworkResult.NetworkError -> _editState.value.copy(isSubmitting = false, savedOffline = true)
                else -> _editState.value.copy(isSubmitting = false, error = "保存失败")
            }
        }
    }

    fun setupNew() {
        _editState.value = WorkRecordEditUiState(editRecordId = null)
    }

    fun setupEdit(recordId: Long) {
        viewModelScope.launch {
            val record = repository.getById(recordId)
            if (record != null) {
                _editState.value = WorkRecordEditUiState(
                    editRecordId = recordId,
                    desc = record.constructionDesc ?: "",
                    siteCondition = record.siteCondition ?: "",
                    abnormalFlag = record.abnormalFlag,
                    abnormalDesc = record.abnormalDesc ?: "",
                    weather = record.weather ?: "",
                    temperature = record.temperature,
                    humidity = record.humidity
                )
            }
        }
    }

    fun updateEditField(field: RecordEditField, value: String) {
        _editState.value = when (field) {
            RecordEditField.DESC -> _editState.value.copy(desc = value)
            RecordEditField.SITE_CONDITION -> _editState.value.copy(siteCondition = value)
            RecordEditField.ABNORMAL_DESC -> _editState.value.copy(abnormalDesc = value)
            RecordEditField.ABNORMAL_FLAG -> _editState.value.copy(
                abnormalFlag = if (_editState.value.abnormalFlag == 1) 0 else 1,
                abnormalDesc = if (_editState.value.abnormalFlag == 1) "" else _editState.value.abnormalDesc
            )
            RecordEditField.WEATHER -> _editState.value.copy(weather = value)
            RecordEditField.TEMPERATURE -> _editState.value.copy(temperature = value.toDoubleOrNull())
            RecordEditField.HUMIDITY -> _editState.value.copy(humidity = value.toDoubleOrNull())
        }
    }
}

enum class RecordEditField { DESC, SITE_CONDITION, ABNORMAL_DESC, ABNORMAL_FLAG, WEATHER, TEMPERATURE, HUMIDITY }

data class WorkRecordListUiState(
    val isLoading: Boolean = true,
    val records: List<LocalWorkOrderRecordEntity> = emptyList(),
    val attachments: List<LocalWorkOrderAttachmentEntity> = emptyList()
)

data class WorkRecordEditUiState(
    val editRecordId: Long? = null,
    val desc: String = "",
    val siteCondition: String = "",
    val abnormalFlag: Int = 0,
    val abnormalDesc: String = "",
    val weather: String = "",
    val temperature: Double? = null,
    val humidity: Double? = null,
    val isSubmitting: Boolean = false,
    val saved: Boolean = false,
    val savedOffline: Boolean = false,
    val error: String? = null
)
