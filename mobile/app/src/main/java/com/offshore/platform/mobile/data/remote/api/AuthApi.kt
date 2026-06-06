package com.offshore.platform.mobile.data.remote.api

import com.offshore.platform.mobile.data.remote.dto.ApiResponse
import com.offshore.platform.mobile.data.remote.dto.LoginResponse
import com.offshore.platform.mobile.data.remote.dto.MobileLoginRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Auth API — login / logout / current user.
 *
 * POST /api/auth/login   (anonymous)
 * POST /api/auth/logout
 * GET  /api/auth/current
 */
interface AuthApi {

    @POST("/api/auth/login")
    suspend fun login(@Body request: MobileLoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("/api/auth/current")
    suspend fun currentUser(): Response<ApiResponse<LoginResponse>>
}
