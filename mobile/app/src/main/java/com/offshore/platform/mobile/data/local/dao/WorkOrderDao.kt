package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalWorkOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LocalWorkOrderEntity>)

    @Update
    suspend fun update(entity: LocalWorkOrderEntity)

    @Query("UPDATE local_work_order SET syncStatus = :status, updatedAt = :updatedAt WHERE serverId = :serverId")
    suspend fun updateSyncStatus(serverId: Long, status: String, updatedAt: String)

    @Query("UPDATE local_work_order SET version = :version, serverId = :serverId, syncStatus = 'SYNCED' WHERE localId = :localId")
    suspend fun markSynced(localId: String, serverId: Long, version: Int)

    @Query("SELECT * FROM local_work_order WHERE serverId = :serverId AND deletedFlag = 0")
    suspend fun getByServerId(serverId: Long): LocalWorkOrderEntity?

    @Query("SELECT * FROM local_work_order WHERE localId = :localId AND deletedFlag = 0")
    suspend fun getByLocalId(localId: String): LocalWorkOrderEntity?

    @Query("SELECT * FROM local_work_order WHERE deletedFlag = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<LocalWorkOrderEntity>>

    @Query("SELECT * FROM local_work_order WHERE deletedFlag = 0 AND projectId = :projectId ORDER BY updatedAt DESC")
    suspend fun getByProjectId(projectId: Long): List<LocalWorkOrderEntity>

    @Query("SELECT * FROM local_work_order WHERE syncStatus IN (:statuses) AND deletedFlag = 0")
    suspend fun getBySyncStatuses(statuses: List<String>): List<LocalWorkOrderEntity>

    @Query("DELETE FROM local_work_order WHERE localId = :localId")
    suspend fun deleteByLocalId(localId: String)

    @Query("SELECT COUNT(*) FROM local_work_order WHERE syncStatus = 'CONFLICT'")
    suspend fun conflictCount(): Int
}
