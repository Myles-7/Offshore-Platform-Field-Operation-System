package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local sync log — one entry per sync batch attempt.
 */
@Entity(
    tableName = "local_sync_log",
    indices = [
        Index(value = ["batchId"], unique = true),
        Index(value = ["syncDirection"], unique = false),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalSyncLogEntity(
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

    // log fields
    val batchId: String = "",
    val syncTaskId: String? = null,
    val syncDirection: String = "PUSH",
    val syncType: String = "INCREMENTAL",
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val conflictCount: Int = 0,
    val totalItems: Int = 0,
    val detailJson: String? = null,
    val createdAt: String? = null
)
