package com.mauro.offlinefirst.di

import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.data.remote.api.MusicApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://mock.music.api/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMusicApiService(retrofit: Retrofit): MusicApiService {
        return retrofit.create(MusicApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(apiService: MusicApiService): RemoteDataSource {
        return RemoteDataSource(apiService)
    }
}