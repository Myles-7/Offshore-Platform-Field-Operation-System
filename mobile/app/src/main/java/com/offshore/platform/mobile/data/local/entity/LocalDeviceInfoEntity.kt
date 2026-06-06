package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local device info — persisted registration status.
 */
@Entity(
    tableName = "local_device_info",
    indices = [
        Index(value = ["deviceId"], unique = true)
    ]
)
data class LocalDeviceInfoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val localId: String,
    val serverId: Long? = null,
    val version: Int = 0,
    val updatedAt: String? = null,
    val syncStatus: String = "PENDING",
    val deviceId: String = "",
    val operatorId: Long? = null,
    val deletedFlag: Int = 0,
    val conflictFlag: Int = 0,

    // device fields
    val deviceName: String? = null,
    val platform: String = "ANDROID",
    val appVersion: String? = null,
    val registeredAt: String? = null,
    val lastHeartbeatAt: String? = null,
    val deviceStatus: String = "ACTIVE"
)
