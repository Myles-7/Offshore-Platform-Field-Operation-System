package com.offshore.platform.mobile.ui.view.workorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderEntity
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.repository.WorkOrderRepository
import com.offshore.platform.mobile.domain.enums.WorkOrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkOrderListViewModel @Inject constructor(
    private val repository: WorkOrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkOrderListUiState())
    val uiState: StateFlow<WorkOrderListUiState> = _uiState.asStateFlow()

    val workOrders: StateFlow<List<LocalWorkOrderEntity>> = repository.observeLocalWorkOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Load local data immediately; if empty, try fetch
        viewModelScope.launch {
            val localCount = workOrders.value.size
            if (localCount == 0) {
                refresh()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = repository.fetchWorkOrders()
            _uiState.value = when (result) {
                is NetworkResult.Success -> {
                    if (workOrders.value.isEmpty()) {
                        _uiState.value.copy(isLoading = false, isEmpty = true)
                    } else {
                        _uiState.value.copy(isLoading = false, isEmpty = false)
                    }
                }
                is NetworkResult.NetworkError -> {
                    if (workOrders.value.isNotEmpty()) {
                        _uiState.value.copy(isLoading = false, isOffline = true)
                    } else {
                        _uiState.value.copy(
                            isLoading = false,
                            isEmpty = true,
                            error = "网络不可用，请连接网络后下拉刷新"
                        )
                    }
                }
                else -> _uiState.value.copy(
                    isLoading = false,
                    error = "加载失败，请下拉重试"
                )
            }
        }
    }

    fun setFilter(statusCode: String?) {
        _uiState.value = _uiState.value.copy(filterStatus = statusCode)
    }
}

data class WorkOrderListUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null,
    val filterStatus: String? = null,
    val filterSearch: String = ""
)
