package com.mauro.offlinefirst.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mauro.offlinefirst.data.local.dao.AlbumDao
import com.mauro.offlinefirst.data.local.dao.SongDao
import com.mauro.offlinefirst.data.local.entity.AlbumEntity
import com.mauro.offlinefirst.data.local.entity.SongEntity

@Database(entities = [SongEntity::class, AlbumEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao

    companion object {
        const val DATABASE_NAME = "offline_first_db_v7"
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE songs ADD COLUMN isFromChart INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}