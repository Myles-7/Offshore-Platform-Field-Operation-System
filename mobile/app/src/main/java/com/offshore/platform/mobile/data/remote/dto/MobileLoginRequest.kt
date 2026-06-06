package com.offshore.platform.mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Extended login request including mobile-specific fields.
 */
@Serializable
data class MobileLoginRequest(
    @SerialName("loginName")
    val loginName: String,
    val password: String,
    val platform: String = "MOBILE",
    @SerialName("deviceId")
    val deviceId: String? = null,
    @SerialName("appVersion")
    val appVersion: String? = null
)
