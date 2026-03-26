package com.mauro.offlinefirst.data.repository

import com.mauro.offlinefirst.data.local.dao.SongDao
import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomainList
import com.mauro.offlinefirst.data.mapper.SongMapper.toEntity
import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val remoteDataSource: RemoteDataSource
) : SongRepository {

    override fun observeSongs(): Flow<Result<List<Song>>> {
        return songDao
            .observeAllSongs()
            .map { entities ->
                Result.success(entities.toDomainList())
            }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }

    override suspend fun syncSongs() {
        try {
            val remoteSongs = remoteDataSource.fetchSongs()
            val entities = remoteSongs.map { it.toEntity() }
            songDao.upsertSongs(entities)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
