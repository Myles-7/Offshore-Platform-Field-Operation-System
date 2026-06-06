package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalAiResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LocalAiResultEntity>)

    @Query("SELECT * FROM local_ai_result WHERE workOrderId = :workOrderId ORDER BY createdAt DESC")
    fun observeByWorkOrderId(workOrderId: Long): Flow<List<LocalAiResultEntity>>

    @Query("DELETE FROM local_ai_result WHERE workOrderId = :workOrderId")
    suspend fun deleteByWorkOrderId(workOrderId: Long)

    @Query("SELECT * FROM local_ai_result WHERE workOrderId = :workOrderId ORDER BY createdAt DESC")
    suspend fun getByWorkOrderIdSync(workOrderId: Long): List<LocalAiResultEntity>
}
