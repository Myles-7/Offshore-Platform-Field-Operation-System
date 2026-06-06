package com.offshore.platform.mobile.data.remote.api

import com.offshore.platform.mobile.data.remote.dto.ApiResponse
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.*

/**
 * Material API — /api/mobile/work-orders/{id}/material-requirements
 * and /api/mobile/work-orders/{id}/material-usage
 */
interface MaterialApi {

    @GET("/api/mobile/work-orders/{workOrderId}/material-requirements")
    suspend fun materialRequirements(
        @Path("workOrderId") workOrderId: Long
    ): Response<ApiResponse<List<JsonObject>>>

    @POST("/api/mobile/work-orders/{workOrderId}/material-usage")
    suspend fun recordUsage(
        @Path("workOrderId") workOrderId: Long,
        @Body body: JsonObject
    ): Response<ApiResponse<JsonObject>>
}
