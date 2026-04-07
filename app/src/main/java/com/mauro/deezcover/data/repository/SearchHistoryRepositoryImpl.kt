package com.mauro.deezcover.data.repository

import com.mauro.deezcover.data.local.dao.SearchHistoryDao
import com.mauro.deezcover.data.local.entity.SearchHistoryEntity
import com.mauro.deezcover.domain.model.SearchHistoryItem
import com.mauro.deezcover.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {

    override fun observeHistory(): Flow<List<SearchHistoryItem>> {
        return searchHistoryDao.observeHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addItem(item: SearchHistoryItem) {
        searchHistoryDao.insert(item.toEntity())
    }

    override suspend fun clearAll() {
        searchHistoryDao.clearAll()
    }

    override suspend fun deleteById(id: String) {
        searchHistoryDao.deleteById(id)
    }
}

private fun SearchHistoryEntity.toDomain(): SearchHistoryItem {
    return SearchHistoryItem(
        id = id,
        title = title,
        subtitle = subtitle,
        imageUrl = imageUrl,
        type = type,
        timestamp = timestamp
    )
}

private fun SearchHistoryItem.toEntity(): SearchHistoryEntity {
    return SearchHistoryEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        imageUrl = imageUrl,
        type = type,
        timestamp = timestamp
    )
}
