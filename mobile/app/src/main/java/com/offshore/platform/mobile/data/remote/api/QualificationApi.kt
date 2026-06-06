package com.offshore.platform.mobile.data.remote.api

import com.offshore.platform.mobile.data.remote.dto.ApiResponse
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.*

/**
 * Qualification API — /api/mobile/my/qualification-status
 * and work-order qualification check.
 */
interface QualificationApi {

    @GET("/api/mobile/my/qualification-status")
    suspend fun myQualificationStatus(): Response<ApiResponse<List<JsonObject>>>
}
