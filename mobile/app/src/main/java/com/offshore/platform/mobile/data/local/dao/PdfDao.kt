package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalPdfEntity

@Dao
interface PdfDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalPdfEntity): Long

    @Query("SELECT * FROM local_pdf WHERE workOrderId = :workOrderId AND deletedFlag = 0 ORDER BY generatedAt DESC")
    suspend fun getByWorkOrderId(workOrderId: Long): List<LocalPdfEntity>

    @Query("SELECT * FROM local_pdf WHERE localId = :localId")
    suspend fun getByLocalId(localId: String): LocalPdfEntity?

    @Query("UPDATE local_pdf SET syncStatus = 'SYNCED' WHERE localId = :localId")
    suspend fun markSynced(localId: String)
}
