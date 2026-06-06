package com.offshore.platform.mobile.di

import com.offshore.platform.mobile.data.repository.AiInferenceEngine
import com.offshore.platform.mobile.data.repository.MockAiInferenceEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiInferenceEngine(): AiInferenceEngine = MockAiInferenceEngine()
}
