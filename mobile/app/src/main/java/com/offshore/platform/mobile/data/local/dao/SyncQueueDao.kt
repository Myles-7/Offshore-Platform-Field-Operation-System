package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalSyncQueueEntity
import kotlinx.coroutines.flow.Flow

/**
 * The most important DAO — drives offline-first behaviour.
 */
@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalSyncQueueEntity): Long

    @Update
    suspend fun update(entity: LocalSyncQueueEntity)

    /** Queue an item for sync. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(entity: LocalSyncQueueEntity)

    /** Count pending items. */
    @Query("SELECT COUNT(*) FROM local_sync_queue WHERE syncStatus = 'PENDING'")
    suspend fun pendingCount(): Int

    /** Get pending items ordered by priority, then creation time. */
    @Query("SELECT * FROM local_sync_queue WHERE syncStatus = 'PENDING' AND nextRetryTime <= :nowMillis ORDER BY priority ASC, queueId ASC LIMIT :limit")
    suspend fun getPendingBatch(nowMillis: Long, limit: Int = 50): List<LocalSyncQueueEntity>

    /** Get items by localId (idempotency check). */
    @Query("SELECT * FROM local_sync_queue WHERE localId = :localId")
    suspend fun getByLocalId(localId: String): List<LocalSyncQueueEntity>

    /** Update item status and retry info after push attempt. */
    @Query("UPDATE local_sync_queue SET syncStatus = :status, retryCount = retryCount + 1, lastError = :error, nextRetryTime = :nextRetryMillis WHERE queueId = :queueId")
    suspend fun updateAfterAttempt(queueId: Long, status: String, error: String?, nextRetryMillis: Long)

    /** Mark as successfully synced. */
    @Query("UPDATE local_sync_queue SET syncStatus = 'SYNCED' WHERE queueId = :queueId")
    suspend fun markSynced(queueId: Long)

    /** Observe pending count as Flow. */
    @Query("SELECT COUNT(*) FROM local_sync_queue WHERE syncStatus = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    /** Delete synced items older than cutoff. */
    @Query("DELETE FROM local_sync_queue WHERE syncStatus = 'SYNCED' AND updatedAt < :cutoff")
    suspend fun pruneSynced(cutoff: String)

    /** Get all items for a work order. */
    @Query("SELECT * FROM local_sync_queue WHERE workOrderId = :workOrderId")
    suspend fun getByWorkOrderId(workOrderId: Long): List<LocalSyncQueueEntity>

    /** Reset stuck items for retry. */
    @Query("UPDATE local_sync_queue SET syncStatus = 'PENDING', retryCount = 0, nextRetryTime = 0 WHERE syncStatus = 'FAILED' AND retryCount < maxRetryCount")
    suspend fun resetFailedForRetry()
}
