package com.offshore.platform.mobile.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.offshore.platform.mobile.OffshoreApp

/**
 * Secure in-memory and on-disk token manager backed by EncryptedSharedPreferences.
 */
object TokenManager {

    private const val PREFS_FILE = "offshore_secure_token"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_REAL_NAME = "real_name"
    private const val KEY_ROLE_CODES = "role_codes"
    private const val KEY_PERMISSION_CODES = "perm_codes"
    private const val KEY_DATA_SCOPE = "data_scope"
    private const val KEY_PRIMARY_PROJECT_ID = "primary_project_id"
    private const val KEY_LAST_LOGIN_TIME = "last_login_time"

    private fun prefs(): SharedPreferences {
        val ctx = OffshoreApp.instance
        val masterKey = MasterKey.Builder(ctx)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            ctx,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ---- token ----

    fun saveToken(token: String) {
        prefs().edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs().getString(KEY_TOKEN, null)

    // ---- session ----

    fun saveSession(
        userId: Long,
        username: String,
        realName: String?,
        roleCodes: List<String>,
        permissionCodes: List<String>,
        dataScope: String?,
        primaryProjectId: Long?
    ) {
        prefs().edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_REAL_NAME, realName)
            .putString(KEY_ROLE_CODES, roleCodes.joinToString(","))
            .putString(KEY_PERMISSION_CODES, permissionCodes.joinToString(","))
            .putString(KEY_DATA_SCOPE, dataScope)
            .putLong(KEY_PRIMARY_PROJECT_ID, primaryProjectId ?: 0L)
            .apply()
    }

    fun saveLastLoginTime(time: String) {
        prefs().edit().putString(KEY_LAST_LOGIN_TIME, time).apply()
    }

    fun getUserId(): Long = prefs().getLong(KEY_USER_ID, 0L)

    fun getUsername(): String? = prefs().getString(KEY_USERNAME, null)

    fun getRealName(): String? = prefs().getString(KEY_REAL_NAME, null)

    fun getRoleCodes(): List<String> =
        prefs().getString(KEY_ROLE_CODES, "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    fun getPermissionCodes(): List<String> =
        prefs().getString(KEY_PERMISSION_CODES, "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    fun getDataScope(): String? = prefs().getString(KEY_DATA_SCOPE, null)

    fun getPrimaryProjectId(): Long = prefs().getLong(KEY_PRIMARY_PROJECT_ID, 0L)

    fun getLastLoginTime(): String? = prefs().getString(KEY_LAST_LOGIN_TIME, null)

    /** Re-hydrate disk token for use — must be called once on app start. */
    fun restoreToken() {
        prefs().getString(KEY_TOKEN, null)
    }

    /** Clear session (logout) — keeps deviceId. */
    fun clearSession() {
        prefs().edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_REAL_NAME)
            .remove(KEY_ROLE_CODES)
            .remove(KEY_PERMISSION_CODES)
            .remove(KEY_DATA_SCOPE)
            .remove(KEY_PRIMARY_PROJECT_ID)
            .remove(KEY_LAST_LOGIN_TIME)
            .apply()
    }

    /** Wipe everything (factory reset scenario). */
    fun clearAll() {
        prefs().edit().clear().apply()
    }
}