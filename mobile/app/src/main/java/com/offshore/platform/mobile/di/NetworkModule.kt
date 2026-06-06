package com.offshore.platform.mobile.di

import com.offshore.platform.mobile.data.remote.ApiClient
import com.offshore.platform.mobile.data.remote.ApiConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Provides the single [Retrofit] and OkHttp instances via ApiClient.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        // Ensure baseUrl is configured before creating Retrofit
        require(ApiConfig.baseUrl.isNotBlank()) { "API base URL is not configured. Check BuildConfig.API_BASE_URL." }
        return ApiClient.retrofit
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = ApiClient.okHttpClient
}
