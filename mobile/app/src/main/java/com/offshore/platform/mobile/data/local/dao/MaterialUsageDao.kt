package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalMaterialUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalMaterialUsageEntity): Long

    @Query("SELECT * FROM local_material_usage WHERE workOrderId = :workOrderId ORDER BY usageTime DESC")
    fun observeByWorkOrderId(workOrderId: Long): Flow<List<LocalMaterialUsageEntity>>

    @Query("SELECT * FROM local_material_usage WHERE syncStatus IN (:statuses)")
    suspend fun getBySyncStatuses(statuses: List<String>): List<LocalMaterialUsageEntity>

    @Query("UPDATE local_material_usage SET syncStatus = 'SYNCED' WHERE localId = :localId")
    suspend fun markSynced(localId: String)

    @Query("SELECT * FROM local_material_usage WHERE workOrderId = :workOrderId AND deletedFlag = 0 ORDER BY usageTime DESC")
    suspend fun getByWorkOrderIdSync(workOrderId: Long): List<LocalMaterialUsageEntity>
}
