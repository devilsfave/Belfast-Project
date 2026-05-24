package com.example.medgem.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LlmModule {
    @Provides
    @Singleton
    fun provideLlmModuleProvider(): LlmModuleProvider {
        return LlmModuleProvider
    }

    @Provides
    @Singleton
    fun provideEmbeddingModuleProvider(): EmbeddingModuleProvider {
        return EmbeddingModuleProvider
    }
}
