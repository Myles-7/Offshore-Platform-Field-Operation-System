package com.offshore.platform.mobile.util

import com.offshore.platform.mobile.OffshoreApp
import java.util.UUID

/**
 * Generates and persists a unique device identifier.
 *
 * - Generated once on first launch.
 * - Stored in plain SharedPreferences (non-sensitive).
 * - Survives logout; only removed on app uninstall / data clear.
 */
object DeviceManager {

    private const val PREFS_FILE = "offshore_device_id"
    private const val KEY_DEVICE_ID = "persistent_device_id"

    private val prefs by lazy {
        OffshoreApp.instance.getSharedPreferences(PREFS_FILE, android.content.Context.MODE_PRIVATE)
    }

    /** Retrieve or generate the device id. */
    fun getOrCreate(): String {
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (existing != null) return existing
        val newId = "android-${UUID.randomUUID().toString().take(12)}"
        prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
        return newId
    }

    /** Re-read from disk — call once on app start. */
    fun restore() {
        // ensure the pref file is initialised
        prefs.getString(KEY_DEVICE_ID, null)
    }

    /** Returns persisted device id or null. */
    fun getDeviceId(): String? = prefs.getString(KEY_DEVICE_ID, null)
}
