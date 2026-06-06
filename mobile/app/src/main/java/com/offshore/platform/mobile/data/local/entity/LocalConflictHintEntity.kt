package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local conflict hint — mirrors sync_conflict from server.
 * Mobile only reads this; conflict resolution happens on PC backend.
 */
@Entity(
    tableName = "local_conflict_hint",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["workOrderId"], unique = false),
        Index(value = ["conflictType"], unique = false)
    ]
)
data class LocalConflictHintEntity(
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

    // conflict fields
    val workOrderId: Long? = null,
    val conflictNo: String? = null,
    val conflictType: String? = null,
    val entityType: String? = null,
    val serverVersion: Int = 0,
    val clientVersion: Int = 0,
    val resolveStatus: String = "PENDING",
    val clientPayload: String? = null,
    val serverPayload: String? = null,
    val message: String? = null,
    val createdAt: String? = null
)
