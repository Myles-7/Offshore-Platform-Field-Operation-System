package com.offshore.platform.mobile.data.remote

import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Unified API client — single source for OkHttp and Retrofit instances.
 *
 * Interceptors (order matters):
 *  1. LoggingInterceptor      — request/response logging
 *  2. AuthInterceptor          — injects Bearer token
 *  3. DeviceIdInterceptor      — injects X-Device-Id header
 */
object ApiClient {

    /** Shared JSON configuration. */
    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        isLenient = true
    }

    /** Standard logging. */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfigWrapper.isDebug) {
            HttpLoggingInterceptor.Level.BODY  // Full request/response logging
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    /** Injects Bearer token if available. */
    private val authInterceptor = Interceptor { chain ->
        val token = TokenManager.getToken()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        chain.proceed(request)
    }

    /** Injects X-Device-Id header. */
    private val deviceIdInterceptor = Interceptor { chain ->
        val deviceId = DeviceManager.getDeviceId()
        val request = if (deviceId.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("X-Device-Id", deviceId)
                .build()
        }
        chain.proceed(request)
    }

    // ---------- OkHttp ----------

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(deviceIdInterceptor)
            .build()
    }

    // ---------- Retrofit ----------

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /** Type-safe service creator. */
    inline fun <reified T> create(): T = retrofit.create(T::class.java)
}
