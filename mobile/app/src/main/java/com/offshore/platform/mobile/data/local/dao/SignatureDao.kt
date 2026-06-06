package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalSignatureEntity

@Dao
interface SignatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalSignatureEntity): Long

    @Query("SELECT * FROM local_signature WHERE serverId = :serverId AND deletedFlag = 0")
    suspend fun getByServerId(serverId: Long): LocalSignatureEntity?

    @Query("SELECT * FROM local_signature WHERE workOrderId = :workOrderId AND deletedFlag = 0 ORDER BY signatureTime DESC")
    suspend fun getByWorkOrderId(workOrderId: Long): List<LocalSignatureEntity>

    @Query("SELECT * FROM local_signature WHERE localId = :localId")
    suspend fun getByLocalId(localId: String): LocalSignatureEntity?

    @Query("UPDATE local_signature SET version = :version, serverId = :serverId, syncStatus = 'SYNCED' WHERE localId = :localId")
    suspend fun markSynced(localId: String, serverId: Long, version: Int)

    @Update
    suspend fun update(entity: LocalSignatureEntity)
}
