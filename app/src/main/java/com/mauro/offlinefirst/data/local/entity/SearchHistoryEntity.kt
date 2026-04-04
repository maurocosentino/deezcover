package com.mauro.offlinefirst.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val type: String,
    val timestamp: Long
)
