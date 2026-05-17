package com.vaultmind.core.network

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

interface VaultMindPlaceholderApi

@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton fun moshi(): Moshi = Moshi.Builder().build()
    @Provides @Singleton fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()
    @Provides @Singleton fun retrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl("https://localhost/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
}
