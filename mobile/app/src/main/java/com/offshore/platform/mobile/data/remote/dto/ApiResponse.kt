package com.offshore.platform.mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Unified API response wrapper — matches backend ApiResponse<T>.
 *
 * Backend: common/response/ApiResponse.java
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": { ... },
 *   "timestamp": "2026-06-05 16:00:00",
 *   "traceId": "trace-id"
 * }
 */
@Serializable
data class ApiResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null,
    val timestamp: String = "",
    @SerialName("traceId")
    val traceId: String? = null
)

/**
 * Backend paginated result.
 *
 * Uses [PageRequest] and [PageResponse] to pass pagination parameters.
 */
@Serializable
data class PageRequest(
    val pageNum: Int = 1,
    val pageSize: Int = 20,
    val keyword: String? = null,
    val sortField: String? = null,
    val sortOrder: String? = null
)

@Serializable
data class PageResponse<T>(
    val records: List<T> = emptyList(),
    val total: Long = 0,
    val pageNum: Int = 1,
    val pageSize: Int = 20
)
