package com.offshore.platform.mobile.data.remote.api

import com.offshore.platform.mobile.data.remote.dto.ApiResponse
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.*

/**
 * Knowledge API — knowledge-base read access.
 */
interface KnowledgeApi {

    @GET("/api/mobile/knowledge/cases")
    suspend fun listCases(): Response<ApiResponse<List<JsonObject>>>
}
