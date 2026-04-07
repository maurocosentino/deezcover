package com.mauro.deezcover.di

import com.mauro.deezcover.data.repository.NewReleaseRepositoryImpl
import com.mauro.deezcover.data.repository.SearchHistoryRepositoryImpl
import com.mauro.deezcover.data.repository.SongRepositoryImpl
import com.mauro.deezcover.domain.repository.NewReleaseRepository
import com.mauro.deezcover.domain.repository.SearchHistoryRepository
import com.mauro.deezcover.domain.repository.SongRepository
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
