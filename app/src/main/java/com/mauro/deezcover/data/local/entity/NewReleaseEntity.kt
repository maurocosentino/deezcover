package com.mauro.deezcover.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "new_releases")
data class NewReleaseEntity(
    @PrimaryKey val albumId: Long,
    val title: String,
    val coverUrl: String,
    val artistName: String,
    val releaseDate: String,
    val pageIndex: Int,
    val sortOrder: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)
