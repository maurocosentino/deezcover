package com.mauro.offlinefirst.domain.model

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String,
    val nbFan: Long = 0L,
    val fanCount: Long? = null,
    val albumCount: Int? = null
)
