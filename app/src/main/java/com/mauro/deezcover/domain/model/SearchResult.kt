package com.mauro.deezcover.domain.model

data class SearchResult(
    val tracks: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList()
)
