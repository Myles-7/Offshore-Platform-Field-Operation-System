package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Sync checkpoint — tracks the last successful pull time per module type.
 *
 * Used by sync/pull to request only incremental changes.
 */
@Entity(
    tableName = "local_sync_checkpoint",
    indices = [
        Index(value = ["moduleType"], unique = true)
    ]
)
data class LocalSyncCheckpointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moduleType: String = "",
    val lastSyncTime: String? = null,
    val lastServerCursor: String? = null,
    val lastSuccessTime: String? = null,
    val updatedAt: String? = null
)
