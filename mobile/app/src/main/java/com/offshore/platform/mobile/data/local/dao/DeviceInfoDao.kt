package com.offshore.platform.mobile.data.local.dao

import androidx.room.*
import com.offshore.platform.mobile.data.local.entity.LocalDeviceInfoEntity

@Dao
interface DeviceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalDeviceInfoEntity): Long

    @Query("SELECT * FROM local_device_info WHERE deviceId = :deviceId")
    suspend fun getByDeviceId(deviceId: String): LocalDeviceInfoEntity?

    @Query("UPDATE local_device_info SET lastHeartbeatAt = :heartbeatAt WHERE deviceId = :deviceId")
    suspend fun updateHeartbeat(deviceId: String, heartbeatAt: String)

    @Query("UPDATE local_device_info SET deviceStatus = :status WHERE deviceId = :deviceId")
    suspend fun updateStatus(deviceId: String, status: String)
}
