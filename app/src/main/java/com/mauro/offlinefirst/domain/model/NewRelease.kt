package com.mauro.offlinefirst.domain.model

data class NewRelease(
    val albumId: Long,
    val title: String,
    val coverUrl: String,
    val artistName: String,
    val releaseDate: String
)
