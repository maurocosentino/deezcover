package com.mauro.offlinefirst.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val albumTitle: String,
    val albumArt: String,
    val durationMs: Long,
    val isAvailableOffline: Boolean,
    val lastUpdated: Long = System.currentTimeMillis(),
    val deezerUrl: String,
    val previewUrl: String
)

