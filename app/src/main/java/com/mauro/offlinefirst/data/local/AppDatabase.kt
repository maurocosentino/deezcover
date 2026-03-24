package com.mauro.offlinefirst.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mauro.offlinefirst.data.local.dao.SongDao
import com.mauro.offlinefirst.data.local.entity.SongEntity

@Database(entities =  [SongEntity::class], exportSchema = true, version = 1)
abstract class AppDatabase : RoomDatabase(){

    abstract fun songDao(): SongDao

    companion object {
        const val DATABASE_NAME = "offline_first_db"
    }
}