package com.offshore.platform.mobile.data.remote.api

import com.offshore.platform.mobile.data.remote.dto.*
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.*

/**
 * Mobile work-order API -- /api/mobile/work-orders/
 *
 * Backend: MobileWorkOrderController + WorkRecordController
 */
interface MobileWorkOrderApi {

    // ---- work orders ----

    @GET("/api/mobile/work-orders")
    suspend fun listMyWorkOrders(): Response<ApiResponse<List<MobileWorkOrderDTO>>>

    @GET("/api/mobile/work-orders/{id}")
    suspend fun getWorkOrder(@Path("id") id: Long): Response<ApiResponse<MobileWorkOrderDTO>>

    @POST("/api/mobile/work-orders/{id}/accept")
    suspend fun acceptWorkOrder(@Path("id") id: Long): Response<ApiResponse<MobileWorkOrderDTO>>

    @POST("/api/mobile/work-orders/{id}/start")
    suspend fun startWorkOrder(@Path("id") id: Long): Response<ApiResponse<MobileWorkOrderDTO>>

    @POST("/api/mobile/work-orders/{id}/feedback")
    suspend fun feedback(
        @Path("id") id: Long,
        @Body body: MobileFeedbackDto
    ): Response<ApiResponse<MobileWorkOrderDTO>>

    @POST("/api/mobile/work-orders/{id}/submit-acceptance")
    suspend fun submitAcceptance(
        @Path("id") id: Long,
        @Body body: MobileSubmitAcceptanceDto
    ): Response<ApiResponse<MobileWorkOrderDTO>>

    // ---- work records ----

    @GET("/api/mobile/work-orders/{workOrderId}/records")
    suspend fun listWorkOrderRecords(
        @Path("workOrderId") workOrderId: Long
    ): Response<ApiResponse<List<MobileWorkRecordResponseDto>>>

    @POST("/api/mobile/work-orders/{workOrderId}/records")
    suspend fun createWorkRecord(
        @Path("workOrderId") workOrderId: Long,
        @Body body: MobileWorkRecordDto
    ): Response<ApiResponse<MobileWorkRecordResponseDto>>

    @PUT("/api/mobile/work-orders/{workOrderId}/records/{recordId}")
    suspend fun updateWorkRecord(
        @Path("workOrderId") workOrderId: Long,
        @Path("recordId") recordId: Long,
        @Body body: MobileWorkRecordDto
    ): Response<ApiResponse<MobileWorkRecordResponseDto>>

    @POST("/api/mobile/work-records/{recordId}/check-items")
    suspend fun submitCheckItems(
        @Path("recordId") recordId: Long,
        @Body body: JsonObject
    ): Response<ApiResponse<JsonObject>>

    // ---- materials + qualification ----

    @GET("/api/mobile/work-orders/{id}/materials")
    suspend fun materials(@Path("id") id: Long): Response<ApiResponse<List<MobileMaterialDTO>>>

    @GET("/api/mobile/work-orders/{id}/qualification-check")
    suspend fun qualificationCheck(@Path("id") id: Long): Response<ApiResponse<List<JsonObject>>>
}
