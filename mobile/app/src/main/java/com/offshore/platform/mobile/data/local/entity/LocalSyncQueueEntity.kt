package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local sync queue — the backbone of offline-first architecture.
 *
 * Every local-mutation that needs to reach the server is written here first,
 * then picked up by SyncWorker for batch push via /api/sync/push.
 *
 * Rules:
 *   - Never delete before sync succeeds.
 *   - Retry up to [maxRetryCount] times with exponential backoff.
 *   - Keep queue across app restarts.
 */
@Entity(
    tableName = "local_sync_queue",
    indices = [
        Index(value = ["localId"], unique = false),
        Index(value = ["syncStatus"], unique = false),
        Index(value = ["nextRetryTime"], unique = false),
        Index(value = ["priority"], unique = false),
        Index(value = ["workOrderId"], unique = false)
    ]
)
data class LocalSyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val queueId: Long = 0,

    // ---- entity identity ----
    val moduleType: String = "",       // WORK_ORDER, WORK_RECORD, ATTACHMENT, SIGNATURE...
    val entityType: String = "",       // same as moduleType typically
    val localId: String = "",
    val serverId: Long? = null,
    val workOrderId: Long? = null,
    val actionType: String = "CREATE", // CREATE, UPDATE, DELETE

    // ---- payload ----
    val payloadJson: String? = null,   // full JSON of the entity
    val fileId: String? = null,        // if this item is about a file
    val checksum: String? = null,

    // ---- sync metadata ----
    val syncStatus: String = "PENDING",
    val priority: Int = 100,           // lower = higher priority
    val retryCount: Int = 0,
    val maxRetryCount: Int = 5,
    val lastError: String? = null,
    val deviceId: String = "",
    val operatorId: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val nextRetryTime: Long = 0L       // epoch millis; 0 = immediate
)
