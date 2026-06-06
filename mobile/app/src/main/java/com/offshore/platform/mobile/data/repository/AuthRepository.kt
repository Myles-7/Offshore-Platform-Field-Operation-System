package com.offshore.platform.mobile.data.repository

import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.remote.api.AuthApi
import com.offshore.platform.mobile.data.remote.api.SyncApi
import com.offshore.platform.mobile.data.remote.dto.*
import com.offshore.platform.mobile.domain.model.UserSession
import com.offshore.platform.mobile.util.AppLogger
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val syncApi: SyncApi
) : BaseRepository() {

    // ---- login / logout / current ----

    suspend fun login(
        username: String,
        password: String,
        deviceId: String,
        appVersion: String
    ): NetworkResult<LoginResponse> {
        AppLogger.d("AuthRepository.login: starting API call")
        val result = safeApiCall {
            AppLogger.d("AuthRepository.login: making request to authApi.login")
            authApi.login(
                MobileLoginRequest(
                    loginName = username,
                    password = password,
                    deviceId = deviceId,
                    appVersion = appVersion
                )
            )
        }
        AppLogger.d("AuthRepository.login: result=$result")
        if (result is NetworkResult.Success) {
            persistLogin(result.data)
        }
        return result
    }

    suspend fun fetchCurrentUser(): NetworkResult<LoginResponse> {
        val result = safeApiCall { authApi.currentUser() }
        if (result is NetworkResult.Success) {
            persistSessionFromResponse(result.data)
        }
        return result
    }

    suspend fun logout(): NetworkResult<Unit> {
        val result = safeApiCall { authApi.logout() }
        // Always clear local session regardless of server response
        TokenManager.clearSession()
        return result
    }

    // ---- device ----

    suspend fun registerDevice(
        deviceId: String,
        deviceName: String?,
        appVersion: String?
    ): NetworkResult<kotlinx.serialization.json.JsonObject> {
        return safeApiCall {
            syncApi.registerDevice(
                DeviceRegisterRequest(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appVersion = appVersion
                )
            )
        }
    }

    suspend fun sendHeartbeat(deviceId: String): NetworkResult<kotlinx.serialization.json.JsonObject> {
        return safeApiCall { syncApi.heartbeat(DeviceHeartbeatRequest(deviceId)) }
    }

    // ---- session helpers ----

    fun isLoggedIn(): Boolean {
        val token = TokenManager.getToken()
        return !token.isNullOrBlank()
    }

    fun canOfflineLogin(): Boolean {
        // If we have a stored token and user info, allow offline mode
        return isLoggedIn() && TokenManager.getUserId() > 0
    }

    fun buildLocalSession(): UserSession? {
        val userId = TokenManager.getUserId()
        if (userId <= 0) return null
        return UserSession(
            userId = userId,
            username = TokenManager.getUsername() ?: "",
            realName = TokenManager.getRealName(),
            roleCodes = TokenManager.getRoleCodes(),
            permissionCodes = TokenManager.getPermissionCodes(),
            dataScope = TokenManager.getDataScope(),
            primaryProjectId = TokenManager.getPrimaryProjectId().takeIf { it > 0 },
            lastLoginTime = TokenManager.getLastLoginTime(),
            isOfflineMode = true
        )
    }

    // ---- internal ----

    private fun persistLogin(response: LoginResponse) {
        TokenManager.saveToken(response.token)
        persistSessionFromResponse(response)
    }

    private fun persistSessionFromResponse(response: LoginResponse) {
        TokenManager.saveSession(
            userId = response.userId,
            username = response.username,
            realName = response.realName,
            roleCodes = response.roleCodes,
            permissionCodes = response.permissionCodes,
            dataScope = response.dataScope,
            primaryProjectId = response.primaryProjectId
        )
        TokenManager.saveLastLoginTime(DateTimeUtil.nowFormatted())
    }
}
