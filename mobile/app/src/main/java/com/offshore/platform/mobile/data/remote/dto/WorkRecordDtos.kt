package com.offshore.platform.mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ===================== Feedback =====================

/** Maps backend MobileFeedbackRequest. */
@Serializable
data class MobileFeedbackDto(
    @SerialName("feedback")
    val feedback: String,
    @SerialName("abnormalFlag")
    val abnormalFlag: Int? = null
)

// ===================== Submit Acceptance =====================

/** Maps backend MobileSubmitAcceptanceRequest. */
@Serializable
data class MobileSubmitAcceptanceDto(
    @SerialName("submitDesc")
    val submitDesc: String
)

// ===================== Work Record =====================

/**
 * Maps backend MobileWorkRecordRequest.
 *
 * Backend uses BigDecimal for temperature/humidity/lat/lng/altitude.
 * We send them as strings to avoid precision loss, or as numbers.
 */
@Serializable
data class MobileWorkRecordDto(
    val localId: String? = null,
    val version: Int? = null,
    @SerialName("recordType")
    val recordType: String = "DAILY",
    @SerialName("constructionTime")
    val constructionTime: String? = null,
    @SerialName("constructionDesc")
    val constructionDesc: String? = null,
    @SerialName("siteCondition")
    val siteCondition: String? = null,
    @SerialName("abnormalFlag")
    val abnormalFlag: Int? = null,
    @SerialName("abnormalDesc")
    val abnormalDesc: String? = null,
    val weather: String? = null,
    val temperature: String? = null,
    val humidity: String? = null,
    @SerialName("locationName")
    val locationName: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val altitude: String? = null,
    @SerialName("deviceId")
    val deviceId: String? = null,
    val remark: String? = null
)

/** Maps backend MobileWorkRecordVO returned from GET/POST/PUT. */
@Serializable
data class MobileWorkRecordResponseDto(
    val id: Long = 0,
    val serverId: Long? = null,
    val localId: String? = null,
    val workOrderId: Long = 0,
    val projectId: Long? = null,
    val recordNo: String? = null,
    val recordType: String? = null,
    val constructionTime: String? = null,
    val constructionUserId: Long? = null,
    val constructionUserName: String? = null,
    val constructionDesc: String? = null,
    val siteCondition: String? = null,
    val abnormalFlag: Int? = null,
    val abnormalDesc: String? = null,
    val weather: String? = null,
    val temperature: String? = null,
    val humidity: String? = null,
    val locationName: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val altitude: String? = null,
    val attachmentCount: Int? = null,
    val aiResultCount: Int? = null,
    val recordStatus: String? = null,
    val submittedAt: String? = null,
    val confirmedBy: Long? = null,
    val confirmedAt: String? = null,
    val version: Int? = null,
    val updatedAt: String? = null,
    val syncStatus: String? = null,
    val conflictFlag: Int? = null
)
