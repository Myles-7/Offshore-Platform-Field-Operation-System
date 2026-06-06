package com.offshore.platform.mobile.data.remote.api

import com.offshore.platform.mobile.data.remote.dto.ApiResponse
import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * File API —— /api/files/
 */
interface FileApi {

    @Multipart
    @POST("/api/files/upload")
    suspend fun upload(
        @Part file: MultipartBody.Part,
        @Part("fileType") fileType: RequestBody,
        @Part("workOrderId") workOrderId: RequestBody? = null,
        @Part("recordId") recordId: RequestBody? = null,
        @Part("localId") localId: RequestBody? = null,
        @Part("deviceId") deviceId: RequestBody? = null
    ): Response<ApiResponse<JsonObject>>

    @Multipart
    @POST("/api/files/batch-upload")
    suspend fun batchUpload(
        @Part files: List<MultipartBody.Part>
    ): Response<ApiResponse<List<JsonObject>>>

    @POST("/api/files/chunk/init")
    suspend fun chunkInit(@Body body: kotlinx.serialization.json.JsonObject): Response<ApiResponse<kotlinx.serialization.json.JsonObject>>

    @Multipart
    @POST("/api/files/chunk/upload")
    suspend fun chunkUpload(@Part body: okhttp3.MultipartBody): Response<ApiResponse<kotlinx.serialization.json.JsonObject>>

    @POST("/api/files/chunk/merge")
    suspend fun chunkMerge(@Body body: kotlinx.serialization.json.JsonObject): Response<ApiResponse<kotlinx.serialization.json.JsonObject>>

    @GET("/api/files/{fileId}/preview")
    suspend fun preview(@Path("fileId") fileId: String): Response<okhttp3.ResponseBody>

    @GET("/api/files/{fileId}/download")
    suspend fun download(@Path("fileId") fileId: String): Response<okhttp3.ResponseBody>

    @DELETE("/api/files/{fileId}")
    suspend fun deleteFile(@Path("fileId") fileId: String): Response<ApiResponse<Unit>>
}
