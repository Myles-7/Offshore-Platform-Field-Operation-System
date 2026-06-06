package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalSyncLogEntity

@Dao
interface SyncLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalSyncLogEntity): Long

    @Query("SELECT * FROM local_sync_log ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 50): List<LocalSyncLogEntity>

    @Query("DELETE FROM local_sync_log WHERE createdAt < :cutoff")
    suspend fun pruneOld(cutoff: String)
}
