package com.offshore.platform.mobile.data.remote.dto

import com.offshore.platform.mobile.domain.model.UserSession

/**
 * Map [LoginResponse] to [UserSession].
 */
fun LoginResponse.toUserSession(isOffline: Boolean = false): UserSession = UserSession(
    userId = userId,
    username = username,
    realName = realName,
    roleCodes = roleCodes,
    permissionCodes = permissionCodes,
    dataScope = dataScope,
    primaryProjectId = primaryProjectId,
    lastLoginTime = null,
    isOfflineMode = isOffline
)
