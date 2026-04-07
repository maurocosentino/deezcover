package com.mauro.deezcover.domain.model

data class NewRelease(
    val albumId: Long,
    val title: String,
    val coverUrl: String,
    val coverXlUrl: String? = null,
    val artistName: String,
    val releaseDate: String
)
