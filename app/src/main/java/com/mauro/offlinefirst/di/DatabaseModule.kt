package com.mauro.offlinefirst.di

import android.content.Context
import androidx.room.Room
import com.mauro.offlinefirst.data.local.AppDatabase
import com.mauro.offlinefirst.data.local.dao.AlbumDao
import com.mauro.offlinefirst.data.local.dao.ArtistDao
import com.mauro.offlinefirst.data.local.dao.NewReleaseDao
import com.mauro.offlinefirst.data.local.dao.SearchHistoryDao
import com.mauro.offlinefirst.data.local.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
       return Room.databaseBuilder(
           context,
           AppDatabase::class.java,
           AppDatabase.DATABASE_NAME
       )
           .addMigrations(
               AppDatabase.MIGRATION_1_2,
               AppDatabase.MIGRATION_2_3,
               AppDatabase.MIGRATION_3_4,
               AppDatabase.MIGRATION_4_5,
               AppDatabase.MIGRATION_5_6,
               AppDatabase.MIGRATION_6_7
           )
           .build()
    }
    @Provides
    fun provideSongDao(database: AppDatabase): SongDao {
        return database.songDao()
    }
    @Provides
    fun provideArtistDao(database: AppDatabase): ArtistDao {
        return database.artistDao()
    }
    @Provides
    fun provideAlbumDao(database: AppDatabase): AlbumDao {
        return database.albumDao()
    }

    @Provides
    fun provideNewReleaseDao(database: AppDatabase): NewReleaseDao {
        return database.newReleaseDao()
    }

    @Provides
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }
}
