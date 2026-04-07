package com.mauro.deezcover.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val nbFan: Long? = null,
    val albumCount: Int? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
