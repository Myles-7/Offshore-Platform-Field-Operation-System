package com.offshore.platform.mobile.ui.view.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.remote.dto.toUserSession
import com.offshore.platform.mobile.data.repository.AuthRepository
import com.offshore.platform.mobile.domain.model.UserSession
import com.offshore.platform.mobile.util.AppLogger
import com.offshore.platform.mobile.util.DeviceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing the full auth flow: splash, login, logout, device registration.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // ---- UI state ----

    private val _authState = MutableStateFlow<LoginUiState>(LoginUiState.Loading)
    val authState: StateFlow<LoginUiState> = _authState.asStateFlow()

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    // ---- splash: auto-login ----

    fun checkAutoLogin() {
        AppLogger.d("checkAutoLogin: starting...")
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            AppLogger.d("checkAutoLogin: isLoggedIn=$isLoggedIn")
            if (!isLoggedIn) {
                AppLogger.d("checkAutoLogin: not logged in, showing login screen")
                _authState.value = LoginUiState.NotLoggedIn
                return@launch
            }
            AppLogger.d("checkAutoLogin: fetching current user...")
            val result = authRepository.fetchCurrentUser()
            when (result) {
                is NetworkResult.Success -> {
                    _session.value = result.data.toUserSession()
                    _authState.value = LoginUiState.LoggedIn
                    registerDeviceIfNeeded()
                }
                is NetworkResult.NetworkError -> {
                    // Offline fallback: use cached session if available
                    val local = authRepository.buildLocalSession()
                    if (local != null) {
                        _session.value = local
                        _authState.value = LoginUiState.OfflineLoggedIn
                    } else {
                        // Token exists but no cached user — force re-login
                        _authState.value = LoginUiState.NotLoggedIn
                    }
                }
                is NetworkResult.Unauthorized -> {
                    authRepository.logout()
                    _authState.value = LoginUiState.NotLoggedIn
                }
                else -> {
                    _authState.value = LoginUiState.NotLoggedIn
                }
            }
        }
    }

    // ---- login ----

    fun login(username: String, password: String) {
        AppLogger.d("login called: username=$username")
        if (username.isBlank() || password.isBlank()) {
            _authState.value = LoginUiState.Error("请输入用户名和密码")
            return
        }
        AppLogger.d("login: starting request...")
        viewModelScope.launch {
            _authState.value = LoginUiState.Loading
            val deviceId = DeviceManager.getOrCreate()
            val result = authRepository.login(
                username = username,
                password = password,
                deviceId = deviceId,
                appVersion = "1.0.0"
            )
            when (result) {
                is NetworkResult.Success -> {
                    _session.value = result.data.toUserSession()
                    _authState.value = LoginUiState.LoggedIn
                    registerDeviceIfNeeded()
                }
                is NetworkResult.Unauthorized -> {
                    _authState.value = LoginUiState.Error("用户名或密码错误")
                }
                is NetworkResult.Forbidden -> {
                    _authState.value = LoginUiState.Error("账号未开通移动端权限")
                }
                is NetworkResult.NetworkError -> {
                    _authState.value = LoginUiState.Error("网络连接失败，请检查网络后重试")
                }
                is NetworkResult.BusinessError -> {
                    _authState.value = LoginUiState.Error(result.message)
                }
                else -> {
                    _authState.value = LoginUiState.Error("登录失败，请重试")
                }
            }
        }
    }

    // ---- logout ----

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _session.value = null
            _authState.value = LoginUiState.NotLoggedIn
        }
    }

    // ---- device ----

    private suspend fun registerDeviceIfNeeded() {
        val deviceId = DeviceManager.getDeviceId() ?: return
        val result = authRepository.registerDevice(
            deviceId = deviceId,
            deviceName = android.os.Build.MODEL,
            appVersion = "1.0.0"
        )
        if (result is NetworkResult.Success) {
            // device registered (or already was)
        }
    }

    // ---- heartbeat ----

    fun sendHeartbeat() {
        viewModelScope.launch {
            val deviceId = DeviceManager.getDeviceId() ?: return@launch
            authRepository.sendHeartbeat(deviceId)
        }
    }

    // ---- helpers ----

    fun clearError() {
        if (_authState.value is LoginUiState.Error) {
            _authState.value = LoginUiState.NotLoggedIn
        }
    }
}

/** Simple login UI state. */
sealed class LoginUiState {
    data object Loading : LoginUiState()
    data object NotLoggedIn : LoginUiState()
    data object LoggedIn : LoginUiState()
    data object OfflineLoggedIn : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
