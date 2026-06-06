package com.offshore.platform.mobile.ui.view.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.dao.*
import com.offshore.platform.mobile.data.local.entity.*
import com.offshore.platform.mobile.data.remote.api.MaterialApi
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MaterialUsageViewModel @Inject constructor(
    private val materialApi: MaterialApi,
    private val materialUsageDao: MaterialUsageDao,
    private val syncQueueDao: SyncQueueDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterialUsageState())
    val uiState: StateFlow<MaterialUsageState> = _uiState.asStateFlow()

    private var workOrderId: Long = 0

    fun load(woId: Long) {
        workOrderId = woId
        viewModelScope.launch {
            materialUsageDao.observeByWorkOrderId(woId).collect { usages ->
                _uiState.value = _uiState.value.copy(usages = usages, isLoading = false)
            }
        }
    }

    fun recordUsage(materialName: String, materialCode: String?, unit: String?, qty: Double, batchNo: String?, remark: String?) {
        viewModelScope.launch {
            val now = DateTimeUtil.nowFormatted()
            val localId = "mu-${UUID.randomUUID()}"
            val entity = LocalMaterialUsageEntity(
                localId = localId, workOrderId = workOrderId,
                materialName = materialName, materialCode = materialCode, unit = unit,
                usageQty = qty, batchNo = batchNo, remark = remark, usageTime = now,
                syncStatus = "LOCAL_ONLY", deviceId = DeviceManager.getOrCreate(),
                operatorId = TokenManager.getUserId().takeIf { it > 0 },
                createdAt = now, updatedAt = now
            )
            materialUsageDao.insert(entity)
            syncQueueDao.enqueue(LocalSyncQueueEntity(
                moduleType = "MATERIAL_USAGE", entityType = "WORK_ORDER_MATERIAL_USAGE",
                localId = localId, workOrderId = workOrderId, actionType = "CREATE",
                payloadJson = "{\"materialName\":\"$materialName\",\"qty\":$qty}",
                syncStatus = "PENDING", deviceId = DeviceManager.getOrCreate(),
                operatorId = TokenManager.getUserId().takeIf { it > 0 },
                createdAt = now, updatedAt = now
            ))
            _uiState.value = _uiState.value.copy(savedOffline = true)
        }
    }
}

data class MaterialUsageState(
    val isLoading: Boolean = true,
    val usages: List<LocalMaterialUsageEntity> = emptyList(),
    val savedOffline: Boolean = false
)
