package com.mauro.offlinefirst.domain.repository

import com.mauro.offlinefirst.domain.model.SearchHistoryItem
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun observeHistory(): Flow<List<SearchHistoryItem>>
    suspend fun addItem(item: SearchHistoryItem)
    suspend fun clearAll()
    suspend fun deleteById(id: String)
}
