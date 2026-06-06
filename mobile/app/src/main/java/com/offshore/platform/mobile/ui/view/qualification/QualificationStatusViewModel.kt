package com.offshore.platform.mobile.ui.view.qualification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.dao.QualificationStatusDao
import com.offshore.platform.mobile.data.local.entity.LocalQualificationStatusEntity
import com.offshore.platform.mobile.data.remote.api.QualificationApi
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QualificationStatusViewModel @Inject constructor(
    private val qualApi: QualificationApi,
    private val dao: QualificationStatusDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(QualificationState())
    val uiState: StateFlow<QualificationState> = _uiState.asStateFlow()

    fun load() {
        if (_uiState.value.isLoading.not() && _uiState.value.certificates.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val uid = TokenManager.getUserId()
                val local = dao.getByUserId(uid)
                val list = if (local.isNotEmpty()) {
                    local
                } else {
                    val result = qualApi.myQualificationStatus()
                    if (result.isSuccessful && result.body()?.code == 200) {
                        val remote = result.body()?.data?.mapNotNull { it.toLocalQual() } ?: emptyList()
                        dao.insertAll(remote)
                        remote
                    } else {
                        emptyList()
                    }
                }
                val expiring = list.filter {
                    it.validStatus in listOf("EXPIRING_SOON", "EXPIRED")
                }
                _uiState.value = QualificationState(
                    certificates = list,
                    warnings = expiring.map { "${it.certificateName ?: "证书"} ${it.validStatus}" },
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

data class QualificationState(
    val isLoading: Boolean = true,
    val certificates: List<LocalQualificationStatusEntity> = emptyList(),
    val warnings: List<String> = emptyList()
)

private fun kotlinx.serialization.json.JsonObject.toLocalQual(): LocalQualificationStatusEntity {
    val s: (String) -> String? = { key -> (this[key] as? kotlinx.serialization.json.JsonPrimitive)?.content }
    val l: (String) -> Long? = { key -> (this[key] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toLongOrNull() }
    return LocalQualificationStatusEntity(
        localId = "qual-${s("id") ?: java.util.UUID.randomUUID()}",
        serverId = l("id"),
        certificateName = s("certificateName"),
        certificateNo = s("certificateNo"),
        validTo = s("validTo"),
        validStatus = s("validStatus") ?: "VALID",
        employeeId = l("employeeId"),
        userId = TokenManager.getUserId().takeIf { it > 0 }
    )
}
