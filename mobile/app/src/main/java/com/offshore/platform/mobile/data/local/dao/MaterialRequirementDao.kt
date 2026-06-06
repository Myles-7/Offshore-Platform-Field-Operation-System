package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalMaterialRequirementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialRequirementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LocalMaterialRequirementEntity>)

    @Query("SELECT * FROM local_material_requirement WHERE workOrderId = :workOrderId ORDER BY materialName")
    fun observeByWorkOrderId(workOrderId: Long): Flow<List<LocalMaterialRequirementEntity>>

    @Query("DELETE FROM local_material_requirement WHERE workOrderId = :workOrderId")
    suspend fun deleteByWorkOrderId(workOrderId: Long)
}
