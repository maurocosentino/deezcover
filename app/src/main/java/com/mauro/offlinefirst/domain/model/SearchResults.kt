package com.mauro.offlinefirst.domain.model

data class SearchResults(
    val tracks: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList()
)
