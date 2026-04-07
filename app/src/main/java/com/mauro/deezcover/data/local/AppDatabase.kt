package com.mauro.deezcover.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mauro.deezcover.data.local.dao.AlbumDao
import com.mauro.deezcover.data.local.dao.ArtistDao
import com.mauro.deezcover.data.local.dao.NewReleaseDao
import com.mauro.deezcover.data.local.dao.SearchHistoryDao
import com.mauro.deezcover.data.local.dao.SongDao
import com.mauro.deezcover.data.local.entity.AlbumEntity
import com.mauro.deezcover.data.local.entity.ArtistEntity
import com.mauro.deezcover.data.local.entity.NewReleaseEntity
import com.mauro.deezcover.data.local.entity.SearchHistoryEntity
import com.mauro.deezcover.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        NewReleaseEntity::class,
        SearchHistoryEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun newReleaseDao(): NewReleaseDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        const val DATABASE_NAME = "offline_first_db_v14"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE songs ADD COLUMN isFromChart INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE albums ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS artists (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        fanCount INTEGER,
                        albumCount INTEGER,
                        lastUpdated INTEGER NOT NULL DEFAULT 0,
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS new_releases (
                        albumId INTEGER NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        coverUrl TEXT NOT NULL,
                        artistName TEXT NOT NULL,
                        releaseDate TEXT NOT NULL,
                        pageIndex INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS artists_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        nbFan INTEGER,
                        albumCount INTEGER,
                        lastUpdated INTEGER NOT NULL DEFAULT 0,
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO artists_new (id, name, imageUrl, nbFan, albumCount, lastUpdated, sortOrder)
                    SELECT id, name, imageUrl, COALESCE(fanCount, 0), albumCount, lastUpdated, sortOrder
                    FROM artists
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE artists")
                database.execSQL("ALTER TABLE artists_new RENAME TO artists")
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS search_history (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        subtitle TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        type TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
