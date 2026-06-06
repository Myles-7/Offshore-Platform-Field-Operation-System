package com.offshore.platform.mobile.di

import com.offshore.platform.mobile.data.remote.api.*
import com.offshore.platform.mobile.data.repository.AuthRepository
import com.offshore.platform.mobile.data.repository.SyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Provides all Retrofit service interfaces.
 * Each @Provides uses the shared [Retrofit] instance from [NetworkModule].
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideMobileWorkOrderApi(retrofit: Retrofit): MobileWorkOrderApi =
        retrofit.create(MobileWorkOrderApi::class.java)

    @Provides
    @Singleton
    fun provideSyncApi(retrofit: Retrofit): SyncApi = retrofit.create(SyncApi::class.java)

    @Provides
    @Singleton
    fun provideFileApi(retrofit: Retrofit): FileApi = retrofit.create(FileApi::class.java)

    @Provides
    @Singleton
    fun provideMaterialApi(retrofit: Retrofit): MaterialApi = retrofit.create(MaterialApi::class.java)

    @Provides
    @Singleton
    fun provideQualificationApi(retrofit: Retrofit): QualificationApi =
        retrofit.create(QualificationApi::class.java)

    @Provides
    @Singleton
    fun provideAiApi(retrofit: Retrofit): AiApi = retrofit.create(AiApi::class.java)

    @Provides
    @Singleton
    fun provideKnowledgeApi(retrofit: Retrofit): KnowledgeApi =
        retrofit.create(KnowledgeApi::class.java)
}
