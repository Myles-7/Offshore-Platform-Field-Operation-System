package com.offshore.platform.mobile.ui.view.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.entity.LocalSyncCheckpointEntity
import com.offshore.platform.mobile.data.local.entity.LocalSyncLogEntity
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.repository.FileUploadRepository
import com.offshore.platform.mobile.data.repository.SyncRepository
import com.offshore.platform.mobile.data.repository.SyncSummary
import com.offshore.platform.mobile.domain.enums.NetworkStatus
import com.offshore.platform.mobile.util.AppLogger
import com.offshore.platform.mobile.util.NetworkMonitor
import com.offshore.platform.mobile.worker.SyncWorker
import com.offshore.platform.mobile.worker.UploadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    val networkStatus get() = networkMonitor.status

    /** Track last auto-sync trigger time to debounce. */
    private var lastAutoSyncTimeMs: Long = 0L
    private val autoSyncMinIntervalMs = 30_000L // 30s debounce

    init {
        loadLocalState()
        observeNetworkRecovery()
    }

    /**
     * When network transitions from DISCONNECTED → CONNECTED, auto-trigger sync.
     * Debounced: no more than once per 30s. Only triggers when logged in and device registered.
     */
    private fun observeNetworkRecovery() {
        viewModelScope.launch {
            networkMonitor.status.collect { status ->
                if (status == NetworkStatus.CONNECTED || status == NetworkStatus.METERED) {
                    val now = System.currentTimeMillis()
                    if (now - lastAutoSyncTimeMs > autoSyncMinIntervalMs) {
                        lastAutoSyncTimeMs = now
                        val token = com.offshore.platform.mobile.util.TokenManager.getToken()
                        if (!token.isNullOrBlank()) {
                            AppLogger.i("SyncViewModel: network recovered → auto-triggering sync")
                            triggerSync()
                        }
                    }
                }
            }
        }
    }

    fun loadLocalState() {
        viewModelScope.launch {
            val pendingCount = syncRepository.getPendingCount()
            val logs = syncRepository.getRecentSyncLogs(limit = 20)
            val checkpoints = syncRepository.getCheckpoints()
            val failedCount = logs.count { it.syncStatus == "FAILED" }
            val conflictCount = logs.count { it.syncStatus == "CONFLICT" }
            val filePendingCount = fileUploadRepository.getPendingCount()

            _uiState.value = _uiState.value.copy(
                pendingCount = pendingCount,
                failedCount = failedCount,
                conflictCount = conflictCount,
                filePendingCount = filePendingCount,
                recentLogs = logs,
                checkpoints = checkpoints,
                isLoading = false
            )
        }
    }

    fun triggerSync() {
        if (_uiState.value.isSyncing) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, lastError = null)

            // First retry failed file uploads
            val fileResult = fileUploadRepository.retryFailedUploads()
            when (fileResult) {
                is NetworkResult.Success -> AppLogger.i("Auto file upload: ${fileResult.data} files uploaded")
                is NetworkResult.NetworkError -> AppLogger.w("Auto file upload: network error")
                else -> { /* continue */ }
            }

            // Then full sync
            val result = syncRepository.fullSync()

            when (result) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        lastSyncResult = result.data,
                        lastError = null
                    )
                }
                is NetworkResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        lastError = "网络连接失败，请检查网络后重试"
                    )
                    AppLogger.w("Auto sync network error")
                }
                is NetworkResult.Unauthorized -> {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        lastError = "登录已过期，请重新登录"
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        lastError = (result as? NetworkResult.BusinessError)?.message ?: "同步失败"
                    )
                }
            }

            // Reload local state
            loadLocalState()
        }
    }

    /** Retry a single failed item by re-queuing it. */
    fun retrySingle(queueId: Long) {
        viewModelScope.launch {
            syncRepository.retryQueueItem(queueId)
            triggerSync()
        }
    }
}

data class SyncUiState(
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    val conflictCount: Int = 0,
    val filePendingCount: Int = 0,
    val recentLogs: List<LocalSyncLogEntity> = emptyList(),
    val checkpoints: List<LocalSyncCheckpointEntity> = emptyList(),
    val lastSyncResult: SyncSummary? = null,
    val lastError: String? = null
)
