package com.mauro.offlinefirst.di

import android.content.Context
import androidx.room.Room
import com.mauro.offlinefirst.data.local.AppDatabase
import com.mauro.offlinefirst.data.local.dao.AlbumDao
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
           .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3) // ← agregar MIGRATION_2_3
           .fallbackToDestructiveMigration()
           .build()
    }
    @Provides
    fun provideSongDao(database: AppDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    fun provideAlbumDao(database: AppDatabase): AlbumDao {
        return database.albumDao()
    }
}