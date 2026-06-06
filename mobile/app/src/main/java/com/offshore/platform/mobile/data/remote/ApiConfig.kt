package com.offshore.platform.mobile.data.remote

import com.offshore.platform.mobile.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Centralised API configuration — all network settings in one place.
 * Base URL comes from BuildConfig per build type.
 */
object ApiConfig {

    /** Base URL for all API requests (trailing slash handled by Retrofit builder). */
    val baseUrl: String
        get() = BuildConfig.API_BASE_URL

    /** Connection timeout in seconds. */
    const val CONNECT_TIMEOUT_SECONDS = 30L

    /** Read timeout in seconds. */
    const val READ_TIMEOUT_SECONDS = 30L

    /** Write timeout in seconds (upload-friendly: 120s). */
    const val WRITE_TIMEOUT_SECONDS = 120L

    /** Max retry count for failed uploads. */
    const val MAX_RETRY_COUNT = 3
}
