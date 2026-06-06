package com.offshore.platform.mobile.ui.view.signature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.dao.*
import com.offshore.platform.mobile.data.local.entity.*
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class AcceptanceUiState(
    val opinion: String = "",
    val signatureFile: File? = null,
    val isSubmitting: Boolean = false,
    val saved: Boolean = false,
    val savedOffline: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AcceptanceViewModel @Inject constructor(
    private val acceptanceDao: AcceptanceDao,
    private val signatureDao: SignatureDao,
    private val syncQueueDao: SyncQueueDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcceptanceUiState())
    val uiState: StateFlow<AcceptanceUiState> = _uiState.asStateFlow()

    private var workOrderId: Long = 0

    fun init(woId: Long) {
        workOrderId = woId
        loadLatestSignature()
    }

    /** Load the most recent signature file for this work order. */
    private fun loadLatestSignature() {
        viewModelScope.launch {
            val sigs = signatureDao.getByWorkOrderId(workOrderId)
            val latestSig = sigs.firstOrNull()
            val sigFile = latestSig?.localFilePath?.let { path ->
                val f = File(path)
                if (f.exists()) f else null
            }
            if (sigFile != null) {
                _uiState.value = _uiState.value.copy(signatureFile = sigFile)
            }
        }
    }

    fun setOpinion(v: String) { _uiState.value = _uiState.value.copy(opinion = v) }

    fun submitAcceptance() {
        // Prevent submission without signature
        if (_uiState.value.signatureFile == null || !_uiState.value.signatureFile!!.exists()) {
            _uiState.value = _uiState.value.copy(error = "请先在签名页面完成签名后再提交验收")
            return
        }
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
                val now = DateTimeUtil.nowFormatted()
                val localId = "acc-${UUID.randomUUID()}"

                val entity = LocalAcceptanceEntity(
                    localId = localId,
                    workOrderId = workOrderId,
                    acceptorName = TokenManager.getRealName() ?: TokenManager.getUsername(),
                    acceptanceTime = now,
                    acceptanceOpinion = _uiState.value.opinion,
                    acceptanceResult = "PASSED",
                    acceptanceStatus = "PENDING",
                    signatureFilePath = _uiState.value.signatureFile?.absolutePath,
                    syncStatus = "LOCAL_ONLY",
                    deviceId = DeviceManager.getOrCreate(),
                    operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now, updatedAt = now
                )
                acceptanceDao.insert(entity)

                val payload = buildJsonObject {
                    put("localId", localId)
                    put("workOrderId", workOrderId)
                    put("opinion", _uiState.value.opinion)
                    put("signatureFilePath", _uiState.value.signatureFile?.absolutePath ?: "")
                    put("acceptorName", entity.acceptorName ?: "")
                }
                syncQueueDao.enqueue(LocalSyncQueueEntity(
                    moduleType = "ACCEPTANCE", entityType = "WORK_ORDER_ACCEPTANCE",
                    localId = localId, workOrderId = workOrderId, actionType = "CREATE",
                    payloadJson = Json.encodeToString(JsonObject.serializer(), payload),
                    syncStatus = "PENDING",
                    deviceId = DeviceManager.getOrCreate(), operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now, updatedAt = now
                ))

                _uiState.value = _uiState.value.copy(isSubmitting = false, saved = true, savedOffline = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "提交失败: ${e.message}")
            }
        }
    }
}
