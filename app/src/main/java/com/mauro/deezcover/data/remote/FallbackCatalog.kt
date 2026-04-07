package com.mauro.deezcover.data.remote

import com.mauro.deezcover.data.remote.dto.AlbumDto
import com.mauro.deezcover.data.remote.dto.DeezerAlbumDetailDto
import com.mauro.deezcover.data.remote.dto.DeezerArtistDto
import com.mauro.deezcover.data.remote.dto.NestedArtistDto
import com.mauro.deezcover.data.remote.dto.SongDto
import com.mauro.deezcover.data.remote.dto.SongAlbumDto

object FallbackCatalog {
    private data class CatalogArtist(
        val id: Long,
        val name: String,
        val imageUrl: String,
        val fanCount: Long,
        val albumCount: Int
    )

    private data class CatalogAlbum(
        val id: Long,
        val title: String,
        val artistId: Long,
        val coverUrl: String,
        val releaseDate: String,
        val recordType: String = "album"
    )

    private data class CatalogTrack(
        val id: Long,
        val title: String,
        val artistId: Long,
        val albumId: Long,
        val durationSeconds: Long,
        val previewUrl: String
    )

    private val artists = listOf(
        CatalogArtist(
            id = 101,
            name = "Aurora Pulse",
            imageUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&q=80&fit=crop",
            fanCount = 820_000,
            albumCount = 3
        ),
        CatalogArtist(
            id = 102,
            name = "Neon Harbor",
            imageUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600&q=80&fit=crop",
            fanCount = 640_000,
            albumCount = 2
        ),
        CatalogArtist(
            id = 103,
            name = "Solar Echo",
            imageUrl = "https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=600&q=80&fit=crop",
            fanCount = 510_000,
            albumCount = 2
        )
    )

    private val albums = listOf(
        CatalogAlbum(
            id = 201,
            title = "Midnight Signals",
            artistId = 101,
            coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&q=80&fit=crop",
            releaseDate = "2025-03-14"
        ),
        CatalogAlbum(
            id = 202,
            title = "City After Rain",
            artistId = 102,
            coverUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600&q=80&fit=crop",
            releaseDate = "2025-08-22"
        ),
        CatalogAlbum(
            id = 203,
            title = "Daybreak Avenue",
            artistId = 103,
            coverUrl = "https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=600&q=80&fit=crop",
            releaseDate = "2024-11-07"
        )
    )

    private val tracks = listOf(
        CatalogTrack(301, "Afterglow Run", 101, 201, 191, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
        CatalogTrack(302, "Static Hearts", 101, 201, 204, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"),
        CatalogTrack(303, "Velvet Frequency", 101, 201, 188, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"),
        CatalogTrack(304, "Terminal Lights", 102, 202, 213, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"),
        CatalogTrack(305, "Night Bus Home", 102, 202, 199, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"),
        CatalogTrack(306, "Concrete Dreams", 102, 202, 221, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"),
        CatalogTrack(307, "Sunline", 103, 203, 176, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3"),
        CatalogTrack(308, "Mirage Motel", 103, 203, 208, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"),
        CatalogTrack(309, "Open Horizon", 103, 203, 194, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3")
    )

    fun chartSongs(): List<SongDto> = tracks.map { it.toSongDto() }

    fun chartAlbums(): List<AlbumDto> = albums.map { it.toAlbumDto() }

    fun albumTracks(albumId: String): List<SongDto> {
        return tracks.filter { it.albumId.toString() == albumId }.map { it.toSongDto() }
    }

    fun albumDetail(albumId: String): DeezerAlbumDetailDto {
        val album = albums.firstOrNull { it.id.toString() == albumId } ?: fallbackAlbum()
        val artist = artistById(album.artistId)
        return DeezerAlbumDetailDto(
            id = album.id,
            title = album.title,
            releaseDate = album.releaseDate,
            recordType = album.recordType,
            artist = DeezerArtistDto(
                id = artist.id,
                name = artist.name,
                pictureSmall = artist.imageUrl,
                pictureMedium = artist.imageUrl,
                pictureBig = artist.imageUrl,
                pictureXl = artist.imageUrl
            )
        )
    }

    fun artistTopTracks(artistId: String): List<SongDto> {
        return tracks.filter { it.artistId.toString() == artistId }.map { it.toSongDto() }
    }

    fun artistDetail(artistId: String): DeezerArtistDto {
        val artist = artists.firstOrNull { it.id.toString() == artistId } ?: artists.first()
        return artist.toArtistDto()
    }

    fun searchTracks(query: String, limit: Int): List<SongDto> {
        val normalized = query.trim().lowercase()
        return tracks
            .filter { track ->
                track.title.lowercase().contains(normalized) ||
                    artistById(track.artistId).name.lowercase().contains(normalized)
            }
            .take(limit)
            .map { it.toSongDto() }
    }

    fun searchAlbums(query: String, limit: Int): List<AlbumDto> {
        val normalized = query.trim().lowercase()
        return albums
            .filter { album ->
                album.title.lowercase().contains(normalized) ||
                    artistById(album.artistId).name.lowercase().contains(normalized)
            }
            .take(limit)
            .map { it.toAlbumDto() }
    }

    fun searchArtists(query: String, limit: Int): List<DeezerArtistDto> {
        val normalized = query.trim().lowercase()
        return artists
            .filter { artist -> artist.name.lowercase().contains(normalized) }
            .take(limit)
            .map { it.toArtistDto() }
    }

    private fun CatalogTrack.toSongDto(): SongDto {
        val artist = artistById(artistId)
        val album = albumById(albumId)
        return SongDto(
            id = id,
            title = title,
            artist = artist.toArtistDto(),
            albumArt = SongAlbumDto(
                coverSmall = album.coverUrl,
                coverMedium = album.coverUrl,
                coverBig = album.coverUrl,
                coverXl = album.coverUrl,
                albumTitle = album.title,
                albumId = album.id
            ),
            duration = durationSeconds,
            link = "https://www.deezer.com/track/$id",
            previewUrl = previewUrl
        )
    }

    private fun CatalogAlbum.toAlbumDto(): AlbumDto {
        return AlbumDto(
            id = id,
            title = title,
            coverSmall = coverUrl,
            coverMedium = coverUrl,
            coverBig = coverUrl,
            coverXl = coverUrl,
            artist = NestedArtistDto(name = artistById(artistId).name)
        )
    }

    private fun CatalogArtist.toArtistDto(): DeezerArtistDto {
        return DeezerArtistDto(
            id = id,
            name = name,
            pictureSmall = imageUrl,
            pictureMedium = imageUrl,
            pictureBig = imageUrl,
            pictureXl = imageUrl,
            fanCount = fanCount,
            albumCount = albumCount
        )
    }

    private fun artistById(artistId: Long): CatalogArtist {
        return artists.firstOrNull { it.id == artistId } ?: artists.first()
    }

    private fun albumById(albumId: Long): CatalogAlbum {
        return albums.firstOrNull { it.id == albumId } ?: fallbackAlbum()
    }

    private fun fallbackAlbum(): CatalogAlbum = albums.first()
}
