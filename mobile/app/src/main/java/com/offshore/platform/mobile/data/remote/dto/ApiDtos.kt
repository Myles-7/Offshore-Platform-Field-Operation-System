package com.offshore.platform.mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ===================== Auth =====================

@Serializable
data class LoginRequest(
    @SerialName("loginName")
    val loginName: String,
    val password: String,
    val platform: String = "MOBILE"
)

@Serializable
data class LoginResponse(
    val token: String = "",
    val userId: Long = 0,
    val username: String = "",
    @SerialName("realName")
    val realName: String? = null,
    val roleCodes: List<String> = emptyList(),
    val permissionCodes: List<String> = emptyList(),
    val dataScope: String? = null,
    val primaryProjectId: Long? = null,
    // Optional — backend may include employeeId in future
    val employeeId: Long? = null
)

// ===================== Device =====================

@Serializable
data class DeviceRegisterRequest(
    @SerialName("deviceId")
    val deviceId: String,
    @SerialName("deviceName")
    val deviceName: String? = null,
    val platform: String = "ANDROID",
    @SerialName("appVersion")
    val appVersion: String? = null
)

/** Heartbeat reuses DeviceRegisterRequest per backend controller. */
typealias DeviceHeartbeatRequest = DeviceRegisterRequest

// ===================== Sync Push =====================

@Serializable
data class SyncPushItem(
    @SerialName("moduleType")
    val moduleType: String,
    @SerialName("entityType")
    val entityType: String,
    @SerialName("actionType")
    val actionType: String,
    @SerialName("localId")
    val localId: String? = null,
    @SerialName("serverId")
    val serverId: Long? = null,
    val version: Int? = null,
    @SerialName("updatedAt")
    val updatedAt: String? = null,
    val payload: kotlinx.serialization.json.JsonObject? = null,
    @SerialName("fileId")
    val fileId: String? = null,
    val checksum: String? = null,
    // ---- sync tracking fields (aligned with backend SyncPushItem) ----
    @SerialName("deletedFlag")
    val deletedFlag: Int? = null,
    @SerialName("syncStatus")
    val itemSyncStatus: String? = null,
    @SerialName("deviceId")
    val itemDeviceId: String? = null,
    @SerialName("operatorId")
    val itemOperatorId: Long? = null,
    @SerialName("conflictFlag")
    val conflictFlag: Int? = null
)

@Serializable
data class SyncPushRequest(
    @SerialName("deviceId")
    val deviceId: String,
    @SerialName("batchId")
    val batchId: String,
    @SerialName("clientTime")
    val clientTime: String,
    @SerialName("appVersion")
    val appVersion: String? = null,
    @SerialName("operatorId")
    val operatorId: Long? = null,
    val items: List<SyncPushItem>
)

/** Maps backend SyncPushResultVO. */
@Serializable
data class SyncPushResponse(
    @SerialName("taskId")
    val taskId: Long? = null,
    @SerialName("batchId")
    val batchId: String? = null,
    @SerialName("successCount")
    val successCount: Int = 0,
    @SerialName("failedCount")
    val failedCount: Int = 0,
    @SerialName("conflictCount")
    val conflictCount: Int = 0,
    val items: List<SyncItemResult> = emptyList()
)

/** Maps backend SyncItemResultVO. Field is syncStatus on the wire. */
@Serializable
data class SyncItemResult(
    @SerialName("localId")
    val localId: String? = null,
    @SerialName("serverId")
    val serverId: Long? = null,
    val version: Int = 0,
    @SerialName("syncStatus")
    val syncStatus: String? = null,
    @SerialName("conflictId")
    val conflictId: Long? = null,
    val message: String? = null,
    @SerialName("entityType")
    val entityType: String? = null,
    @SerialName("moduleType")
    val moduleType: String? = null,
    @SerialName("actionType")
    val actionType: String? = null
)

// ===================== Sync Pull =====================

/**
 * Backend SyncPullRequest uses {deviceId, cursor, lastSyncTime, limit, entityTypes}.
 */
@Serializable
data class SyncPullRequest(
    @SerialName("deviceId")
    val deviceId: String,
    val cursor: String? = null,
    @SerialName("lastSyncTime")
    val lastSyncTime: String? = null,
    val limit: Int? = null,
    @SerialName("entityTypes")
    val entityTypes: List<String>? = null
)

/**
 * Backend SyncPullVO: {cursor, serverTime, nextCursor, hasMore, ackRequired, items: List<SyncPullItemVO>}.
 * items are now structured per entityType.
 */
@Serializable
data class SyncPullItemVO(
    @SerialName("moduleType")
    val moduleType: String? = null,
    @SerialName("entityType")
    val entityType: String? = null,
    @SerialName("serverId")
    val serverId: Long? = null,
    @SerialName("localId")
    val localId: String? = null,
    val version: Int = 0,
    @SerialName("updatedAt")
    val updatedAt: String? = null,
    @SerialName("deletedFlag")
    val deletedFlag: Int? = null,
    @SerialName("operatorId")
    val operatorId: Long? = null,
    @SerialName("deviceId")
    val deviceId: String? = null,
    val payload: kotlinx.serialization.json.JsonObject? = null
)

@Serializable
data class SyncPullResponse(
    val cursor: String? = null,
    @SerialName("serverTime")
    val serverTime: String? = null,
    @SerialName("nextCursor")
    val nextCursor: String? = null,
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("ackRequired")
    val ackRequired: Boolean = true,
    val items: List<SyncPullItemVO> = emptyList()
)

/** Convenience accessor — kept for backward compatibility with SyncDataMapper. */
data class SyncPullItem(
    val entityType: String,
    val serverId: Long,
    val version: Int = 0,
    val updatedAt: String? = null,
    val payload: kotlinx.serialization.json.JsonObject? = null
)

// ===================== Sync Ack =====================

@Serializable
data class SyncAckRequest(
    @SerialName("deviceId")
    val deviceId: String,
    @SerialName("batchId")
    val batchId: String,
    val cursor: String? = null,
    @SerialName("lastSyncCursor")
    val lastSyncCursor: String? = null
)
