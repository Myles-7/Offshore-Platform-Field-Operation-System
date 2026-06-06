package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalKnowledgeCaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeCaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LocalKnowledgeCaseEntity>)

    @Query("SELECT * FROM local_knowledge_case WHERE deletedFlag = 0 ORDER BY title ASC")
    suspend fun getAllList(): List<LocalKnowledgeCaseEntity>

    @Query("SELECT * FROM local_knowledge_case WHERE deletedFlag = 0 ORDER BY title ASC")
    fun observeAll(): Flow<List<LocalKnowledgeCaseEntity>>

    @Query("SELECT * FROM local_knowledge_case WHERE keywords LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<LocalKnowledgeCaseEntity>

    @Query("DELETE FROM local_knowledge_case")
    suspend fun deleteAll()
}
