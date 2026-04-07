package com.mauro.deezcover.domain.repository

import com.mauro.deezcover.domain.model.SearchHistoryItem
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun observeHistory(): Flow<List<SearchHistoryItem>>
    suspend fun addItem(item: SearchHistoryItem)
    suspend fun clearAll()
    suspend fun deleteById(id: String)
}
