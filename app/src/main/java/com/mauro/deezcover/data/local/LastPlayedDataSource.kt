package com.mauro.deezcover.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LastPlayedDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val lastPlayedSongIdKey = stringPreferencesKey("last_played_song_id")

    val lastPlayedSongId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[lastPlayedSongIdKey]
    }

    suspend fun saveLastPlayedSongId(songId: String) {
        dataStore.edit { preferences ->
            preferences[lastPlayedSongIdKey] = songId
        }
    }
}
