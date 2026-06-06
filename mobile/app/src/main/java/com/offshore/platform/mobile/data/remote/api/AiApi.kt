package com.offshore.platform.mobile.data.remote.api

import com.offshore.platform.mobile.data.remote.dto.ApiResponse
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.*

/**
 * AI API — /api/mobile/work-orders/{id}/ai-results
 */
interface AiApi {

    @GET("/api/mobile/work-orders/{workOrderId}/ai-results")
    suspend fun aiResults(
        @Path("workOrderId") workOrderId: Long
    ): Response<ApiResponse<List<JsonObject>>>
}
