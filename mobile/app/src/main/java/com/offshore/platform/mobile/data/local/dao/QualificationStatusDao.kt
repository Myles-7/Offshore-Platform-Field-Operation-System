package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalQualificationStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QualificationStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LocalQualificationStatusEntity>)

    @Query("SELECT * FROM local_qualification_status WHERE userId = :userId AND deletedFlag = 0")
    suspend fun getByUserId(userId: Long): List<LocalQualificationStatusEntity>

    @Query("SELECT * FROM local_qualification_status WHERE deletedFlag = 0 ORDER BY validStatus ASC")
    fun observeAll(): Flow<List<LocalQualificationStatusEntity>>

    @Query("DELETE FROM local_qualification_status")
    suspend fun deleteAll()
}
