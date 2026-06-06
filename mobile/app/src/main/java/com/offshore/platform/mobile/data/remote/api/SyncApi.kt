package com.offshore.platform.mobile.data.remote.api

import com.offshore.platform.mobile.data.remote.dto.*
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.*

/**
 * Sync API -- /api/sync/
 *
 * Backend: SyncController
 *
 * Key deviations from earlier docs:
 *   - SyncPullRequest uses {deviceId, cursor, limit}, NOT lastSyncTime/entityTypes.
 *   - SyncPullVO returns {cursor, items: List<Object>} where items are heterogeneous.
 *   - SyncItemResultVO uses syncStatus field, not status.
 *   - Heartbeat reuses DeviceRegisterRequest per SyncController.
 */
interface SyncApi {

    @POST("/api/sync/device/register")
    suspend fun registerDevice(@Body request: DeviceRegisterRequest): Response<ApiResponse<JsonObject>>

    @POST("/api/sync/device/heartbeat")
    suspend fun heartbeat(@Body request: DeviceRegisterRequest): Response<ApiResponse<JsonObject>>

    @POST("/api/sync/pull")
    suspend fun pull(@Body request: SyncPullRequest): Response<ApiResponse<SyncPullResponse>>

    @POST("/api/sync/push")
    suspend fun push(@Body request: SyncPushRequest): Response<ApiResponse<SyncPushResponse>>

    @POST("/api/sync/ack")
    suspend fun ack(@Body request: SyncAckRequest): Response<ApiResponse<Unit>>
}
