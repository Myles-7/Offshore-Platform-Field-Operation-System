package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalSyncCheckpointEntity

@Dao
interface SyncCheckpointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LocalSyncCheckpointEntity)

    @Query("SELECT * FROM local_sync_checkpoint WHERE moduleType = :moduleType")
    suspend fun getByModuleType(moduleType: String): LocalSyncCheckpointEntity?

    @Query("SELECT * FROM local_sync_checkpoint")
    suspend fun getAll(): List<LocalSyncCheckpointEntity>
}
