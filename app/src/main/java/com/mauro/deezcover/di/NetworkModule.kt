package com.mauro.deezcover.di

import com.mauro.deezcover.data.remote.RemoteDataSource
import com.mauro.deezcover.data.remote.api.MusicApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val HTTPS_BASE_URL = "https://api.deezer.com/"
    private const val HTTP_BASE_URL = "http://api.deezer.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .callTimeout(8, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .protocols(listOf(Protocol.HTTP_1_1))
            .connectionSpecs(
                listOf(
                    ConnectionSpec.COMPATIBLE_TLS,
                    ConnectionSpec.MODERN_TLS
                )
            )
            .build()
    }

    @Provides
    @Singleton
    @Named("https")
    fun provideHttpsRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(HTTPS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("http")
    fun provideHttpRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(HTTP_BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .callTimeout(8, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .protocols(listOf(Protocol.HTTP_1_1))
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("https")
    fun provideHttpsMusicApiService(@Named("https") retrofit: Retrofit): MusicApiService {
        return retrofit.create(MusicApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("http")
    fun provideHttpMusicApiService(@Named("http") retrofit: Retrofit): MusicApiService {
        return retrofit.create(MusicApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(
        @Named("https") apiService: MusicApiService,
        @Named("http") cleartextApiService: MusicApiService
    ): RemoteDataSource {
        return RemoteDataSource(apiService, cleartextApiService)
    }
}
