package com.llicorp.memosontology.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ServerConfig(private val context: Context) {
    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val USERNAME = stringPreferencesKey("username")
        private val PASSWORD = stringPreferencesKey("password")
        private val FUSEKI_URL = stringPreferencesKey("fuseki_url")
    }

    // Empty default — user must configure via SettingsScreen
    val serverUrl: Flow<String> = context.dataStore.data.map { it[SERVER_URL] ?: "" }
    val username: Flow<String> = context.dataStore.data.map { it[USERNAME] ?: "" }
    val password: Flow<String> = context.dataStore.data.map { it[PASSWORD] ?: "" }
    val fusekiUrl: Flow<String> = context.dataStore.data.map { it[FUSEKI_URL] ?: "" }

    suspend fun updateConfig(url: String, user: String, pass: String) {
        context.dataStore.edit {
            it[SERVER_URL] = url
            it[USERNAME] = user
            it[PASSWORD] = pass
        }
    }

    suspend fun updateFusekiUrl(url: String) {
        context.dataStore.edit { it[FUSEKI_URL] = url }
    }
}
