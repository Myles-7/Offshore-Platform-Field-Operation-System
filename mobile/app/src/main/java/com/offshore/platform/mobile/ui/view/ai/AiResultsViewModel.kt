package com.offshore.platform.mobile.ui.view.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.dao.AiResultDao
import com.offshore.platform.mobile.data.local.entity.LocalAiResultEntity
import com.offshore.platform.mobile.data.remote.api.AiApi
import com.offshore.platform.mobile.data.remote.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiResultsViewModel @Inject constructor(
    private val aiApi: AiApi,
    private val dao: AiResultDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiResultsState())
    val uiState: StateFlow<AiResultsState> = _uiState.asStateFlow()

    fun load(workOrderId: Long) {
        viewModelScope.launch {
            dao.observeByWorkOrderId(workOrderId).collect { local ->
                _uiState.value = _uiState.value.copy(results = local, isLoading = false)
            }
        }
    }
}

data class AiResultsState(
    val isLoading: Boolean = true,
    val results: List<LocalAiResultEntity> = emptyList()
)
