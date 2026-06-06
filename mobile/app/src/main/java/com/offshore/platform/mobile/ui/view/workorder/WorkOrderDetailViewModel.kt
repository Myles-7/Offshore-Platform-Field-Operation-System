package com.offshore.platform.mobile.ui.view.workorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderEntity
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.repository.WorkOrderRepository
import com.offshore.platform.mobile.data.repository.WorkOrderStateChange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import com.offshore.platform.mobile.data.remote.dto.MobileMaterialDTO
import javax.inject.Inject

@HiltViewModel
class WorkOrderDetailViewModel @Inject constructor(
    private val repository: WorkOrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkOrderDetailUiState())
    val uiState: StateFlow<WorkOrderDetailUiState> = _uiState.asStateFlow()

    fun loadWorkOrder(serverId: Long) {
        // First load from local
        viewModelScope.launch {
            val local = repository.getLocalById(serverId)
            if (local != null) {
                _uiState.value = _uiState.value.copy(
                    workOrder = local,
                    isLoading = false
                )
            }
            // Then try refresh from server
            val result = repository.getWorkOrderFromApi(serverId)
            if (result is NetworkResult.Success) {
                val localAfter = repository.getLocalById(serverId)
                _uiState.value = _uiState.value.copy(
                    workOrder = localAfter,
                    isLoading = false
                )
            } else if (local != null) {
                _uiState.value = _uiState.value.copy(isOffline = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载工单详情失败"
                )
            }
        }
    }

    fun loadMaterials(serverId: Long) {
        viewModelScope.launch {
            val result = repository.fetchMaterials(serverId)
            if (result is NetworkResult.Success) {
                _uiState.value = _uiState.value.copy(materials = result.data)
            }
        }
    }

    fun loadQualificationCheck(serverId: Long) {
        viewModelScope.launch {
            val result = repository.fetchQualificationCheck(serverId)
            if (result is NetworkResult.Success) {
                _uiState.value = _uiState.value.copy(qualificationChecks = result.data)
            }
        }
    }

    // ---- status actions (stage 10) ----

    fun acceptWorkOrder() {
        val wo = _uiState.value.workOrder ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            val result = repository.acceptWorkOrder(wo.serverId ?: return@launch)
            handleStateChangeResult(result)
        }
    }

    fun startWorkOrder() {
        val wo = _uiState.value.workOrder ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            val result = repository.startWorkOrder(wo.serverId ?: return@launch)
            handleStateChangeResult(result)
        }
    }

    fun submitFeedback(desc: String, abnormalFlag: Int = 0, abnormalDesc: String? = null) {
        val wo = _uiState.value.workOrder ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            val result = repository.submitFeedback(
                serverId = wo.serverId ?: return@launch,
                desc = desc,
                abnormalFlag = abnormalFlag,
                abnormalDesc = abnormalDesc
            )
            handleStateChangeResult(result)
        }
    }

    fun submitForAcceptance(desc: String) {
        val wo = _uiState.value.workOrder ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            val result = repository.submitForAcceptance(
                serverId = wo.serverId ?: return@launch,
                desc = desc
            )
            handleStateChangeResult(result)
        }
    }

    private suspend fun handleStateChangeResult(result: NetworkResult<WorkOrderStateChange>) {
        when (result) {
            is NetworkResult.Success -> {
                val change = result.data
                val local = repository.getLocalById(change.serverId)
                _uiState.value = _uiState.value.copy(
                    workOrder = local,
                    isSubmitting = false,
                    statusMessage = if (change.syncedOnline) "操作成功" else "已离线保存，待同步",
                    isOffline = !change.syncedOnline
                )
            }
            is NetworkResult.NetworkError -> {
                val wo = _uiState.value.workOrder
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    statusMessage = "已离线保存，待同步",
                    isOffline = true,
                    workOrder = wo?.let { repository.getLocalById(it.serverId ?: 0L) } ?: wo
                )
            }
            else -> {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = (result as? NetworkResult.BusinessError)?.message ?: "操作失败"
                )
            }
        }
    }
}

data class WorkOrderDetailUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val statusMessage: String? = null,
    val workOrder: LocalWorkOrderEntity? = null,
    val materials: List<MobileMaterialDTO> = emptyList(),
    val qualificationChecks: List<JsonObject> = emptyList()
)
