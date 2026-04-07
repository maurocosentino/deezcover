package com.mauro.offlinefirst.data.repository
import com.mauro.offlinefirst.data.local.dao.NewReleaseDao
import com.mauro.offlinefirst.data.mapper.NewReleaseMapper.toDomain
import com.mauro.offlinefirst.data.mapper.NewReleaseMapper.toEntity
import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.domain.model.NewRelease
import com.mauro.offlinefirst.domain.repository.NewReleaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NewReleaseRepositoryImpl @Inject constructor(
    private val newReleaseDao: NewReleaseDao,
    private val remoteDataSource: RemoteDataSource
) : NewReleaseRepository {

    override fun observeNewReleases(pageIndex: Int): Flow<List<NewRelease>> {
        return newReleaseDao.observeNewReleasesByPage(pageIndex).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncNewReleases(pageIndex: Int) {
        try {
            val releases = remoteDataSource.fetchNewReleases(pageIndex)
            val entities = releases.mapIndexed { index, dto ->
                dto.toEntity(pageIndex = pageIndex, sortOrder = index)
            }
            newReleaseDao.replaceNewReleasesForPage(pageIndex, entities)
        } catch (exception: Exception) {
            val cachedCount = newReleaseDao.countNewReleasesByPage(pageIndex)
            if (cachedCount > 0) {
                return
            }
            throw exception
        }
    }

    override suspend fun fetchFeaturedAlbum(): List<NewRelease> {
        return try {
            remoteDataSource.fetchFeaturedAlbumSelection().map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
