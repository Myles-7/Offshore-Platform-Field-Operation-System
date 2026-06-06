package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalAcceptanceEntity

@Dao
interface AcceptanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalAcceptanceEntity): Long

    @Query("SELECT * FROM local_acceptance WHERE serverId = :serverId AND deletedFlag = 0")
    suspend fun getByServerId(serverId: Long): LocalAcceptanceEntity?

    @Query("SELECT * FROM local_acceptance WHERE workOrderId = :workOrderId AND deletedFlag = 0")
    suspend fun getByWorkOrderId(workOrderId: Long): LocalAcceptanceEntity?

    @Query("SELECT * FROM local_acceptance WHERE localId = :localId")
    suspend fun getByLocalId(localId: String): LocalAcceptanceEntity?

    @Query("UPDATE local_acceptance SET version = :version, serverId = :serverId, syncStatus = 'SYNCED' WHERE localId = :localId")
    suspend fun markSynced(localId: String, serverId: Long, version: Int)

    @Update
    suspend fun update(entity: LocalAcceptanceEntity)
}
