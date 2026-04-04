package com.mauro.offlinefirst.di

import com.mauro.offlinefirst.data.repository.NewReleaseRepositoryImpl
import com.mauro.offlinefirst.data.repository.SearchHistoryRepositoryImpl
import com.mauro.offlinefirst.data.repository.SongRepositoryImpl
import com.mauro.offlinefirst.domain.repository.NewReleaseRepository
import com.mauro.offlinefirst.domain.repository.SearchHistoryRepository
import com.mauro.offlinefirst.domain.repository.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(
        impl: SongRepositoryImpl
    ): SongRepository

    @Binds
    @Singleton
    abstract fun bindNewReleaseRepository(
        impl: NewReleaseRepositoryImpl
    ): NewReleaseRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(
        impl: SearchHistoryRepositoryImpl
    ): SearchHistoryRepository
}
