package com.mauro.deezcover.domain.model

data class SearchHistoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val type: String,
    val timestamp: Long
)
