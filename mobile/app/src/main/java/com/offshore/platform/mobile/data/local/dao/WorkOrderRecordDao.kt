package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkOrderRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalWorkOrderRecordEntity): Long

    @Update
    suspend fun update(entity: LocalWorkOrderRecordEntity)

    @Query("SELECT * FROM local_work_order_record WHERE serverId = :serverId AND deletedFlag = 0")
    suspend fun getByServerId(serverId: Long): LocalWorkOrderRecordEntity?

    @Query("SELECT * FROM local_work_order_record WHERE localId = :localId AND deletedFlag = 0")
    suspend fun getByLocalId(localId: String): LocalWorkOrderRecordEntity?

    @Query("SELECT * FROM local_work_order_record WHERE id = :id AND deletedFlag = 0")
    suspend fun getById(id: Long): LocalWorkOrderRecordEntity?

    @Query("SELECT * FROM local_work_order_record WHERE workOrderId = :workOrderId AND deletedFlag = 0 ORDER BY constructionTime DESC")
    fun observeByWorkOrderId(workOrderId: Long): Flow<List<LocalWorkOrderRecordEntity>>

    @Query("SELECT * FROM local_work_order_record WHERE syncStatus IN (:statuses) AND deletedFlag = 0")
    suspend fun getBySyncStatuses(statuses: List<String>): List<LocalWorkOrderRecordEntity>

    @Query("UPDATE local_work_order_record SET version = :version, serverId = :serverId, syncStatus = 'SYNCED' WHERE localId = :localId")
    suspend fun markSynced(localId: String, serverId: Long, version: Int)

    @Query("UPDATE local_work_order_record SET syncStatus = :status WHERE localId = :localId")
    suspend fun updateSyncStatus(localId: String, status: String)

    @Query("SELECT * FROM local_work_order_record WHERE workOrderId = :workOrderId AND deletedFlag = 0 ORDER BY constructionTime DESC")
    suspend fun getByWorkOrderIdSync(workOrderId: Long): List<LocalWorkOrderRecordEntity>
}
