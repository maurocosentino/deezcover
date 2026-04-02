package com.mauro.offlinefirst.data.repository

import android.util.Log
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

    companion object {
        private const val TAG = "NewReleaseRepository"
    }

    override fun observeNewReleases(pageIndex: Int): Flow<List<NewRelease>> {
        return newReleaseDao.observeNewReleasesByPage(pageIndex).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncNewReleases(pageIndex: Int) {
        Log.d(TAG, "syncNewReleases:start pageIndex=$pageIndex")
        try {
            val releases = remoteDataSource.fetchNewReleases(pageIndex)
            val entities = releases.mapIndexed { index, dto ->
                dto.toEntity(pageIndex = pageIndex, sortOrder = index)
            }
            newReleaseDao.replaceNewReleasesForPage(pageIndex, entities)
            Log.d(TAG, "syncNewReleases:stored pageIndex=$pageIndex count=${entities.size}")
        } catch (exception: Exception) {
            val cachedCount = newReleaseDao.countNewReleasesByPage(pageIndex)
            if (cachedCount > 0) {
                Log.w(
                    TAG,
                    "syncNewReleases:using cached pageIndex=$pageIndex count=$cachedCount",
                    exception
                )
                return
            }
            throw exception
        }
    }
}
