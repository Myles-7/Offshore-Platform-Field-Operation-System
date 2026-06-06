package com.offshore.platform.mobile.domain.model

/**
 * In-memory session state — survives activity recreation but not process death.
 * Persisted fields live in [TokenManager].
 */
data class UserSession(
    val userId: Long,
    val username: String,
    val realName: String?,
    val roleCodes: List<String>,
    val permissionCodes: List<String>,
    val dataScope: String?,
    val primaryProjectId: Long?,
    val lastLoginTime: String?,
    val isOfflineMode: Boolean = false
) {
    val isLoggedIn: Boolean get() = userId > 0 && userId != 0L

    companion object {
        val EMPTY = UserSession(
            userId = 0,
            username = "",
            realName = null,
            roleCodes = emptyList(),
            permissionCodes = emptyList(),
            dataScope = null,
            primaryProjectId = null,
            lastLoginTime = null
        )
    }
}
