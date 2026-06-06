package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalWorkOrderAttachmentEntity): Long

    @Update
    suspend fun update(entity: LocalWorkOrderAttachmentEntity)

    @Query("SELECT * FROM local_attachment WHERE serverId = :serverId AND deletedFlag = 0")
    suspend fun getByServerId(serverId: Long): LocalWorkOrderAttachmentEntity?

    @Query("SELECT * FROM local_attachment WHERE localId = :localId")
    suspend fun getByLocalId(localId: String): LocalWorkOrderAttachmentEntity?

    @Query("SELECT * FROM local_attachment WHERE workOrderId = :workOrderId AND deletedFlag = 0 ORDER BY captureTime DESC")
    fun observeByWorkOrderId(workOrderId: Long): Flow<List<LocalWorkOrderAttachmentEntity>>

    @Query("SELECT * FROM local_attachment WHERE uploadStatus IN (:statuses)")
    suspend fun getByUploadStatuses(statuses: List<String>): List<LocalWorkOrderAttachmentEntity>

    @Query("UPDATE local_attachment SET fileId = :fileId, uploadStatus = 'UPLOADED' WHERE localId = :localId")
    suspend fun markFileUploaded(localId: String, fileId: String)

    @Query("UPDATE local_attachment SET syncStatus = 'SYNCED' WHERE localId = :localId")
    suspend fun markSynced(localId: String)

    @Query("SELECT * FROM local_attachment WHERE workOrderId = :workOrderId AND deletedFlag = 0 ORDER BY captureTime DESC")
    suspend fun getByWorkOrderIdSync(workOrderId: Long): List<LocalWorkOrderAttachmentEntity>
}
