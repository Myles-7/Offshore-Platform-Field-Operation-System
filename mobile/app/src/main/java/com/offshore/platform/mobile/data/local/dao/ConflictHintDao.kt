package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalConflictHintEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConflictHintDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LocalConflictHintEntity>)

    @Query("SELECT * FROM local_conflict_hint WHERE workOrderId = :workOrderId ORDER BY createdAt DESC")
    suspend fun getByWorkOrderId(workOrderId: Long): List<LocalConflictHintEntity>

    @Query("SELECT COUNT(*) FROM local_conflict_hint WHERE resolveStatus = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("DELETE FROM local_conflict_hint WHERE resolveStatus != 'PENDING'")
    suspend fun pruneResolved()
}
